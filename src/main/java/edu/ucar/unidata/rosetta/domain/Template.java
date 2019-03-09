/*
 * Copyright (c) 2012-2019 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * An object representing a rosetta template. Used for both custom and known file types. As per:
 * https://github.com/Unidata/rosetta/wiki/Rosetta-Template-Attributes
 */
public class Template {

    private String cfType;
    private String community;
    private String creationDate;
    private String delimiter;
    private String format;  // required
    private List<RosettaGlobalAttribute> globalMetadata;
    private List<Integer> headerLineNumbers;
    private String platform;
    private String rosettaVersion;
    private String serverId;
    private String templateVersion;
    private List<VariableInfo> variableInfoList;

    /**
     * Override equals() for Template.
     */
    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (!(obj instanceof Template)) {
            return false;
        }

        Template t = (Template) obj;

        return Objects.equals(cfType, t.getCfType()) &&
                Objects.equals(community, t.getCommunity()) &&
                Objects.equals(creationDate, t.getCreationDate()) &&
                Objects.equals(delimiter, t.getDelimiter()) &&
                Objects.equals(format, t.getFormat()) &&
                Objects.equals(globalMetadata, t.getGlobalMetadata()) &&
                Objects.equals(headerLineNumbers, t.getHeaderLineNumbers()) &&
                Objects.equals(platform, t.getPlatform()) &&
                Objects.equals(rosettaVersion, t.getRosettaVersion()) &&
                Objects.equals(serverId, t.getServerId()) &&
                Objects.equals(templateVersion, t.getTemplateVersion()) &&
                Objects.equals(variableInfoList, t.getVariableInfoList());
    }

    /**
     * Override Object.hashCode() to implement equals.
     */
    @Override
    public int hashCode() {
        return Objects.hash(cfType, community, creationDate, delimiter,
                format, globalMetadata, headerLineNumbers, platform, rosettaVersion,
                serverId, templateVersion, variableInfoList);
    }

    /**
     * Returns the CF type.
     *
     * @return The CF type.
     */
    public String getCfType() {
        return cfType;
    }

    /**
     * Returns the community.
     *
     * @return The community.
     */
    public String getCommunity() {
        return community;
    }

    /**
     * Returns the creation date of the template in ISO 8601 Notation format (e.g.
     * yyyy-mm-ddThh:mm:ss.ffffff)
     *
     * @return The creation date.
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Returns the data file delimiter.
     *
     * @return The delimiter.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Returns the data file format.
     *
     * @return The format.
     */
    public String getFormat() {
        return format;
    }

    /**
     * Returns the global metadata.
     *
     * @return The global metadata.
     */
    public List<RosettaGlobalAttribute> getGlobalMetadata() {
        return globalMetadata;
    }

    /**
     * Returns the header line number of the data file.
     *
     * @return The header line numbers.
     */
    public List<Integer> getHeaderLineNumbers() {
        return headerLineNumbers;
    }

    /**
     * Returns the platform.
     *
     * @return The platform.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Returns the version of rosetta to make the template.
     *
     * @return The version of rosetta.
     */
    public String getRosettaVersion() {
        return rosettaVersion;
    }

    /**
     * Returns the ID of the specific rosetta server to make the template.
     *
     * @return The server ID.
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * Returns the template version.
     *
     * @return The template version
     */
    public String getTemplateVersion() {
        return templateVersion;
    }

    /**
     * Returns the variable information.
     *
     * @return The variable information.
     */
    public List<VariableInfo> getVariableInfoList() {
        return variableInfoList;
    }

    /**
     * Sets the CF type.
     *
     * @param cfType The CF type.
     */
    public void setCfType(String cfType) {
        this.cfType = cfType;
    }

    /**
     * Sets the community.
     *
     * @param community The community.
     */
    public void setCommunity(String community) {
        this.community = community;
    }

    /**
     * Sets the creation date of the template in ISO 8601 Notation format (e.g.
     * yyyy-mm-ddThh:mm:ss.ffffff)
     *
     * @param creationDate The creation date.
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Sets the data file delimiter.
     *
     * @param delimiter The delimiter.
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Sets  the data file format.
     *
     * @param format The format.
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Sets the global metadata.
     *
     * @param globalMetadata The global metadata.
     */
    public void setGlobalMetadata(List<RosettaGlobalAttribute> globalMetadata) {
        this.globalMetadata = globalMetadata;
    }

    /**
     * Sets the header line number of the data file.
     *
     * @param headerLineNumbers The header line numbers.
     */
    public void setHeaderLineNumbers(List<Integer> headerLineNumbers) {
        this.headerLineNumbers = headerLineNumbers;
    }

    /**
     * Sets the platform.
     *
     * @param platform The platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * Sets the version of rosetta to make the template.
     *
     * @param rosettaVersion The version of rosetta.
     */
    public void setRosettaVersion(String rosettaVersion) {
        this.rosettaVersion = rosettaVersion;
    }

    /**
     * Gets the ID of the specific rosetta server to make the template.
     *
     * @param serverId The server ID.
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Sets the template version.
     *
     * @param templateVersion The template version
     */
    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    /**
     * Sets the variable information.
     *
     * @param variableInfoList The variable information.
     */
    public void setVariableInfoList(List<VariableInfo> variableInfoList) {
        this.variableInfoList = variableInfoList;
    }

    /**
     * String representation of this object.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, new TransactionLogStyle());
    }

    /**
     * Update a template with new values found in a second template. Generally, only metadata
     * are updated. Used for overriding global and variable metadata during batch processing.
     *
     * @param updatedTemplate template containing new metadata
     */
    public void update(Template updatedTemplate) {

        List<Integer> headerLineNumbersUpdate = updatedTemplate.getHeaderLineNumbers();
        if (headerLineNumbersUpdate != null) {
            // simple to update - just replace old list with new list
            this.headerLineNumbers = headerLineNumbersUpdate;
        }

        List<RosettaGlobalAttribute> globalMetadataUpdates = updatedTemplate.getGlobalMetadata();
        if (globalMetadataUpdates != null) {
            // A little more complex. We need to go through each new piece of global metadata,
            // search for it by name in the existing list of global metadata, and remove it if it
            // exits. Then, we can merge the new and existing lists of global metadata.
            for (RosettaGlobalAttribute globalMetadataUpdate : globalMetadataUpdates) {
                Predicate<RosettaAttribute> attributePredicate = attr -> attr.getName().equals(globalMetadataUpdate.getName());
                this.globalMetadata.removeIf(attributePredicate);
            }
            this.globalMetadata.addAll(globalMetadataUpdates);
        }

        List<VariableInfo> variableInfoListUpdates = updatedTemplate.getVariableInfoList();
        if (variableInfoListUpdates != null) {
            // There are two cases to handle here:
            // For each piece of new variableInfo, check if the name matches the name of a VariableInfo
            // object in the list. If so, update it using VariableInfo.update(). If the name of the new
            // piece of variableInfo is not in the list, then it is new and can be added to the list.

            // Let's get all of the names of the variableInfo currently held in variableInfoList.
            List<String> names = new ArrayList<>();
            for (VariableInfo variableInfo : variableInfoList) {
                names.add(variableInfo.getName());
            }

            // Now, let's figure out what to do with each new piece of variableInfo.
            for (VariableInfo variableInfoUpdate : variableInfoListUpdates) {
                String updateName = variableInfoUpdate.getName();
                // For each updated variableInfo entry, check if the name already appears in
                // this object's variableInfoList.
                if (names.contains(updateName)) {
                    // Name already in list, so update it where it is found.
                    int updateIndex = names.indexOf(updateName);
                    VariableInfo oldVarInfo = variableInfoList.get(updateIndex);
                    // If updated template has colNum set to -1, that means remove the variable.
                    if (variableInfoUpdate.getColumnId() == -9) {
                        variableInfoList.remove(updateIndex);
                    } else {
                        oldVarInfo.updateVariableInfo(variableInfoUpdate);
                        variableInfoList.set(updateIndex, oldVarInfo);
                    }
                } else {
                    // This is a new addition - go ahead and add it to the list.
                    variableInfoList.add(variableInfoUpdate);
                }
            }
        }
    }





}
