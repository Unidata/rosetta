/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.batch;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataRetrievalFailureException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import edu.ucar.unidata.rosetta.converters.known.etuff.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.converters.custom.dsg.NetcdfFileManager;
import edu.ucar.unidata.rosetta.domain.Template;
import edu.ucar.unidata.rosetta.domain.batch.BatchProcessZip;
import edu.ucar.unidata.rosetta.domain.resources.Delimiter;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.repository.resources.DelimiterResourceDao;
import edu.ucar.unidata.rosetta.util.PathUtils;
import edu.ucar.unidata.rosetta.util.PropertyUtils;
import edu.ucar.unidata.rosetta.util.TemplateFactory;
import edu.ucar.unidata.rosetta.util.TemplateUtils;
import ucar.ma2.InvalidRangeException;

public class BatchFileManagerImpl implements BatchFileManager {

    private static final Logger logger = Logger.getLogger(BatchFileManagerImpl.class);

    private DelimiterResourceDao delimiterResourceDao;

    private static final Map<String, String> delimiterMap;
    static {
        delimiterMap = new HashMap<String, String>();
        delimiterMap.put("Tab", "\t");
        delimiterMap.put("Comma", ",");
        delimiterMap.put("Whitespace", "\\s+");
        delimiterMap.put("Colon", ":");
        delimiterMap.put("Semicolon", ";");
        delimiterMap.put("Single Quote", "\'");
        delimiterMap.put("Double Quote", "\"");
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

    /**
     * processes the uploaded data file and converts all data files within to netCDF.
     *
     * @param batchZipFile The batchZipFile object representing the uploaded zip file to process.
     * @return The path to the zip file containing all converted files.
     * @throws IOException If unable to access template file.
     * @throws RosettaDataException  If unable to parse data file with given delimiter.
     */
    public String batchProcess(BatchProcessZip batchZipFile) throws IOException, RosettaDataException {
        String uniqueId = batchZipFile.getId();
        String userFilesDir = PropertyUtils.getUserFilesDir();

        String filePath = FilenameUtils.concat(userFilesDir, uniqueId);
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

        String mainTemplateFile = "";
        for (String inventoryFile : inventory) {
            if (inventoryFile.endsWith(File.separator + "rosetta.template")) {
                mainTemplateFile = inventoryFile;
            }
        }

        // load maimn template
        Template baseTemplate = TemplateFactory.makeTemplateFromJsonFile(Paths.get(mainTemplateFile));
        String format = baseTemplate.getFormat();

        // todo log errors and return that in the zip file, if errors occur

        // process data files based on convertTo type
        if (format.equals("custom")) {
            for (String file : inventory) {
                if (!file.endsWith("template") && !file.endsWith("metadata")) {
                    // found a data file.
                    Path dataFile = Paths.get(file);

                    // make a copy of the base template so that we can make modifications
                    Template template = TemplateUtils.copy(baseTemplate);

                    // look for a template file specific to the data file
                    Path potentialTemplateFile = PathUtils.replaceExtension(dataFile, ".template");
                    if (Files.exists(potentialTemplateFile)) {
                        Template fileTemplate = TemplateFactory.makeTemplateFromJsonFile(potentialTemplateFile);
                        template.update(fileTemplate);
                    }

                    // look for a metadata file specific to the data file
                    Path potentialMetadataFile = PathUtils.replaceExtension(dataFile, ".metadata");
                    if (Files.exists(potentialMetadataFile)) {
                        Template fileTemplate = TemplateFactory.makeTemplateFromMetadataFile(potentialMetadataFile);
                        template.update(fileTemplate);
                    }

                    // now find the proper converter
                    NetcdfFileManager dsgWriter = null;
                    for (NetcdfFileManager potentialDsgWriter : NetcdfFileManager.getConverters()) {
                        if (potentialDsgWriter.isMine(baseTemplate.getCfType())) {
                            dsgWriter = potentialDsgWriter;
                            break;
                        }
                    }

                    // Get the delimiter symbol.
                    String delimiter;
                    try {
                        // Try using the delimiter (standard) passed from the db.
                        if (delimiterMap.containsKey(template.getDelimiter())) {
                            delimiter = delimiterMap.get(template.getDelimiter());
                        } else {
                            delimiter = template.getDelimiter();
                        }
                        //Delimiter delimiterName = delimiterResourceDao.lookupDelimiterByName(template.getDelimiter());
                        //delimiter = delimiterName.getCharacterSymbol();
                    } catch (DataRetrievalFailureException e) {
                        // Delimiter is not standard. Try parsing using the delimiter provided by the user.
                        delimiter = template.getDelimiter();
                    }

                    String netcdfFile = dsgWriter.createNetcdfFile(dataFile, template, delimiter);
                    convertedFiles.add(netcdfFile);
                }
            }
        } else if (format.equals("eTuff")) {
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


        return zipFileName;
    }


    /**
     * Sets the data access object (DAO) for the Delimiter object.
     *
     * @param delimiterResourceDao The service DAO representing a Delimiter object.
     */
    public void setDelimiterResourceDao(DelimiterResourceDao delimiterResourceDao) {
        this.delimiterResourceDao = delimiterResourceDao;
    }

}
