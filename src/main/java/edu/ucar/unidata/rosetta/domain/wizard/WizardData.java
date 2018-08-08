package edu.ucar.unidata.rosetta.domain.wizard;

/**
 * Abstract class all wizard domain objects inherit.
 *
 * @author oxelson@ucar.edu
 */
public abstract class WizardData {

  private String id;

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
}
