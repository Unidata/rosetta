/*
 * Copyright (c) 2012-2018 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.Variable;

import java.util.List;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * The data access object representing metadata.
 *
 * @author oxelson@ucar.edu
 */
public interface VariableDao {

    /**
     * Looks up and retrieves a list of persisted Variable objects using the given id.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @return The Variable object.
     */
    public List<Variable> lookupVariable(String wizardDataId);



    /**
     * Persists the information in the given list of variable objects.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param variables The list of Variable objects to persist.
     * @throws DataRetrievalFailureException If unable to persist the Variable objects.
     */
    public void persistVariables(String wizardDataId, List<Variable> variables) throws DataRetrievalFailureException;

    /**
     * Persists the information in the given variable object.
     *
     * @param wizardDataId The id of the corresponding WizardData object.
     * @param variable The Variable object to persist.
     * @throws DataRetrievalFailureException If unable to persist the Variable object.
     */
    public void persistVariable(String wizardDataId, Variable variable) throws DataRetrievalFailureException;

    /**
     * Updated the information corresponding to the given list of variable objects.
     *
     * @param variables The list of variable objects to update.
     * @throws DataRetrievalFailureException If unable to update persisted Variable objects.
     */
    public void updatePersistedVariables(List<Variable> variables) throws DataRetrievalFailureException;

    /**
     * Updated the information corresponding to the given variable object.
     *
     * @param variable The variable object to update.
     * @throws DataRetrievalFailureException If unable to update persisted Variable object.
     */
    public void updatePersistedVariable(Variable variable) throws DataRetrievalFailureException;


    /**
     * Deletes the persisted list of variable information using the given id.
     *
     * @param variables The list of variable objects to update.
     * @throws DataRetrievalFailureException If unable to delete persisted variable information.
     */
    public void deletePersistedVariables(List<Variable> variables) throws DataRetrievalFailureException ;

    /**
     * Deletes the persisted variable object information using the given id.
     *
     * @param variable The variable object to delete.
     * @throws DataRetrievalFailureException If unable to delete persisted variable information.
     */
    public void deletePersistedVariable(Variable variable) throws DataRetrievalFailureException ;
}
