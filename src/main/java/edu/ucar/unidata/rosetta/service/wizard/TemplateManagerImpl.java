/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.GlobalMetadata;
import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.RosettaAttribute;
import edu.ucar.unidata.rosetta.domain.RosettaGlobalAttribute;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.Variable;
import edu.ucar.unidata.rosetta.domain.VariableInfo;
import edu.ucar.unidata.rosetta.domain.VariableMetadata;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.repository.wizard.GlobalMetadataDao;
import edu.ucar.unidata.rosetta.repository.wizard.VariableDao;
import edu.ucar.unidata.rosetta.service.ResourceManager;
import edu.ucar.unidata.rosetta.service.ServerInfoBean;

import edu.ucar.unidata.rosetta.util.JsonUtils;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import edu.ucar.unidata.rosetta.util.TransactionLogUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;
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

    private GlobalMetadataDao globalMetadataDao;
    private VariableDao variableDao;

    private static final String  TEMPLATE_VERSION = "1.0";

    private static final Logger logger = Logger.getLogger(TemplateManagerImpl.class);

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;

    @Resource(name = "resourceManager")
    private ResourceManager resourceManager;

    @Resource(name = "wizardManager")
    private WizardManager wizardManager;

    /**
     * Retrieves persisted data to create a Template object which is used to
     * write to a template file and a transaction log.
     *
     * @param id    The unique transaction ID associated with this template.
     * @return  A Template object.
     * @throws RosettaFileException  If unable to create the template file.
     */
    public Template createTemplate(String id) throws RosettaFileException {

        // Get the persisted data.
        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(id);

        // Create the template.
        Template template = new Template();

        // Data from the WizardData object.
        String format = wizardData.getDataFileType().toLowerCase();
        if (format.equals("custom_file_type")) {
            format = "custom";
        }
        template.setFormat(format);
        if (!format.equals("etuff")) {
            template.setCommunity(wizardData.getCommunity());
            String platform = wizardData.getPlatform();
            template.setPlatform(platform);
            String cfType = wizardData.getCfType().toLowerCase();
            if (cfType.equals("") || cfType == null) {
                cfType = resourceManager.getCFTypeFromPlatform(platform);
            }
            template.setCfType(cfType);
            String delimiter = wizardData.getDelimiter();
            template.setDelimiter(delimiter);

            List<String> headerLineNumbers = Arrays.asList(wizardData.getHeaderLineNumbers().split(","));
            template.setHeaderLineNumbers(headerLineNumbers.stream().map(Integer::parseInt).collect(Collectors.toList()));

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
                if (name.equals("do_not_use") || name.equals("DO_NOT_USE")) {
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
        }

        // RosettaGlobalAttribute
        List<GlobalMetadata> globalMetadata = globalMetadataDao.lookupGlobalMetadata(id);
        List<RosettaGlobalAttribute> rosettaGlobalAttributes = new ArrayList<>();
        for (GlobalMetadata item: globalMetadata) {
            if (item.getMetadataValueType() != null) {
                rosettaGlobalAttributes.add(new RosettaGlobalAttribute(item.getMetadataKey(), item.getMetadataValue(), item.getMetadataValueType().toUpperCase(), item.getMetadataGroup()));
            } else {
                rosettaGlobalAttributes.add(new RosettaGlobalAttribute(item.getMetadataKey(), item.getMetadataValue(), "STRING", item.getMetadataGroup()));
            }
        }
        template.setGlobalMetadata(rosettaGlobalAttributes);

        // Providence data.
        template.setTemplateVersion(TEMPLATE_VERSION);
        template.setRosettaVersion(ServerInfoBean.getVersion());
        // ISO 8601 Notation (e.g. yyyy-mm-ddThh:mm:ss.ffffff)
        String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        String date = dateFormat.format(new Date());
        template.setCreationDate(date);

        template.setServerId(PropertyUtils.getHostName());

        // Create the template file or die trying.
        try {
            writeTemplateToFile(template, id);
        } catch (IOException e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            throw new RosettaFileException("Unable to create template file " + errors);
        }

        return template;

    }


    /**
     * Consults the list of provided MetadataProfile objects to get the
     * metadata value type (e.g.: data type) of the metadata attribute value
     * provided in the VariableMetadata object. As per:
     * https://github.com/Unidata/rosetta/wiki/Metadata-Profile-Schema
     *
     * @param metadataProfiles  List of MetadataProfile objects to consult.
     * @param variableMetadata  The VariableMetadata object containing the variable metadata.
     * @return  The metadata value type.
     */
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
     * Gleans information from the provided Variable object to populate the
     * "control metadata" in a VariableInfo object (used for Template creation).
     * As per: https://github.com/Unidata/rosetta/wiki/Rosetta-Template-Attributes
     *
     * @param variable  The Variable object.
     * @return  A list of RosettaAttribute objects containing the control metadata.
     */
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

    /**
     * Gleans information from the provided list of VariableMetadata objects to populate the
     * "variable data" in a VariableInfo object (used for Template creation), as per:
     * as per: https://github.com/Unidata/rosetta/wiki/Rosetta-Template-Attributes
     *
     * @param requiredMetadata  A list of VariableMetadata objects.
     * @param metadataProfiles  A list of MetadataProfile objects (for setting the metadata value type).
     * @return A list of RosettaAttribute objects containing the variable data.
     */
    private List<RosettaAttribute> populateVariableData(List<VariableMetadata> requiredMetadata, List<MetadataProfile> metadataProfiles) {
        List<RosettaAttribute> rosettaAttributes = new ArrayList<>();
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

    /**
     * Sets the data access object (DAO) for the GlobalMetadata object.
     *
     * @param globalMetadataDao The service DAO representing a GlobalMetadata object.
     */
    public void setGlobalMetadataDao(GlobalMetadataDao globalMetadataDao) {
        this.globalMetadataDao = globalMetadataDao;
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
     * Creates a template file using the information in the provided Template object.
     *
     * @param template  The Template object.
     * @param id  The unique transaction ID associated with this Template object.
     * @throws RosettaFileException  If unable to create the required directory (if needed).
     * @throws IOException  If unable to create the template file.
     */
    private void writeTemplateToFile(Template template, String id) throws RosettaFileException, IOException {
        // Just in case the needed directory hasn't been created.
        String userFilesDirPath = FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id);
        File userFilesDir = new File(userFilesDirPath);
        if (!userFilesDir.exists()) {
            logger.info("Creating user files directory at " + userFilesDir.getPath());
            if (!userFilesDir.mkdirs()) {
                throw new RosettaFileException("Unable to create user files directory for " + id);
            }
        }

        // Convert Template object to JSON string.
        String jsonString = JsonUtils.mapObjectToJson(template);

        // Write the template file.
        String templateFilePath = FilenameUtils.concat(userFilesDirPath, "rosetta.template");
        try (BufferedWriter bufferedWriter = new BufferedWriter(
                new FileWriter(new File(templateFilePath)))) {
            bufferedWriter.write(jsonString);
        }

        // Update the transaction log.
        TransactionLogUtils.writeToLog(id, "Template written:\n" + template.transactionLogFormat());
    }

}
