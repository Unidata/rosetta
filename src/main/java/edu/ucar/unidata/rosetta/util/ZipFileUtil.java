package edu.ucar.unidata.rosetta.util;

import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
Utility class for zip file manipulation
 */

public class ZipFileUtil {

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

    public static ArrayList<String> unzipAndInventory(File inputZipFile, File uncompressed_dir) {
        // http://www.avajava.com/tutorials/lessons/how-do-i-unzip-the-contents-of-a-zip-file.html
        // No need to reinvent the wheel. However, there isn't any license on this, or any way
        // to really give attribution here.
        String template = "";
        ArrayList<String> inventory = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(inputZipFile);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                String name = zipEntry.getName();

                Path filePath = Paths.get(uncompressed_dir.toString(), name.toString());
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

}
