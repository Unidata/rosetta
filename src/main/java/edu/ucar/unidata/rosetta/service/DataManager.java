package edu.ucar.unidata.rosetta.service;

import edu.ucar.unidata.rosetta.domain.Data;

import javax.servlet.http.HttpServletRequest;

public interface DataManager {

    /**
     * Looks up and retrieves a Data object using the given id.
     *
     * @param id    The id of the Data object.
     * @return      The Data object corresponding to the given id.
     */
    public Data lookupById(int id);

    /**
     * Persists the information in the given data object.
     *
     * @param data  The Data object to persist.
     */
    public void persistData(Data data, HttpServletRequest request);

    /**
     * Updated the persisted information corresponding to the given data object.
     *
     * @param data  The data object to update.
     */
    public void updateData(Data data);

    /**
     * Deletes the persisted data object information.
     *
     * @param id    The id of the Data object to delete.
     */
    public void deleteData(int id);

}
