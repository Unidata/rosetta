package edu.ucar.unidata.rosetta.domain;

/**
 * Form-backing object for CF Type data.
 */
public class CFType {

    private String cfType = null;    // user knows about cf types and explicitly set the cf type.
    private String platform = null;  // user doesn't know what cf type to use and selected a platform.

    /*
     * Returns the cfType.
     *
     * @return  The cfType.
     */
    public String getCfType() {
        return cfType;
    }

    /*
     * Sets the cfType.
     *
     * @param cfType  The cfType.
     */
    public void setCfType(String cfType) {
        this.cfType = cfType;
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
        if (cfType != null) {
            return cfType;
        } else {
            return platform;
        }
    }
}
