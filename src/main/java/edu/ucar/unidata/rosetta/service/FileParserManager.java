package edu.ucar.unidata.rosetta.service;

import java.io.File;
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
     * @return  The parsed file data.  
     */
    public List<List<String>> getParsedFileData();

    /**
     * Sets each line of the file data parsed by delimiter into a 
     * List<String> which is then stored into List<List<String>>. 
     * 
     * @param parsedFileData  The parsed file data. 
     */
    public void setParsedFileData(List<List<String>> parsedFileData);

    /**
     * A simple method that reads each line of a file, appends a new line character
     * and appends to a StringBuffer, and returns the StringBuffer string value. This
     * method is used to parse the file data when no header lines have been specified.
     * TODO: refactor to return JSON
     * 
     * @param filePath  The path to the file on disk.
     * @return  A String of the file data parsed by line.
     */
    public String parseByLine(String filePath);

    /**
     * This method reads each line of a file and if more than one delimiter has been 
     * specified by the user, it normalizes the delimiters in the line to match the 
     * selectedDelimiter (for ease of parsing purposes on the client-side), or simply 
     * appends  a new line character and appends to a StringBuffer, and returns the  
     * StringBuffer string value. This method is used to parse the file data when 
     * both the header lines and delimiter(s) have been specified by the user.
     * TODO: refactor to return JSON
     * 
     * @param filePath  The path to the file on disk.
     * @param selectedDelimiter  The delimiter selected to which any other delimiters will be normalized.
     * @param delimiterList  The List<String> of delimiters specified by the user.
     * @param headerLineList  The List<String> of header lines specified by the user.
     * @return  A String of the file data parsed by the delimiter(s).
     */
    public String normalizeDelimiters(String filePath, String selectedDelimiter, List<String> delimiterList, List<String> headerLineList);
	
    /**
     * A simple method that reads each line of a file, and looks for blank lines.
	 * Blank line = empty, only whitespace, or null (as per StringUtils).
     * 
     * @param file
     *            The path to the file on disk.
     * @return  The number of blank lines in the file. 
     */
	public int getBlankLines(File file);

}


