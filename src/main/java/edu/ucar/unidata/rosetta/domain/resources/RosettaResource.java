package edu.ucar.unidata.rosetta.domain.resources;

/**
 * Abstract class for resources located in WEB-INF/classes/resources/*
 * Data from the resource XML file is used to populate implementing classes
 * which are then persisted in the database for quick retrieval.
 *
 * @author oxelson@bu.edu
 */
public abstract class RosettaResource implements Comparable<RosettaResource> {

  private String name;

  /**
   * Returns the name of this resource.
   *
   * @return The name.
   */
  public String getName() {
    return name;
  }


  /**
   * Sets the name of this resource.
   *
   * @param name  The name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Compares this object with the specified object for order.
   *
   * @param comparisonObject The object to compare this object against.
   * @return Returns a -1, 0, or 1 if this object is less than, equal to, or greater than the
   * specified object.
   */
  public int compareTo(RosettaResource comparisonObject) {
    return (this.getName().compareTo(comparisonObject.getName()));
  }
}
