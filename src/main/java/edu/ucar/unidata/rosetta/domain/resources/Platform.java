package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Platform extends RosettaResource {

  private int id;
  private String imgPath;
  private String cfType;
  private String community;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return super.getName();
  }

  public void setName(String name) {
    super.setName(name);
  }

  public String getImgPath() {
    return imgPath;
  }

  public void setImgPath(String imgPath) {
    this.imgPath = imgPath;
  }

  public String getCfType() {
    return cfType;
  }

  public void setCfType(String cfType) {
    this.cfType = cfType;
  }

  public String getCommunity() {
    return community;
  }

  public void setCommunity(String community) {
    this.community = community;
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
