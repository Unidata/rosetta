package edu.ucar.unidata.rosetta.util;

import edu.ucar.unidata.rosetta.exceptions.RosettaFileException;

import java.io.*;
import java.util.Date;
import java.util.Locale;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


/**
 * Utility class to convert xls, xlsx files to Comma Separated Value (CSV) format.
 *
 * @author sarms@ucar.edu
 * @author oxelson@ucar.edu
 */
public class XlsToCsvUtil {

  private final static Logger logger = Logger.getLogger(XlsToCsvUtil.class);


  // If a cell is empty, set the value in the CSV file to -999
  private static final String MISSING_FILL_VALUE = "-999";

  /**
   * Converts the provided xls/xlsx file to Comma Separated Value (CSV) format.
   *
   * @param xlsFile Path to xls/xlsx file
   * @param csvFile Path where csv file should be created. If null, csv file will be created in the
   * same location as @param xlsFile
   * @return True if file conversion was successful; otherwise false.
   * @throws RosettaFileException If unable to convert xls/xlsx file to csv.
   */
  public static boolean convert(String xlsFile, String csvFile) throws RosettaFileException {

    // File to store data in form of CSV
    if (csvFile == null) {
      csvFile = FilenameUtils.removeExtension(xlsFile) + ".csv";
    }

    try {
      File f = new File(csvFile);
      // If file doesn't exists, then create it.
      if (!f.exists()) {
        if (!f.createNewFile()) {
          return false; // Unable to create file.
        }
      }

      String encoding = "UTF8";
      try (OutputStream os = new FileOutputStream(f);
          OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
          BufferedWriter bw = new BufferedWriter(osw)) {

        //Excel document to be imported
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("en", "EN"));
        Workbook w = Workbook.getWorkbook(new File(xlsFile), ws);

        // Gets the sheets from workbook
        for (int sheet = 0; sheet < w.getNumberOfSheets(); sheet++) {
          Sheet s = w.getSheet(sheet);
          Cell[] row;

          // Gets the cells from sheet
          String contents;
          for (int i = 0; i < s.getRows(); i++) {
            row = s.getRow(i);
            if (row.length > 0) {
              Boolean rowIsEmpty = Boolean.TRUE;
              Boolean skipFirstComma = Boolean.FALSE;
              contents = getCellContents(row[0]);
              if (!contents.equals("")) {
                rowIsEmpty = Boolean.FALSE;
                bw.write(contents);
              } else {
                skipFirstComma = Boolean.TRUE;
              }
              for (int j = 1; j < row.length; j++) {
                contents = getCellContents(row[j]);
                if (!contents.equals("")) {
                  rowIsEmpty = Boolean.FALSE;
                  if (!skipFirstComma) {
                    bw.write(',');
                    bw.write(contents);
                  } else {
                    skipFirstComma = Boolean.FALSE;
                    bw.write(contents);
                  }
                } else {
                  if ((j != row.length - 1) && (!skipFirstComma)) {
                    bw.write(" ");
                  }
                  skipFirstComma = Boolean.TRUE;
                }
              }
              if (!rowIsEmpty) {
                bw.newLine();
              }
            }
          }
        }

      } catch (IOException | BiffException e) {
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        logger.error(errors);
        throw new RosettaFileException("Unable to convert file to CSV format: " + errors);
      }

    } catch (IOException e) {
      StringWriter errors = new StringWriter();
      e.printStackTrace(new PrintWriter(errors));
      logger.error(errors);
      throw new RosettaFileException("Unable to create CSV file: " + errors);
    }
    return true; // If we made it this far, then it's true.
  }

  /**
   * Get the contents of a cell. If the cell @param row is formatted as a date, then convert the
   * value to seconds since 1970-01-01.
   *
   * @param row Cell object representing a row
   * @return String representation of value contained within the row
   */
  private static String getCellContents(Cell row) {
    String contents;

    if (row.getType() == CellType.DATE) {
      DateCell dc = (DateCell) row;
      Date cellDate = dc.getDate();
      // epoch - milliseconds since 1970-01-01
      long epoch = cellDate.getTime();
      // contents - seconds since 1970-01-01
      contents = Long.toString(epoch / 1000);
    } else {
      contents = row.getContents();
      contents = checkCellContents(contents);
    }
    return contents;
  }

  /**
   * Check the cell contents for a missing value. If cell is empty set the cell content value to
   * MISSING_FILL_VALUE.
   *
   * @param contents the contents of a cell of the spreadsheet
   * @return contents with the missing value replaced
   */
  private static String checkCellContents(String contents) {
    if (contents.endsWith(",")) {
      contents = contents.replace(",", "");
    }
    if (contents.equals("---")) {
      contents = MISSING_FILL_VALUE;
    }
    return contents;
  }
}
