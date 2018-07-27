package edu.ucar.unidata.rosetta.domain.resources;

public abstract class RosettaResource implements Comparable<RosettaResource> {

  private String name;

  public String getName() {
    return name;
  }

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
