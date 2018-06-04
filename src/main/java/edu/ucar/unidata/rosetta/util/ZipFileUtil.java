package edu.ucar.unidata.rosetta.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Utility class for zip file manipulation.
 *
 * @author sarms@ucar.edu
 * @author oxelson@ucar.edu
 */

public class ZipFileUtil {

    private static final Logger logger = Logger.getLogger(ZipFileUtil.class);

    private String name;

    public ZipFileUtil(String zipFileName) {
        this.setName(zipFileName.replace("file://", ""));
    }

    public String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
    }

    public void addAllToZip(List<String> filesToAdd) {
        addAllToZip("", filesToAdd.toArray(new String[0]));
    }

    public void addAllToZip(String root, List<String> filesToAdd) {
        addAllToZip(root, filesToAdd.toArray(new String[0]));
    }

    public void addAllToZip(String root, String[] filesToAdd) {
        FileInputStream inStream;
        byte[] buffer = new byte[1024];
        int bytesRead;

        // Create input and output streams
        try {

            ZipOutputStream outStream = new ZipOutputStream(
                    new FileOutputStream(this.getName()));

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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String readFileFromZip(String fileName) {
        String data = "";
        String line;
        try (ZipFile zf = new ZipFile(this.getName())) {
            // ZipEntry fileInZip = zf.getEntry(topLevelZipDir + "/" +
            // fileName);
            ZipEntry fileInZip = zf.getEntry(fileName);
            if (fileInZip != null) {
                InputStream inStream = zf.getInputStream(fileInZip);
                BufferedReader buffReader = new BufferedReader(
                        new InputStreamReader(inStream));
                while ((line = buffReader.readLine()) != null) {
                    data = data + line;
                }
                buffReader.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     *
     * Original code taken from http://www.avajava.com/tutorials/lessons/how-do-i-unzip-the-contents-of-a-zip-file.html
     *
     * @param zipFileName
     * @param uncompressed_dir
     * @return
     */
    public static List<String> unzipAndInventory(String zipFileName, String uncompressed_dir) {

        String zipFilePath = FilenameUtils.concat(uncompressed_dir, zipFileName);

        logger.info("uncomepressed dir: "  + uncompressed_dir);
        String template = "";
        List<String> inventory = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(new File(zipFilePath));
            logger.info("Files in " + zipFile.getName() + ":");
            Enumeration<?> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) e.nextElement();

                String name = zipEntry.getName();
                logger.info(" " + name);

                Path filePath = Paths.get(uncompressed_dir, name);
                File file = filePath.toFile();
                logger.info("file: " + file.getName());
                
                if (name.endsWith("/")) {
                    logger.info("name ends with /");
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                logger.info("parent file: " + parent.getName());
                if (parent != null) {
                    logger.info("parent not null");
                    parent.mkdirs();
                }

                InputStream inputStream = zipFile.getInputStream(zipEntry);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = inputStream.read(bytes)) >= 0) {
                    logger.info("writing to outputstream");
                    fileOutputStream.write(bytes, 0, length);
                }
                inputStream.close();
                fileOutputStream.close();
                logger.info("added to inventory: " + file.getAbsolutePath());
                inventory.add(file.getAbsolutePath());
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inventory;
    }
}
