package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.AsciiFile;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service for parsing file data.
 */
public class NcmlFileManagerImpl implements NcmlFileManager {

    protected static Logger logger = Logger.getLogger(NcmlFileManagerImpl.class);

    private Map<String, HashMap> variableMetadataMap;
    private Map<String,String> variableNameMap;
    private Map<String, String> platformMetadataMap;
    private Map<String, String> generalMetadataMap;


    private HashMap<String, ArrayList<String>> coordVars;
    private HashMap<String, Integer> nameCounts = new HashMap<String, Integer>();
    private List<String> nonCoordVarList = new ArrayList<String>();
    private List<String> usedVarNames = new ArrayList<String>();

    private Document doc;
    private String shapeAttr;
    private String coordAttr;


    private void init(AsciiFile file) {
        // get and set the various metadata maps
        this.variableNameMap = file.getVariableNameMap();
        this.variableMetadataMap = file.getVariableMetadataMap();
        this.platformMetadataMap = file.getPlatformMetadataMap();
        this.generalMetadataMap = file.getGeneralMetadataMap();
    }

    private Element makeSimpleAttribute(String name, String value) {
        Element attribute;
        attribute = this.doc.createElement("attribute");
        attribute.setAttribute("name", name);
        attribute.setAttribute("value", value);

        return attribute;
    }

    private Element makeSimpleValue(String value) {
        Element attribute;
        attribute = this.doc.createElement("values");;
        attribute.appendChild(this.doc.createTextNode(value));

        return attribute;
    }

    private Element createNcfVariable(String sessionStorageKey) {
        Element variable;

        variable = this.doc.createElement("variable");
        String userName = this.variableNameMap.get(sessionStorageKey);
        String varName = userName.replace(" ", "_");
        if (!this.usedVarNames.contains(varName)) {
            this.usedVarNames.add(varName);
            this.nameCounts.put(varName, 1);
        } else {
            Integer newCount = this.nameCounts.get(varName) + 1;
            this.nameCounts.put(varName, newCount);
            varName = varName + "_" + newCount.toString();
        }

        HashMap variableMetadata = this.variableMetadataMap.get(sessionStorageKey + "Metadata");
        Object coordVarTypeOb = variableMetadata.get("_coordinateVariableType");
        String coordVarType = "";
        if (coordVarTypeOb != null) {
            coordVarType = coordVarTypeOb.toString();
        }

        if (this.nonCoordVarList.contains(sessionStorageKey)) {
            variable.setAttribute("name", varName);
        } else if (this.coordVars.get(coordVarType).contains(sessionStorageKey)) {
            variable.setAttribute("name", coordVarType);
            variable.setAttribute("shape", coordVarType);
        }
        String type = (String) variableMetadata.get("dataType");
        if (type.equals("Text")) {
            type = "string";
        } else if (type.equals("Integer")) {
            type = "int";
        } else if (type.equals("Float")) {
            type = "float";
        }
        variable.setAttribute("type", type);

        if (!userName.equals(varName)) {
            variable.appendChild(makeSimpleAttribute("_userSuppliedName", userName));
        }
        Set<String> variableMetadataKeys = variableMetadata.keySet();
        Iterator<String> variableMetadataKeysIterator = variableMetadataKeys.iterator();
        String metadataKey;
        String metadataValue;
        while (variableMetadataKeysIterator.hasNext()) {
            metadataKey = variableMetadataKeysIterator.next();
            metadataValue = variableMetadata.get(metadataKey).toString();
            if (!metadataKey.equals("dataType")) {
                variable.appendChild(makeSimpleAttribute(metadataKey, metadataValue));
            }
        }

        String columnId = sessionStorageKey.replace("variableName", "");
        variable.appendChild(makeSimpleAttribute("_columnId", columnId));

        if (this.nonCoordVarList.contains(sessionStorageKey)) {
            variable.setAttribute("shape", this.shapeAttr.replace(" ", ","));
            variable.appendChild(makeSimpleAttribute("coordinates", this.coordAttr));
        }

        return variable;
    }

    // name: "latitude", "longitude", "altitude"
    private Element createCoordVarsFromPlatform(String name) {
        Element variable;

        variable = this.doc.createElement("variable");
        variable.setAttribute("name", name.substring(0,3));
        variable.setAttribute("type", "float");

        String coordVarUnits = this.platformMetadataMap.get(name + "Units");
        Boolean unitChanged = false;
        if (coordVarUnits.equals("degrees_west")) {
            coordVarUnits = "degrees_east";
            unitChanged=true;
        } else if (coordVarUnits.equals("degrees_south")) {
            coordVarUnits = "degrees_north";
            unitChanged=true;
        }
        variable.appendChild(makeSimpleAttribute("units", coordVarUnits));
        variable.appendChild(makeSimpleAttribute("standard_name", name));
        variable.appendChild(makeSimpleAttribute("_platformMetadata", "true"));

        String coordVarVal = this.platformMetadataMap.get(name);
        if (unitChanged) {
            coordVarVal = "-" + coordVarVal;
        }
        variable.appendChild(makeSimpleValue(coordVarVal));

        if (!name.equals("altitude")) {
            variable.appendChild(makeSimpleAttribute("long_name", name));
        } else {
            variable.appendChild(makeSimpleAttribute("long_name", "height above mean seal-level"));
            variable.appendChild(makeSimpleAttribute("positive", "up"));
            variable.appendChild(makeSimpleAttribute("axis", "Z"));
        }

        return variable;
    }

    private Element createPlatformInfo(AsciiFile file){

        Element variable;

        variable = this.doc.createElement("variable");
        variable.setAttribute("name", "station_id");
        variable.setAttribute("type", "string");
        variable.appendChild(makeSimpleAttribute("cf_role", file.getCfType()));
        variable.appendChild(makeSimpleAttribute("long_name", "station_id"));
        variable.appendChild(makeSimpleAttribute("standard_name", "station_id"));
        variable.appendChild(makeSimpleAttribute("_platformMetadata", "true"));
        variable.appendChild(makeSimpleValue(this.platformMetadataMap.get("platformName").toString().replaceAll(" ","_")));

        return variable;
    }

    private Element createRosettaInfo(AsciiFile file){
        String rosetta_version = ServerInfoBean.getVersion();

        Element variable;
        Element attribute;

        variable = this.doc.createElement("variable");
        variable.setAttribute("name", "Rosetta");
        variable.setAttribute("type", "String");
        variable.setAttribute("version", rosetta_version);

        variable.appendChild(makeSimpleAttribute("long_name", "Rosetta front-end sessionStorage JSON String"));
        attribute = this.doc.createElement("values");
        attribute.setAttribute("separator", "\t");
        Text values = this.doc.createTextNode(file.getJsonStrSessionStorage());
        attribute.appendChild(values);
        variable.appendChild(attribute);

        return variable;
    }

    public String createNcmlFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException {
        try {
            // make sure downloadDir exists and, if not, create it
            File downloadTarget = new File(downloadDirPath);
            if (!downloadTarget.exists()) {
                logger.warn("created download path");
                if (!downloadTarget.mkdirs()) {
                    throw new IOException("Unable to create download directory " + downloadTarget.getAbsolutePath());
                }
            }

            init(file);

            // look for coordinate and non-coordinate variables
            this.coordVars = new HashMap<String, ArrayList<String>>();
            ArrayList<String> coordVarList = new ArrayList<String>();
            Set<String> variableNameKeys = this.variableNameMap.keySet();
            Iterator<String> variableNameKeysIterator = variableNameKeys.iterator();
            String key, value;
            Map<String, String> variableMetadata;

            while (variableNameKeysIterator.hasNext()) {
                //ex:
                // key = "variableName1"
                // value = "time"
                key = variableNameKeysIterator.next();
                value = this.variableNameMap.get(key);
                if (!value.equals("Do Not Use")) {
                    variableMetadata = this.variableMetadataMap.get(key + "Metadata");
                    // check if variable is a coordinate variable!
                    if (variableMetadata.containsKey("_coordinateVariable")) {
                        if(variableMetadata.get("_coordinateVariable").equals("coordinate")){
                            String coordVarType = variableMetadata.get("_coordinateVariableType");
                            coordVarList = this.coordVars.get(coordVarType);
                            if (coordVarList == null) {
                                coordVarList = new ArrayList<String>();
                            }
                            coordVarList.add(key);
                            coordVars.put(coordVarType, coordVarList);
                        } else {
                            this.nonCoordVarList.add(key);
                        }
                    }
                }
            }

            String ncmlFilePath = downloadDirPath + "/" + FilenameUtils.removeExtension(file.getFileName()) + ".ncml";
            logger.warn("create ncmlFilePath: " + ncmlFilePath);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setValidating(true);
            docFactory.setAttribute(
                    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                    "http://www.w3.org/2001/XMLSchema");
            docFactory.setAttribute(
                    "http://java.sun.com/xml/jaxp/properties/schemaSource",
                    "http://www.unidata.ucar.edu/schemas/netcdf/ncml-2.2.xsd");

            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root element
            this.doc = docBuilder.newDocument();

            Element netcdf = this.doc.createElement("netcdf");
            netcdf.setAttribute("xmlns", "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
            this.doc.appendChild(netcdf);

            logger.warn("create dimensions in ncml/n");
            Iterator<String> coordVarNameIterator;

            this.shapeAttr = "";
            for (String coordType : coordVars.keySet()) {
                // coordAttr is the attribute that defines the coord system for variables
                if (this.shapeAttr.equals("")) {
                    this.shapeAttr = coordType;
                } else {
                    this.shapeAttr = this.shapeAttr + " " + coordType;
                }

                coordVarNameIterator = coordVars.get(coordType).iterator();
                while (coordVarNameIterator.hasNext()) {
                    key = coordVarNameIterator.next();
                    Element dim = doc.createElement("dimension");
                    // set dimension name based on coordType

                    dim.setAttribute("name", coordType);
                    logger.warn("number of data lines to parse: " + Integer.toString(parseFileData.size()));
                    // TODO get actual length of variable
                    dim.setAttribute("length", Integer.toString(parseFileData.size()));
                    logger.warn("append dim\n");
                    netcdf.appendChild(dim);

                }
            }

            // create a coordinate attribute based on CF1.6 DSG
            this.coordAttr = "";
            if (file.getCfType().equals("timeSeries")) {
                this.coordAttr = "time lat lon alt";
            }

            Element attribute;
            Element variable;

            // CF specific attribute elements
            netcdf.appendChild(makeSimpleAttribute("Conventions", "CF-1.6"));
            netcdf.appendChild(makeSimpleAttribute("featureType", file.getCfType()));

            // global metadata

            for (Map.Entry<String, String> entry : this.generalMetadataMap.entrySet()) {
                if (entry.getValue() != null) {
                    netcdf.appendChild(makeSimpleAttribute(entry.getKey(), entry.getValue()));
                }
            }

            // platform metadata

            // Latitude
            if (platformMetadataMap.containsKey("latitude")) {
                netcdf.appendChild(createCoordVarsFromPlatform("latitude"));
            }

            // Longitude
            if (platformMetadataMap.containsKey("longitude")) {
                netcdf.appendChild(createCoordVarsFromPlatform("longitude"));
            }

            // Altitude
            if (platformMetadataMap.containsKey("altitude")) {
                netcdf.appendChild(createCoordVarsFromPlatform("altitude"));
            }

            // Platform ID
            if (platformMetadataMap.containsKey("platformName")) {
                netcdf.appendChild(createPlatformInfo(file));
            }

            // look at coordVars and create the appropriate coordinate variables
            String[] specialTimeNames = {"relTime", "fullDateTime", "date", "time"};
            ArrayList buildTimeTriggers = new ArrayList<String>(4);
            buildTimeTriggers.addAll(Arrays.asList(specialTimeNames));
            ArrayList timeVarTypes = new ArrayList<String>();

            for (String coordType : coordVars.keySet()) {
                coordVarNameIterator = coordVars.get(coordType).iterator();
                if (buildTimeTriggers.contains(coordType)) {
                    // time related variables are special in that they may span
                    // multiple variables. We will mark these variables with a
                    // "timeRelatedVariable" attribute set to true. Then, on the
                    // backend (i.e. Rosetta.java), we will combine these various
                    // time related variables and do the right thing.
                    // ToDo the thing above
                    key = coordVarNameIterator.next();
                    variable = createNcfVariable(key);
                    variable.appendChild(makeSimpleAttribute("timeRelatedVariable", "true"));
                    netcdf.appendChild(variable);
                } else {
                    while (coordVarNameIterator.hasNext()) {
                        key = coordVarNameIterator.next();
                        netcdf.appendChild(createNcfVariable(key));
                    }
                }
            }

            // create variables for the non-coordinate variables
            Iterator<String> nonCoordVarNameIterator = nonCoordVarList.iterator();
            this.usedVarNames = new ArrayList<String>();

            while (nonCoordVarNameIterator.hasNext()) {
                key = nonCoordVarNameIterator.next();
                netcdf.appendChild(createNcfVariable(key));
            }

            // rosetta variable (to hold rosetta specific metadata...value is float, version of rosetta
            //netcdf.appendChild(createRosettaInfo(file));
            String rosetta_version = "0.1";

            variable = doc.createElement("variable");
            variable.setAttribute("name", "Rosetta");
            variable.setAttribute("type", "String");
            variable.setAttribute("version", rosetta_version);

            attribute = doc.createElement("attribute");
            attribute.setAttribute("name", "long_name");
            attribute.setAttribute("value", "Rosetta front-end sessionStorage JSON String");
            variable.appendChild(attribute);


            attribute = doc.createElement("values");
            attribute.setAttribute("separator", "\t");
            Text values = doc.createTextNode(file.getJsonStrSessionStorage());
            attribute.appendChild(values);
            variable.appendChild(attribute);
            netcdf.appendChild(variable);

            // all done! Save NcML xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            File ncmlFile = new File(ncmlFilePath);
            StreamResult result = new StreamResult(ncmlFile);

            transformer.transform(source, result);


            if (ncmlFile.exists()) {
                return ncmlFilePath;
            } else {
                logger.error("Error!  ncml file " + ncmlFilePath + "was not created.");
                return null;
            }
        } catch (ParserConfigurationException e) {
            logger.error("Parser not configured: " + e.getMessage());
            logger.error(e.getMessage());
            return null;
        } catch (TransformerException e) {
            logger.error(e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }
}