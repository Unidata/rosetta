/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.init.resources;

import edu.ucar.unidata.rosetta.domain.resources.CfType;
import edu.ucar.unidata.rosetta.domain.resources.Community;
import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import edu.ucar.unidata.rosetta.domain.resources.FileType;
import edu.ucar.unidata.rosetta.domain.resources.MetadataProfile;
import edu.ucar.unidata.rosetta.domain.resources.Platform;
import edu.ucar.unidata.rosetta.domain.resources.RosettaResource;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import java.io.File;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.NonTransientDataAccessResourceException;

/**
 * An implementation of the DbInitManager that creates the default embedded derby database for the rosetta application.
 */
@SuppressWarnings("SpellCheckingInspection")
public class EmbeddedDerbyDbInitManager implements DbInitManager {

  private static final Logger logger = LogManager.getLogger();

  /**
   * Adds default admin user during database creation.
   *
   * @param props RosettaProperties from which the database username and password are glean.
   * @throws NonTransientDataAccessResourceException If unable to create instance of the database driver.
   * @throws SQLException                            If an SQL exceptions occurs during insert transaction.
   */
  private void addDefaultAdminUser(Properties props) throws NonTransientDataAccessResourceException, SQLException {

    String insertStatement = "INSERT INTO users "
        + "(userName, password, accessLevel, accountStatus, emailAddress, fullName, dateCreated, dateModified) VALUES "
        + "(?,?,?,?,?,?,?,?)";

    // Create database connection
    try (Connection connection = createDatabaseConnection(props);
      PreparedStatement insertAdminUserPS = connection.prepareStatement(insertStatement)) {

      insertAdminUserPS.setString(1, "admin");
      insertAdminUserPS.setString(2, "$2a$10$gJ4ITtIMNpxsU0xmx6qoE.0MGZ2fv8HpoaL1IlgNdhBlUgmcVwRDO");
      insertAdminUserPS.setInt(3, 2);
      insertAdminUserPS.setInt(4, 1);
      insertAdminUserPS.setString(5, "admin@foo.bar.baz");
      insertAdminUserPS.setString(6, "Rosetta Administrator");
      insertAdminUserPS.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
      insertAdminUserPS.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
      insertAdminUserPS.executeUpdate();

      // Clean up.
      insertAdminUserPS.close();
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

    if (!dbFile.exists()) { // Create the database tables;
      logger.info("Database does not exist yet.  Creating...");

      String createPropertiesTable = "CREATE TABLE properties " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "propertyKey VARCHAR(255) not null, " + "propertyValue VARCHAR(255) not null, "
          + "dateCreated TIMESTAMP not null" + ")";
      createTable(createPropertiesTable, props);

      // Populate properties table.
      populatePropertiesTable(props);

      // Table containing uploaded file data.
      String createUploadedFileTable = "CREATE TABLE uploadedFiles " + "(" + "id VARCHAR(255) not null, "
          + "fileName VARCHAR(255) not null, " + "fileType VARCHAR(50) not null" + ")";
      createTable(createUploadedFileTable, props);

      String createWizardDataTable =
          "CREATE TABLE wizardData " + "(" + "id VARCHAR(255) primary key not null, " + "cfType VARCHAR(50), "
              + "community VARCHAR(100), " + "metadataProfile VARCHAR(255), " + "platform VARCHAR(100), "
              + "dataFileType VARCHAR(255), " + "headerLineNumbers VARCHAR(255), " + "delimiter VARCHAR(255)" + ")";
      createTable(createWizardDataTable, props);

      String createVariablesTable = "CREATE TABLE variables " + "("
          + "variableId INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "wizardDataId VARCHAR(255)," + "columnNumber INT, " + "variableName VARCHAR(255), "
          + "metadataType VARCHAR(14), " + "metadataTypeStructure VARCHAR(20), " + "verticalDirection VARCHAR(4), "
          + "metadataValueType VARCHAR(10)" + ")";
      createTable(createVariablesTable, props);

      String createVariableMetadataTable = "CREATE TABLE variableMetadata " + "(" + "variableId INT, "
          + "complianceLevel VARCHAR(255), " + "metadataKey VARCHAR(255), " + "metadataValue VARCHAR(255)" + ")";
      createTable(createVariableMetadataTable, props);

      String createGlobalMetadataTable =
          "CREATE TABLE globalMetadata " + "(" + "wizardDataId VARCHAR(255)," + "metadataGroup VARCHAR(255), "
              + "metadataValueType VARCHAR(255), " + "metadataKey VARCHAR(255), " + "metadataValue VARCHAR(255)" + ")";
      createTable(createGlobalMetadataTable, props);

      String createMetadataProfileDataTable = "CREATE TABLE metadataProfileData " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "attributeName VARCHAR(100), " + "complianceLevel VARCHAR(12), " + "description CLOB(64000), "
          + "displayName VARCHAR(255), " + "exampleValues CLOB(64000), " + "metadataGroup VARCHAR(255), "
          + "metadataProfileName VARCHAR(20), " + "metadataProfileVersion VARCHAR(20), " + "metadataType VARCHAR(255), "
          + "metadataTypeStructureName VARCHAR(255), " + "metadataValueType VARCHAR(255)" + ")";
      createTable(createMetadataProfileDataTable, props);

      String createIgnoreListTable = "CREATE TABLE ignoreList " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "metadataType VARCHAR(255), " + "attributeName VARCHAR(100)" + ")";
      createTable(createIgnoreListTable, props);

      insertMetadataProfiles(props);

      String createPlatformTable = "CREATE TABLE platforms " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "name VARCHAR(255), " + "imgPath VARCHAR(255), " + "cfType INTEGER, " + "community INTEGER" + ")";
      createTable(createPlatformTable, props);

      String createFileTypeTable = "CREATE TABLE fileTypes " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "name VARCHAR(255)" + ")";
      createTable(createFileTypeTable, props);

      String createCfTypeTable = "CREATE TABLE cfTypes " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "name VARCHAR(255)" + ")";
      createTable(createCfTypeTable, props);

      String createMetadataProfileTable = "CREATE TABLE metadataProfiles " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "name VARCHAR(10), " + "community INTEGER" + ")";
      createTable(createMetadataProfileTable, props);

      String createCommunityTable = "CREATE TABLE communities " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "name VARCHAR(255), " + "fileType INTEGER " + ")";
      createTable(createCommunityTable, props);

      String createDelimiterTable = "CREATE TABLE delimiters " + "("
          + "id INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "name VARCHAR(255), " + "characterSymbol VARCHAR(10)" + ")";
      createTable(createDelimiterTable, props);

      // Insert the resources into the db.
      insertResources(props);

      String createUsersTable = "CREATE TABLE users" + "("
          + "userId INTEGER primary key not null GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
          + "userName VARCHAR(50) not null, " + "password VARCHAR(80) not null, " + "accessLevel INTEGER not null, "
          + "accountStatus INTEGER not null, " + "emailAddress VARCHAR(75) not null, "
          + "fullName VARCHAR(100) not null, " + "dateCreated TIMESTAMP not null, " + "dateModified TIMESTAMP not null"
          + ")";
      createTable(createUsersTable, props);

      // Add default admin user to users table.
      addDefaultAdminUser(props);
    } else { // Update existing properties table.
      populatePropertiesTable(props);
      logger.info("Nothing to do here... Database already exists.");
    }
  }

  /**
   * Creates a table in the derby database.
   *
   * @param statement The create SQL statement.
   * @param props     RosettaProperties from which the database username and password are glean.
   * @throws NonTransientDataAccessResourceException If unable to create instance of the database driver.
   * @throws SQLException                            If an SQL exceptions occurs during create table transaction.
   */
  private void createTable(String statement, Properties props)
      throws NonTransientDataAccessResourceException, SQLException {

    // Create database connection
    Connection connection = createDatabaseConnection(props);
    PreparedStatement createTablePS = connection.prepareStatement(statement);
    createTablePS.executeUpdate();

    // Clean up.
    createTablePS.close();
    connection.close();
  }

  /**
   * Creates a database connection.
   *
   * @param props RosettaProperties from which the database username and password are glean.
   */
  private Connection createDatabaseConnection(Properties props) {
    return createDatabaseConnection(props, false);
  }

  /**
   * Creates a database connection.
   *
   * @param props    RosettaProperties from which the database username and password are glean.
   * @param shutdown Action the database connection will take (create or shutdown).
   */
  private Connection createDatabaseConnection(Properties props, boolean shutdown) {
    try {
      Class.forName(props.getProperty("jdbc.driverClassName"));
    } catch (ClassNotFoundException e) {
      throw new NonTransientDataAccessResourceException("Unable to find database driver class: " + e);
    }
    // Database attributes stored as Properties.
    Properties connectionProperties = new Properties();

    // Action property for the data connection.
    if (shutdown) {
      connectionProperties.put("shutdown", "true");
    } else {
      connectionProperties.put("create", "true");
    }

    String username = StringUtils.stripToNull(props.getProperty("jdbc.username"));
    String password = StringUtils.stripToNull(props.getProperty("jdbc.password"));
    if (username != null && password != null) {
      connectionProperties.put("username", username);
      connectionProperties.put("password", password);
    }

    String url = props.getProperty("jdbc.url");

    // Connect to the database.
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(url, connectionProperties);
    } catch (SQLException e) {
      // As per the Derby docs, the shutdown commands always raise SQLExceptions. (lame!)
      logger.info("Shutting down database...");
    }
    return connection;
  }

  /**
   * Inserts the metadata profile data glean from xml files into the database.
   *
   * @param props RosettaProperties from which the database username and password are glean.
   * @throws SQLException         If an SQL exceptions occurs during insert transaction.
   * @throws RosettaDataException If unable to access the metadata profiles to persist.
   */
  private void insertMetadataProfiles(Properties props) throws SQLException, RosettaDataException {
    // Create database connection
    Connection connection = createDatabaseConnection(props);
    String insertStatement =
        "INSERT INTO metadataProfileData (" + "attributeName, complianceLevel, description, exampleValues, "
            + "metadataGroup, metadataProfileName, metadataProfileVersion, "
            + "metadataType, metadataTypeStructureName, metadataValueType) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    MetadataProfileLoader metadataProfileLoader = new MetadataProfileLoader();
    List<edu.ucar.unidata.rosetta.domain.MetadataProfile> metadataProfiles =
        metadataProfileLoader.loadMetadataProfiles();
    for (edu.ucar.unidata.rosetta.domain.MetadataProfile metadataProfile : metadataProfiles) {
      PreparedStatement insertMetadataProfilesPS = connection.prepareStatement(insertStatement);
      insertMetadataProfilesPS.setString(1, metadataProfile.getAttributeName());
      insertMetadataProfilesPS.setString(2, metadataProfile.getComplianceLevel());
      insertMetadataProfilesPS.setString(3, metadataProfile.getDescription());
      insertMetadataProfilesPS.setString(4, metadataProfile.getExampleValues());
      insertMetadataProfilesPS.setString(5, metadataProfile.getMetadataGroup());
      insertMetadataProfilesPS.setString(6, metadataProfile.getMetadataProfileName());
      insertMetadataProfilesPS.setString(7, metadataProfile.getMetadataProfileVersion());
      insertMetadataProfilesPS.setString(8, metadataProfile.getMetadataType());
      insertMetadataProfilesPS.setString(9, metadataProfile.getMetadataTypeStructureName());
      insertMetadataProfilesPS.setString(10, metadataProfile.getMetadataValueType());
      insertMetadataProfilesPS.executeUpdate();
      // Clean up.
      insertMetadataProfilesPS.close();
    }

    String[] ignoreListValues = {"CoordinateVariable=axis", "CoordinateVariable=coverage_content_type",
        "CoordinateVariable=_FillValue", "CoordinateVariable=valid_min", "CoordinateVariable=valid_min",
        "CoordinateVariable=valid_max", "DataVariable=_FillValue", "DataVariable=coordinates",
        "DataVariable=coverage_content_type", "DataVariable=valid_min", "DataVariable=valid_max", "Global=featureType",
        "Global=conventions", "MetadataGroup=geospatial_lat_start", "MetadataGroup=geospatial_lon_start",
        "MetadataGroup=time_coverage_start", "MetadataGroup=geospatial_lat_end", "MetadataGroup=geospatial_lon_end",
        "MetadataGroup=time_coverage_end"};

    insertStatement = "INSERT INTO ignoreList (metadataType, attributeName) VALUES (?, ?)";
    for (String ignoreListValue : ignoreListValues) {
      PreparedStatement insertIgnoreListPS = connection.prepareStatement(insertStatement);
      String[] ignore = ignoreListValue.split("=");
      insertIgnoreListPS.setString(1, ignore[0]);
      insertIgnoreListPS.setString(2, ignore[1]);
      insertIgnoreListPS.executeUpdate();
      // Clean up.
      insertIgnoreListPS.close();
    }
  }

  /**
   * Inserts the rosetta-specific resources glean from xml files into the database.
   *
   * @param props RosettaProperties from which the database username and password are glean.
   * @throws NonTransientDataAccessResourceException If unable to create instance of database driver.
   * @throws SQLException                            If an SQL exceptions occurs during insert transaction.
   * @throws RosettaDataException                    If unable to access the resource to persist.
   */
  private void insertResources(Properties props)
      throws NonTransientDataAccessResourceException, SQLException, RosettaDataException {

    // Create database connection
    Connection connection = createDatabaseConnection(props);
    ResourceLoader resourceManager = new ResourceLoader();
    List<RosettaResource> resources = resourceManager.loadResources();
    for (RosettaResource resource : resources) {
      // Set the resources depending on the type.
      if (resource instanceof CfType) {  // CF type resource.
        String insertStatement = "INSERT INTO cfTypes (name) VALUES (?)";
        PreparedStatement insertCFTypeResourcesPS = connection.prepareStatement(insertStatement);
        insertCFTypeResourcesPS.setString(1, resource.getName());
        insertCFTypeResourcesPS.executeUpdate();
        // Clean up.
        insertCFTypeResourcesPS.close();

      } else if (resource instanceof Delimiter) { // Delimiter resource.
        String insertStatement = "INSERT INTO delimiters (name, characterSymbol) VALUES (?, ?)";
        PreparedStatement insertDelimiterResourcesPS = connection.prepareStatement(insertStatement);
        insertDelimiterResourcesPS.setString(1, resource.getName());
        insertDelimiterResourcesPS.setString(2, ((Delimiter) resource).getCharacterSymbol());
        insertDelimiterResourcesPS.executeUpdate();
        // Clean up.
        insertDelimiterResourcesPS.close();

      } else if (resource instanceof FileType) { // File type resource.
        String insertStatement = "INSERT INTO fileTypes (name) VALUES (?)";
        PreparedStatement insertFileTypeResourcesPS = connection.prepareStatement(insertStatement);
        insertFileTypeResourcesPS.setString(1, resource.getName());
        insertFileTypeResourcesPS.executeUpdate();
        // Clean up.
        insertFileTypeResourcesPS.close();

      } else if (resource instanceof Platform) {  // Platform resource.
        // Get the primary key values for the cfTypes and stash them in a map for quick access.
        Map<String, Integer> cfTypeMap = new HashMap<>();
        String selectStatement = "SELECT * FROM cfTypes";
        ResultSet rs = connection.prepareStatement(selectStatement).executeQuery();
        while (rs.next()) {
          int id = rs.getInt("id");
          String name = rs.getString("name");
          cfTypeMap.put(name, id);
        }

        // Get the primary key values for the communities and stash them in a map for quick access.
        Map<String, Integer> communityMap = new HashMap<>();
        selectStatement = "SELECT DISTINCT id, name FROM communities";
        PreparedStatement selectCommunitiesPS = connection.prepareStatement(selectStatement);
        rs = selectCommunitiesPS.executeQuery();
        while (rs.next()) {
          int id = rs.getInt("id");
          String name = rs.getString("name");
          communityMap.put(name, id);
        }
        // Clean up.
        selectCommunitiesPS.close();

        String insertStatement = "INSERT INTO platforms (name, imgPath, cfType, community) VALUES (?, ?, ?, ?)";
        PreparedStatement insertPlatformResourcesPS = connection.prepareStatement(insertStatement);
        insertPlatformResourcesPS.setString(1, resource.getName());
        insertPlatformResourcesPS.setString(2, ((Platform) resource).getImgPath());
        insertPlatformResourcesPS.setInt(3, cfTypeMap.get(((Platform) resource).getCfType()));
        insertPlatformResourcesPS.setInt(4, communityMap.get(((Platform) resource).getCommunity()));
        insertPlatformResourcesPS.executeUpdate();
        // Clean up.
        insertPlatformResourcesPS.close();

      } else if (resource instanceof Community) { // Community resource.
        // Get the primary key values for the file types and stash them in a map for quick access.
        Map<String, Integer> fileTypeMap = new HashMap<>();
        String selectStatement = "SELECT * FROM fileTypes";
        ResultSet rs = connection.prepareStatement(selectStatement).executeQuery();
        while (rs.next()) {
          int id = rs.getInt("id");
          String name = rs.getString("name");
          fileTypeMap.put(name, id);
        }

        // Create an entry in the communities table for all of the file types.
        List<String> fileTypes = ((Community) resource).getFileType();
        String insertStatement = "INSERT INTO communities (name, fileType) VALUES (?, ?)";
        for (String fileType : fileTypes) {
          PreparedStatement insertCommunityResourcesPS = connection.prepareStatement(insertStatement);
          insertCommunityResourcesPS.setString(1, resource.getName());
          insertCommunityResourcesPS.setInt(2, fileTypeMap.get(fileType));
          insertCommunityResourcesPS.executeUpdate();
          // Clean up.
          insertCommunityResourcesPS.close();
        }

      } else { // Metadata profile resource.
        // Get the primary key values for the communities and stash them in a map for quick access.
        Map<String, Integer> communityMap = new HashMap<>();
        String selectStatement = "SELECT DISTINCT id, name FROM communities";
        PreparedStatement selectCommunitiesPS = connection.prepareStatement(selectStatement);
        ResultSet rs = selectCommunitiesPS.executeQuery();
        while (rs.next()) {
          int id = rs.getInt("id");
          String name = rs.getString("name");
          communityMap.put(name, id);
        }
        // Clean up.
        selectCommunitiesPS.close();

        // Create an entry in the metadata profiles table for all of the communities.
        List<Community> communities = ((MetadataProfile) resource).getCommunities();
        String metadataProfileStatement = "INSERT INTO metadataProfiles (name, community) VALUES (?, ?)";
        for (Community community : communities) {
          PreparedStatement insertCommunityResourcesPS = connection.prepareStatement(metadataProfileStatement);
          insertCommunityResourcesPS.setString(1, resource.getName());
          insertCommunityResourcesPS.setInt(2, communityMap.get(community.getName()));
          insertCommunityResourcesPS.executeUpdate();
          // Clean up.
          insertCommunityResourcesPS.close();
        }
      }
    }

    // Clean up.
    connection.close();
  }

  /**
   * Populates a table with configuration properties data.
   *
   * @param props RosettaProperties from which the database username and password are glean.
   * @throws NonTransientDataAccessResourceException If unable to create instance of the database driver.
   * @throws SQLException                            If an SQL exceptions occurs during insert transaction.
   */
  private void populatePropertiesTable(Properties props) throws NonTransientDataAccessResourceException, SQLException {

    // Create database connection
    Connection connection = createDatabaseConnection(props);

    // See if properties have already been persisted prior to this time.
    String selectStatement = "SELECT * FROM properties";
    PreparedStatement selectPropertiesPS = connection.prepareStatement(selectStatement);
    ResultSet rs = selectPropertiesPS.executeQuery();
    Map<String, String> propertiesMap = new HashMap<>();
    while (rs.next()) {
      propertiesMap.put(rs.getString("propertyKey"), rs.getString("propertyValue"));
    }
    // Clean up.
    selectPropertiesPS.close();

    // Create prepared statements to persist the property data. If the data is already persisted, compare the
    // value to what is stored in the database. Log and differences and update the persisted value if necessary.
    // TODO: In future, notify admin of first these differences via interface and let him/her sort it out.
    Enumeration propertyNames = props.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String key = (String) propertyNames.nextElement();
      String value = props.getProperty(key);

      if (propertiesMap.containsKey(key)) {
        // Property has been already been persisted. See if persisted value matches what is in the properties.

        if (!propertiesMap.get(key).equals(value)) {
          // Persisted data has different value than what is in the properties file. Update persisted data.
          logger.info("Persisted " + key + " to be changed from " + propertiesMap.get(key) + " to " + value);
          String updateStatement =
              "UPDATE properties SET propertyKey = ?, propertyValue = ? AND dateCreated =? WHERE propertyKey = ?";
          PreparedStatement updatePropertiesPS = connection.prepareStatement(updateStatement);
          updatePropertiesPS.setString(1, key);
          updatePropertiesPS.setString(2, value);
          updatePropertiesPS.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
          updatePropertiesPS.setString(4, key);
          updatePropertiesPS.executeUpdate();
          // Clean up.
          updatePropertiesPS.close();
        }
      } else {
        // Property has NOT been persisted before. Add it.
        String insertStatement = "INSERT INTO properties(propertyKey, propertyValue, dateCreated) " + "VALUES (?,?,?)";
        PreparedStatement insertPropertiesPS = connection.prepareStatement(insertStatement);
        insertPropertiesPS.setString(1, key);
        insertPropertiesPS.setString(2, value);
        insertPropertiesPS.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        insertPropertiesPS.executeUpdate();
        // Clean up.
        insertPropertiesPS.close();
      }
    }

    // Clean up.
    connection.close();
  }

  /**
   * Shuts down the embedded derby database by de-registering the driver.
   *
   * @param props RosettaProperties from application.properties that may be used for database shutdown.
   * @throws SQLException If an SQL exceptions occurs during database shutdown.
   */
  public void shutdownDatabase(Properties props) throws SQLException {

    logger.info("Shutting down database...");

    // Create the connection and tell the database to shut down.
    Connection connection = createDatabaseConnection(props, true);
    // Clean up.
    connection.close();

    // De-register database driver.
    String url = props.getProperty("jdbc.url");
    Driver driver = DriverManager.getDriver(url);
    logger.info("De-registering jdbc driver.");
    DriverManager.deregisterDriver(driver);
  }
}
