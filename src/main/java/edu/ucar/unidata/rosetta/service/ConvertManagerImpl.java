package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.converters.TagUniversalFileFormat;
import edu.ucar.unidata.rosetta.domain.AsciiFile;
import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.dsg.NetcdfFileManager;

import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import ucar.ma2.InvalidRangeException;


public class ConvertManagerImpl implements ConvertManager {

    private static final Logger logger = Logger.getLogger(ConvertManagerImpl.class);

    @Resource(name = "netcdfFileManager")
    private NetcdfFileManager netcdfFileManager;

    @Resource(name = "dataManager")
    private DataManager dataManager;

    @Resource(name = "fileParserManager")
    private FileManager fileManager;

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;


    public String convertToNetCDF(Data data) throws IOException, IllegalArgumentException, InvalidRangeException, RosettaDataException {

        String filePathUploads = FilenameUtils.concat(dataManager.getUploadDir(), data.getId());
        String filePathDownloads = FilenameUtils.concat(dataManager.getDownloadDir(), data.getId());
        File localFileDir = new File(filePathDownloads);

        if (!localFileDir.exists())
            if (!localFileDir.mkdirs())
                throw new IOException("Unable to create " + data.getId() + " subdirectory in downloads directory.");
        String netcdfFileName = FilenameUtils.removeExtension(data.getDataFileName()) + ".nc";
        String ncFileToCreate = FilenameUtils.concat(filePathDownloads, netcdfFileName);

        // The data file type uploaded by the user decides how it is converted to netCDF.
        if (!data.getDataFileType().equals("Custom_File_Type")) {
            // Not a custom file type, so use one of Sean's converters.

                TagUniversalFileFormat tagUniversalFileFormat = new TagUniversalFileFormat();
                tagUniversalFileFormat.parse(FilenameUtils.concat(filePathUploads, data.getDataFileName()));
                String netcdfFile = tagUniversalFileFormat.convert(ncFileToCreate);

        } else {
            // Custom file type, so we need to convert it here.
            NetcdfFileManager dsgWriter;
            for (NetcdfFileManager potentialDsgWriter : netcdfFileManager.asciiToDsg()) {
                // Get the CF type.
                String cfType = data.getCfType();
                if (cfType == null || cfType.equals(""))
                    cfType = dataManager.getCFTypeFromPlatform(data.getPlatform());

                // Does this DSG writer handle this particular CF type?
                if (potentialDsgWriter.isMine(cfType)) {

                    // Get the header info.
                    List<String> header;
                    List<String> headerLineList;
                    if (!data.getNoHeaderLines() && data.getHeaderLineNumbers() != null)
                        headerLineList = Arrays.asList(data.getHeaderLineNumbers().split(","));
                    else
                        headerLineList = new ArrayList<>();

                    header = fileManager.getHeaderLinesFromFile(FilenameUtils.concat(filePathUploads, data.getDataFileName()), headerLineList);

                    // Get the parsed file data.
                    List<List<String>> parseFileData = fileManager.parseByDelimiter(FilenameUtils.concat(filePathUploads, data.getDataFileName()), headerLineList, dataManager.getDelimiterSymbol(data.getDelimiter()));

                    // A hack to temporarily bridge the old rosetta code with the new. MUST REFACTOR!
                    AsciiFile asciiFile = new AsciiFile();
                    asciiFile.setCfType(cfType);
                    asciiFile.setUniqueId(data.getId());
                    asciiFile.setFileName(data.getDataFileName());
                    asciiFile.setDelimiters(data.getDelimiter());
                    asciiFile.setDelimiterList(data.getDelimiter());
                    asciiFile.setHeaderLineNumbers(data.getHeaderLineNumbers());
                    asciiFile.setHeaderLineList(data.getHeaderLineNumbers());
                    asciiFile.setPlatformMetadataMap(new HashMap<String, String>()); // LEAVING EMPTY
                    asciiFile.setGeneralMetadataMap(metadataManager.getGeneralMetadataMap(data.getId(), "general"));
                    asciiFile.setVariableNameMap(metadataManager.getVariableNameMap(data.getId(), "variable"));
                    asciiFile.setVariableMetadataMap(metadataManager.getVariableMetadataMap(data.getId(), "variable"));
                    asciiFile.setParseHeaderForMetadataList(new ArrayList<String>()); // LEAVING EMPTY
                    String netcdfFile = potentialDsgWriter.createNetcdfFile(asciiFile, parseFileData, header, dataManager.getDownloadDir());
                    break;
                }
            }
        }
        return netcdfFileName;
    }
}
