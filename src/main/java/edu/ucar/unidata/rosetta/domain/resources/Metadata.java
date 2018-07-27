package edu.ucar.unidata.rosetta.domain.resources;


public abstract class Metadata implements Comparable<Metadata> {

  private String displayName;
  private String tagName;
  private String description;
  private String category;
  private boolean required;

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCetagory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public boolean getRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  /**
   * Compares this object with the specified object for order.
   *
   * @param comparisonObject The GenericMetadata object to compare this object against.
   * @return Returns a -1, 0, or 1 if this object is less than, equal to, or greater than the
   * specified object.
   */
  public int compareTo(Metadata comparisonObject) {
    // Required metadata has precedence over non-required metadata.

    if (!this.getRequired() && comparisonObject.getRequired()) {
      // This object isn't required and the specified object is required.
      return -1;

    } else if (this.getRequired() && !comparisonObject.getRequired()) {
      // This object is required and the specified object isn't required.
      return 1;


    } else if (this.getRequired() && comparisonObject.getRequired()) {
      // Both objects are required. Determine order lexicographically.
      return (this.getDisplayName().compareTo(comparisonObject.getDisplayName()));

    } else {
      // Both objects are not required. Determine order lexicographically.
      return (this.getDisplayName().compareTo(comparisonObject.getDisplayName()));
    }

  }
}
