package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.CFType;

import java.util.Map;

public interface DataManager {

    public Map<String,Object> getData();

    public String getCFData();

    public void setCFData(CFType cfType) ;

}
