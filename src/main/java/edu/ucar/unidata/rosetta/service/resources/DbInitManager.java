package edu.ucar.unidata.rosetta.service.resources;

import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import java.sql.SQLException;
import java.util.Properties;
import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * The DbInitManager is responsible for creating and shutting down the
 * database needed for the rosetta application. The class that implements this interface
 * will be read during application initialization to create the database if it doesn't
 * already exist (see the dbInitManager bean in the WEB-INF/rosetta-servlet.xml file),
 * and right before application destruction.
 *
 * Those who do not want to use the default embedded database that comes with rosetta
 * will need to implement this interface to utilize their own database. Please see the
 * database schema and specs outlined in the rosetta documentation for more information.
 *
 * @author oxelson@ucar.edu
 */
public interface DbInitManager {

    /**
     * Creates the database required by the rosetta application. Using the database schema
     * and specs outlined in the rosetta documentation, developers are responsible for the
     * creation and population of the database using this method.
     *
     * The props argument contains information gleaned from the application.properties file.
     * Developers can pass custom database information to this method via application.properties,
     * such as:
     *
     *      1) database driver
     *      2) database name
     *      3) database username and password
     *      4) etc.
     *
     * @param props RosettaProperties from application.properties that may be used to create the database.
     * @throws NonTransientDataAccessResourceException  If unable to create or access the database.
     * @throws SQLException If an SQL exceptions occurs during database creation.
     * @throws RosettaDataException If unable to access the rosetta resources to persist.
     */
    public void createDatabase(Properties props)
            throws NonTransientDataAccessResourceException, SQLException, RosettaDataException;

    /**
     * Prepares and shuts down the database.  (E.g., de-registering drivers, etc.)
     *
     * @param props RosettaProperties from application.properties that may be used for database shutdown.
     * @throws SQLException  If an SQL exceptions occurs during database shutdown.
     */
    public void shutdownDatabase(Properties props) throws SQLException;
}
