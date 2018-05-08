package edu.ucar.unidata.rosetta.domain;

/**
 * Form-backing object for CF Type data.
 */
public class CFType {

    private String specifiedCFType = null;  // user knows about cf types and explicitly set the cf type.
    private String platform = null;         // user doesn't know what cf type to use and selected a platform.

    /*
     * Returns the specifiedCFType.
     *
     * @return  The specifiedCFType.
     */
    public String getSpecifiedCFType() {
        return specifiedCFType;
    }

    /*
     * Sets the specifiedCFType.
     *
     * @param specifiedCFType  The specifiedCFType.
     */
    public void setSpecifiedCFType(String specifiedCFType) {
        this.specifiedCFType = specifiedCFType;
    }

    /*
     * Returns the platform.
     *
     * @return  The platform.
     */
    public String getPlatform() {
        return platform;
    }

    /*
     * Sets the platform.
     *
     * @param platform  The platform.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String toString() {
        if (specifiedCFType != null) {
            return specifiedCFType;
        } else {
            return platform;
        }

    }
}
