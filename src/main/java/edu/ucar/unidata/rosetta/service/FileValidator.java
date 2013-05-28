package edu.ucar.unidata.rosetta.service;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;
import org.springframework.validation.Errors;

import edu.ucar.unidata.rosetta.domain.AsciiFile;

@Component
public class FileValidator implements Validator  {
/*
    private String cfType = null;
    private String uniqueId = null;
    private String fileName = null;
    private List<String> delimiterList = new ArrayList<String> ();
    private List<String> headerLineList = new ArrayList<String> ();
    private List<String> platformMetadataList = new ArrayList<String> ();
    private List<String> generalMetadataList = new ArrayList<String> ();
    private HashMap<String, String> variableNameMap = new HashMap<String, String> ();
    private HashMap<String, HashMap> variableMetadataMap = new HashMap<String, HashMap> ();

*/
    private String[] NAUGHTY_STRINGS = {"<script>", "../", "javascript", "::", "&quot;", "fromcharCode", "%3", "$#", "alert(", ".js", ".source", "\\", "scriptlet", ".css", "binding:", ".htc", "vbscript", "mocha:", "livescript:", "base64", "\00", "xss:", "%77", "0x", "IS NULL;", "1;", "; --", "1=1"}; 
    private String[] NAUGHTY_CHARS = {"<", ">", "`", "^", "|", "}", "{"}; 


    public boolean supports(Class clazz) {
        return AsciiFile.class.equals(clazz);
    }

    public void validate(Object obj, Errors errors) {
        AsciiFile file = (AsciiFile) obj;
        validateInput(file.getCfType(), errors);  
        validateInput(file.getUniqueId(), errors); 
        validateInput(file.getFileName(), errors); 
        validateList(file.getDelimiterList(), errors);
        validateList(file.getHeaderLineList(), errors); 

    }


    public void validateList(List<String> list, Errors errors) {
        Iterator<String> iterator = list.iterator();
	    while (iterator.hasNext()) {
		    String input = iterator.next();
            validateInput(input, errors);  
	    }
    }


    public void validateInput(String input, Errors errors) {
        String badChar = checkForNaughtyChars(input);
        if (badChar != null) {
            errors.reject("Bad value submitted: " + badChar);
            return;
        }
        String badString = checkForNaughtyStrings(input);
        if (badString != null) {
            errors.reject("Bad value submitted: " + badString);
            return;
        }
    }


    public String checkForNaughtyStrings(String itemToCheck) {
          for (String item : NAUGHTY_STRINGS) {
              if (StringUtils.contains(StringUtils.lowerCase(itemToCheck), item)) {
                  return item;
              } 
          }
          return null;
    }

    public String checkForNaughtyChars(String itemToCheck) {
          for (String item : NAUGHTY_CHARS) {
              if (StringUtils.contains(itemToCheck, item)) {
                  return item;
              } 
          }
          return null;
    }

    /**
     * Checks to make sure the delimiter count per line is the same for each line.
     * If the count per line is inconsistent, then there is an error.
     * 
     * @param filePath  The path to the file on disk.
     * @thows RuntimeException if the delimiter count per line doesn't match.
     */
    public void validateDelimiterCount(String filePath, Object obj, Errors errors) {
        AsciiFile file = (AsciiFile) obj;
        List<String> delimiterList = file.getDelimiterList();
        List<String> headerLineList = file.getHeaderLineList();
        int lineCount = 0;
        int delimiterRunningTotal = 0;
        boolean dataLine = false;
        String currentLine; 
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            while ((currentLine = reader.readLine()) != null) {
                // If NOT a header line
                if (!headerLineList.contains(new Integer(lineCount).toString())) {
                    // Check to make sure the delimiter count per line is the same for each line.
                    Iterator<String> delimiterIterator = delimiterList.iterator();            
                    while (delimiterIterator.hasNext()) {  
                        String delimiter = delimiterIterator.next();
                        // Find out how many instances of the delimiter are in the line of data
                        int delimiterCount = StringUtils.countMatches(currentLine, delimiter);
                        // Assign FIRST delimiter count number to running total value and
                        // if any subsequent delimiters or lines have a different value, there is an error
                        if (!dataLine) {
                            delimiterRunningTotal = delimiterCount;
                            dataLine = true;
                        } else {       
                            if (delimiterRunningTotal != delimiterCount) {
                                errors.reject("File line of data contains an irregular delimiter count at line number: " + new Integer(lineCount).toString() + " for delimiter: " + delimiter);
                            }
                        }                               
                    }
                }
            } 
            lineCount++; 
        } catch (IOException e) {
            errors.reject(e.getMessage());
        }              
    }



}
