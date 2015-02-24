package edu.ucar.unidata.rosetta.publishers;

import org.apache.log4j.Logger;
import org.ramadda.repository.client.RepositoryClient;

/**
 * Created by lesserwhirls on 2/14/14.
 */
public class UnidataRamadda implements Publisher {


    private String path = "/repository";
    private int port = 443;

    protected static Logger logger = Logger.getLogger(AcadisGateway.class);
    String server;
    String userId;
    String passwd;
    String fileToUpload;
    String parent;
    String entryName;
    String entryDescription;
    String msg;

    public String getMessage() {
        return msg;
    }

    public UnidataRamadda(String server, String userId, String passwd, String parent,
                          String filePath, String entryName, String entryDescription) {
        this.entryName = entryName;
        this.entryDescription = entryDescription;
        this.server = server;
        this.userId = userId;
        this.passwd = passwd;
        this.parent = parent;
        fileToUpload = filePath;
    }

    public boolean publish() {

        boolean successful = false;
        try {
            RepositoryClient client = new RepositoryClient(server, port, path, userId, passwd);

            msg = client.uploadFile(entryName, entryDescription, parent,
                    this.fileToUpload);
            if (client.isValidSession(true, null)) {
                successful = true;
            } else {
                successful = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return successful;
    }
}
