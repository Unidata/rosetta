package edu.ucar.unidata.rosetta.domain;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Object representing a remote file to be uploaded to Rosetta.
 *
 * An arbitrary entity containing the description of a remote file stored on
 * the ACADIS gateway.
 */
public class RemoteAcadisUploadedFile {

    private String fileName = null;
    private String uniqueId = null;
    private String remoteAccessUrl = null;
    private String templateName = null;
    private String templateAccessUrl = null;

    /*
    * Returns the remote access URL of the datafile on the
    * ACADIS server to be transformed by Rosetta.
    *
    * @return  The remote access url.
    */
    public String getRemoteAccessUrl() {
        return remoteAccessUrl;
    }


    /*
    * Set the remote access URL of the datafile on the
    * ACADIS server to be transformed by Rosetta.
    *
    * @param remoteAccessUrl  The remote access URL.
    */
    public void setRemoteAccessUrl(String remoteAccessUrl) {
        this.remoteAccessUrl = remoteAccessUrl;
    }

    /*
     * Returns the name of the uploaded file.
     * 
     * @return  The uploaded file name. 
     */
    public String getFileName() {
        return fileName;
    }

    /*
     * Sets the file name of the uploaded file. 
     * 
     * @param fileName  The uploaded file name. 
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /*
     * Returns the unique ID of the uploaded file.
     *
     * @return  The unique ID.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /*
     * Sets the unique ID of the uploaded file.
     *
     * @param  The unique ID.
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getTemplateAccessUrl() {
        return templateAccessUrl;
    }

    public void setTemplateAccessUrl(String templateAccessUrl) {
        this.templateAccessUrl = templateAccessUrl;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Boolean retrieveFile(String outputFilePath) {
        Boolean success = false;
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient httpClient = httpClientBuilder.build();

        Map<String,String> fileNames = new HashMap<>();
        fileNames.put(this.getFileName(), this.getRemoteAccessUrl());
        if (this.getTemplateName() != null) {
            fileNames.put(this.getTemplateName(), this.getTemplateAccessUrl());
        }
        for (Map.Entry<String, String> entry : fileNames.entrySet()) {
            String fileToGet = entry.getKey();
            String accessUrl = entry.getValue();
            String fullOutputFilePath = FilenameUtils.concat(outputFilePath, fileToGet);
            try {
                HttpGet httpGet = new HttpGet(accessUrl);
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                BufferedInputStream bis = new BufferedInputStream(entity.getContent());
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fullOutputFilePath)));
                int inByte;
                while ((inByte = bis.read()) != -1) {
                    bos.write(inByte);
                }
                bis.close();
                bos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String fileToCheck = fullOutputFilePath;
            if (new File(fileToCheck).exists()) {
                success = true;
            }
        }
        return success;
    }

}
