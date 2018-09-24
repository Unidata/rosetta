/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object for persisted wizard data.
 */
public interface WizardDataDao {

    /**
     * Looks up and returns the persisted wizard data using the given ID.
     *
     * @param id  The ID associated with the wizard data..
     * @return  The data in a WizardData object.
     * @throws DataRetrievalFailureException If unable to find persisted wizard data corresponding to ID.
     */
    public WizardData lookupWizardDataById(String id) throws DataRetrievalFailureException;

    /**
     * Persists the provided wizard data for the first time.
     *
     * @param wizardData  The WizardData object containing the data to persist.
     * @throws DataRetrievalFailureException  If unable to persist wizard data or if wizard data already is persisted.
     */
    public void persistWizardData(WizardData wizardData) throws DataRetrievalFailureException;

    /**
     * Updates persisted wizard data with the provided new values.
     *
     * @param wizardData The WizardData object containing the data to update.
     * @throws DataRetrievalFailureException  If unable to update the persisted wizard data.
     */
    public void updatePersistedWizardData(WizardData wizardData) throws DataRetrievalFailureException;
}
