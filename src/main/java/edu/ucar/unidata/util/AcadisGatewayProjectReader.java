package edu.ucar.unidata.util;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lesserwhirls on 2/14/14.
 */
public class AcadisGatewayProjectReader {

    private String server = "www.aoncadis.org";
    private int port = 443;
    private String scheme = "https";
    protected static Logger logger = Logger.getLogger(AcadisGatewayProjectReader.class);


    private HttpHost gatewayHost;
    private HttpClientBuilder gatewayClientBuilder;
    private String userAgent;
    private URI uri;
    private HttpResponse latestResponse;
    private String wgetText;
    private Map<String, String> inventory;

    private HttpHost makeHost() {
        // create client for given host
        HttpHost httpHost = new HttpHost(
                server, port, scheme);

        return httpHost;
    }

    private HttpClientBuilder makeHttpClientBuilder() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        return httpClientBuilder;
    }

    private CloseableHttpClient makeHttpClient() {
        gatewayClientBuilder = makeHttpClientBuilder();

        // finally, build the http client
        CloseableHttpClient httpclient =
                gatewayClientBuilder.build();

        return httpclient;
    }

    private URI makeWgetUri(String dsShortName) throws URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(gatewayHost.getSchemeName())
                .setHost(gatewayHost.getHostName())
                .setPath("/dataset/" + dsShortName + "/file.wget")
                .build();
        return uri;
    }

    private String makeUserAgent() {
        String rosettaVersion = "0.2-SNAPSHOT";
        String userAgent = "Unidata/Rosetta_"+ rosettaVersion;

        return userAgent;
    }


    private CloseableHttpClient gatewayGet(URI uri) throws IOException {

        CloseableHttpClient httpClient = makeHttpClient();
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("User-Agent", userAgent);

        latestResponse = httpClient.execute(gatewayHost,
                httpGet);

        return httpClient;

    }


    private void createInventory() {
        inventory = new HashMap<>();
        String name, downloadUrl;
        for (String line: wgetText.split("\n")) {
            if (line.startsWith("wget")) {
                String[] lineArray = line.split(" ");
                int numParts = lineArray.length;
                name = lineArray[numParts - 2];
                name = name.replaceAll("'", "");
                downloadUrl = lineArray[numParts - 1];
                downloadUrl = downloadUrl.replaceAll("'", "");
                inventory.put(name, downloadUrl);
            }
        }
    }

    public Map<String, String> getInventory() {
        return inventory;
    }

    public AcadisGatewayProjectReader(String dsShortName) {
        try {
            init(dsShortName);
        } catch (URISyntaxException e) {
            logger.error("Could not create an AcadisGatewayProjectReader for the dataset with a short name of " + dsShortName);
            logger.error(e.getMessage());
            logger.error(e.getStackTrace());
            e.printStackTrace();
        }
    }

    private void init(String dsShortName) throws URISyntaxException {

        userAgent = makeUserAgent();

        gatewayHost = makeHost();

        uri = makeWgetUri(dsShortName);

    }



    public Boolean read() {
        boolean successful = false;
        try {
            // get request for wget script
            CloseableHttpClient httpClient = gatewayGet(uri);

            int status = latestResponse.getStatusLine().getStatusCode();
            if (status == 200) {
                wgetText = EntityUtils.toString(latestResponse.getEntity());
                httpClient.close();
                createInventory();
                successful = true;

            }
        } catch (Exception exc) {
            logger.debug(exc.getMessage());
        } finally {

            return successful;
        }
    }


    public static void main(String [] args) throws IOException, URISyntaxException {

        String dsShortName = "greenland";
        AcadisGatewayProjectReader projectReader = new AcadisGatewayProjectReader(dsShortName);
        projectReader.read();
        Map<String, String> inventory = projectReader.getInventory();
        for (String name : inventory.keySet()) {
            String downloadUrl = inventory.get(name);
            System.out.println("name: " + name + " access: " + downloadUrl);
        }
    }


}
