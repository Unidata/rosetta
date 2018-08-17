package edu.ucar.unidata.rosetta.domain.wizard;

import edu.ucar.unidata.rosetta.domain.MetadataProfile;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Form-backing object for the wizard to collect a collection of metadata profiles.
 * Used for dynamic form binding in Spring to collect multiple metadata profile objects.
 * The term 'Cmd' in the name refers to the command object used in form data binding.
 */
public class MetadataProfileCmd {

    private List<MetadataProfile> metadataProfiles = new ArrayList<>();

    /**
     * Returns the collection of metadata profiles.
     *
     * @return  The metadata profiles.
     */
    public List<MetadataProfile> getMetadataProfiles() {
        return metadataProfiles;
    }

    /**
     * Sets the collection of metadata profiles.
     *
     * @param metadataProfiles The metadata profiles.
     */
    public void setMetadataProfiles(List<MetadataProfile> metadataProfiles) {
        this.metadataProfiles.addAll(metadataProfiles);
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
