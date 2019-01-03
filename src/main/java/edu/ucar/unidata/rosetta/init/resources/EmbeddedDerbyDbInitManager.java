/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.init.resources;

import edu.ucar.unidata.rosetta.domain.resources.*;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.NonTransientDataAccessResourceException;

import java.io.File;
import java.sql.*;
import java.util.*;

/**
 * An implementation of the DbInitManager that creates the default embedded derby database for the rosetta application.
 *
 * @author oxelson@ucar.edu
 */
public class EmbeddedDerbyDbInitManager implements DbInitManager {

    private static final Logger logger = Logger.getLogger(EmbeddedDerbyDbInitManager.class);


    /**
     * Adds default admin user during database creation.
     *
     * @param props RosettaProperties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException If unable to create instance of the database
     *                                                 driver.
     * @throws SQLException  If an SQL exceptions occurs during insert transaction.
     */
    private void addDefaultAdminUser(Properties props)
            throws NonTransientDataAccessResourceException, SQLException {

        Connection connection;
        PreparedStatement preparedStatement;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException(
                    "Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url");
        if (username != null && password != null) {
            connection = DriverManager.getConnection(url, username, password);
        } else {
            connection = DriverManager.getConnection(url);
        }

        String statement = "INSERT INTO users " +
                "(userName, password, accessLevel, accountStatus, emailAddress, fullName, dateCreated, dateModified) VALUES "
                +
                "(?,?,?,?,?,?,?,?)";

        preparedStatement = connection.prepareStatement(statement);
        preparedStatement.setString(1, "admin");
        preparedStatement.setString(2, "$2a$10$gJ4ITtIMNpxsU0xmx6qoE.0MGZ2fv8HpoaL1IlgNdhBlUgmcVwRDO");
        preparedStatement.setInt(3, 2);
        preparedStatement.setInt(4, 1);
        preparedStatement.setString(5, "admin@foo.bar.baz");
        preparedStatement.setString(6, "Rosetta Administrator");
        preparedStatement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
        preparedStatement.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
        preparedStatement.executeUpdate();

        // Clean up.
        if (preparedStatement != null) {
            preparedStatement.close();
        }

        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Creates the embedded derby database for the rosetta.
     *
     * @param props RosettaProperties used to create the database.
     * @throws NonTransientDataAccessResourceException If unable to create or access the database.
     * @throws SQLException                            If an SQL exceptions occurs during database creation.
     * @throws RosettaDataException                    If unable to access the rosetta resources to persist.
     */
    @Override
    public void createDatabase(Properties props)
            throws NonTransientDataAccessResourceException, SQLException, RosettaDataException {

        // Get relevant properties.
        String rosettaHome = props.getProperty("rosetta.home");
        String databaseName = props.getProperty("jdbc.dbName");
        String url = props.getProperty("jdbc.url") + rosettaHome + "/" + databaseName;
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
                    "propertyKey VARCHAR(255) not null, " +
                    "propertyValue VARCHAR(255) not null, " +
                    "dateCreated TIMESTAMP not null" +
                    ")";
            createTable(createPropertiesTable, props);

            // Populate properties table.
            populatePropertiesTable(props);

            // Table containing uploaded file data.
            String createUploadedFileTable = "CREATE TABLE uploadedFiles " +
                "(" +
                "id VARCHAR(255) not null, " +
                "fileName VARCHAR(255) not null, " +
                "fileType VARCHAR(50) not null" +
                ")";
            createTable(createUploadedFileTable, props);

            String createWizardDataTable = "CREATE TABLE wizardData " +
                "(" +
                "id VARCHAR(255) primary key not null, " +
                "cfType VARCHAR(50), " +
                "community VARCHAR(100), " +
                "metadataProfile VARCHAR(20), " +
                "platform VARCHAR(100), " +
                "dataFileType VARCHAR(255), " +
                "headerLineNumbers VARCHAR(255), " +
                "delimiter VARCHAR(255)" +
                ")";
            createTable(createWizardDataTable, props);

            String createVariablesTable = "CREATE TABLE variables " +
                "(" +
                "variableId INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "wizardDataId VARCHAR(255)," +
                "columnNumber INT, " +
                "variableName VARCHAR(255), " +
                "metadataType VARCHAR(14), " +
                "metadataTypeStructure VARCHAR(20), " +
                "verticalDirection VARCHAR(4), " +
                "metadataValueType VARCHAR(10)" +
                ")";
            createTable(createVariablesTable, props);


            String createVariableMetadataTable = "CREATE TABLE variableMetadata " +
                "(" +
                "variableId INT, " +
                "complianceLevel VARCHAR(255), " +
                "metadataKey VARCHAR(255), " +
                "metadataValue VARCHAR(255)" +
                ")";
            createTable(createVariableMetadataTable, props);

            String createGlobalMetadataTable = "CREATE TABLE globalMetadata " +
                "(" +
                "wizardDataId VARCHAR(255)," +
                "metadataGroup VARCHAR(255), " +
                "metadataValueType VARCHAR(255), " +
                "metadataKey VARCHAR(255), " +
                "metadataValue VARCHAR(255)" +
                ")";
            createTable(createGlobalMetadataTable, props);

            String createMetadataProfileDataTable = "CREATE TABLE metadataProfileData " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "attributeName VARCHAR(100), " +
                "complianceLevel VARCHAR(12), " +
                "description CLOB(64000), " +
                "displayName VARCHAR(255), " +
                "exampleValues CLOB(64000), " +
                "metadataGroup VARCHAR(255), " +
                "metadataProfileName VARCHAR(20), " +
                "metadataProfileVersion VARCHAR(20), " +
                "metadataType VARCHAR(255), " +
                "metadataTypeStructureName VARCHAR(255), " +
                "metadataValueType VARCHAR(255)" +
                ")";
            createTable(createMetadataProfileDataTable, props);

            String createIgnoreListTable = "CREATE TABLE ignoreList " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "metadataType VARCHAR(255), " +
                "attributeName VARCHAR(100)" +
                ")";
            createTable(createIgnoreListTable, props);

            insertMetadataProfiles(props);

            String createPlatformTable = "CREATE TABLE platforms " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "name VARCHAR(255), " +
                "imgPath VARCHAR(255), " +
                "cfType INTEGER, " +
                "community INTEGER" +
                ")";
            createTable(createPlatformTable, props);

            String createFileTypeTable = "CREATE TABLE fileTypes " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                +
                "name VARCHAR(255)" +
                ")";
            createTable(createFileTypeTable, props);

            String createCfTypeTable = "CREATE TABLE cfTypes " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                +
                "name VARCHAR(255)" +
                ")";
            createTable(createCfTypeTable, props);

            String createMetadataProfileTable = "CREATE TABLE metadataProfiles " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                + "name VARCHAR(10), " +
                "community INTEGER"
                + ")";
            createTable(createMetadataProfileTable, props);

            String createCommunityTable = "CREATE TABLE communities " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                "name VARCHAR(255), " +
                "fileType INTEGER " +
                ")";
            createTable(createCommunityTable, props);

            String createDelimiterTable = "CREATE TABLE delimiters " +
                "(" +
                "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                +
                "name VARCHAR(255), " +
                "characterSymbol VARCHAR(10)" +
                ")";
            createTable(createDelimiterTable, props);

            // Insert the resources into the db.
            insertResources(props);

            String createUsersTable = "CREATE TABLE users" +
                "(" +
                "userId INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                +
                "userName VARCHAR(50) not null, " +
                "password VARCHAR(80) not null, " +
                "accessLevel INTEGER not null, " +
                "accountStatus INTEGER not null, " +
                "emailAddress VARCHAR(75) not null, " +
                "fullName VARCHAR(100) not null, " +
                "dateCreated TIMESTAMP not null, " +
                "dateModified TIMESTAMP not null" +
                ")";
            createTable(createUsersTable, props);

            // Add default admin user to users table.
            addDefaultAdminUser(props);

            // Okay, we're done.  Shut down this particular connection to the database.
            try {
                connection = DriverManager.getConnection(url + ";shutdown=true");
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                // As per the Derby docs, the shutdown commands always raise SQLExceptions. (lame!)
                logger.info("Finished creating database. Shutting down database...");
            }


        } else {

            // Update existing properties table.
            populatePropertiesTable(props);

            logger.info("Nothing to do here... Database already exists.");
        }
    }

    /**
     * Creates a table in the derby database.
     *
     * @param statement The create SQL statement.
     * @param props     RosettaProperties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException If unable to create instance of the database
     *                                                 driver.
     * @throws SQLException                            If an SQL exceptions occurs during create table transaction.
     */
    private void createTable(String statement, Properties props)
            throws NonTransientDataAccessResourceException, SQLException {

        Connection connection;
        PreparedStatement preparedStatement;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException(
                    "Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url") + ";create=true";

        if (username != null && password != null) {
            connection = DriverManager.getConnection(url, username, password);
        } else {
            connection = DriverManager.getConnection(url);
        }

        preparedStatement = connection.prepareStatement(statement);
        preparedStatement.executeUpdate();

        // Clean up.
        if (preparedStatement != null) {
            preparedStatement.close();
        }

        if (connection != null) {
            connection.close();
        }
    }

    /**
     *
     * @param props
     * @throws SQLException
     * @throws RosettaDataException
     */
    private void insertMetadataProfiles(Properties props) throws SQLException, RosettaDataException {
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
        if (username != null && password != null) {
            connection = DriverManager.getConnection(url, username, password);
        } else {
            connection = DriverManager.getConnection(url);
        }

        String insertStatement = "INSERT INTO metadataProfileData ("
            + "attributeName, complianceLevel, description, exampleValues, "
            + "metadataGroup, metadataProfileName, metadataProfileVersion, "
            + "metadataType, metadataTypeStructureName, metadataValueType) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        MetadataProfileLoader metadataProfileLoader = new MetadataProfileLoader();
        List<edu.ucar.unidata.rosetta.domain.MetadataProfile> metadataProfiles = metadataProfileLoader.loadMetadataProfiles();
        for (edu.ucar.unidata.rosetta.domain.MetadataProfile metadataProfile : metadataProfiles) {
            preparedStatement = connection.prepareStatement(insertStatement);
            preparedStatement.setString(1, metadataProfile.getAttributeName());
            preparedStatement.setString(2, metadataProfile.getComplianceLevel());
            preparedStatement.setString(3, metadataProfile.getDescription());
            preparedStatement.setString(4, metadataProfile.getExampleValues());
            preparedStatement.setString(5, metadataProfile.getMetadataGroup());
            preparedStatement.setString(6, metadataProfile.getMetadataProfileName());
            preparedStatement.setString(7, metadataProfile.getMetadataProfileVersion());
            preparedStatement.setString(8, metadataProfile.getMetadataType());
            preparedStatement.setString(9, metadataProfile.getMetadataTypeStructureName());
            preparedStatement.setString(10, metadataProfile.getMetadataValueType());

            preparedStatement.executeUpdate();
        }

        String[] ignoreListValues = {
                "CoordinateVariable=axis",
                "CoordinateVariable=coverage_content_type",
                "CoordinateVariable=_FillValue",
                "CoordinateVariable=valid_min",
                "CoordinateVariable=valid_min",
                "CoordinateVariable=valid_max",
                "DataVariable=_FillValue",
                "DataVariable=coordinates",
                "DataVariable=coverage_content_type",
                "DataVariable=valid_min",
                "DataVariable=valid_max",
                "Global=featureType",
                "Global=conventions",
                "MetadataGroup=geospatial_lat_start",
                "MetadataGroup=geospatial_lon_start",
                "MetadataGroup=time_coverage_start",
                "MetadataGroup=geospatial_lat_end",
                "MetadataGroup=geospatial_lon_end",
                "MetadataGroup=time_coverage_end"
        };


        insertStatement = "INSERT INTO ignoreList (metadataType, attributeName) VALUES (?, ?)";
        for (int i = 0; i < ignoreListValues.length; i++) {
            preparedStatement = connection.prepareStatement(insertStatement);
            String[] ignore = ignoreListValues[i].split("=");
            preparedStatement.setString(1, ignore[0]);
            preparedStatement.setString(2, ignore[1]);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Inserts the rosetta-specific resources glean from xml files into the database.
     *
     * @param props RosettaProperties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException If unable to create instance of database
     *                                                 driver.
     * @throws SQLException                            If an SQL exceptions occurs during insert transaction.
     * @throws RosettaDataException                    If unable to access the resource to persist.
     */
    private void insertResources(Properties props)
            throws NonTransientDataAccessResourceException, SQLException, RosettaDataException {

        Connection connection;
        PreparedStatement preparedStatement = null;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException(
                    "Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url");
        if (username != null && password != null) {
            connection = DriverManager.getConnection(url, username, password);
        } else {
            connection = DriverManager.getConnection(url);
        }

        // Define our statements for the various resource types.
        String delimiterStatement = "INSERT INTO delimiters (name, characterSymbol) VALUES (?, ?)";
        String cfTypeStatement = "INSERT INTO cfTypes (name) VALUES (?)";
        String fileTypeStatement = "INSERT INTO fileTypes (name) VALUES (?)";
        String metadataProfileStatement = "INSERT INTO metadataProfiles (name, community) VALUES (?, ?)";
        String platformStatement = "INSERT INTO platforms (name, imgPath, cfType, community) VALUES (?, ?, ?, ?)";
        String communityStatement = "INSERT INTO communities (name, fileType) VALUES (?, ?)";

        ResourceLoader resourceManager = new ResourceLoader();
        List<RosettaResource> resources = resourceManager.loadResources();
        for (RosettaResource resource : resources) {
            // Set the resources depending on the type.
            if (resource instanceof CfType) {
                // CF type resource.
                preparedStatement = connection.prepareStatement(cfTypeStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.executeUpdate();

            } else if (resource instanceof Delimiter) {
                // Delimiter resource.
                preparedStatement = connection.prepareStatement(delimiterStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.setString(2, ((Delimiter) resource).getCharacterSymbol());
                preparedStatement.executeUpdate();

            } else if (resource instanceof FileType) {
                // File type resource.
                preparedStatement = connection.prepareStatement(fileTypeStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.executeUpdate();

            } else if (resource instanceof Platform) {
                // Platform resource.

                // Get the primary key values for the cfTypes and stash them in a map for quick access.
                Map<String, Integer> cfTypeMap = new HashMap<>();
                String getCfTypeStatement = "SELECT * FROM cfTypes";
                preparedStatement = connection.prepareStatement(getCfTypeStatement);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    cfTypeMap.put(name, id);
                }

                // Get the primary key values for the communities and stash them in a map for quick access.
                Map<String, Integer> communityMap = new HashMap<>();
                String getCommunityStatement = "SELECT DISTINCT id, name FROM communities";
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
            } else if (resource instanceof Community) {
                // Community resource.

                // Get the primary key values for the file types and stash them in a map for quick access.
                Map<String, Integer> fileTypeMap = new HashMap<>();
                String getFileTypeStatement = "SELECT * FROM fileTypes";
                preparedStatement = connection.prepareStatement(getFileTypeStatement);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    fileTypeMap.put(name, id);
                }

                // Create an entry in the communities table for all of the file types.
                List<String> fileTypes = ((Community) resource).getFileType();
                for (String fileType : fileTypes) {
                    preparedStatement = connection.prepareStatement(communityStatement);
                    preparedStatement.setString(1, resource.getName());
                    preparedStatement.setInt(2, fileTypeMap.get(fileType));
                    preparedStatement.executeUpdate();
                }

            } else {
                // Metadata profile resource.

                // Get the primary key values for the communities and stash them in a map for quick access.
                Map<String, Integer> communityMap = new HashMap<>();
                String getCommunityStatement = "SELECT DISTINCT id, name FROM communities";
                preparedStatement = connection.prepareStatement(getCommunityStatement);
                ResultSet rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    communityMap.put(name, id);
                }

                // Create an entry in the metadata profiles table for all of the communities.
                List<Community> communities = ((MetadataProfile) resource).getCommunities();
                for (Community community : communities) {
                    preparedStatement = connection.prepareStatement(metadataProfileStatement);
                    preparedStatement.setString(1, resource.getName());
                    preparedStatement.setInt(2, communityMap.get(community.getName()));
                    preparedStatement.executeUpdate();
                }
            }
        }

        // Clean up.
        if (preparedStatement != null) {
            preparedStatement.close();
        }

        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Populates a table with configuration properties data.
     *
     * @param props RosettaProperties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException If unable to create instance of the database
     *                                                 driver.
     * @throws SQLException                            If an SQL exceptions occurs during insert transaction.
     */
    private void populatePropertiesTable(Properties props)
            throws NonTransientDataAccessResourceException, SQLException {

        Connection connection;
        PreparedStatement preparedStatement;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException(
                    "Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url");
        if (username != null && password != null) {
            connection = DriverManager.getConnection(url, username, password);
        } else {
            connection = DriverManager.getConnection(url);
        }

        // See if properties have already been persisted prior to this time.
        String statement = "SELECT * FROM properties";
        preparedStatement = connection.prepareStatement(statement);
        ResultSet rs = preparedStatement.executeQuery();
        Map<String, String> propertiesMap = new HashMap<>();
        while (rs.next()) {
            propertiesMap.put(rs.getString("propertyKey"), rs.getString("propertyValue"));
        }

        // Create prepared statements to persist the property data.  If the data is already persisted, compare the
        // value to what is stored in the database.  Log and differences and update the persisted value if necessary.
        // TODO: In future, notify admin of first these differences via interface and let him/her sort it out.
        Enumeration propertyNames = (Enumeration) props.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String value = props.getProperty(key);

            if (propertiesMap.containsKey(key)) {
                // Property has been already been persisted.  See if persisted value matches what is in the properties.

                if (!propertiesMap.get(key).equals(value)) {
                    // Persisted data has different value than what is in the properties file.  Update persisted data.
                    logger.info("Persisted " + key + " to be changed from " + propertiesMap.get(key) + " to "
                            + value);
                    statement = "UPDATE properties SET propertyKey = ?, propertyValue = ? AND dateCreated =? WHERE propertyKey = ?";
                    preparedStatement = connection.prepareStatement(statement);
                    preparedStatement.setString(1, key);
                    preparedStatement.setString(2, value);
                    preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    preparedStatement.setString(4, key);
                    preparedStatement.executeUpdate();
                }

            } else {
                // Property has NOT been persisted before. Add it.
                statement = "INSERT INTO properties(propertyKey, propertyValue, dateCreated) " +
                        "VALUES (?,?,?)";
                preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setString(1, key);
                preparedStatement.setString(2, value);
                preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                preparedStatement.executeUpdate();
            }
        }

        // Clean up.
        if (preparedStatement != null) {
            preparedStatement.close();
        }

        if (connection != null) {
            connection.close();
        }
    }

    /**
     * Shuts down the embedded derby database by de-registering the driver.
     *
     * @param props RosettaProperties from application.properties that may be used for database
     *              shutdown.
     * @throws SQLException If an SQL exceptions occurs during database shutdown.
     */
    public void shutdownDatabase(Properties props) throws SQLException {

        Connection connection;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException(
                    "Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url") + ";create=true";

        // Okay, we're done.  Shut down this particular connection to the database.
        try {
            if (username != null && password != null) {
                connection = DriverManager.getConnection(url + ";shutdown=true", username, password);
            } else {
                connection = DriverManager.getConnection(url + ";shutdown=true");
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            // As per the Derby docs, the shutdown commands always raise SQLExceptions. (lame!)
            logger.info("Shutting down database...");
        }

        Driver driver = DriverManager.getDriver(url);
        logger.info("De-registering jdbc driver.");
        DriverManager.deregisterDriver(driver);
    }
}


