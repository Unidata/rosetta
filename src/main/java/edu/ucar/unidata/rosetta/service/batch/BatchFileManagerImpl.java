/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.batch;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import edu.ucar.unidata.rosetta.converters.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.batch.BatchProcessZip;
import ucar.ma2.InvalidRangeException;

public class BatchFileManagerImpl implements BatchFileManager {

    /* temp - will replace with Jen's refactored service classes */
    private static final String ROSETTA_HOME = System.getProperty("rosetta.content.root.path");  // set in $JAVA_OPTS
    private static final String DOWNLOAD_DIR = "downloads";
    private static final String UPLOAD_DIR = "uploads";

    private static final Logger logger = Logger.getLogger(BatchFileManagerImpl.class);

    /**
     * Create directory where rosetta stashes files uploaded by users.
     *
     * @return The full path to the uploads directory.
     * @throws IOException If unable to create the uploads directory.
     */
    private String createUploadDirectory() throws IOException {
        File uploadDir = new File(FilenameUtils.concat(ROSETTA_HOME, UPLOAD_DIR));

        // If this directory doesn't exist, create it.
        if (!uploadDir.exists()) {
            logger.info("Creating uploads directory at " + uploadDir.getPath());
            if (!uploadDir.mkdirs()) {
                throw new IOException("Unable to create uploads directory.");
            }
        }
        return String.valueOf(uploadDir);
    }

    private static ArrayList<String> unzipAndInventory(File inputZipFile, File uncompressed_dir) {
        // http://www.avajava.com/tutorials/lessons/how-do-i-unzip-the-contents-of-a-zip-file.html
        // No need to reinvent the wheel. However, there isn't any license on this, or any way
        // to really give attribution here.
        ArrayList<String> inventory = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(inputZipFile);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();
                String name = zipEntry.getName();

                Path filePath = Paths.get(uncompressed_dir.toString(), name);
                File file = filePath.toFile();
                if (name.endsWith("/")) {
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                if (parent != null) {
                    parent.mkdirs();
                }

                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();
                inventory.add(file.getAbsolutePath());
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inventory;
    }

    private ArrayList<String> cleanupInventory(ArrayList<String> inventory) {
        // first pass based on typical .gitignore for OS generated files
        Stream<String> cleanInventory = inventory.stream()
                //.map(inventoryFile -> inventoryFile.toUpperCase())
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".Spotlight-V100"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".Trashes"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "ehthumbs.db"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "Thumbs.db"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, "/__MACOSX/"))
                .filter(inventoryFile -> !StringUtils.containsIgnoreCase(inventoryFile, ".DS_STORE"));

        return cleanInventory.collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }

    private void addAllToZip(String zipFileName, String root, List<String> filesToAdd) throws IOException {
        addAllToZip(zipFileName, root, filesToAdd.toArray(new String[0]));
    }

    private void addAllToZip(String zipFileName, String root, String[] filesToAdd) throws IOException {
        FileInputStream inStream;
        byte[] buffer = new byte[1024];
        int bytesRead;

        // Create input and output streams
        try {

            ZipOutputStream outStream = new ZipOutputStream(
                    new FileOutputStream(zipFileName));

            for (String inputFile : filesToAdd) {
                inStream = new FileInputStream(inputFile);
                String filenameInZip = FilenameUtils.getName(inputFile);
                if (!root.equals(""))
                    filenameInZip = root + "/" + filenameInZip;

                // Add a zip entry to the output stream
                outStream.putNextEntry(new ZipEntry(filenameInZip));

                while ((bytesRead = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, bytesRead);
                }

                // Close zip entry and file streams
                outStream.closeEntry();
                inStream.close();
            }
            outStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String batchProcess(BatchProcessZip batchZipFile) throws IOException {
        String uniqueId = batchZipFile.getId();
        String uploadDir = createUploadDirectory();

        String filePath = FilenameUtils.concat(uploadDir, uniqueId);
        List<String> convertedFiles = new ArrayList<>();

        // make directory in which the uploaded zip file will be saved
        File localFileDir = new File(filePath);
        if (!localFileDir.exists()) {
            localFileDir.mkdirs();
        }

        // actually save the uploaded zip file to disk
        File uploadedFile = new File(FilenameUtils.concat(filePath, batchZipFile.getBatchZipName()));
        try (FileOutputStream outputStream = new FileOutputStream(uploadedFile)) {
            outputStream.write(batchZipFile.getBatchZipFile().getFileItem().get());
            outputStream.flush();
        }

        // extract zip file to a special __extracted__ directory
        String extractToDir = FilenameUtils.concat(filePath, "__extracted__");
        File toDirectory = new File(extractToDir);
        if (!localFileDir.exists()) {
            localFileDir.mkdirs();
        }

        ArrayList<String> inventory = new ArrayList<>();
        if (uploadedFile.getName().endsWith(".zip")) {
            inventory = unzipAndInventory(uploadedFile, toDirectory);
        }

        // clean up inventory - some auto-generated OS files can really play havoc
        // if they are not accounted for. For example: "__MACOSX/"

        inventory = cleanupInventory(inventory);

        String template = "";
        for (String inventoryFile : inventory) {
            if (inventoryFile.endsWith("template")) {
                template = inventoryFile;
            }
        }

        // load template
        JSONParser parser = new JSONParser();
        String convertFrom = "";
        try {
            FileReader templateFileReader = new FileReader(template);
            Object obj = parser.parse(templateFileReader);
            templateFileReader.close();
            JSONObject jsonObject = (JSONObject) obj;
            convertFrom = (String) jsonObject.get("convertFrom");
            System.out.println(convertFrom);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // process data files based on convertTo type
        if (convertFrom.equals("tuff")) {
            for (String inventoryFile : inventory) {
                if (!inventoryFile.endsWith("template") && !inventoryFile.endsWith("metadata")) {
                    TagUniversalFileFormat tuff = new TagUniversalFileFormat();
                    tuff.parse(inventoryFile);
                    String fullFileNameExt = FilenameUtils.getExtension(inventoryFile);
                    String ncfile = inventoryFile.replace(fullFileNameExt, "nc");
                    ncfile = FilenameUtils.concat(filePath, ncfile);
                    try {
                        String convertedFile = tuff.convert(ncfile);
                        convertedFiles.add(convertedFile);
                    } catch (InvalidRangeException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        // return zip file containing one or more converted files in body of the response
        String zipFileName = FilenameUtils.concat(filePath, "converted_files.zip");
        addAllToZip(zipFileName, "converted_files", convertedFiles);

        return "hi";
    }

}
