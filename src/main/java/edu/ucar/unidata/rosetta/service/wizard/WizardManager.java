/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.service.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;

import javax.servlet.http.HttpServletRequest;

/**
 * Service for handling CF type and related data collected from the wizard.
 */
public interface CfTypeManager {

    /**
     * Looks up and retrieves persisted CF type data using the given ID.
     *
     * @param id The ID corresponding to the data to retrieve.
     * @return The persisted Cf type data.
     */
    public WizardData lookupPersistedCfTypeDataById(String id);

    /**
     * Persists the provided CF type data.
     *
     * @param wizardData The CF type data to persist.
     */
    public void persistCfTypeData(WizardData wizardData);

    /**
     * Processes the data submitted by the user containing CF type information. If an ID already
     * exists, the persisted data corresponding to that ID is collected and updated with the newly
     * submitted data.  If no ID exists (is null), the data is persisted for the first time.
     *
     * @param id         The unique ID corresponding to already persisted data (may be null).
     * @param wizardData The WizardData object containing user-submitted CF type information.
     * @param request    HttpServletRequest used to make unique IDs for new data.
     * @throws RosettaDataException If unable to lookup the metadata profile.
     */
    public void processCfType(String id, WizardData wizardData, HttpServletRequest request) throws RosettaDataException;

    /**
     * Updates persisted CF type data with the information in the provided CFTypeData object.
     *
     * @param wizardData The updated CF type data.
     */
    public void updatePersistedCfTypeData(WizardData wizardData);

}
