package edu.ucar.unidata.rosetta.domain;

import java.io.Serializable;
import java.util.Locale;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

/**
 * A POJO to hold the data collected from the user in the Rosetta application (acts as a
 * form-backing-object).
 *
 * @author oxelson@ucar.edu
 */
public class Data implements Serializable {

  private String id;
  private String cfType;
  private String community;
  private String platform;
  private String dataFileType;
  private CommonsMultipartFile dataFile = null;
  private String dataFileName;
  private CommonsMultipartFile positionalFile = null;
  private String positionalFileName;
  private CommonsMultipartFile templateFile = null;
  private String templateFileName;
  private String headerLineNumbers;
  private boolean noHeaderLines;
  private String delimiter;
  private String otherDelimiter;
  private String submit;
  private String variableMetadata;
  private String netcdfFile;
  private String zip;
  private Locale decimalSeparatorLocale = Locale.ENGLISH;


  /**
   * Returns the unique id associated with this object.
   *
   * @return The unique id.
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the unique id associated with this object.
   *
   * @param id The unique id.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Returns the cfType.
   *
   * @return The cfType.
   */
  public String getCfType() {
    return cfType;
  }

  /**
   * Sets the cfType.
   *
   * @param cfType The cfType.
   */
  public void setCfType(String cfType) {
    this.cfType = cfType;
  }

  /**
   * Returns the user's community.
   *
   * @return The community.
   */
  public String getCommunity() {
    return community;
  }

  /**
   * Sets the user's community.
   *
   * @param community The user's community.
   */
  public void setCommunity(String community) {
    this.community = community;
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
   * Sets the platform.
   *
   * @param platform The platform.
   */
  public void setPlatform(String platform) {
    this.platform = platform;
  }

  /**
   * Returns the data file type.
   *
   * @return The data file type.
   */
  public String getDataFileType() {
    return dataFileType;
  }

  /**
   * Sets the data file type.
   *
   * @param dataFileType The data file type.
   */
  public void setDataFileType(String dataFileType) {
    this.dataFileType = dataFileType;
  }

  /**
   * Returns the uploaded data file in CommonsMultipartFile format.
   *
   * @return The CommonsMultipartFile data file.
   */
  public CommonsMultipartFile getDataFile() {
    return dataFile;
  }

  /**
   * Sets the uploaded data file as a CommonsMultipartFile file.
   *
   * @param dataFile The CommonsMultipartFile data file.
   */
  public void setDataFile(CommonsMultipartFile dataFile) {
    setDataFileName(dataFile.getOriginalFilename());
    this.dataFile = dataFile;
  }

  /**
   * Returns the name of the data file.
   *
   * @return The name of the data file.
   */
  public String getDataFileName() {
    return dataFileName;
  }

  /**
   * Sets the name of the data file.
   *
   * @param dataFileName The name of the data file.
   */
  public void setDataFileName(String dataFileName) {
    this.dataFileName = dataFileName;
  }

  /**
   * Returns the uploaded positional file in CommonsMultipartFile format.
   *
   * @return The CommonsMultipartFile positional file.
   */
  public CommonsMultipartFile getPositionalFile() {
    return positionalFile;
  }

  /**
   * Sets the uploaded positional file as a CommonsMultipartFile file.
   *
   * @param positionalFile The CommonsMultipartFile positional file.
   */
  public void setPositionalFile(CommonsMultipartFile positionalFile) {
    setPositionalFileName(positionalFile.getOriginalFilename());
    this.positionalFile = positionalFile;
  }

  /**
   * Returns the name of the positional file.
   *
   * @return The name of the positional file.
   */
  public String getPositionalFileName() {
    return positionalFileName;
  }

  /**
   * Sets the name of the positional file.
   *
   * @param positionalFileName The name of the positional file.
   */
  public void setPositionalFileName(String positionalFileName) {
    this.positionalFileName = positionalFileName;
  }

  /**
   * Returns the uploaded template file in CommonsMultipartFile format.
   *
   * @return The CommonsMultipartFile template file.
   */
  public CommonsMultipartFile getTemplateFile() {
    return templateFile;
  }

  /**
   * Sets the uploaded template file as a CommonsMultipartFile file.
   *
   * @param templateFile The CommonsMultipartFile template file.
   */
  public void setTemplateFile(CommonsMultipartFile templateFile) {
    setTemplateFileName(templateFile.getOriginalFilename());
    this.templateFile = templateFile;
  }

  /**
   * Returns the name of the template file.
   *
   * @return The name of the template file.
   */
  public String getTemplateFileName() {
    return templateFileName;
  }

  /**
   * Sets the name of the template file.
   *
   * @param templateFileName The name of the template file.
   */
  public void setTemplateFileName(String templateFileName) {
    this.templateFileName = templateFileName;
  }

  /**
   * Returns the header line numbers of the data file.
   *
   * @return The header line numbers.
   */
  public String getHeaderLineNumbers() {
    return headerLineNumbers;
  }

  /**
   * Sets the header line numbers of the data file.
   *
   * @param headerLineNumbers The header line numbers.
   */
  public void setHeaderLineNumbers(String headerLineNumbers) {
    this.headerLineNumbers = headerLineNumbers;
  }


  /**
   * Returns the no leader lines value.
   *
   * @return The no header lines value.
   */
  public boolean getNoHeaderLines() {
    return noHeaderLines;
  }

  /**
   * Sets the no leader lines value.
   *
   * @param noHeaderLines The no header lines value.
   */
  public void setNoHeaderLines(boolean noHeaderLines) {
    this.noHeaderLines = noHeaderLines;
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
   * Sets the data file delimiter.
   *
   * @param delimiter The data file delimiter.
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }


  /**
   * Returns the other data file delimiter.
   *
   * @return The other delimiter.
   */
  public String getOtherDelimiter() {
    return otherDelimiter;
  }

  /**
   * Sets the other data file delimiter.
   *
   * @param otherDelimiter The other delimiter.
   */
  public void setOtherDelimiter(String otherDelimiter) {
    this.otherDelimiter = otherDelimiter;
  }

  /**
   * Returns the submit type.
   *
   * @return The submit type.
   */
  public String getSubmit() {
    return submit;
  }

  /**
   * Sets the submit type.
   *
   * @param submit The submit type.
   */
  public void setSubmit(String submit) {
    this.submit = submit;
  }

  /**
   * Returns the variable metadata.
   *
   * @return The variable metadata.
   */
  public String getVariableMetadata() {
    return variableMetadata;
  }

  /**
   * Sets the variable metadata.
   *
   * @param variableMetadata The variable metadata.
   */
  public void setVariableMetadata(String variableMetadata) {
    this.variableMetadata = variableMetadata;
  }

  /**
   * Returns the name of the converted netCDF file.
   *
   * @return The name of the converted netCDF File.
   */
  public String getNetcdfFile() {
    return netcdfFile;
  }

  /**
   * Sets the name of the converted netCDF file.
   *
   * @param netcdfFile The name of the converted netCDF File.
   */
  public void setNetcdfFile(String netcdfFile) {
    this.netcdfFile = netcdfFile;
  }

  /**
   * Returns the name of the zip file.
   *
   * @return The name of the zip File.
   */
  public String getZip() {
    return zip;
  }

  /**
   * Sets the name of the zip file.
   *
   * @param zip The name of the zip File.
   */
  public void setZip(String zip) {
    this.zip = zip;
  }

  /**
   * Returns the Locale to use for the decimal separator.
   *
   * @return The Locale.
   */
  public Locale getDecimalSeparatorLocale() {
    return decimalSeparatorLocale;
  }

  /**
   * Sets the locale to FRENCH if "Comma" is given as input.
   *
   * Else it sets it to ENGLISH (for Point as separator), which is the default.
   *
   * @param decimalSeparator Text representation of the decimal separator to be used.
   */
  public void setDecimalSeparator(String decimalSeparator) {
    switch (decimalSeparator) {
      case "Comma":
        this.decimalSeparatorLocale = Locale.FRENCH;
        break;
      case "Point":
      default:
        this.decimalSeparatorLocale = Locale.ENGLISH;
        break;
    }
  }

  /**
   * String representation of this Data object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
