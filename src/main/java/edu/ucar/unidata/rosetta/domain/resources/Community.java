/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain.resources;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Object representing a community resource (see WEB-INF/classes/resources/communities.xml).
 * Data from the resource XML file is used to populate this object which is then
 * persisted in the database for quick retrieval.
 *
 * @author oxelson@bu.edu
 */
public class Community extends RosettaResource {

  private List<String> fileType = new ArrayList<>();
  private int id;
  private List<Platform> platforms = new ArrayList<>();

  /**
   * Adds a file type to the list of file types.
   *
   * @param fileType  The file type to add.
   */
  public void addToFileType(String fileType) {
    this.fileType.add(fileType);
  }

  /**
   * Returns the list of file types associated with this resource.
   *
   * @return  The file types.
   */
  public List<String> getFileType() {
    return fileType;
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
   * Returns a list of platforms associated with this resource.
   *
   * @return  The platforms.
   */
  public List<Platform> getPlatforms() {
    return platforms;
  }

  /**
   * Sets the list of file types associated with this resource;
   *
   * @param fileType  The file types.
   */
  public void setFileType(List<String> fileType) {
    this.fileType = fileType;
  }

  /**
   * Adds a file type to the list of file types.
   * (This method is used during resource persistence during start up).
   *
   * @param fileType  The file type to add.
   */
  public void setFileType(String fileType) {
    this.fileType.add(fileType);
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
   * Sets a list of platforms associated with this resource;
   *
   * @param platforms  The platforms.
   */
  public void setPlatforms(List<Platform> platforms) {
    this.platforms = platforms;
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
