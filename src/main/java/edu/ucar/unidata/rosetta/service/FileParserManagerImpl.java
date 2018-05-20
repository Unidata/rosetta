package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.util.JsonUtil;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ucar.unidata.util.StringUtil2;

/**
 * Service for parsing file data.
 */
public class FileParserManagerImpl implements FileParserManager {

    protected static Logger logger = Logger.getLogger(FileParserManagerImpl.class);


    public List<List<String>> parsedFileData = new ArrayList<>();
    public List<String> header = new ArrayList<>();

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
     * @param parsedFileData The parsed file data.
     */
    public void setParsedFileData(List<List<String>> parsedFileData) {
        this.parsedFileData = parsedFileData;
    }

    public List<String> getHeader() {
        return header;
    }

    public void setHeader(List<String> header) {
        this.header = header;
    }

    /**
     * A simple method that reads each line of a file, appends a new line
     * character & adds to a List. The list is then turned into a JSON string.
     *
     * @param filePath The path to the file on disk.
     * @return A JSON String of the file data parsed by line.
     * @throws IOException For any file I/O or JSON conversions problems.
     */
    public String parseByLine(String filePath) throws IOException {
        List<String> fileContents = new ArrayList<>();
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (StringUtils.isNotBlank(currentLine)) {
                    fileContents.add(StringEscapeUtils.escapeHtml4(currentLine));
                }
            }
        }
        return JsonUtil.mapObjectToJSON(fileContents);
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
     * @param filePath   The path to the file on disk.
     * @param delimiter  The used delimiter to parse the data.
     * @param headerLineList  The List<String> of header lines specified by the user.
     * @return A String of the file data parsed by the delimiter(s).
     */
    /*
    public String normalizeDelimiters(String filePath, String delimiter, List<String> headerLineList) throws IOException {
        List<List<String>> parsedData = new ArrayList<>();
        List<String> headerData = new ArrayList<>();
        StringBuffer stringBuffer = new StringBuffer();
        int lineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (StringUtils.isNotBlank(currentLine)) {
                    // If a header line we don't have to deal with the delimiter
                    if (headerLineList.contains(String.valueOf(lineCount))) {
                        stringBuffer.append(currentLine + "\n");
                        headerData.add(currentLine);
                    } else {
                        String symbol =  delimiterDao.lookupByName(delimiter).getSymbol();
                        // Parse line data based on delimiter.
                        if (delimiter.equals("Whitespace")) {
                            // This will use ANY white space, variable number spaces, tabs, etc. as
                            // the delimiter...not that the delimiter is " ", and is defined
                            // in the convertDelimiters of FileParserManagerimpl.java.
                            //
                            // This special case also needs to be handled by variableSpecification.js
                            // in the gridForVariableSpecification function.
                            stringBuffer.append(currentLine + "\n");
                            String[] tokens = StringUtil2.splitString(currentLine);
                            List<String> valList = Arrays.asList(tokens);
                            parsedData.add(valList);

                        } else { // only one delimiter
                            stringBuffer.append(currentLine + "\n");
                            String[] lineComponents = currentLine.split(symbol);
                            List<String> list = new ArrayList<>(Arrays.asList(lineComponents));
                            parsedData.add(list);
                        }
                    }
                    lineCount++;
                }
            }
            setParsedFileData(parsedData);
            setHeader(headerData);
        }
        return stringBuffer.toString();
    }
*/
    /**
     * A simple method that reads each line of a file, and looks for blank lines.
     * Blank line = empty, only whitespace, or null (as per StringUtils).
     *
     * @param file The path to the file on disk.
     * @return The number of blank lines in the file.
     */
    public int getBlankLines(File file) {
        String currentLine;
        int blankLineCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while ((currentLine = reader.readLine()) != null) {
                if (StringUtils.isBlank(currentLine)) {
                    blankLineCount++;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return blankLineCount;
    }


}
