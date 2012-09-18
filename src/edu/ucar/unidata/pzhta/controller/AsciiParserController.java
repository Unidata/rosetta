package edu.ucar.unidata.pzhta.controller;

import org.grlea.log.SimpleLogger;

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
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.xml.sax.SAXException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import edu.ucar.unidata.pzhta.domain.AsciiFile;

import edu.ucar.unidata.pzhta.Pzhta;

/**
 * Controller to parse ASCII file data.
 *
 */

@Controller
public class AsciiParserController {

    private static final SimpleLogger log = new SimpleLogger(AsciiParserController.class);
    private final String tmpDir = System.getProperty("java.io.tmpdir");
    private final String downloadDir = System.getProperty("catalina.base") + "/webapps/pzhtaDownload";

    @RequestMapping(value="/parse", method=RequestMethod.POST)
    @ResponseBody
    public String parseFile(AsciiFile file, BindingResult result) {
        String filePath = tmpDir + "/" + file.getUniqueId() + "/" + file.getFileName();
        StringBuffer sBuffer = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String sCurrentLine;            
            int lineCount = 0;
            int delimiterRunningTotal = 0;
            boolean dataLine = false;
            List <String> delimiterList = file.getDelimiterList(); 
            String selectedDelimiter = "";    
            ArrayList<ArrayList> outerList = new ArrayList<ArrayList>();  
            if (!file.getDelimiterList().isEmpty()) {
                selectedDelimiter = delimiterList.get(0);  
            }             
            while ((sCurrentLine = reader.readLine()) != null) {
                if (file.getHeaderLineNumberList().isEmpty()) {
                    sBuffer.append(sCurrentLine + "\n");     
                } else {
                    if (file.getDelimiterList().isEmpty()) {
                        log.error("ASCII file header lines are present, but no delimiters are specified.");
                        return null;
                    } else {
                        if (file.getHeaderLineNumberList().contains(new Integer(lineCount).toString())) {
                            sBuffer.append(sCurrentLine + "\n");    
                        } else {             
                            Iterator<String> delimiterIterator = delimiterList.iterator();
                            while (delimiterIterator.hasNext()) {  
                                String delimiter = delimiterIterator.next();
                                int delimiterCount = StringUtils.countMatches(sCurrentLine, delimiter);
                                if (!dataLine) {
                                    delimiterRunningTotal = delimiterCount;
                                    dataLine = true;
                                } else {       
                                    if (delimiterRunningTotal != delimiterCount) {
                                        log.error("ASCII file line of data contains an irregular delimiter count at line number: " + new Integer(lineCount).toString() + " for delimiter: " + delimiter);
                                        return null;
                                    }
                                }                               
                            }
                            if (delimiterList.size() != 1) {
                                String[] delimiters = (String[])delimiterList.toArray(new String[delimiterList.size()]);
                                for(int i = 1; i< delimiters.length; i++){ 
		                     String updatedLineData = sCurrentLine.replaceAll(delimiters[i],  selectedDelimiter);
                                     sBuffer.append(updatedLineData + "\n");   
                                     String[] lineComponents = updatedLineData.split(selectedDelimiter);
                                     ArrayList<String> innerList = new ArrayList<String>(Arrays.asList(lineComponents));
                                     outerList.add(innerList);
                                }                            
                            } else {
                                sBuffer.append(sCurrentLine + "\n");  
                                String[] lineComponents = sCurrentLine.split(selectedDelimiter); 
                                ArrayList<String> innerList = new ArrayList<String>(Arrays.asList(lineComponents));
                                outerList.add(innerList);
                            }
                        }
                    }
                }
                lineCount++;
            }   
            if (!file.getDelimiterList().isEmpty()) {
                selectedDelimiter = selectedDelimiter + "\n";
            }
            if (!file.getVariableNameMap().isEmpty()) {     
                String ncmlFile = createNcmlFile(file);  
                Pzhta ncWriter = new Pzhta();
                String fileOut = downloadDir + "/" + FilenameUtils.removeExtension(file.getFileName()) + ".nc";
                if (ncWriter.convert(ncmlFile, fileOut, outerList)) {
                    return fileOut + "\n" + ncmlFile;
                } else {
                    log.error("netCDF file not created.");
                    return null;
                }
            } else {
                return selectedDelimiter + sBuffer.toString();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public String createNcmlFile(AsciiFile file) {
        String ncmlFilePath = downloadDir + "/" + FilenameUtils.removeExtension(file.getFileName()) + ".ncml";
        log.error("createNcmlFile ncmlFilePath " + ncmlFilePath);
        try  {
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

            // attribute elements
            if (file.getTitle() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "title");
                attribute.setAttribute("value", file.getTitle());
                netcdf.appendChild(attribute);
            }

            if (file.getInstitution() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "institution");
                attribute.setAttribute("value", file.getInstitution());
                netcdf.appendChild(attribute);
            }
            if (file.getProcessor() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "processor");
                attribute.setAttribute("value", file.getProcessor());
                netcdf.appendChild(attribute);
            }
            if (file.getVersion() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "version");
                attribute.setAttribute("value", file.getVersion());
                netcdf.appendChild(attribute);
            }
            if (file.getSource() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "source");
                attribute.setAttribute("value", file.getSource());
                netcdf.appendChild(attribute);
            }
            if (file.getDescription() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "title");
                attribute.setAttribute("value", file.getTitle());
                netcdf.appendChild(attribute);
            }
            if (file.getComment() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "comment");
                attribute.setAttribute("value", file.getComment());
                netcdf.appendChild(attribute);
            }
            if (file.getHistory() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "history");
                attribute.setAttribute("value", file.getHistory());
                netcdf.appendChild(attribute);
            }
            if (file.getReferences() != null) {
                Element attribute = doc.createElement("attribute");
                attribute.setAttribute("name", "references");
                attribute.setAttribute("value", file.getReferences());
                netcdf.appendChild(attribute);
            }



            HashMap <String, String> variableNameMap = file.getVariableNameMap();
            HashMap <String, HashMap> variableMetadataMap = file.getVariableMetadataMap();
            Set<String> variableNameKeys = variableNameMap.keySet();
            Iterator<String> variableNameKeysIterator = variableNameKeys.iterator();
            while (variableNameKeysIterator.hasNext()) {  
                String key = variableNameKeysIterator.next();
                String value = variableNameMap.get(key);
                if (!value.equals("Do Not Use")) {
                    Element variable = doc.createElement("variable");
                    variable.setAttribute("name", value);
                    HashMap <String, String> variableMetadata = variableMetadataMap.get(key + "Metadata");
                    String type = variableMetadata.get("dataType");
                    if (type.equals("text")) {
                        type = "string";
                    } else if (type.equals("integer")) {
                        type = "int";
                    }
                    variable.setAttribute("type", type);

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
                    String columnId = key.replace("variable", "");
                    Element attribute = doc.createElement("attribute");
                    attribute.setAttribute("name", "_columnId");
                    attribute.setAttribute("value", columnId);
                    variable.appendChild(attribute);

                    netcdf.appendChild(variable);
                } else {
                    continue;
                }
            }


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            File ncmlFile = new File(ncmlFilePath);
            StreamResult result = new StreamResult(ncmlFile);
 
            transformer.transform(source, result);


            if(ncmlFile.exists()) { 
                return ncmlFilePath;
            } else {
                log.error("Error!  ncml file " + ncmlFilePath + "was not created.");
                return null;
            }
        } catch (ParserConfigurationException e) {
            log.error("Parser not configured: " + e.getMessage());
            log.error(e.getMessage());
            return null;
        } catch (TransformerException e) {
            log.error(e.getMessage());
            return null;
        }
    }

}


