package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Object representing a delimiter resource (see WEB-INF/classes/resources/delimiters.xml).
 * Data from the resource XML file is used to populate this object which is then
 * persisted in the database for quick retrieval.
 *
 * @author oxelson@bu.edu
 */
public class Delimiter extends RosettaResource {

  private String characterSymbol;
  private int id;

  /**
   * Returns the character symbol of this delimiter.
   *
   * @return The character symbol.
   */
  public String getCharacterSymbol() {
    return characterSymbol;
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
   * set the character symbol of this delimiter.
   *
   * @param characterSymbol The character symbol.
   */
  public void setCharacterSymbol(String characterSymbol) {
    this.characterSymbol = characterSymbol;
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
