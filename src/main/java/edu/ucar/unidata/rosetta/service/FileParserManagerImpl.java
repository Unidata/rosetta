package edu.ucar.unidata.rosetta.service;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import ucar.unidata.util.StringUtil2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for parsing file data.
 */
public class FileParserManagerImpl implements FileParserManager {

    protected static Logger logger = Logger
            .getLogger(FileParserManagerImpl.class);

    public List<List<String>> parsedFileData = new ArrayList<List<String>>();

    /**
     * Returns each line of the file data parsed by delimiter into a
     * List<String> which is then stored into List<List<String>>.
     * 
     * @return The parsed file data.
     */
    public List<List<String>> getParsedFileData() {
        return parsedFileData;
    }

    /**
     * Sets each line of the file data parsed by delimiter into a List<String>
     * which is then stored into List<List<String>>.
     * 
     * @param parsedFileData
     *            The parsed file data.
     */
    public void setParsedFileData(List<List<String>> parsedFileData) {
        this.parsedFileData = parsedFileData;
    }

    /**
     * A simple method that reads each line of a file, appends a new line
     * character and appends to a StringBuffer, and returns the StringBuffer
     * string value. This method is used to parse the file data when no header
     * lines have been specified. TODO: refactor to return JSON
     * 
     * @param filePath
     *            The path to the file on disk.
     * @return A String of the file data parsed by line.
     */
    public String parseByLine(String filePath) {
        StringBuffer stringBuffer = new StringBuffer();
        String currentLine;
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath))) {
            while ((currentLine = reader.readLine()) != null) {
                stringBuffer.append(currentLine + "\n");
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        return stringBuffer.toString();
    }

    /**
     * This method reads each line of a file and if more than one delimiter has
     * been specified by the user, it normalizes the delimiters in the line to
     * match the selectedDelimiter (for ease of parsing purposes on the
     * client-side), or simply appends a new line character and appends to a
     * StringBuffer, and returns the StringBuffer string value. This method is
     * used to parse the file data when both the header lines and delimiter(s)
     * have been specified by the user. TODO: refactor to return JSON
     * 
     * @param filePath
     *            The path to the file on disk.
     * @param selectedDelimiter
     *            The delimiter selected to which any other delimiters will be
     *            normalized.
     * @param delimiterList
     *            The List<String> of delimiters specified by the user.
     * @param headerLineList
     *            The List<String> of header lines specified by the user.
     * @return A String of the file data parsed by the delimiter(s).
     */
    public String normalizeDelimiters(String filePath,
            String selectedDelimiter, List<String> delimiterList,
            List<String> headerLineList) {
        List<List<String>> parsedData = new ArrayList<List<String>>();
        StringBuffer stringBuffer = new StringBuffer();
        int lineCount = 0;
        String currentLine;
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath))) {
            while ((currentLine = reader.readLine()) != null) {
                // If a header line we don't have to deal with the delimiter
                if (headerLineList.contains(new Integer(lineCount).toString())) {
                    stringBuffer.append(currentLine + "\n");
                } else {
                    // Parse line data based on delimiter count
                    if (delimiterList.size() != 1) { // more than one delimiter
                        String[] delimiters = (String[]) delimiterList
                                .toArray(new String[delimiterList.size()]);
                        // "Normalize" delimiters for parsing purposes
                        for (int i = 1; i < delimiters.length; i++) {
                            // Change all delimiters the selected delimiter
                            String updatedLineData = currentLine.replaceAll(
                                    StringEscapeUtils
                                            .escapeHtml4(delimiters[i]),
                                    StringEscapeUtils
                                            .escapeHtml4(selectedDelimiter));
                            stringBuffer.append(updatedLineData + "\n");
                            String[] lineComponents = updatedLineData
                                    .split(selectedDelimiter);
                            List<String> list = new ArrayList<String>(
                                    Arrays.asList(lineComponents));
                            parsedData.add(list);
                        }
                    } else if (selectedDelimiter == " ") {
                        // This will use ANY white space, variable number
                        // spaces, tabs, etc. as
                        // the delimiter...not that the delimiter is " ", and is
                        // defined
                        // in the convertDelimiters of
                        // FileParserManagerimpl.java.
                        //
                        // This special case also needs to be handled by
                        // variableSpecification.js
                        // in the gridForVariableSpecification function.
                        stringBuffer.append(currentLine + "\n");
                        String[] tokens = StringUtil2.splitString(currentLine);
                        List<String> valList = Arrays.asList(tokens);
                        parsedData.add(valList);

                    } else { // only one delimiter
                        stringBuffer.append(currentLine + "\n");
                        String[] lineComponents = currentLine
                                .split(selectedDelimiter);
                        List<String> list = new ArrayList<String>(
                                Arrays.asList(lineComponents));
                        parsedData.add(list);
                    }
                }
                lineCount++;
            }
            setParsedFileData(parsedData);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return null;
        }
        return stringBuffer.toString();
    }
}
