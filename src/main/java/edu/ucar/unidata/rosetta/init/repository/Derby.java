package edu.ucar.unidata.rosetta.init.repository;

import edu.ucar.unidata.rosetta.util.RosettaProperties;

import java.io.File;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * An implementation of the DatabaseInitializationManager that creates
 * the default embedded derby database for the rosetta application.
 */
public class Derby implements DatabaseInitializationManager {

    protected static Logger logger = Logger.getLogger(Derby.class);

    private static final String RESOURCE_PATH = "resources/sql/embeddedDerby/";
    private static final String PROPERTIES_FILE = "db.properties";

    /**
     * Creates the embedded derby database for the rosetta.
     *
     * @param props Properties used to create the database.
     * @throws NonTransientDataAccessResourceException  If unable to create or access the database.
     * @throws SQLException If an SQL exceptions occurs during database creation.
     */
    @Override
    public void createDatabase(Properties props) throws NonTransientDataAccessResourceException, SQLException {

        String rosettaHome = props.getProperty("rosettaHome");
        String dbResourcePath = FilenameUtils.concat(rosettaHome, RESOURCE_PATH);

        Properties dbProps = RosettaProperties.getProperties(FilenameUtils.concat(dbResourcePath,  PROPERTIES_FILE));
        String databaseName = props.getProperty("databaseName");

        // Create connection URL.
        String url = "jdbc:derby:" + rosettaHome + "/" + databaseName;

        // Create derby database.
        File dbFile = new File(rosettaHome + databaseName);
        Connection connection = null;
        if (!dbFile.exists()) {
            logger.info("Database does not exist yet.  Creating...");
            if (!dbFile.mkdirs()) {
                throw new NonTransientDataAccessResourceException("Unable to create embedded derby database.");
            }

            // Create the database tables;
            String createPropertiesTable = "CREATE TABLE properties " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "rosettaHome VARCHAR(100) not null, " +
                    "uploadDir VARCHAR(100) not null, " +
                    "downloadDir VARCHAR(100) not null, " +
                    "maxUpload INTEGER not null" +
                    ");";
            createTable(url, createPropertiesTable, props);

            String createDataTable = "CREATE TABLE data " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "cfType VARCHAR(100) not null, " +
                    "fileName VARCHAR(100) not null, " +
                    "headerLineNumbers INTEGER not null, " +
                    "delimiter INTEGER not null" +
                    ");";
            createTable(url, createDataTable, props);

            // Populate properties table.
            populatePropertiesTable(url, props);

            connection = DriverManager.getConnection(url + ";shutdown=true");

            if (connection != null) {
                connection.close();
            }

        } else {
            logger.info("Nothing to do here... Database already exists.");
        }
    }


    /**
     * Shuts down the embedded derby database by de-registering the driver.
     *
     * @param props Properties from rosetta.properties that may be used for database shutdown.
     * @throws SQLException   If an SQL exceptions occurs during database shutdown.
     */
    public void shutdownDatabase(Properties props) throws SQLException {

        String rosettaHome = props.getProperty("rosettaHome");
        String dbResourcePath = FilenameUtils.concat(rosettaHome, RESOURCE_PATH);

        Properties dbProps = RosettaProperties.getProperties(FilenameUtils.concat(dbResourcePath,  PROPERTIES_FILE));
        String databaseName = props.getProperty("databaseName");

        // Create connection URL.
        String url = "jdbc:derby:" + rosettaHome + "/" + databaseName;

        Driver driver = DriverManager.getDriver(url);
        logger.info("De-registering jdbc driver.");
        DriverManager.deregisterDriver(driver);
    }

    /**
     *
     * @param url       The url to access the database.
     * @param statement The create SQL statement.
     * @param props     Properties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException  If unable to create instance of the database driver.
     * @throws SQLException If an SQL exceptions occurs during create table transaction.
     */
    private void createTable(String url, String statement, Properties props) throws NonTransientDataAccessResourceException, SQLException {

        Connection connection;
        PreparedStatement preparedStatement;

        try {
            Class.forName(props.getProperty("driver"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException("Unable to find database drive class: " + e);
        }

        String username = props.getProperty("username");
        String password = props.getProperty("password");
        if (username != null && password != null)
            connection = DriverManager.getConnection(url + ";create=true", username, password);
        else
            connection = DriverManager.getConnection(url + ";create=true");

        preparedStatement = connection.prepareStatement(statement);
        preparedStatement.executeUpdate();

        // Clean up.
        if (preparedStatement != null)
            preparedStatement.close();

        if (connection != null)
            connection.close();
    }


    /**
     *
     * @param url   The url to access the database.
     * @param props Properties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException  If unable to create instance of the database driver.
     * @throws SQLException If an SQL exceptions occurs during insert transaction.
     */
    private void populatePropertiesTable(String url, Properties props) throws NonTransientDataAccessResourceException, SQLException {

        Connection connection;
        PreparedStatement preparedStatement;

        try {
            Class.forName(props.getProperty("driver"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException("Unable to find database drive class: " + e);
        }

        String username = props.getProperty("username");
        String password = props.getProperty("password");
        if (username != null && password != null)
            connection = DriverManager.getConnection(url + ";create=true", username, password);
        else
            connection = DriverManager.getConnection(url + ";create=true");

        String statement = "INSERT INTO properties(rosettaHome, uploadDir, downloadDir, maxUpload) " +
                "VALUES (?,?,?,?)";

        preparedStatement = connection.prepareStatement(statement);
        preparedStatement.setString(1, props.getProperty("rosettaHome"));
        preparedStatement.setString(2, props.getProperty("uploadDir"));
        preparedStatement.setString(3, props.getProperty("downloadDir"));
        preparedStatement.setString(4, props.getProperty("maxUpload"));

        preparedStatement.executeUpdate();

        // Clean up.
        if (preparedStatement != null)
            preparedStatement.close();

        if (connection != null)
            connection.close();
    }
}


