package edu.ucar.unidata.rosetta.publishers;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class is designed to create a publisher for the ACADIS Gateway
 */
public class AcadisGateway implements Publisher {

    private String path = "/api/dataset";
    private int port = 443;
    private String scheme = "https";
    protected static Logger logger = Logger.getLogger(AcadisGateway.class);

    private String server;
    private HttpHost gatewayHost;
    private HttpClientBuilder gatewayClientBuilder;
    private String userAgent;
    private File fileToPublish;
    private CredentialsProvider gatewayCredentialsProvider;
    private URI uri;
    private HttpResponse latestResponse;
    private String gatewayDownloadUrl;
    private String gatewayProjectUrl;
    private String parent;


    private HttpHost makeHost() {
        // create client for given host
        HttpHost httpHost = new HttpHost(
                server, port, scheme);

        return httpHost;
    }

    private HttpClientBuilder makeHttpClientBuilder(CredentialsProvider credentialsProvider) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder = httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        return httpClientBuilder;
    }

    private CredentialsProvider makeCredentialsProvider(String userName, String passwd) {
        // add authentication credentials
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, passwd);
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, creds);
        return credentialsProvider;
    }

    private CloseableHttpClient makeHttpClient(CredentialsProvider credentialsProvider) {
        gatewayClientBuilder = makeHttpClientBuilder(credentialsProvider);

        // finally, build the http client
        CloseableHttpClient httpclient =
                gatewayClientBuilder.build();

        return httpclient;
    }

    private URI makeUri(String putPath) throws URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme(gatewayHost.getSchemeName())
                .setHost(gatewayHost.getHostName())
                .setPath(putPath)
                .build();
        return uri;
    }

    private String makeUserAgent() {
        String rosettaVersion = "0.2-SNAPSHOT";
        String userAgent = "Unidata/Rosetta_"+ rosettaVersion;

        return userAgent;
    }

    private FileEntity makeFileEntity(File fileToUpload) {

        FileEntity entity = new FileEntity(fileToUpload, ContentType.DEFAULT_BINARY);

        return entity;
    }

    private HttpResponse gatewayPut(URI uri) throws IOException {

        CloseableHttpClient httpClient = makeHttpClient(gatewayCredentialsProvider);
        HttpPut httpPut = new HttpPut(uri);
        httpPut.setHeader("User-Agent", userAgent);

        httpPut.setEntity(makeFileEntity(fileToPublish));

        // force digest auth here on execute?
        HttpResponse response = httpClient.execute(gatewayHost,
                httpPut);
        httpClient.close();

        return response;

    }

    private HttpResponse gatewayDelete(URI uri) throws IOException {
        CloseableHttpClient httpClient = makeHttpClient(gatewayCredentialsProvider);
        HttpDelete httpDel = new HttpDelete(uri);
        httpDel.setHeader("User-Agent", userAgent);
        HttpResponse response = httpClient.execute(gatewayHost,
                httpDel);
        httpClient.close();

        return response;
    }

    public AcadisGateway(String server, String userId, String passwd, String shortName, String filePath) {
        try {
            init(server, userId, passwd, shortName, filePath);
        } catch (URISyntaxException e) {
            logger.error("Could not create an AcadisGateway Publisher for the project with a short name of " + shortName);
            logger.error(e.getMessage());
            logger.error(e.getStackTrace());
            e.printStackTrace();
        }
    }

    private void init(String server, String userId, String passwd, String parent, String filePath) throws URISyntaxException {

        this.server = server;
        fileToPublish = new File(filePath);
        String ncFileName = fileToPublish.getName();
        //String fullPutUrl = server + path + parent + "/files/" + ncFileName;
        String putPath =  path + "/" + parent + "/file/" + ncFileName;

        userAgent = makeUserAgent();

        gatewayHost = makeHost();

        gatewayCredentialsProvider = makeCredentialsProvider(userId, passwd);

        uri = makeUri(putPath);

    }

    private Boolean retryPublish() throws IOException {
        boolean successful = false;
        latestResponse = gatewayDelete(uri);
        int status = latestResponse.getStatusLine().getStatusCode();
        if (status == 200) {
            latestResponse = gatewayPut(uri);
            status = latestResponse.getStatusLine().getStatusCode();
            if (status == 201) {
                successful = true;
            }
        }
        return successful;
    }

    public HttpResponse getLatestResponse() {
        return latestResponse;
    }

    private void setGatewayDownloadUrl() {
        Header[] locationHeader = latestResponse.getHeaders("Location");
        if (locationHeader.length > 0) {
            gatewayDownloadUrl = locationHeader[0].getValue();
        }
    }

    private void setGatewayProjectUrl(String parent) {
        //gatewayProjectUrl = "https://cadis.prototype.ucar.edu/dataset/" +  parent + "html";
        gatewayProjectUrl = "https://www.aoncadis.org/dataset/" +  parent + "html";
    }

    public String getGatewayProjectUrl() {
        return gatewayProjectUrl;
    }


    public String getGatewayDownloadUrl() {
        return gatewayDownloadUrl;
    }

    public boolean publish() {
        boolean successful = false;
        try {
            // put request using full Put Url
            latestResponse = gatewayPut(uri);
            int status = latestResponse.getStatusLine().getStatusCode();
            if (status == 201) {
                successful = true;
            } else if (status == 400) {
                successful = false;
                // check if file aleady exists - if so, we need to delete it first and then try again
                Header[] validationError = latestResponse.getHeaders("Validation Error(s)");
                if (validationError.length > 0) {
                    boolean fileExists = validationError[0].getValue().toLowerCase().contains("fileexists");
                    if (fileExists) {
                        // retry -> delete first, then republish
                        successful = retryPublish();
                    }
                }
            }
        } catch (Exception exc) {
            logger.debug(exc.getMessage());
        } finally {
            if (successful) {
                setGatewayDownloadUrl();
                setGatewayProjectUrl(parent);
            }
            return successful;
        }
    }
}
