package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.AsciiFile;
import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.dsg.NetcdfFileManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;


public class ConvertManagerImpl implements ConvertManager {

    private static final Logger logger = Logger.getLogger(ConvertManagerImpl.class);

    @Resource(name = "netcdfFileManager")
    private NetcdfFileManager netcdfFileManager;

    @Resource(name = "dataManager")
    private DataManager dataManager;

    @Resource(name = "fileParserManager")
    private FileParserManager fileParserManager;

    @Resource(name = "metadataManager")
    private MetadataManager metadataManager;


    public String convertToNetCDF(Data data) throws IOException, IllegalArgumentException {
        String netcdfFile = null;
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

                String filePath = FilenameUtils.concat(FilenameUtils.concat(dataManager.getUploadDir(), data.getId()), data.getDataFileName());
                header = fileParserManager.getHeaderLinesFromFile(filePath, headerLineList);

                // Get the parsed file data.
                List<List<String>> parseFileData = fileParserManager.parseByDelimiter(filePath, headerLineList, dataManager.getDelimiterSymbol(data.getDelimiter()));

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
                netcdfFile = potentialDsgWriter.createNetcdfFile(asciiFile, parseFileData, header, dataManager.getDownloadDir());
                break;
            }
        }
        return netcdfFile;
    }
}
