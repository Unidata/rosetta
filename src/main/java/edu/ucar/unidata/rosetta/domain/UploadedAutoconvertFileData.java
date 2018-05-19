package edu.ucar.unidata.rosetta.domain;

/**
 * Created by lesserwhirls on 3/28/16.
 */
public class UploadedAutoconvertFileData extends UploadedFileData {

    private String convertFrom = null;
    private String convertTo = null;

    public String getConvertFrom() {
        return convertFrom;
    }

    public void setConvertFrom(String convertFrom) {
        this.convertFrom = convertFrom;
    }

    public String getConvertTo() {
        return convertTo;
    }

    public void setConvertTo(String convertTo) {
        this.convertTo = convertTo;
    }
}
