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

    /**
     * This method reads each line of a file and if more than one delimiter has been
     * specified by the user, it normalizes the delimiters in the line to match the
     * selectedDelimiter (for ease of parsing purposes on the client-side), or simply
     * appends  a new line character and appends to a StringBuffer, and returns the
     * StringBuffer string value. This method is used to parse the file data when
     * both the header lines and delimiter(s) have been specified by the user.
     * TODO: refactor to return JSON
     *
     * @param filePath          The path to the file on disk.
     * @param selectedDelimiter The delimiter selected to which any other delimiters will be
     *                          normalized.
     * @param delimiterList     The List<String> of delimiters specified by the user.
     * @param headerLineList    The List<String> of header lines specified by the user.
     * @return A String of the file data parsed by the delimiter(s).
     */
    public String normalizeDelimiters(String filePath, String selectedDelimiter, List<String> delimiterList, List<String> headerLineList);

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


