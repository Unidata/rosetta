package edu.ucar.unidata.rosetta.domain.resources;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class Community extends RosettaResource {

    public int id;
    public List<String> fileType = new ArrayList<>();
    public List<String> platforms = new ArrayList<>();

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

    public List<String> getPlatforms() {
        return platforms;
    }

    public void addToPlatforms(String platform) {
        this.platforms.add(platform);
    }

    /**
     * String representation of this object.
     *
     * @return  The string representation.
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
