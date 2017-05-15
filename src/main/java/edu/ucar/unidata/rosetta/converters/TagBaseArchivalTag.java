/*
 * Copyright (c) 2012-2017 University Corporation for Atmospheric Research/Unidata
 */

package edu.ucar.unidata.rosetta.converters;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import edu.ucar.unidata.rosetta.util.RosettaProperties;
import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import ucar.nc2.time.CalendarDate;

import static java.nio.file.Files.copy;

/**
 * Tag Base Archival Tags (flat file).
 *
 * This class controls the conversion of Archival Tags as exported by TagBase.
 */

public class TagBaseArchivalTag {

    //static Logger log = Logger.getLogger(TagBaseArchivalTag.class.getName());

    private String tagBaseArchivalFile = "";
    private List<Attribute> globalAttrs = new ArrayList<>();
    private HashMap<String, TreeMap<Long, Ob>> data = new HashMap<>();
    private HashMap<String, TreeMap<Integer, HistBin>> binInfoMin = new HashMap<>();
    private HashMap<String, TreeMap<Integer, HistBin>> binInfoMax = new HashMap<>();

    public TagBaseArchivalTag() {}

    class HistBin {
        private float binValue;
        private String binUnit;

        public HistBin(float value, String unit) {
            this.binValue = value;
            this.binUnit = unit;
        }

        public float getBinValue() {
            return binValue;
        }

        public String getBinUnit() {
            return binUnit;
        }
    }

    class Ob {

        private String value;
        private String unit;

        public String getValue() {
            return value;
        }

        public String getUnit() {
            return unit;
        }

        public Ob(String value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }

    private void updateBinInfo(HashMap<String, TreeMap<Integer, HistBin>> binInfo, String name, int binNumber, HistBin histBin) {
        TreeMap<Integer, HistBin> tmpBinList;

        if (binInfo.containsKey(name)) {
            tmpBinList = binInfo.get(name);
        } else {
            tmpBinList = new TreeMap<>();
        }

        tmpBinList.put(binNumber, histBin);
        binInfo.put(name, tmpBinList);
    }

    private void addData(String name, long time, Ob newOb) {
        // time is seconds since 1970-01-01 from java.util.Date.getTime()
        TreeMap<Long, Ob> tmpObList;

        if (data.containsKey(name)) {
            tmpObList = data.get(name);
        } else {
            tmpObList = new TreeMap<>();
        }

        tmpObList.put(time, newOb);
        data.put(name, tmpObList);
    }

    private String cleanUnit(String unitStr) {
        return null;
    }

    private void processHeaderLine(String line) {
        String[] keyVals;
        if (!line.startsWith("//") && (!line.startsWith("\""))) {
            // hack until header output is all of the form key = value
            line = line.substring(1);
            line = line.replace("\"", "");
            int locOfEq = line.indexOf("=");
            int locOfColon = line.indexOf(":");
            if (locOfColon > 0) {
                // possible that key/value delimited by colon and not equal sign
                if (locOfEq > 0) {
                    // equal sign happens before colon - most likely delimted by equal sign
                    if (locOfEq < locOfColon) {
                        keyVals = line.split("=", 2);
                    } else {
                        keyVals = line.split(":", 2);
                    }
                } else {
                    keyVals = line.split(":", 2);
                }
            } else {
                // normal situation, where attrs of the form :key = value
                keyVals = line.split("=",2);
            }
            if (keyVals.length == 2) {
                globalAttrs.add(new Attribute(keyVals[0].trim(), keyVals[1].trim()));
            } else {
                // missed something
                System.out.println("Global Attr line " + line + " not parsed correctly");
            }
        }
    }

    private void processBinLine(String line) {
        String[] dataEntry = line.split(",");
        int binNumber;
        String binName;
        // bin definition - no datetime associated with it
        float value = Float.parseFloat(dataEntry[2]);
        String unit = dataEntry[4];
        String fullBinName = dataEntry[3];
        HistBin histBin = new HistBin(value, unit);

        String[] splitFullBinName = fullBinName.split("BinMin|BinMax");
        if (splitFullBinName.length == 2) {
            binNumber = Integer.parseInt(splitFullBinName[1]);
            binName = splitFullBinName[0].replace("Hist","");
            if (fullBinName.contains("BinMin")) {
                updateBinInfo(binInfoMin, binName, binNumber, histBin);
            } else {
                updateBinInfo(binInfoMax, binName, binNumber, histBin);
            }
        } else {
            System.out.println("No idea how to get bin number from: " + line);
        }
    }

    private void processDataLine(String line) {
        String[] dataEntry = line.split(",");
        line = line.replaceAll("\"", "");
        if (line.startsWith(",")) {
            // starting with , means there is no date/time, so not an observation
            processBinLine(line);
        } else {
            // this could be an actual data line
            if (dataEntry.length == 5 |
                    (dataEntry.length == 4 && line.endsWith(","))) {
                // full data line, but could be missing units
                String datetime = dataEntry[0];
                int variableId = Integer.parseInt(dataEntry[1]);
                String value = dataEntry[2];
                String name = dataEntry[3];
                String unit = "";
                if(!line.endsWith(",")) {
                    unit = dataEntry[4];
                }
                Ob thisOb = new Ob(value, unit);
                CalendarDate calendarDate = CalendarDate.parseISOformat("gregorian", datetime);
                addData(name, calendarDate.toDate().getTime(), thisOb);
            } else {
                // cannot identify data line
                System.out.println("No idea what this is: " + line);
            }
        }
    }

    private boolean writeNcFile(String ncFileName) {
        return false;
    }

    public void parse(String tagBaseArchiveFile) {
        System.out.println("start conversion, baby!");
        this.tagBaseArchivalFile = tagBaseArchiveFile;
        String line;
        boolean readSuccess;

        Path tbFilePath = Paths.get(tagBaseArchiveFile);
        try {
            BufferedReader br;
            if (tagBaseArchiveFile.endsWith(".gz")) {
                InputStream fileStream = new FileInputStream(tbFilePath.toString());
                InputStream gzipStream = new GZIPInputStream(fileStream);
                Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.ISO_8859_1);
                br = new BufferedReader(decoder);
            } else {
                br = Files.newBufferedReader(tbFilePath, StandardCharsets.ISO_8859_1);
            }

            System.out.println("opened it!");
            line = br.readLine();
            line = line.trim().trim();
            System.out.println("reading it!");
            while (br.ready()) {
                // Read the header (delimited by the : character or //, but ignore // as these are
                // just comments)
                String first = line.substring(0, 1);
                if (first.equals(":") | first.equals("/") | first.equals("\"")) {
                    processHeaderLine(line);
                } else {
                    // data line
                    processDataLine(line);
                }

                // other stuff?

                // read next line
                line = br.readLine().trim();
            }
            //System.out.println(globalAttrs);
        } catch (IOException ioe) {
            readSuccess = false;
            System.out.println(ioe.getMessage());
        }

        System.out.println("done!");
        printInventory();
    }

    private void printInventory() {
        System.out.println("Inventory:");
        List sortedKeys=new ArrayList(data.keySet());
        Collections.sort(sortedKeys);

        for (Object name: sortedKeys) {
            int len = data.get(name).size();
            System.out.println("   " + name + " (" + len + ")");
        }
    }

    public HashMap<String, String> getGlobalMetadata() {
        // should probably encode in json to return to browser
        HashMap<String, String> globalMetadata = new HashMap<>();
        for (Attribute attr : globalAttrs) {
            globalMetadata.put(attr.getFullName(), attr.getStringValue());
        }
        return globalMetadata;
    }

    public String convert(String ncfile) {
        // open a netcdf file
        try (NetcdfFileWriter ncfw = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, ncfile)) {
            Group group = null;

            // create time dimension
            //ncfw.addDimension(group, timeDim.getFullName(), timeDim.getLength());

            // add global attribute
            for (Attribute ga : globalAttrs) {
                ncfw.addGroupAttribute(group, ga);
            }

            ncfw.create();

            // write the data, yo!
            //for (String dataVar : data.keySet()) {
            //    Array thisData = ArrayFloat.makeArray(DataType.FLOAT, data.get(dataVar));
            //    Variable thisVar = ncfw.findVariable(dataVar);
            //    ncfw.write(thisVar, thisData);
            //}

            // write out trajectory id
            //Array siteIdArr = ArrayChar.makeFromString(siteId, siteId.length());
            //ncfw.write(trajId, siteIdArr);

            // close shop
            ncfw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ncfile;
    }

    public static void main(String[] args) {
        String file = "/Users/sarms/dev/unidata/repos/rosetta/src/test/data/conversions/TagBaseArchiveFlatFile/TagDataFlatFileExample.txt";
        String zipfile = "/Users/sarms/dev/unidata/repos/rosetta/src/test/data/conversions/TagBaseArchiveFlatFile/TagDataFlatFileExample.gz";
        TagBaseArchivalTag converter = new TagBaseArchivalTag();
        converter.parse(zipfile);
        String ncfile = converter.convert("/Users/sarms/dev/unidata/repos/rosetta/src/test/data/conversions/TagBaseArchiveFlatFile/TagDataFlatFileExample.nc");
        System.out.println(ncfile);
    }
}