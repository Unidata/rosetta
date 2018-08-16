/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object for CF type and related data.
 */
public interface CfTypeDataDao {

    /**
     * Looks up and returns the persisted CF type and related data using the given ID.
     *
     * @param id  The ID associated with the CF type data.
     * @return  The CF type data in a WizardData object.
     * @throws DataRetrievalFailureException If unable to find persisted CF type data corresponding to ID.
     */
    public WizardData lookupCfDataById(String id) throws DataRetrievalFailureException;

    /**
     * Persists the provided Cf type and related data.
     *
     * @param wizardData  The WizardData object containing the CF type and related data.
     * @throws DataRetrievalFailureException  If unable to persist CF type data or if CF type already is persisted.
     */
    public void persistCfTypeData(WizardData wizardData) throws DataRetrievalFailureException;

    /**
     * Updates persisted CF type and related data with the provided new values.
     *
     * @param wizardData The WizardData object containing the CF type and related data.
     * @throws DataRetrievalFailureException  If unable to update the persisted CF type data.
     */
    public void updatePersistedCfTypeData(WizardData wizardData) throws DataRetrievalFailureException;
}
