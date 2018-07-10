package edu.ucar.unidata.rosetta.init.resources;

import edu.ucar.unidata.rosetta.domain.resources.*;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;

import java.io.File;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.util.*;

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
     * Adds default admin user during database creation.
     *
     * @param props RosettaProperties from which the database username and password are glean.
     * @throws NonTransientDataAccessResourceException  If unable to create instance of the database driver.
     * @throws SQLException If an SQL exceptions occurs during insert transaction.
     */
    private void addDefaultAdminUser(Properties props) throws NonTransientDataAccessResourceException, SQLException {

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

        String statement = "INSERT INTO users " +
                "(userName, password, accessLevel, accountStatus, emailAddress, fullName, dateCreated, dateModified) VALUES " +
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
        if (preparedStatement != null)
            preparedStatement.close();

        if (connection != null)
            connection.close();
    }

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
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255)" +
                    ")";
            createTable(createFileTypeTable, props);

            String createCfTypeTable = "CREATE TABLE cfTypes " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255)" +
                    ")";
            createTable(createCfTypeTable, props);

            String createCommunityTable = "CREATE TABLE communities " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255), " +
                    "fileType INTEGER" +
                    ")";
            createTable(createCommunityTable, props);

            String createDelimiterTable = "CREATE TABLE delimiters " +
                    "(" +
                    "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
                    "name VARCHAR(255), " +
                    "characterSymbol VARCHAR(10)" +
                    ")";
            createTable(createDelimiterTable, props);

            // Insert the resources into the db.
            insertResources(props);

            String createUsersTable = "CREATE TABLE users" +
                    "(" +
                    "userId INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
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
            } catch(SQLException e) {
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
        String delimiterStatement = "INSERT INTO delimiters (name, characterSymbol) VALUES (?, ?)";
        String cfTypeStatement = "INSERT INTO cfTypes (name) VALUES (?)";
        String fileTypeStatement = "INSERT INTO fileTypes (name) VALUES (?)";
        String platformStatement = "INSERT INTO platforms (name, imgPath, cfType, community) VALUES (?, ?, ?, ?)";
        String communityStatement = "INSERT INTO communities (name, fileType) VALUES (?, ?)";

        ResourceManager resourceManager = new ResourceManager();
        List<RosettaResource> resources = resourceManager.loadResources();
        for (RosettaResource resource: resources) {
            // Set the resources depending on the type.
            if (resource instanceof CfType) {
                // File Type resource.
                preparedStatement = connection.prepareStatement(cfTypeStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.executeUpdate();

            } else if (resource instanceof Delimiter) {
                // File Type resource.
                preparedStatement = connection.prepareStatement(delimiterStatement);
                preparedStatement.setString(1, resource.getName());
                preparedStatement.setString(2, ((Delimiter) resource).getCharacterSymbol());
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
            } else {
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

    /**
     * Populates a table with configuration properties data.
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

        // See if properties have already been persisted prior to this time.
        String statement = "SELECT * FROM properties";
        preparedStatement = connection.prepareStatement(statement);
        ResultSet rs = preparedStatement.executeQuery();
        Map<String, String> propertiesMap = new HashMap<>();
        while(rs.next()) {
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
                    logger.info("Persisted " + key + " to be changed from " + propertiesMap.get(key) + " to " + value);
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
        if (preparedStatement != null)
            preparedStatement.close();

        if (connection != null)
            connection.close();
    }

    /**
     * Shuts down the embedded derby database by de-registering the driver.
     *
     * @param props RosettaProperties from application.properties that may be used for database shutdown.
     * @throws SQLException   If an SQL exceptions occurs during database shutdown.
     */
    public void shutdownDatabase(Properties props) throws SQLException {

        Connection connection;

        try {
            Class.forName(props.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException e) {
            throw new NonTransientDataAccessResourceException("Unable to find database drive class: " + e);
        }

        String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
        String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
        String url = props.getProperty("jdbc.url")  + ";create=true";


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
        } catch(SQLException e) {
            // As per the Derby docs, the shutdown commands always raise SQLExceptions. (lame!)
            logger.info("Shutting down database...");
        }


        Driver driver = DriverManager.getDriver(url);
        logger.info("De-registering jdbc driver.");
        DriverManager.deregisterDriver(driver);
    }
}


