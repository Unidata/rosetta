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
import edu.ucar.unidata.rosetta.domain.wizard.UploadedFileCmd;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.repository.wizard.GlobalMetadataDao;
import edu.ucar.unidata.rosetta.repository.wizard.VariableDao;
import edu.ucar.unidata.rosetta.repository.wizard.WizardDataDao;
import edu.ucar.unidata.rosetta.service.ServerInfoBean;

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

    public void createTemplate(String id) {

        // Get the persisted data.
        WizardData wizardData = wizardManager.lookupPersistedWizardDataById(id);
        UploadedFileCmd uploadedFileCmd = uploadedFileManager.lookupPersistedDataById(id);

        // Create the template.
        Template template = new Template();

        // Data from the WizardData object.
        template.setCfType(wizardData.getCfType());
        template.setCommunity(wizardData.getCommunity());
        template.setDelimiter(wizardData.getDelimiter());
        template.setFormat(wizardData.getDataFileType());
        template.setPlatform(wizardData.getPlatform());
        List<String> headerLineNumbers = Arrays.asList(wizardData.getHeaderLineNumbers().split(","));
        template.setHeaderLineNumbers(headerLineNumbers.stream().map(Integer::parseInt).collect(Collectors.toList()));

        // RosettaGlobalAttribute
        List<GlobalMetadata> globalMetadata = globalMetadataDao.lookupGlobalMetadata(id);
        List<RosettaGlobalAttribute> rosettaGlobalAttributes = new ArrayList<>();
        for (GlobalMetadata item: globalMetadata) {
            rosettaGlobalAttributes.add(new RosettaGlobalAttribute(item.getMetadataKey(), item.getMetadataValue(), item.getMetadataValueType(), item.getMetadataGroup()));
        }
        template.setGlobalMetadata(rosettaGlobalAttributes);

        // Need this to get the metadataValueType for the RosettaAttribute data.
        List<MetadataProfile> metadataProfiles = metadataManager.getMetadataProfiles(id, "variable");

        // VariableInfo
        List<Variable> variables = variableDao.lookupVariables(id);
        List<VariableInfo> variableInfoList = new ArrayList<>();
        for (Variable variable : variables) {
            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setColumnId(variable.getColumnNumber());
            variableInfo.setName(variable.getVariableName());

            // variableMetadata
            List<RosettaAttribute> variableMetadata = populateVariableData(variable.getRequiredMetadata(), metadataProfiles);
            variableMetadata.addAll(populateVariableData(variable.getRecommendedMetadata(), metadataProfiles));
            variableMetadata.addAll(populateVariableData(variable.getAdditionalMetadata(), metadataProfiles));
            variableInfo.setVariableMetadata(variableMetadata);

            // rosettaControlMetadata
            List<RosettaAttribute> rosettaControlMetadata = populateRosettaControlMetadata(variable);
            variableInfo.setRosettaControlMetadata(rosettaControlMetadata);
            variableInfoList.add(variableInfo);
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
        logger.info(template);
    }

    private List<RosettaAttribute> populateRosettaControlMetadata(Variable variable) {
        List<RosettaAttribute> rosettaAttributes = new ArrayList<>();

        RosettaAttribute metadataType = new RosettaAttribute();
        metadataType.setName("metadataType");
        metadataType.setValue(variable.getMetadataType());
        rosettaAttributes.add(metadataType);

        RosettaAttribute metadataTypeStructure = new RosettaAttribute();
        metadataType.setName("metadataTypeStructure");
        metadataType.setValue(variable.getMetadataTypeStructure());
        rosettaAttributes.add(metadataTypeStructure);

        RosettaAttribute verticalDirection = new RosettaAttribute();
        metadataType.setName("verticalDirection");
        metadataType.setValue(variable.getVariableName());
        rosettaAttributes.add(verticalDirection);

        RosettaAttribute  metadataValueType = new RosettaAttribute();
        metadataType.setName(" metadataValueType");
        metadataType.setValue(variable.getMetadataValueType());
        rosettaAttributes.add( metadataValueType);

        return rosettaAttributes;
    }


    private List<RosettaAttribute> populateVariableData(List<VariableMetadata> requiredMetadata, List<MetadataProfile> metadataProfiles) {
        List<RosettaAttribute> rosettaAttributes = new ArrayList<>();
        for (VariableMetadata variableMetadata : requiredMetadata) {
            RosettaAttribute rosettaAttribute = new RosettaAttribute();
            rosettaAttribute.setName(variableMetadata.getMetadataKey());
            rosettaAttribute.setValue(variableMetadata.getMetadataValue());
            rosettaAttribute.setType(getMetadataValueType(metadataProfiles, variableMetadata));
            rosettaAttributes.add(rosettaAttribute);
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
                     metadataValueType = metadataProfile.getMetadataValueType();
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
