package edu.ucar.unidata.rosetta.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing file data.
 */
public interface FileParserManager {

    public List<List<String>> parsedFileData = new ArrayList<List<String>>();

    /**
     * Returns each line of the file data parsed by delimiter into a
     * List<String> which is then stored into List<List<String>>.
     *
     * @return The parsed file data.
     */
    public List<List<String>> getParsedFileData();

    /**
     * Sets each line of the file data parsed by delimiter into a
     * List<String> which is then stored into List<List<String>>.
     *
     * @param parsedFileData The parsed file data.
     */
    public void setParsedFileData(List<List<String>> parsedFileData);

    /**
     * A simple method that reads each line of a file, appends a new line
     * character & adds to a List. The list is then turned into a JSON string.
     *
     * @param filePath The path to the file on disk.
     * @return A JSON String of the file data parsed by line.
     * @throws IOException For any file I/O or JSON conversions problems.
     */
    public String parseByLine(String filePath) throws IOException;

    public List<String>getHeaderLinesFromFile(String filePath, List<String> headerLineList) throws IOException;

    public List<List<String>> parseByDelimiter(String filePath, List<String> headerLineList, String delimiter) throws IOException;


    //String normalizeDelimiters(String filePath, String delimiter, List<String> headerLineList) throws IOException;

    /**
     * A simple method that reads each line of a file, and looks for blank lines.
     * Blank line = empty, only whitespace, or null (as per StringUtils).
     *
     * @param file The path to the file on disk.
     * @return The number of blank lines in the file.
     */
    public int getBlankLines(File file);

    /**
     * Returns the lines in the header as List<String>
     *
     * @return The header.
     */
    public List<String> getHeader();

    /**
     * Sets the lines in the header as List<String>
     *
     * @param header The header.
     */
    public void setHeader(List<String> header);
}


