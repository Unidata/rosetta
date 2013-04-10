package edu.ucar.unidata.pzhta.service;

import org.apache.log4j.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;

import org.springframework.stereotype.Service;


import edu.ucar.unidata.pzhta.domain.AsciiFile;

import edu.ucar.unidata.pzhta.Pzhta;

/**
 * Service for parsing file data.
 */
public class NcmlFileManagerImpl implements NcmlFileManager {

    protected static Logger logger = Logger.getLogger(NcmlFileManagerImpl.class);


    public String createNcmlFile(AsciiFile file, List<List<String>> parseFileData, String downloadDirPath) throws IOException {
        try  {
            // make sure downloadDir exists and, if not, create it
            File downloadTarget = new File(downloadDirPath);
            if (!downloadTarget.exists()) {
                logger.warn("created download path");
                if (!downloadTarget.mkdir()) {
                    throw new IOException("Unable to create download directory.");
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

            logger.warn("create time dimension in ncml/n");
            Element dim = doc.createElement("dimension");
            dim.setAttribute("name", "time");
            
            logger.warn("number of data lines to parse: " + Integer.toString(parseFileData.size()));
            dim.setAttribute("length", Integer.toString(parseFileData.size()));
            logger.warn("append time dim\n");
            netcdf.appendChild(dim);

            // CF specific attribute elements
            if (true) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "Conventions");
                attribute.setAttribute("value", "CF-1.6");
                netcdf.appendChild(attribute);
            }

            if (true) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "featureType");
                attribute.setAttribute("value", file.getCfType());
                netcdf.appendChild(attribute);
            }
            
            Map<String, String> generalMetadataMap = new HashMap<String, String>();
            for (Map.Entry<String, String> entry : generalMetadataMap.entrySet()) {
                if (entry.getValue() != null) {
                    Element attribute = doc.createElement("attribute");
                    attribute.setAttribute("name", entry.getKey());
                    attribute.setAttribute("value", entry.getValue());
                    netcdf.appendChild(attribute);
                }
	        }

            Map<String, String> platformMetadataMap = new HashMap<String, String>();

            // Latitude
            if (platformMetadataMap.containsKey("latitude")) {
                Element variable = doc.createElement("variable");
                variable.setAttribute("name", "lat");
                variable.setAttribute("type", "float");

                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "units");
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

                attribute = doc.createElement("values");
                Text values = doc.createTextNode(platformMetadataMap.get("latitude"));
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            // Longitude
            if (platformMetadataMap.containsKey("longitude")) {
                Element variable = doc.createElement("variable");
                variable.setAttribute("name", "lat");
                variable.setAttribute("type", "float");

                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "units");
                attribute.setAttribute("value", platformMetadataMap.get("longitudeUnits"));
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "long_name");
                attribute.setAttribute("value", "longitude");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "standard_name");
                attribute.setAttribute("value", "longitude");
                variable.appendChild(attribute);

                attribute = doc.createElement("values");
                Text values = doc.createTextNode(platformMetadataMap.get("longitude"));
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            // Altitude
            if (platformMetadataMap.containsKey("altitude")) {
                Element variable = doc.createElement("variable");
                variable.setAttribute("name", "lat");
                variable.setAttribute("type", "float");

                Element attribute = doc.createElement("attribute");
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

                attribute = doc.createElement("values");
                Text values = doc.createTextNode(platformMetadataMap.get("altitude"));
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            // Platform ID
            if (platformMetadataMap.containsKey("platformName")) {
                Element variable = doc.createElement("variable");
                variable.setAttribute("name", platformMetadataMap.get("platformName"));
                variable.setAttribute("type", "string");

                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "cf_role");
                attribute.setAttribute("value", file.getCfType());
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "long_name");
                attribute.setAttribute("value", "platform name");
                variable.appendChild(attribute);

                attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "standard_name");
                attribute.setAttribute("value", platformMetadataMap.get("platformName"));
                variable.appendChild(attribute);

                attribute = doc.createElement("values");
                Text values = doc.createTextNode(platformMetadataMap.get("platformName"));
                attribute.appendChild(values);
                variable.appendChild(attribute);
                netcdf.appendChild(variable);
            }

            Map <String, String> variableNameMap = file.getVariableNameMap();
            Map <String, HashMap> variableMetadataMap = file.getVariableMetadataMap();
            Set<String> variableNameKeys = variableNameMap.keySet();
            Iterator<String> variableNameKeysIterator = variableNameKeys.iterator();
            while (variableNameKeysIterator.hasNext()) {  
                String key = variableNameKeysIterator.next();
                String value = variableNameMap.get(key);
                if (!value.equals("Do Not Use")) {
                    Element variable = doc.createElement("variable");
                    variable.setAttribute("name", value);
                    Map <String, String> variableMetadata = variableMetadataMap.get(key + "Metadata");
                    String type = variableMetadata.get("dataType");
                    if (type.equals("Text")) {
                        type = "string";
                    } else if (type.equals("Integer")) {
                        type = "int";
                    } else if (type.equals("Float")) {
                        type = "float";
                    }
                    variable.setAttribute("type", type);

                    // TODO: specific to this CF profile
                    variable.setAttribute("shape", "time");

                    Set<String> variableMetadataKeys = variableMetadata.keySet();
                    Iterator<String> variableMetadataKeysIterator = variableMetadataKeys.iterator();
                    while (variableMetadataKeysIterator.hasNext()) {  
                        Element attribute = doc.createElement("attribute");
                        String metadataKey = variableMetadataKeysIterator.next();
                        String metadatValue = variableMetadata.get(metadataKey);
                        if (!metadataKey.equals("dataType")) {
                            attribute.setAttribute("name", metadataKey);
                            attribute.setAttribute("value", metadatValue);
                        } else {
                            continue;
                        }
                        variable.appendChild(attribute);
                    }
                    String columnId = key.replace("variableName", "");
                    Element attribute = doc.createElement("attribute");
                    attribute.setAttribute("name", "_colu{mnId");
                    attribute.setAttribute("value", columnId);
                    variable.appendChild(attribute);

                    // TODO: Specific tot his CF profile
                    if (!value.equals("time")) {
                        attribute = doc.createElement("attribute");
                        attribute.setAttribute("name", "coordinates");
                        attribute.setAttribute("value", "time lat lon alt");
                        variable.appendChild(attribute);
                    }

                    netcdf.appendChild(variable);
                } else {
                    continue;
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            File ncmlFile = new File(ncmlFilePath);
            StreamResult result = new StreamResult(ncmlFile);

            transformer.transform(source, result);


            if(ncmlFile.exists()) { 
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
        }
    }


}


