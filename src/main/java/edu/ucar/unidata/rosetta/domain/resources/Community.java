package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Community extends RosettaResource {

  private int id;
  private List<String> fileType = new ArrayList<>();
  private List<Platform> platforms = new ArrayList<>();

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

  public List<String> getFileType() {
    return fileType;
  }

  public void setFileType(String fileType) {
    this.fileType.add(fileType);
  }

  public void addToFileType(String fileType) {
    this.fileType.add(fileType);
  }

  public void setFileType(List<String> fileType) {
    this.fileType = fileType;
  }

  public List<Platform> getPlatforms() {
    return platforms;
  }

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
