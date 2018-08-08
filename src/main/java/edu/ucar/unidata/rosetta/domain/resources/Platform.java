package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Object representing a platform resource (see WEB-INF/classes/resources/platforms.xml).
 * Data from the resource XML file is used to populate this object which is then
 * persisted in the database for quick retrieval.
 *
 * @author oxelson@bu.edu
 */
public class Platform extends RosettaResource {

  private String cfType;
  private String community;
  private int id;
  private String imgPath;

  /**
   * Returns the CF type associated with this resource.
   *
   * @return  The CF type.
   */
  public String getCfType() {
    return cfType;
  }

  /**
   * Returns the community associated with this resource.
   *
   * @return  The community.
   */
  public String getCommunity() {
    return community;
  }

  /**
   * Returns the persistence-layer ID associated with this resource.
   *
   * @return The ID.
   */
  public int getId() {
    return id;
  }

  /**
   * Returns the image path associated with this resource (for display purposes).
   *
   * @return The image path.
   */
  public String getImgPath() {
    return imgPath;
  }

  /**
   * Sets the CF type associated with this resource.
   *
   * @param cfType The CF type.
   */
  public void setCfType(String cfType) {
    this.cfType = cfType;
  }

  /**
   * Sets the community associated with this resource.
   *
   * @param community  The community.
   */
  public void setCommunity(String community) {
    this.community = community;
  }

  /**
   * Sets the persistence-layer ID associated with this resource.
   *
   * @param id The ID.
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Sets the image path associated with this resource (for display purposes).
   *
   * @param imgPath The image path.
   */
  public void setImgPath(String imgPath) {
    this.imgPath = imgPath;
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
