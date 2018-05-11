package edu.ucar.unidata.rosetta.service;

import java.io.File;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * An implementation of the DbInitManager that creates
 * the default embedded derby database for the rosetta application.
 */
public class EmbeddedDerbyDbInitManager implements DbInitManager {

    private static final Logger logger = Logger.getLogger(EmbeddedDerbyDbInitManager.class);


    /**
     * Creates the embedded derby database for the rosetta.
     *
     * @param props Properties used to create the database.
     * @throws NonTransientDataAccessResourceException  If unable to create or access the database.
     * @throws SQLException If an SQL exceptions occurs during database creation.
     */
    @Override
    public void createDatabase(Properties props) throws NonTransientDataAccessResourceException, SQLException {

        // Get relevant properties.
        String rosettaHome = props.getProperty("rosetta.home");
        String databaseName = props.getProperty("jdbc.dbName");
        String url = props.getProperty("jdbc.url").replaceAll("\\$\\{rosetta.home}", rosettaHome);
        url = url.replaceAll("\\$\\{jdbc.dbName}", databaseName);
        props.setProperty("jdbc.url", url);

        // Create derby database file.
        File dbFile = new File(FilenameUtils.concat(rosettaHome, databaseName));

        Connection connection;
        if (!dbFile.exists()) {
            logger.info("Database does not exist yet.  Creating...");
            // Create the database tables;
            String createPropertiesTable = "CREATE TABLE properties " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "rosettaHome VARCHAR(100) not null, " +
                    "uploadDir VARCHAR(100) not null, " +
                    "downloadDir VARCHAR(100) not null, " +
                    "maxUpload INTEGER not null" +
                    ")";
            createTable(createPropertiesTable, props);

            String createDataTable = "CREATE TABLE data " +
                    "(" +
                    "id INTEGER primary key not null, " +
                    "platform VARCHAR(100) not null, " +
                    "cfType VARCHAR(100) not null, " +
                    "fileName VARCHAR(100) not null, " +
                    "headerLineNumbers INTEGER not null, " +
                    "delimiter INTEGER not null" +
                    ")";
            createTable(createDataTable, props);

            // Populate properties table.
            populatePropertiesTable(props);

            // Okay, we're done.  Shut down this particular connection to the database.
            try {
                connection = DriverManager.getConnection(url + ";shutdown=true");
                if (connection != null) {
                    connection.close();
                }
            } catch(SQLException e) {
                // As per the Derby docs, the shutdown commands always raise SQLExceptions. (lame!)
                logger.info("Finished creating database. Shutting down database...");
            }


        } else {
            logger.info("Nothing to do here... Database already exists.");
        }
    }


    /**
     * Shuts down the embedded derby database by de-registering the driver.
     *
     * @param props Properties from application.properties that may be used for database shutdown.
     * @throws SQLException   If an SQL exceptions occurs during database shutdown.
     */
    public void shutdownDatabase(Properties props) throws SQLException {

        // Get relevant properties.
        String rosettaHome = props.getProperty("rosetta.home");
        String databaseName = props.getProperty("jdbc.dbName");
        String url = props.getProperty("jdbc.url").replaceAll("\\$\\{rosetta.home}", rosettaHome);
        url = url.replaceAll("\\$\\{jdbc.dbName}", databaseName);
        props.setProperty("jdbc.url", url);

        // Okay, we're done.  Shut down this particular connection to the database.
        try {
            Connection connection = DriverManager.getConnection(url + ";shutdown=true");
            if (connection != null) {
                connection.close();
            }
        } catch(SQLException e) {
            // As per the Derby docs, the shutdown commands always raise SQLExceptions. (lame!)
            logger.info("Shutting down database...");
        }


        Driver driver = DriverManager.getDriver(url);
        logger.info("De-registering jdbc driver.");
        DriverManager.deregisterDriver(driver);
    }

    /**
     * Creates a table in the derby database.
     *
     * @param statement The create SQL statement.
     * @param props     Properties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException  If unable to create instance of the database driver.
     * @throws SQLException If an SQL exceptions occurs during create table transaction.
     */
    private void createTable(String statement, Properties props) throws NonTransientDataAccessResourceException, SQLException {

        Connection connection;
        PreparedStatement preparedStatement;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException("Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url")  + ";create=true";

        if (username != null && password != null) {
            connection = DriverManager.getConnection(url, username, password);
        } else {
            connection = DriverManager.getConnection(url);
        }

        preparedStatement = connection.prepareStatement(statement);
        preparedStatement.executeUpdate();

        // Clean up.
        if (preparedStatement != null)
            preparedStatement.close();

        if (connection != null)
            connection.close();
    }


    /**
     * Populates a table with data.
     *
     * @param props Properties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException  If unable to create instance of the database driver.
     * @throws SQLException If an SQL exceptions occurs during insert transaction.
     */
    private void populatePropertiesTable(Properties props) throws NonTransientDataAccessResourceException, SQLException {

        Connection connection;
        PreparedStatement preparedStatement;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException("Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url");
        if (username != null && password != null)
            connection = DriverManager.getConnection(url, username, password);
        else
            connection = DriverManager.getConnection(url);

        String statement = "INSERT INTO properties(rosettaHome, uploadDir, downloadDir, maxUpload) " +
                "VALUES (?,?,?,?)";
        preparedStatement = connection.prepareStatement(statement);
        preparedStatement.setString(1, props.getProperty("rosetta.home"));
        preparedStatement.setString(2, props.getProperty("rosetta.uploadDir"));
        preparedStatement.setString(3, props.getProperty("rosetta.downloadDir"));
        preparedStatement.setString(4, props.getProperty("rosetta.maxUpload"));

        preparedStatement.executeUpdate();

        // Clean up.
        if (preparedStatement != null)
            preparedStatement.close();

        if (connection != null)
            connection.close();
    }
}


