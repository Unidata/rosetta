package edu.ucar.unidata.rosetta.service.resources;

import edu.ucar.unidata.rosetta.domain.resources.*;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;

import java.io.File;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * An implementation of the DbInitManager that creates
 * the default embedded derby database for the rosetta application.
 *
 * @author oxelson@ucar.edu
 */
public class EmbeddedDerbyDbInitManager implements DbInitManager {

    private static final Logger logger = Logger.getLogger(EmbeddedDerbyDbInitManager.class);


    /**
     * Creates the embedded derby database for the rosetta.
     *
     * @param props RosettaProperties used to create the database.
     * @throws NonTransientDataAccessResourceException  If unable to create or access the database.
     * @throws SQLException If an SQL exceptions occurs during database creation.
     * @throws RosettaDataException If unable to access the rosetta resources to persist.
     */
    @Override
    public void createDatabase(Properties props) throws NonTransientDataAccessResourceException, SQLException, RosettaDataException {

        // Get relevant properties.
        String rosettaHome = props.getProperty("rosetta.home");
        String databaseName = props.getProperty("jdbc.dbName");
        String url = props.getProperty("jdbc.url").replaceAll("\\$\\{rosetta.home\\}", rosettaHome);
        url = url.replaceAll("\\$\\{jdbc.dbName\\}", databaseName);
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
                    "rosettaHome VARCHAR(255) not null, " +
                    "uploadDir VARCHAR(255) not null, " +
                    "downloadDir VARCHAR(255) not null, " +
                    "maxUpload INTEGER not null" +
                    ")";
            createTable(createPropertiesTable, props);

            String createDataTable = "CREATE TABLE data " +
                    "(" +
                    "id VARCHAR(255) primary key not null, " +
                    "platform VARCHAR(255), " +
                    "community VARCHAR(255), " +
                    "cfType VARCHAR(255), " +
                    "dataFileName VARCHAR(255), " +
                    "dataFileType VARCHAR(255), " +
                    "positionalFileName VARCHAR(255), " +
                    "templateFileName VARCHAR(255), " +
                    "headerLineNumbers VARCHAR(255), " +
                    "noHeaderLines VARCHAR(255), " +
                    "delimiter VARCHAR(255)," +
                    "otherDelimiter VARCHAR(255)," +
                    "netcdfFile VARCHAR(255)," +
                    "zip VARCHAR(255)" +
                    ")";
            createTable(createDataTable, props);

            String createVariableMetadataTable = "CREATE TABLE metadata " +
                    "(" +
                    "id VARCHAR(255), " +
                    "type VARCHAR(255), " +
                    "metadataKey VARCHAR(255), " +
                    "metadataValue VARCHAR(255)" +
                    ")";
            createTable(createVariableMetadataTable, props);

            String createPlatformTable = "CREATE TABLE platform " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255), " +
                    "imgPath VARCHAR(255), " +
                    "cfType INTEGER, " +
                    "community INTEGER" +
                    ")";
            createTable(createPlatformTable, props);

            String createFileTypeTable = "CREATE TABLE fileType " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255)" +
                    ")";
            createTable(createFileTypeTable, props);

            String createCfTypeTable = "CREATE TABLE cfType " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255)" +
                    ")";
            createTable(createCfTypeTable, props);

            String createCommunityTable = "CREATE TABLE community " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255), " +
                    "fileType INTEGER" +
                    ")";
            createTable(createCommunityTable, props);


            // Populate properties table.
            populatePropertiesTable(props);

            // Insert the resources into the db.
            insertResources(props);


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
     * @param props RosettaProperties from application.properties that may be used for database shutdown.
     * @throws SQLException   If an SQL exceptions occurs during database shutdown.
     */
    public void shutdownDatabase(Properties props) throws SQLException {

        // Get relevant properties.
        String rosettaHome = props.getProperty("rosetta.home");
        String databaseName = props.getProperty("jdbc.dbName");
        String url = props.getProperty("jdbc.url").replaceAll("\\$\\{rosetta.home\\}", rosettaHome);
        url = url.replaceAll("\\$\\{jdbc\\.dbName\\}", databaseName);
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
     * @param props     RosettaProperties from which the database username and password are glean.
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
     * @param props RosettaProperties from which the database username and password are glean.
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


    /**
     * Inserts the rosetta-specific resources glean from xml files into the database.
     *
     * @param props RosettaProperties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException  If unable to create instance of the database driver.
     * @throws SQLException If an SQL exceptions occurs during insert transaction.
     * @throws RosettaDataException If unable to access the resource to persist.
     */
    private void insertResources(Properties props) throws NonTransientDataAccessResourceException, SQLException, RosettaDataException {

        Connection connection;
        PreparedStatement preparedStatement = null;

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

        // Define our statements for the various resource types.
        String cfTypeStatement = "INSERT INTO cfType(name) VALUES (?)";
        String fileTypeStatement = "INSERT INTO fileType(name) VALUES (?)";
        String platformStatement = "INSERT INTO platform(name, imgPath, cfType, community) VALUES (?, ?, ?, ?)";
        String communityStatement = "INSERT INTO community(name, fileType) VALUES (?, ?)";

        ResourceManager resourceManager = new ResourceManager();
        List<RosettaResource> resources = resourceManager.loadResources();
        for (RosettaResource resource: resources) {
            // Set the resources depending on the type.
            if (resource instanceof CfType) {
                // File Type resource.
                preparedStatement = connection.prepareStatement(cfTypeStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.executeUpdate();

            } else if (resource instanceof FileType) {
                // File Type resource.
                preparedStatement = connection.prepareStatement(fileTypeStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.executeUpdate();

            } else if (resource instanceof Platform) {
                // Platform resource.

                // Get the primary key values for the cfTypes and stash them in a map for quick access.
                Map<String, Integer> cfTypeMap = new HashMap<>();
                String getCfTypeStatement = "SELECT * FROM cfType";
                preparedStatement = connection.prepareStatement(getCfTypeStatement);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    cfTypeMap.put(name, id);
                }

                // Get the primary key values for the communities and stash them in a map for quick access.
                Map<String, Integer> communityMap = new HashMap<>();
                String getCommunityStatement = "SELECT DISTINCT id, name FROM community";
                preparedStatement = connection.prepareStatement(getCommunityStatement);
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    communityMap.put(name, id);
                }
                preparedStatement = connection.prepareStatement(platformStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.setString(2, ((Platform) resource).getImgPath());
                preparedStatement.setInt(3, cfTypeMap.get(((Platform) resource).getCfType()));
                preparedStatement.setInt(4, communityMap.get(((Platform) resource).getCommunity()));
                preparedStatement.executeUpdate();
            } else {
                // Community resource.

                // Get the primary key values for the file types and stash them in a map for quick access.
                Map<String, Integer> fileTypeMap = new HashMap<>();
                String getFileTypeStatement = "SELECT * FROM fileType";
                preparedStatement = connection.prepareStatement(getFileTypeStatement);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    fileTypeMap.put(name, id);
                }
                // Create an entry in the community database for all of the file types.
                List<String> fileTypes = ((Community) resource).getFileType();
                for (String fileType: fileTypes) {
                    preparedStatement = connection.prepareStatement(communityStatement);
                    preparedStatement.setString(1, resource.getName());
                    preparedStatement.setInt(2, fileTypeMap.get(fileType));
                    preparedStatement.executeUpdate();
                }
            }
        }

        // Clean up.
        if (preparedStatement != null)
            preparedStatement.close();

        if (connection != null)
            connection.close();
    }


}


