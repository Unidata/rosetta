package edu.ucar.unidata.rosetta.service;

import org.ramadda.repository.client.RepositoryClient;

public class RamaddaPublisher {

    private static ResourceManagerImpl resourceManager = new ResourceManagerImpl();

    private void setResources() {
        resourceManager.loadResources();
    }

    public static void main(String args[]) {
        RamaddaPublisher rp = new RamaddaPublisher();
        rp.setResources();
        String userId = "";
        String passwd = "";

        String server = "motherlode.ucar.edu";
        String parent = "c8c04a3c-d32c-42b8-8c3c-5c174aaa0991";
        String entryName = "myTest";
        String entryDescription = "just a test";
        String filePath = "";
        try {
            RepositoryClient client = new RepositoryClient(server, 80, "/repository", userId, passwd);
            client.uploadFile(entryName, entryDescription, parent,
                    filePath);
            String[] msg = { "" };
            if (client.isValidSession(true, msg)) {
                System.err.println("Valid session");
            } else {
                System.err.println("Invalid session:" + msg[0]);
            }
        } catch (Exception e)  {
            e.printStackTrace();
        }
    }
}
