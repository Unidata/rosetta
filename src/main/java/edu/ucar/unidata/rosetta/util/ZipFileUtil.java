/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.util;

import static java.util.stream.Collectors.toCollection;
import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for zip file manipulation. Method code taken from the following and modified:
 * http://www.avajava.com/tutorials/lessons/how-do-i-unzip-the-contents-of-a-zip-file.html
 *
 * @author sarms@ucar.edu
 * @author oxelson@ucar.edu
 */

public class ZipFileUtil {

  private static final Logger logger = LogManager.getLogger();


  /**
   * Zips the contents of a directory into a zip file.
   *
   * @param pathToDir The path to the directory whose contents will be added to the zip file.
   * @param zipFileName The name of the zip file to create.
   * @throws RosettaFileException If unable to create zip file.
   */
  public static void zip(String pathToDir, String zipFileName) throws RosettaFileException {
    // Get the contents of the directory to zip.
    File directoryToZip = new File(pathToDir);
    if (directoryToZip.list() == null) {
      throw new RosettaFileException("No data in directory " + pathToDir + " to zip.");
    }

    ArrayList<String> dirContents = new ArrayList<>(Arrays.asList(directoryToZip.list()));

    FileInputStream inStream;
    byte[] buffer = new byte[1024];
    int bytesRead;

    // Construct zip file name and path.
    zipFileName = FilenameUtils.getName(zipFileName);
    zipFileName = zipFileName.replace("file://", "");
    zipFileName = FilenameUtils.concat(pathToDir, zipFileName + ".zip");
    File zipFile = new File(zipFileName);

    // Create input and output streams.
    try (ZipOutputStream outStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
      logger.info("Zipping contents of " + directoryToZip);
      for (String inputFile : dirContents) {
        inStream = new FileInputStream(FilenameUtils.concat(pathToDir, inputFile));
        String filenameInZip = FilenameUtils.getName(inputFile);
        // if (!pathToDir.equals(""))
        // filenameInZip = pathToDir + "/" + filenameInZip;

        // Add a zip entry to the output stream.
        outStream.putNextEntry(new ZipEntry(filenameInZip));

        while ((bytesRead = inStream.read(buffer)) > 0) {
          outStream.write(buffer, 0, bytesRead); // Write it.
        }

        // Close zip entry and file streams
        outStream.closeEntry();
        inStream.close();
      }
    } catch (IOException e) {
      throw new RosettaFileException("Unable to read file from zip file: " + e);
    }
  }

  /**
   * Cleans up the zip file inventory. (Some auto-generated OS files can really play havoc if they
   * are not accounted for.)
   *
   * @param inventory The zip file inventory.
   * @return The inventory with the bad files removed.
   */
  private static List<String> cleanInventory(List<String> inventory) {
    // first pass based on typical .gitignore for OS generated files
    Stream<String> cleanInventory =
        inventory.stream().filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".Spotlight-V100"))
            .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".Trashes"))
            .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "ehthumbs.db"))
            .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "Thumbs.db"))
            .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "/__MACOSX/"))
            .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".DS_STORE"));

    return cleanInventory.collect(toCollection(ArrayList::new));
  }

  /**
   * Writes the inventory (list of contents) of a compressed file to disk for future use.
   *
   * @param inventory The compressed file inventory.
   * @param filePath The path to where the inventory file will be written.
   * @throws RosettaFileException If unable to write inventory file to disk.
   */
  private static void createInventory(List<String> inventory, String filePath) throws RosettaFileException {
    File inventoryFile = new File(FilenameUtils.concat(filePath, "rosetta.inventory"));

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(inventoryFile, true))) {
      for (String entry : inventory) {
        writer.append(entry);
        writer.append("\n");
      }
    } catch (IOException e) {
      throw new RosettaFileException("Unable write zip inventory to disk: " + e);
    }
  }

  public static List<String> getInventory(String filePath) throws RosettaFileException {
    File inventoryFile = new File(FilenameUtils.concat(filePath, "rosetta.inventory"));

    List<String> inventory = new ArrayList<>();
    try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inventoryFile))) {
      String currentLine;
      while ((currentLine = bufferedReader.readLine()) != null) {
        inventory.add(currentLine);
      }
    } catch (IOException e) {
      throw new RosettaFileException("Unable to retrieve inventory file data: " + e);
    }
    return inventory;
  }

  /**
   * Reads and returns that data from a file in a zip file in string format (used to get JSON file
   * data).
   *
   * @param fileNameToFind The file entry to find in the zip file.
   * @param zipFileName The name of the zip file in which to search for the requested file.
   * @return The file data
   * @throws RosettaFileException If unable to read file from zip file.
   */
  public static String readFileFromZip(String fileNameToFind, String zipFileName) throws RosettaFileException {
    zipFileName = zipFileName.replace("file://", "");
    StringBuilder data = new StringBuilder();
    String line;
    try (ZipFile zf = new ZipFile(zipFileName)) {
      ZipEntry fileInZip = zf.getEntry(fileNameToFind);
      // Does the file exists in zip file?
      if (fileInZip != null) {
        // Get the file data.
        InputStream inStream = zf.getInputStream(fileInZip);
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(inStream));
        while ((line = buffReader.readLine()) != null) {
          data.append(line);
        }
        buffReader.close();
        inStream.close();
      }
    } catch (IOException e) {
      throw new RosettaFileException("Unable to read file from zip file: " + e);
    }
    return data.toString();
  }

  /**
   * Unzips a zip file.
   *
   * @param zipFileName The name of the zip file.
   * @param uncompressed_dir The directory in which the zip file will be unzipped.
   * @throws RosettaFileException Unable to unzip file and take inventory.
   */
  public static void unZip(String zipFileName, String uncompressed_dir) throws RosettaFileException {
    zipFileName = zipFileName.replace("file://", "");
    // The full path to our zip file to uncompress.
    String zipFilePath = FilenameUtils.concat(uncompressed_dir, zipFileName);
    List<String> inventory = new ArrayList<>();
    try (ZipFile zipFile = new ZipFile(new File(zipFilePath))) {
      Enumeration<?> e = zipFile.entries();
      while (e.hasMoreElements()) {
        ZipEntry zipEntry = (ZipEntry) e.nextElement();

        // Name of zip file entry.
        String name = zipEntry.getName();

        // Full path to zip file entry.
        Path filePath = Paths.get(uncompressed_dir, name);
        File file = filePath.toFile();

        // Directory.
        if (name.endsWith("/")) {
          if (file.mkdirs()) {
            continue;
          } else {
            throw new RosettaFileException("Unable to create unzipped directory during zip file expansion.");
          }
        }
        // Parent.
        File parent = file.getParentFile();
        if (parent != null) {
          if (parent.mkdirs()) {
            throw new RosettaFileException("Unable to create unzipped parent directory during zip file expansion.");
          }
        }

        InputStream inputStream = zipFile.getInputStream(zipEntry);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        byte[] bytes = new byte[1024];
        int length;
        while ((length = inputStream.read(bytes)) >= 0) {
          fileOutputStream.write(bytes, 0, length); // Write it.
        }
        inputStream.close();
        fileOutputStream.close();

        // Add file to inventory.
        if (!name.endsWith("/")) {
          inventory.add(name);
        }
      }

    } catch (IOException e) {
      throw new RosettaFileException("Unable to unzip file: " + e);
    }
    inventory = cleanInventory(inventory); // Clean up inventory.
    createInventory(inventory, uncompressed_dir);
  }
}
