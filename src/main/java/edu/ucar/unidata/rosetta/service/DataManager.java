package edu.ucar.unidata.rosetta.service;

import java.util.Map;

public interface DataManager {

    public Map<String,Object> getData();

    public String getCFData();

    public void setCFData(String cfType);

}
