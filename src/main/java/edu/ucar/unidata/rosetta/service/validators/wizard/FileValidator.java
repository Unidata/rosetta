package edu.ucar.unidata.rosetta.service.validators.wizard;

import edu.ucar.unidata.rosetta.service.validators.CommonValidator;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import edu.ucar.unidata.rosetta.domain.AsciiFile;

/**
 * @author oxelson@ucar.edu
 */
@Component("fileValidator")
public class FileValidator extends CommonValidator implements Validator {
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


    public boolean supports(Class clazz) {
        return AsciiFile.class.equals(clazz);
    }

    public void validate(Object obj, Errors errors) {
        /*
        AsciiFile file = (AsciiFile) obj;
        validateInput(file.getCfType(), errors);
        validateInput(file.getUniqueId(), errors);
        validateInput(file.getFileName(), errors);
        validateList(file.getDelimiterList(), errors);
        validateList(file.getHeaderLineList(), errors);
        */

    }

    private void validateList(List<String> list, Errors errors) {
        for (String input : list) {
            validateInput(input, errors);
        }
    }

    /**
     * Checks to make sure the delimiter count per line is the same for each line.
     * If the count per line is inconsistent, then there is an error.
     *
     * @param filePath The path to the file on disk.
     */
    private void validateDelimiterCount(String filePath, Object obj, Errors errors) {
        /*
        AsciiFile file = (AsciiFile) obj;
        List<String> delimiterList = file.getDelimiterList();
        List<String> headerLineList = file.getHeaderLineList();
        int lineCount = 0;
        int delimiterRunningTotal = 0;
        boolean dataLine = false;
        String currentLine;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((currentLine = reader.readLine()) != null) {
                // If NOT a header line
                if (!headerLineList.contains(String.valueOf(lineCount))) {
                    // Check to make sure the delimiter count per line is the same for each line.
                    for (String delimiter : delimiterList) {
                        // Find out how many instances of the delimiter are in the line of data
                        int delimiterCount = StringUtils.countMatches(currentLine, delimiter);
                        // Assign FIRST delimiter count number to running total value and
                        // if any subsequent delimiters or lines have a different value, there is an error
                        if (!dataLine) {
                            delimiterRunningTotal = delimiterCount;
                            dataLine = true;
                        } else {
                            if (delimiterRunningTotal != delimiterCount) {
                                errors.reject("File line of data contains an irregular delimiter count at line number: " + String.valueOf(lineCount) + " for delimiter: " + delimiter);
                            }
                        }
                    }
                }
                lineCount++;
            }
        } catch (IOException e) {
            errors.reject(e.getMessage());
        }
        */
    }


}
