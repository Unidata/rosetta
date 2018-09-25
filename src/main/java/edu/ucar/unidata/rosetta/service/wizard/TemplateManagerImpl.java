/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.service.wizard;


import com.fasterxml.jackson.core.JsonProcessingException;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.domain.VariableMetadata;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.GlobalMetadataDao;
import edu.ucar.unidata.rosetta.repository.wizard.VariableDao;
import edu.ucar.unidata.rosetta.repository.wizard.WizardDataDao;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.ServerInfoBean;

import edu.ucar.unidata.rosetta.util.JsonUtil;

import edu.ucar.unidata.rosetta.util.PropertyUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.io.FilenameUtils;

import org.apache.log4j.Logger;

/**
 * Implements template manager functionality.
 */
public class TemplateManagerImpl implements TemplateManager {

    private VariableDao variableDao;
    private GlobalMetadataDao globalMetadataDao;

    private static final String  TEMPLATE_VERSION = "1.0";

    private static final Logger logger = Logger.getLogger(WizardManagerImpl.class);

    @Resource(name = "uploadedFileManager")
    private UploadedFileManager uploadedFileManager;

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    public Template createTemplate(String id) throws RosettaFileException {

        // Get the persisted data.
        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(id);
        UploadedFileCmd uploadedFileCmd = uploadedFileManager.lookupPersistedDataById(id);

        // Create the template.
        Template template = new Template();

        logger.info("platform" + wizardData.getPlatform());

        // Data from the WizardData object.
        template.setCommunity(wizardData.getCommunity());
        String platform = wizardData.getPlatform();
        template.setPlatform(platform);
        String cfType = wizardData.getCfType().toLowerCase();
        if (cfType.equals("") || cfType == null) {
            cfType = resourceManager.getCFTypeFromPlatform(platform);
        }
        template.setCfType(cfType);
        String delimiter = resourceManager.getDelimiterSymbol(wizardData.getDelimiter());
        if (delimiter.equals("")) {
            delimiter = " ";
        }
        template.setDelimiter(delimiter);
        String format = wizardData.getDataFileType().toLowerCase();
        if (format.equals("custom_file_type")) {
            format = "custom";
        }
        template.setFormat(format);
        List<String> headerLineNumbers = Arrays.asList(wizardData.getHeaderLineNumbers().split(","));
        template.setHeaderLineNumbers(headerLineNumbers.stream().map(Integer::parseInt).collect(Collectors.toList()));

        // RosettaGlobalAttribute
        List<GlobalMetadata> globalMetadata = globalMetadataDao.lookupGlobalMetadata(id);
        List<RosettaGlobalAttribute> rosettaGlobalAttributes = new ArrayList<>();
        for (GlobalMetadata item: globalMetadata) {
            rosettaGlobalAttributes.add(new RosettaGlobalAttribute(item.getMetadataKey(), item.getMetadataValue(), item.getMetadataValueType().toUpperCase(), item.getMetadataGroup()));
        }
        template.setGlobalMetadata(rosettaGlobalAttributes);

        // Need this to get the metadataValueType for the RosettaAttribute data.
        List<MetadataProfile> metadataProfiles = metadataManager.getMetadataProfiles(id, "variable");

        // VariableInfo
        List<Variable> variables = variableDao.lookupVariables(id);
        List<VariableInfo> variableInfoList = new ArrayList<>();
        for (Variable variable : variables) {

            VariableInfo variableInfo = new VariableInfo();
            // Common to all.
            variableInfo.setColumnId(variable.getColumnNumber());
            // Common to all.
            String name = variable.getVariableName();
            if (name.equals("do_not_use")) {
                variableInfo.setName(name.toUpperCase());
                variableInfoList.add(variableInfo);
            } else {
                variableInfo.setName(variable.getVariableName());

                // variableMetadata
                List<RosettaAttribute> variableMetadata = populateVariableData(
                    variable.getRequiredMetadata(), metadataProfiles);
                variableMetadata.addAll(
                    populateVariableData(variable.getRecommendedMetadata(), metadataProfiles));
                variableMetadata.addAll(
                    populateVariableData(variable.getAdditionalMetadata(), metadataProfiles));
                variableInfo.setVariableMetadata(variableMetadata);


                // rosettaControlMetadata
                List<RosettaAttribute> rosettaControlMetadata = populateRosettaControlMetadata(
                    variable);
                variableInfo.setRosettaControlMetadata(rosettaControlMetadata);
                variableInfoList.add(variableInfo);
            }
        }
        template.setVariableInfoList(variableInfoList);

        // Providence data.
        template.setTemplateVersion(TEMPLATE_VERSION);
        template.setRosettaVersion(ServerInfoBean.getVersion());
        // ISO 8601 Notation (e.g. yyyy-mm-ddThh:mm:ss.ffffff)
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        String date = dateFormat.format(new Date());
        template.setCreationDate(date);

        InetAddress ip;
        String hostname = null;
        try {
            ip = InetAddress.getLocalHost();
            hostname = ip.getHostName();
        } catch (UnknownHostException e) {
            logger.error(e);
        }
        template.setServerId(hostname);

        try {
            writeTemplateToFile(template, id);
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            throw new RosettaFileException("Unable to create template file " + errors);
        }

        return template;

    }

    private void writeTemplateToFile(Template template, String id) throws RosettaFileException, IOException {
        String downloadDirPath = FilenameUtils.concat(PropertyUtils.getDownloadDir(), id);
        File downloadDir = new File(downloadDirPath);
        if (!downloadDir.exists()) {
            logger.info("Creating downloads directory at " + downloadDir.getPath());
            if (!downloadDir.mkdirs()) {
                throw new RosettaFileException("Unable to create downloads directory for " + id);
            }
        }

        String templateFilePath = FilenameUtils.concat(downloadDirPath, "rosetta.template");
        logger.info(templateFilePath);

        String jsonString = JsonUtil.mapObjectToJSON(template);

        try (BufferedWriter bufferedWriter = new BufferedWriter(
            new FileWriter(new File(templateFilePath)))) {
            logger.info(jsonString);
            bufferedWriter.write(jsonString);
        }
    }

    private List<RosettaAttribute> populateRosettaControlMetadata(Variable variable) {

        List<RosettaAttribute> rosettaControlMetadata = new ArrayList<>();

        // Create RosettaAttribute for coordinate variable info.
        RosettaAttribute coordVar = new RosettaAttribute();
        coordVar.setName("coordinateVariable");
        String coordinateVariable = variable.getMetadataType();
        if (coordinateVariable.equals("coordinate")) {
            coordVar.setValue("true");
        } else {
            coordVar.setValue("false");
        }
        coordVar.setType("BOOLEAN");
        rosettaControlMetadata.add(coordVar);

        // Create RosettaAttribute for coordinate variable type info.
        RosettaAttribute coordVarType = new RosettaAttribute();
        coordVarType.setName("coordinateVariableType");
        coordVarType.setValue(variable.getMetadataTypeStructure());
        coordVarType.setType("STRING");
        rosettaControlMetadata.add(coordVarType);

        String positive = variable.getVerticalDirection();
        if (positive != null) {
            // Create RosettaAttribute for positive info.
            RosettaAttribute pos = new RosettaAttribute();
            pos.setName("positive");
            pos.setValue(positive);
            pos.setType("STRING");
            rosettaControlMetadata.add(pos);
        }

        // Create RosettaAttribute for type info.
        RosettaAttribute type = new RosettaAttribute();
        type.setName("type");
        String t = variable.getMetadataValueType();
        if (t.equals("text")) {
            t = "String";
        }
        type.setValue(t);
        type.setType("STRING");
        rosettaControlMetadata.add(type);

        return rosettaControlMetadata;
    }


    private List<RosettaAttribute> populateVariableData(List<VariableMetadata> requiredMetadata, List<MetadataProfile> metadataProfiles) {
        List<RosettaAttribute> rosettaAttributes = new ArrayList<>();
        logger.info("size: " + requiredMetadata.size());
        int i = 0;
        for (VariableMetadata variableMetadata : requiredMetadata) {
            RosettaAttribute rosettaAttribute = new RosettaAttribute();
            if (variableMetadata.getMetadataKey() != null) {
                rosettaAttribute.setName(variableMetadata.getMetadataKey());
                rosettaAttribute.setValue(variableMetadata.getMetadataValue());
                rosettaAttribute.setType(getMetadataValueType(metadataProfiles, variableMetadata));
                rosettaAttributes.add(rosettaAttribute);
                i++;
            }
        }
        return rosettaAttributes;
    }

    private String getMetadataValueType(List<MetadataProfile> metadataProfiles, VariableMetadata variableMetadata) {
        String metadataValueType = null;
        for (MetadataProfile metadataProfile : metadataProfiles) {
            String complianceLevel = variableMetadata.getComplianceLevel();
            if (complianceLevel.equals("additional")) {
                complianceLevel = "optional";
            }
            if (metadataProfile.getAttributeName().equals(variableMetadata.getMetadataKey())) {
                if (metadataProfile.getComplianceLevel().equals(complianceLevel)) {
                     metadataValueType = metadataProfile.getMetadataValueType().toUpperCase();
                    break;
                }
            }
        }
        return metadataValueType;
    }

    /**
     * Sets the data access object (DAO) for the Variable object.
     *
     * @param variableDao The service DAO representing a Variable object.
     */
    public void setVariableDao(VariableDao variableDao) {
        this.variableDao = variableDao;
    }

    /**
     * Sets the data access object (DAO) for the GlobalMetadata object.
     *
     * @param globalMetadataDao The service DAO representing a GlobalMetadata object.
     */
    public void setGlobalMetadataDao(GlobalMetadataDao globalMetadataDao) {
        this.globalMetadataDao = globalMetadataDao;
    }

}
