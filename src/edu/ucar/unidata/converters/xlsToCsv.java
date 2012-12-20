/*
 * Copyright 1997-2012 Unidata Program Center/University Corporation for
 * Atmospheric Research, P.O. Box 3000, Boulder, CO 80307,
 * support@unidata.ucar.edu.
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.ucar.unidata.converters;


import jxl.*;

import java.io.*;

import java.util.*;


/**
 * Class description
 *
 *
 * @version        Enter version here..., Tue, Dec 18, '12
 * @author         Enter your name here...
 */
public class xlsToCsv {

    /** _more_ */
    private static final String missingFillValue = "-999";

    /**
     * _more_
     *
     * @param xlsFile _more_
     * @param csvFile _more_
     */
    public static void convert(String xlsFile, String csvFile) {
        try {
            //Excel document to be imported
            WorkbookSettings ws = new WorkbookSettings();
            ws.setLocale(new Locale("en", "EN"));
            Workbook w = Workbook.getWorkbook(new File(xlsFile), ws);

            //File to store data in form of CSV
            if (csvFile == null) {
                if (xlsFile.contains(".xlsx")) {
                    csvFile = xlsFile.replace(".xls", ".csv");
                } else {
                    csvFile = xlsFile.replace(".xls", ".csv");
                }
            }

            File               f        = new File(csvFile);

            OutputStream       os       = new FileOutputStream(f);
            String             encoding = "UTF8";
            OutputStreamWriter osw      = new OutputStreamWriter(os,
                                              encoding);
            BufferedWriter     bw       = new BufferedWriter(osw);

            // Gets the sheets from workbook
            for (int sheet = 0; sheet < w.getNumberOfSheets(); sheet++) {
                Sheet s = w.getSheet(sheet);

                //bw.write(s.getName());
                //bw.newLine();

                Cell[] row = null;

                // Gets the cells from sheet
                String contents;
                for (int i = 0; i < s.getRows(); i++) {
                    row = s.getRow(i);
                    if (row.length > 0) {
                        Boolean rowIsEmpty = Boolean.TRUE;
                        Boolean skipFirstComma = Boolean.FALSE;
                        contents = getCellContents(row[0]);
                        if ( !contents.equals("")) {
                            rowIsEmpty = Boolean.FALSE;
                            bw.write(contents);
                        } else {
                            skipFirstComma = Boolean.TRUE;
                        }
                        for (int j = 1; j < row.length; j++) {
                            contents = getCellContents(row[j]);
                            if ( !contents.equals("")) {
                                rowIsEmpty = Boolean.FALSE;
                                if (!skipFirstComma) {
                                    bw.write(',');
                                    bw.write(contents);
                                } else {
                                    skipFirstComma = Boolean.FALSE;
                                    bw.write(contents);
                                }
                            } else {
                                if ((j != row.length -1) && (!skipFirstComma)) {
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
            bw.flush();
            bw.close();
        } catch (UnsupportedEncodingException e) {
            System.err.println(e.toString());
        } catch (IOException e) {
            System.err.println(e.toString());
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    /**
     * _more_
     *
     * @param row _more_
     *
     * @return _more_
     */
    private static String getCellContents(Cell row) {
        String contents = "";

        if (row.getType() == CellType.DATE) {
            DateCell dc       = (DateCell) row;
            Date     cellDate = dc.getDate();
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
     * _more_
     *
     * @param contents _more_
     *
     * @return _more_
     */
    private static String checkCellContents(String contents) {
        if (contents.endsWith(",")) {
            contents = contents.replace(",", "");
        }
        if (contents.equals("---")) {
            contents = missingFillValue;
        }
        return contents;
    }

    /**
     * _more_
     *
     * @param args _more_
     */
    public static void main(String[] args) {
        String xlsFile =
            "/Users/lesserwhirls/dev/unidata/pzhta/pzhta/src/edu/ucar/unidata/converters/test/xlsToCsv/ilu01_07_10.xls";

        convert(xlsFile, null);
    }

}
