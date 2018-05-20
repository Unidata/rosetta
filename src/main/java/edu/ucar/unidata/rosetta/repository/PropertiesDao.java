package edu.ucar.unidata.rosetta.repository;

/**
 * @author oxelson@ucar.edu
 */
public interface PropertiesDao {
    /**
     * Looks up and retrieves the persisted uploads directory.
     *
     * @return The persisted uploads directory.
     */
    public String lookupUploadDirectory();
}
