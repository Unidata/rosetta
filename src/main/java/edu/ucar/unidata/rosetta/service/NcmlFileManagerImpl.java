package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.AsciiFile;
import edu.ucar.unidata.rosetta.service.ServerInfoBean;
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

            // get metadata maps
            Map<String, String> variableNameMap = file.getVariableNameMap();
            Map<String, HashMap> variableMetadataMap = file.getVariableMetadataMap();
            Map<String, String> generalMetadataMap = file.getGeneralMetadataMap();
            Map<String, String> platformMetadataMap = file.getPlatformMetadataMap();


            // look for coordinate and non-coordinate variables
            HashMap<String, ArrayList<String>> coordVars = new HashMap<String, ArrayList<String>>();
            ArrayList<String> nonCoordVarList = new ArrayList<String>();
            Set<String> variableNameKeys = variableNameMap.keySet();
            Iterator<String> variableNameKeysIterator = variableNameKeys.iterator();
            String key, value;
            Map<String, String> variableMetadata;
            ArrayList<String> coordVarList = new ArrayList<String>();

            while (variableNameKeysIterator.hasNext()) {
                //ex:
                // key = "variableName1"
                // value = "time"
                key = variableNameKeysIterator.next();
                value = variableNameMap.get(key);
                if (!value.equals("Do Not Use")) {
                    variableMetadata = variableMetadataMap.get(key + "Metadata");
                    // check if variable is a coordinate variable!
                    if (variableMetadata.containsKey("_coordinateVariable")) {
                        if(variableMetadata.get("_coordinateVariable").equals("coordinate")){
                            String coordVarType = variableMetadata.get("_coordinateVariableType");
                            coordVarList = coordVars.get(coordVarType);
                            if (coordVarList == null) {
                                coordVarList = new ArrayList<String>();
                            }
                            coordVarList.add(key);
                            coordVars.put(coordVarType, coordVarList);
                        } else {
                            nonCoordVarList.add(key);
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
            Document doc = docBuilder.newDocument();

            Element netcdf = doc.createElement("netcdf");
            netcdf.setAttribute("xmlns", "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
            doc.appendChild(netcdf);

            //TODO look at coordVars and create the appropriate dimensions
            logger.warn("create dimensions in ncml/n");
            Iterator<String> coordVarNameIterator;
            //List<String> usedCoordVarNames = new ArrayList<String>();
            //HashMap<String, Integer> coordNameCounts = new HashMap<String, Integer>();

            String shapeAttr = "";
            for (String coordType : coordVars.keySet()) {
                // coordAttr is the attribute that defines the coord system for variables
                if (shapeAttr.equals("")) {
                    shapeAttr = coordType;
                } else {
                    shapeAttr = shapeAttr + " " + coordType;
                }

                coordVarNameIterator = coordVars.get(coordType).iterator();
                while (coordVarNameIterator.hasNext()) {
                    key = coordVarNameIterator.next();
                    value = variableNameMap.get(key);
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
            String coordAttr = "";
            if (file.getCfType().equals("timeSeries")) {
                coordAttr = "time lat lon alt";
            }

            Element attribute;
            Element variable;

            // CF specific attribute elements
            attribute = doc.createElement("attribute");
            attribute.setAttribute("name", "Conventions");
            attribute.setAttribute("value", "CF-1.6");
            netcdf.appendChild(attribute);

            attribute = doc.createElement("attribute");
            attribute.setAttribute("name", "featureType");
            attribute.setAttribute("value", file.getCfType());
            netcdf.appendChild(attribute);

            // global metadata

            for (Map.Entry<String, String> entry : generalMetadataMap.entrySet()) {
                if (entry.getValue() != null) {
                    attribute = doc.createElement("attribute");
                    attribute.setAttribute("name", entry.getKey());
                    attribute.setAttribute("value", entry.getValue());
                    netcdf.appendChild(attribute);
                }
            }

            // platform metadata

            // Latitude
            if (platformMetadataMap.containsKey("latitude")) {
                variable = doc.createElement("variable");
                variable.setAttribute("name", "lat");
                variable.setAttribute("type", "float");

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "units");
                String latUnits = platformMetadataMap.get("longitudeUnits");

                Boolean unitChanged = false;
                if (latUnits.equals("degrees_south")) {
                    latUnits = "degrees_north";
                    unitChanged=true;
                }

                attribute.setAttribute("value", platformMetadataMap.get("latitudeUnits"));
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "long_name");
                attribute.setAttribute("value", "latitude");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "standard_name");
                attribute.setAttribute("value", "latitude");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "_platformMetadata");
                attribute.setAttribute("value", "true");
                variable.appendChild(attribute);

                attribute = doc.createElement("values");

                String latVal = platformMetadataMap.get("latitude");

                if (unitChanged) {
                    latVal = "-" + latVal;
                }
                Text values = doc.createTextNode(latVal);
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            // Longitude
            if (platformMetadataMap.containsKey("longitude")) {
                variable = doc.createElement("variable");
                variable.setAttribute("name", "lon");
                variable.setAttribute("type", "float");

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "units");
                String lonUnits = platformMetadataMap.get("longitudeUnits");
                Boolean unitChanged = false;
                if (lonUnits.equals("degrees_west")) {
                    lonUnits = "degrees_east";
                    unitChanged=true;
                }

                attribute.setAttribute("value", lonUnits);
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "long_name");
                attribute.setAttribute("value", "longitude");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "standard_name");
                attribute.setAttribute("value", "longitude");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "_platformMetadata");
                attribute.setAttribute("value", "true");
                variable.appendChild(attribute);

                attribute = doc.createElement("values");
                String lonVal = platformMetadataMap.get("longitude");

                if (unitChanged) {
                    lonVal = "-" + lonVal;
                }
                Text values = doc.createTextNode(lonVal);
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            // Altitude
            if (platformMetadataMap.containsKey("altitude")) {
                variable = doc.createElement("variable");
                variable.setAttribute("name", "alt");
                variable.setAttribute("type", "float");

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "units");
                attribute.setAttribute("value", platformMetadataMap.get("altitudeUnits"));
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "long_name");
                attribute.setAttribute("value", "height above mean sea-level");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "standard_name");
                attribute.setAttribute("value", "altitude");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "positive");
                attribute.setAttribute("value", "up");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "axis");
                attribute.setAttribute("value", "Z");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "_platformMetadata");
                attribute.setAttribute("value", "true");
                variable.appendChild(attribute);

                attribute = doc.createElement("values");
                Text values = doc.createTextNode(platformMetadataMap.get("altitude"));
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            // Platform ID
            if (platformMetadataMap.containsKey("platformName")) {
                variable = doc.createElement("variable");
                variable.setAttribute("name", "station_id");
                variable.setAttribute("type", "string");

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "cf_role");
                attribute.setAttribute("value", file.getCfType());
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "long_name");
                attribute.setAttribute("value", "station_id");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "standard_name");
                attribute.setAttribute("value", "station_id");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "_platformMetadata");
                attribute.setAttribute("value", "true");
                variable.appendChild(attribute);

                attribute = doc.createElement("values");
                Text values = doc.createTextNode(platformMetadataMap.get("platformName"));
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            //TODO check for multiple time definitions and aggregate the cols into one variable

            // look at coordVars and create the appropriate coordinate variables
            String type;
            for (String coordType : coordVars.keySet()) {

                coordVarNameIterator = coordVars.get(coordType).iterator();
                while (coordVarNameIterator.hasNext()) {
                    key = coordVarNameIterator.next();
                    value = variableNameMap.get(key);
                    variableMetadata = variableMetadataMap.get(key + "Metadata");
                    variable = doc.createElement("variable");
                    String userName = value;
                    String varName = value.replace(" ", "_");

                    variable.setAttribute("name", coordType);
                    variable.setAttribute("shape", coordType);

                    type = variableMetadata.get("dataType");
                    if (type.equals("Text")) {
                        type = "string";
                    } else if (type.equals("Integer")) {
                        type = "int";
                    } else if (type.equals("Float")) {
                        type = "float";
                    }
                    variable.setAttribute("type", type);

                    if (!userName.equals(varName)) {
                        attribute = doc.createElement("attribute");
                        attribute.setAttribute("name", "_userSuppliedName");
                        attribute.setAttribute("value", userName);
                        variable.appendChild(attribute);
                    }

                    Set<String> variableMetadataKeys = variableMetadata.keySet();
                    Iterator<String> variableMetadataKeysIterator = variableMetadataKeys.iterator();
                    String metadataKey;
                    String metadataValue;
                    while (variableMetadataKeysIterator.hasNext()) {
                        attribute = doc.createElement("attribute");
                        metadataKey = variableMetadataKeysIterator.next();
                        metadataValue = variableMetadata.get(metadataKey);
                        if (!metadataKey.equals("dataType")) {
                            attribute.setAttribute("name", metadataKey);
                            attribute.setAttribute("value", metadataValue);
                        } else {
                            continue;
                        }
                        variable.appendChild(attribute);
                    }

                    String columnId = key.replace("variableName", "");
                    attribute = doc.createElement("attribute");
                    attribute.setAttribute("name", "_columnId");
                    attribute.setAttribute("value", columnId);
                    variable.appendChild(attribute);
                    netcdf.appendChild(variable);
                }
            }


            // create variables for the non-coordinate variables

            Iterator<String> nonCoordVarNameIterator = nonCoordVarList.iterator();
            List<String> usedVarNames = new ArrayList<String>();
            HashMap<String, Integer> nameCounts = new HashMap<String, Integer>();

            while (nonCoordVarNameIterator.hasNext()) {
                key = nonCoordVarNameIterator.next();
                value = variableNameMap.get(key);
                variableMetadata = variableMetadataMap.get(key + "Metadata");
                variable = doc.createElement("variable");
                String userName = value;
                String varName = value.replace(" ", "_");
                if (!usedVarNames.contains(varName)) {
                    usedVarNames.add(varName);
                    nameCounts.put(varName,1);
                } else {
                    Integer newCount = nameCounts.get(varName) + 1;
                    nameCounts.put(varName, newCount);
                    varName = varName + "_" + newCount.toString();
                }
                variable.setAttribute("name", varName);

                type = variableMetadata.get("dataType");
                if (type.equals("Text")) {
                    type = "string";
                } else if (type.equals("Integer")) {
                    type = "int";
                } else if (type.equals("Float")) {
                    type = "float";
                }
                variable.setAttribute("type", type);
                if (!userName.equals(varName)) {
                    attribute = doc.createElement("attribute");
                    attribute.setAttribute("name", "_userSuppliedName");
                    attribute.setAttribute("value", userName);
                    variable.appendChild(attribute);
                }
                Set<String> variableMetadataKeys = variableMetadata.keySet();
                Iterator<String> variableMetadataKeysIterator = variableMetadataKeys.iterator();
                String metadataKey;
                String metadataValue;
                while (variableMetadataKeysIterator.hasNext()) {
                    attribute = doc.createElement("attribute");
                    metadataKey = variableMetadataKeysIterator.next();
                    metadataValue = variableMetadata.get(metadataKey);
                    if (!metadataKey.equals("dataType")) {
                        attribute.setAttribute("name", metadataKey);
                        attribute.setAttribute("value", metadataValue);
                    } else {
                        continue;
                    }
                    variable.appendChild(attribute);
                }
                String columnId = key.replace("variableName", "");
                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "_columnId");
                attribute.setAttribute("value", columnId);
                variable.appendChild(attribute);

                variable.setAttribute("shape", shapeAttr.replace(" ", ","));
                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "coordinates");
                attribute.setAttribute("value", coordAttr);
                variable.appendChild(attribute);

                netcdf.appendChild(variable);
            }


            // rosetta variable (to hold rosetta specific metadata...value is float, version of rosetta



            String rosetta_version = ServerInfoBean.getVersion();

            variable = doc.createElement("variable");
            variable.setAttribute("name", "Rosetta");
            variable.setAttribute("type", "String");
            variable.setAttribute("version", rosetta_version);

            attribute = doc.createElement("attribute");
            attribute.setAttribute("name", "version");
            attribute.setAttribute("value", rosetta_version);
            variable.appendChild(attribute);

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


