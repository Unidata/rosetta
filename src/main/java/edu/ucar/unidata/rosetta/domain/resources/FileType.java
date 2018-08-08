/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Object representing a file type resource (see WEB-INF/classes/resources/fileTypes.xml).
 * Data from the resource XML file is used to populate this object which is then
 * persisted in the database for quick retrieval.
 *
 * @author oxelson@bu.edu
 */
public class FileType extends RosettaResource {

  private int id;

  /**
   * Returns the persistence-layer ID associated with this resource.
   *
   * @return The ID.
   */
  public int getId() {
    return id;
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
   * String representation of this object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
