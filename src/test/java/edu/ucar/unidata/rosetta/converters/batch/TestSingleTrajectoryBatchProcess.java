/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.converters.batch;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ucar.unidata.rosetta.util.test.category.NeedsLocalServer;
import edu.ucar.unidata.rosetta.util.test.util.TestUtils;

import static org.junit.Assert.assertEquals;

/**
 * Test the conversion of an eTUFF file
 */
@Category(NeedsLocalServer.class)
public class TestSingleTrajectoryBatchProcess {

    private static Path testDir = TestUtils.getTestDataDirPath();
    private static Path partialDataDir = Paths.get("singleTrajectory", "waveglider");

    private Path uploadFile;


    private static String topLevelDir = "/trajectories";

    String batchProcessUrl = TestUtils.getTestServerUrl() + "/batchProcess";

    @Before
    public void makeUploadZip() throws IOException {

        Path dataDir = testDir.resolve(partialDataDir);
        uploadFile = dataDir.resolve("test_single_trajectory_api.zip");

        List<String> filesToAdd = Arrays.asList(
                "ASL22.txt",
                "ASL22.metadata",
                "ASL42.txt",
                "ASL42.metadata",
                "rosetta.template");

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        // locate file system by using the syntax
        // defined in java.net.JarURLConnection
        String uploadFileUri = uploadFile.toUri().toString();
        URI uri = URI.create("jar:" + uploadFileUri);

        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
            for (String file : filesToAdd) {
                Path externalTxtFile = dataDir.resolve(file);
                Path zipTopLevelDir = zipfs.getPath(topLevelDir);
                Files.createDirectories(zipTopLevelDir);
                Path pathInZipfile = zipTopLevelDir.resolve(file);
                Files.copy(externalTxtFile, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @Test
    public void testBatchProcessSingleTrajectory() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            File fileToUpload = uploadFile.toFile();
            FileBody fileBody = new FileBody(fileToUpload);

            HttpPost post = new HttpPost(batchProcessUrl);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("batchZipFile", fileBody);
            HttpEntity entity = builder.build();

            post.setEntity(entity);
            HttpResponse response = httpclient.execute(post);
            StatusLine statusLine = response.getStatusLine();
            assert statusLine.getStatusCode() == 200;

            HttpEntity responseEntity = response.getEntity();
            long contentLength = responseEntity.getContentLength();

            Header[] clHeader = response.getHeaders("Content-Length");
            assert clHeader.length == 1;
            assertEquals(Long.valueOf(clHeader[0].getValue()), Long.valueOf(contentLength));

            Header[] ctHeader = response.getHeaders("Content-Type");
            assert ctHeader.length == 1;
            assertEquals(ctHeader[0].getValue(), "application/zip");

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void cleanup() throws IOException {
        if (Files.exists(uploadFile)) {
            Files.delete(uploadFile);
        }
    }
}