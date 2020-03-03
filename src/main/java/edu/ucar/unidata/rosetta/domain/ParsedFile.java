/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;
import edu.ucar.unidata.rosetta.util.VariableInfoUtils;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.jni.netcdf.Nc4Iosp;

/**
 * An object to hold parsed data.
 *
 * @author sarms@ucar.edu
 */
public class ParsedFile {

  private List<String> header;
  private Map<Integer, List<String>> stringData;
  private Map<Integer, Array> arrayData;
  private Map<Integer, DataType> dataTypes;

  /**
   * Returns the headers of a data file.
   *
   * @return The headers.
   */
  public List<String> getHeader() {
    return header;
  }


  /**
   * Returns the parsed data of a data file as a list of Strings.
   *
   * @return The parsed data (Strings).
   */
  public Map<Integer, List<String>> getStringData() {
    return stringData;
  }


  /**
   * Returns the parsed data of a data file as a list of Array types.
   *
   * @return The parsed data (Arrays).
   */
  public Map<Integer, Array> getArrayData() {
    return arrayData;
  }

  /**
   *
   * @param datafile
   * @param template
   * @param delimiter
   * @throws IOException
   * @throws RosettaDataException If unable to parse file with provided delimiter.
   */
  public ParsedFile(Path datafile, Template template, String delimiter) throws IOException, RosettaDataException {

    List<Integer> headerLineNumbers = template.getHeaderLineNumbers();
    headerLineNumbers.sort(Collections.reverseOrder());

    List<String> dataLines = Collections.emptyList();
    dataLines = Files.readAllLines(datafile);

    header = new ArrayList<String>();
    for (int headerLine : headerLineNumbers) {
      header.add(dataLines.get(headerLine));
      dataLines.remove(headerLine);
    }

    List<VariableInfo> variableInfoList = template.getVariableInfoList();
    HashMap<Integer, String> columnDataTypes = new HashMap<>();
    for (VariableInfo variableInfo : variableInfoList) {
      int colId = variableInfo.getColumnId();
      String name = variableInfo.getName();
      if (VariableInfoUtils.isVarUsed(variableInfo)) {
        List<RosettaAttribute> rosettaControlMetadata = variableInfo.getRosettaControlMetadata();
        if (rosettaControlMetadata != null) {
          for (RosettaAttribute attr : rosettaControlMetadata) {
            if (attr.getName().equals("type")) {
              columnDataTypes.put(colId, attr.getValue());
            }
          }
        }
      }
    }

    Set<Integer> columnsToRead = columnDataTypes.keySet();

    // init stringData
    stringData = new HashMap<Integer, List<String>>();
    for (Integer colNum : columnsToRead) {
      stringData.put(colNum, new ArrayList<String>());
    }

    try {
      if (delimiter.equals("\\\\s+")) {
        delimiter = "\\s+";
      }
      for (String line : dataLines) {
        String[] splitLine = line.split(delimiter);
        for (int colNum : columnsToRead) {
          stringData.get(colNum).add(splitLine[colNum]);
        }
      }
    } catch (ArrayIndexOutOfBoundsException e) {
      throw new RosettaDataException("Unable to parse data file with provided delimiter: " + e);
    }


    // convert string data to netCDF-Java Array
    arrayData = new HashMap<>();
    dataTypes = new HashMap<>();

    for (int colNum : columnsToRead) {
      List<String> strData = stringData.get(colNum);
      String type = columnDataTypes.get(colNum);
      Array arr = null;
      DataType dataType = null;

      if (type.toLowerCase().equals("string") || type.toLowerCase().equals("text")) {
        dataType = DataType.STRING;
      } else if (type.toLowerCase().equals("integer")) {
        dataType = DataType.INT;
      } else if (type.toLowerCase().equals("float")) {
        dataType = DataType.FLOAT;
      } else if (type.toLowerCase().equals("double")) {
        dataType = DataType.DOUBLE;
      } else if (type.toLowerCase().equals("BOOLEAN")) {
        dataType = DataType.BOOLEAN;
      }

      if (dataType != null) {
        arrayData.put(colNum, Array.makeArray(dataType, strData));
      } else {
        System.out.println("datatype " + type + "not converted yet");
      }
    }
  }
}
