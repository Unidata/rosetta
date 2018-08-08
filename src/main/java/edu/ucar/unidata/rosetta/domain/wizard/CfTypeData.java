package edu.ucar.unidata.rosetta.domain.wizard;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import edu.ucar.unidata.rosetta.controller.wizard.CfTypeController;

/**
 * Form-backing object for the wizard to collect CF type and associated data.
 *
 * @author oxelson@ucar.edu
 */
public class CfTypeData extends WizardData {

  private static final Logger logger = Logger.getLogger(CfTypeData.class);

  private String cfType;
  private String community;
  private String metadataProfile;
  private String platform;

  public CfTypeData() {
    setMetadataProfile("CF");
  }

  /**
   * Returns the CF Type selected by the user.
   *
   * @return The CF Type.
   */
  public String getCfType() {
    return cfType;
  }

  /**
   * Sets the CF Type selected by the user.
   *
   * @param cfType The CF Type.
   */
  public void setCfType(String cfType) {
    this.cfType = cfType;
  }

  /**
   * Returns the community selected by the user.
   *
   * @return The community.
   */
  public String getCommunity() {
    return community;
  }

  /**
   * Sets the community selected by the user.
   *
   * @param community The community.
   */
  public void setCommunity(String community) {
    this.community = community;
  }

  /**
   * Returns the metadata profile selected by the user.
   *
   * @return The metadata profile.
   */
  public String getMetadataProfile() {
    return metadataProfile;
  }

  /**
   * Sets the metadata profile selected by the user.
   *
   * @param metadataProfile The metadata profile.
   */
  public void setMetadataProfile(String metadataProfile) {
      this.metadataProfile = metadataProfile;
  }

  /**
   * Returns the platform selected by the user.
   *
   * @return The platform.
   */
  public String getPlatform() {
    return platform;
  }

  /**
   * Sets the platform selected by the user.
   *
   * @param platform The platform.
   */
  public void setPlatform(String platform) {
    this.platform = platform;
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
