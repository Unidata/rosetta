package edu.ucar.unidata.rosetta.domain;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ucar.netcdf.Attribute;

/**
 * An object representing a rosetta template. Used for both custom
 * and known file types. As per:
 * https://github.com/Unidata/rosetta/wiki/Rosetta-Template-Attributes
 *
 * @author sarms@ucar.edu
 * @author oxelson@ucar.edu
 */
public class Template {

  private String cfType;
  private String community;
  private String creationDate;
  private String delimiter;
  private String format;  // required
  private List<Attribute> globalMetadata;
  private List<Integer> headerLineNumbers;
  private String platform;
  private String rosettaVersion;
  private String serverId;
  private String templateVersion;
  private List<VariableInfo> variableInfoList;

  /**
   * Returns the CF type.
   *
   * @return  The CF type.
   */
  public String getCfType() {
    return cfType;
  }

  /**
   * Returns the community.
   *
   * @return  The community.
   */
  public String getCommunity() {
    return community;
  }

  /**
   * Returns the creation date of the template
   * in ISO 8601 Notation format
   * (e.g. yyyy-mm-ddThh:mm:ss.ffffff)
   *
   * @return  The creation date.
   */
  public String getCreationDate() {
    return creationDate;
  }

  /**
   * Returns the data file delimiter.
   *
   * @return  The delimiter.
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * Returns the data file format.
   *
   * @return  The format.
   */
  public String getFormat() {
    return format;
  }

  /**
   * Returns the global metadata.
   *
   * @return  The global metadata.
   */
  public List<Attribute> getGlobalMetadata() {
    return globalMetadata;
  }

  /**
   * Returns the header line number of the data file.
   *
   * @return  The header line numbers.
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
   * @return  The variable information.
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
   * @param community  The community.
   */
  public void setCommunity(String community) {
    this.community = community;
  }

  /**
   * Sets the creation date of the template
   * in ISO 8601 Notation format
   * (e.g. yyyy-mm-ddThh:mm:ss.ffffff)
   *
   * @param creationDate  The creation date.
   */
  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Sets the data file delimiter.
   *
   * @param delimiter  The delimiter.
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * Sets  the data file format.
   *
   * @param format  The format.
   */
  public void setFormat(String format) {
    this.format = format;
  }

  /**
   * Sets the global metadata.
   *
   * @param globalMetadata  The global metadata.
   */
  public void setGlobalMetadata(List<Attribute> globalMetadata) {
    this.globalMetadata = globalMetadata;
  }

  /**
   * Sets the header line number of the data file.
   *
   * @param headerLineNumbers  The header line numbers.
   */
  public void setHeaderLineNumbers(List<Integer> headerLineNumbers) {
    this.headerLineNumbers = headerLineNumbers;
  }

  /**
   * Sets the platform.
   *
   * @param platform  The platform.
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
   * @param variableInfoList  The variable information.
   */
  public void setVariableInfoList(
      List<VariableInfo> variableInfoList) {
    this.variableInfoList = variableInfoList;
  }

  /**
   * String representation of this object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
