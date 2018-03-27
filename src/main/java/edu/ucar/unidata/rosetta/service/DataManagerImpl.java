package edu.ucar.unidata.rosetta.service;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class DataManagerImpl implements DataManager {

    protected static Logger logger = Logger.getLogger(DataManagerImpl.class);

    private static final String DATA_FILE = "data.txt";

    @Override
    public Map<String,Object> getData() {

        Map<String,Object> data = new HashMap<>();

        // Get the file data.
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            // Get the expressions from the file.
            File file = new File(DATA_FILE);

            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Validate expressions to make sure they are formed correctly (see method for details).
                String[] tokens = line.split("=>");
                data.put(tokens[0], tokens[1]);
            }
        } catch (FileNotFoundException e) {
            // Thrown if file not found during FileReader creation.
            System.out.println("Opps! A fatal error occurred. Unable to create FileReader for " + DATA_FILE + ":");
            printExceptionStackTrace(e);
        } catch (IOException e) {
            // Thrown by BufferedReader if unable to read file line.
            System.out.println("Opps! A fatal error occurred. Unable to read file " + DATA_FILE + ":");
            printExceptionStackTrace(e);
        } finally {
            // Close the FileReader & BufferedReader.
            try {
                assert fileReader != null;
                fileReader.close();
                assert bufferedReader != null;
                bufferedReader.close();
            } catch (IOException e) {
                System.out.println("Opps! A fatal error occurred. Unable to close FileReader: ");
                printExceptionStackTrace(e);
            }
        }
        return data;
    }

    private void setData(String data) {

        // Declare a FileWriter.
        FileWriter fileWriter = null;
        try {
            // Create the File object for the data file.
            File file = new File(DATA_FILE);

            // Finish instantiating the FileWriter with the data file.
            fileWriter = new FileWriter(file);

            // Write the data to the file.
            fileWriter.write(data);
        } catch (NullPointerException e) {
            System.out.println("Opps! A fatal error occurred. Unable to create File object for " + DATA_FILE + ":");
            printExceptionStackTrace(e);
        } catch (IOException e) {
            System.out.println("Opps! A fatal error occurred. Unable to write to " + DATA_FILE + ":");
            printExceptionStackTrace(e);
        } finally {
            // Close the FileWriter.
            try {
                assert fileWriter != null;
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Opps! A fatal error occurred. Unable to close FileWriter: ");
                printExceptionStackTrace(e);
            }
        }

    }

    public String getCFData() {
        String cfType = null;
        Map<String,Object> fileData = getData();
        Iterator it = fileData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getKey().equals("cfType")) {
                cfType = (String) pair.getValue();
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        return cfType;
    }

    public void setCFData(String cfType) {

        Map<String,Object> fileData = getData();
        StringBuilder stringBuilder = new StringBuilder(fileData.size());
        Iterator it = fileData.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if (pair.getKey().equals("cfType")) {
                stringBuilder.append("" + "=>" + cfType);
            } else {
                stringBuilder.append(pair.getKey() + "=>" + pair.getValue());
            }
            it.remove(); // avoids a ConcurrentModificationException
        }
        setData(stringBuilder.toString());
    }





    /**
     * Prints the full stack trace of an exception.
     *
     * @param e The exception to print.
     */
    private void printExceptionStackTrace(Exception e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        System.out.println(errors.toString());
    }

}
