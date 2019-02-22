/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */
package edu.ucar.unidata.rosetta.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;

import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;
import edu.ucar.unidata.rosetta.service.ServerInfoBean;

/**
 * Utilities for handling the transaction logging.
 */
public class TransactionLogUtils {

    private static final Logger logger = Logger.getLogger(TransactionLogUtils.class);

    /**
     * Creates the transaction.log file using the provided unique ID.
     *
     * @param id   The unique ID associated with the transaction.
     * @throws RosettaFileException  If unable to create the transaction.log file.
     */
    public static void createLog(String id) throws RosettaFileException {

        String pathToLocalFileDir = FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id);

        // Location in which to write the transaction.log.
        File localFileDir = new File(pathToLocalFileDir);

        // make sure the directory has been created.
        IoUtils.createUserFilesSubDirectory(id);

        // Create the transaction.log file.
        File transactionLog = new File(FilenameUtils.concat(pathToLocalFileDir, "transaction.log"));

        try {
            if (!transactionLog.createNewFile()) {
                // Not a show-stopper, but log it.
               logger.info("Transaction log file already exists: " + pathToLocalFileDir);
            }
        } catch(IOException e) {
            throw new RosettaFileException("Error creating transaction log: " + e);
        }

        // Write initial message.
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        String sb = "ROSETTA TRANSACTION LOG" +
                "\n" +
                formatter.format(ZonedDateTime.now()) +
                "\n" +
                PropertyUtils.getHostName() +
                "\n" +
                ServerInfoBean.getVersion() +
                "\n";
        writeToLog(id, sb);
    }


    /**
     * Appends the provided message to the transaction.log file.
     *
     * @param id   The unique ID associated with the transaction.
     * @param message  The message to append to the log.
     * @throws RosettaFileException  If unable to append to the transaction.log file.
     */
    public static void writeToLog(String id, String message) throws RosettaFileException {

        String pathToTransactionLog= FilenameUtils.concat(FilenameUtils.concat(PropertyUtils.getUserFilesDir(), id), "transaction.log");
        File transactionLog = new File(pathToTransactionLog);

        // Throw an exception if the log file doesn't exist.
        if (!transactionLog.exists()) {
            throw new RosettaFileException(
                    "Unable to write to transaction log.  Transaction log file does not exist: " + pathToTransactionLog);
        }

        // Append provided message to the file.
        try (FileOutputStream outputStream = new FileOutputStream(transactionLog, true)) {
            outputStream.write(message.getBytes(Charset.forName("UTF-8")));
        } catch (IOException e) {
            throw new RosettaFileException("Unable write uploaded file to disk: " + e);
        }

    }
}
