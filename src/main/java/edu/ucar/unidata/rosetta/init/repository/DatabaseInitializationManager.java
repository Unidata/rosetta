package edu.ucar.unidata.rosetta.init.repository;

import java.sql.SQLException;
import java.util.Properties;
import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * The DatabaseInitializationManager creates the database needed for the rosetta application.
 * An implementing class will be read during application initialization so that the database
 * is created if needed.
 *
 * Implementing classes will need to: 
 *
 * will need to be placed in the same package and the rosetta-servlet.xml
 * file needs to be up
 */
public interface DatabaseInitializationManager {

    /**
     * Creates the database for the rosetta. The props argument should contain the following:
     *
     *      1) database driver information.
     *      2)
     *
     * @param props Properties used to create the database.
     * @throws NonTransientDataAccessResourceException  If unable to create or access the database.
     * @throws SQLException If an SQL exceptions occurs during database creation.
     */
    public void createDatabase(Properties props)
            throws NonTransientDataAccessResourceException, SQLException;
}
