/*
 * Copyright (c) 2012-2017 University Corporation for Atmospheric Research/Unidata
 */

package edu.ucar.unidata.rosetta.converters;

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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import edu.ucar.unidata.rosetta.util.TestDir;

import static org.junit.Assert.assertEquals;

/**
 * Test the conversion of an eTUFF file
 */
public class TestTagUniversalFileFormatBatchProcess {

    private static String etuffDir = String.join(File.separator, "conversions", "TagUniversalFileFormat");
    private static String etuffFileTld = TestDir.rosettaLocalTestDataDir + etuffDir;
    private static String uploadFile = String.join(File.separator, etuffFileTld, "test_simple_api.zip");

    String postUrl = "http://localhost:8888/rosetta/batchProcess";

    @Test
    @Ignore
    public void testBatchProcessEtuff() {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            File fileToUpload = new File(uploadFile);
            FileBody fileBody = new FileBody(fileToUpload);

            HttpPost post = new HttpPost(postUrl);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("file", fileBody);
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
}