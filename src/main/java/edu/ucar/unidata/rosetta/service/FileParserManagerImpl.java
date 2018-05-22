package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.converters.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.util.JsonUtil;
import ucar.unidata.util.StringUtil2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

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

    public List<String> getHeaderLinesFromFile(String filePath, List<String> headerLineList) throws IOException {
        List<String> headerData = new ArrayList<>();
        int lineCount = 0;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (StringUtils.isNotBlank(currentLine)) {
                    // Get the header lines.
                    if (headerLineList.contains(String.valueOf(lineCount))) {
                        headerData.add(currentLine);
                    }
                }
                lineCount++;
            }
        }
        return headerData;
    }


    public List<List<String>> parseByDelimiter(String filePath, List<String> headerLineList, String delimiter) throws IOException {
        List<List<String>> parsedData = new ArrayList<>();
        int lineCount = 0;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = bufferedReader.readLine()) != null) {
                if (StringUtils.isNotBlank(currentLine)) {
                    // Ignore the header lines.
                    if (!headerLineList.contains(String.valueOf(lineCount))) {
                        // Parse line data based on delimiter.
                        if (delimiter.equals(" ")) {
                            // This will use ANY white space, variable number spaces, tabs, etc. as
                            // the delimiter...not that the delimiter is " ", and is defined
                            // in the convertDelimiters of FileParserManagerimpl.java.
                            //
                            // This special case also needs to be handled by variableSpecification.js
                            // in the gridForVariableSpecification function.
                            String[] tokens = StringUtil2.splitString(currentLine);
                            List<String> valList = Arrays.asList(tokens);
                            parsedData.add(valList);
                        } else { // all other delimiters
                            String[] lineComponents = currentLine.split(delimiter);
                            List<String> list = new ArrayList<>(Arrays.asList(lineComponents));
                            parsedData.add(list);
                        }
                    }
                }
                lineCount++;
            }
        }
        return parsedData;
    }

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
