package edu.ucar.unidata.rosetta.service.wizard;

import javax.servlet.http.HttpServletRequest;

import edu.ucar.unidata.rosetta.domain.wizard.CfTypeData;
import edu.ucar.unidata.rosetta.exceptions.RosettaDataException;

/**
 * Service for handling CF type and related data.
 *
 * @author oxelson@ucar.edu
 */
public interface CfTypeDataManager {

  /**
   * Looks up and retrieves persisted Cf type data using the given ID.
   *
   * @param id  The ID corresponding to the data to retrieve.
   * @return    The persisted Cf type data.
   */
  public CfTypeData lookupPersistedCfTypeDataById(String id);

  /**
   * Persists the provided CF type data.
   *
   * @param cfTypeData  The CF type data to persist.
   */
  public void persistCfTypeData(CfTypeData cfTypeData);

  /**
   * Processes the data submitted by the user containing CF type information. If an ID already
   * exists, the persisted data corresponding to that ID is collected and updated with the newly
   * submitted data.  If no ID exists (is null), the data is persisted for the first time.
   *
   * @param id The unique ID corresponding to already persisted data (may be null).
   * @param cfTypeData The CfTypeData object containing user-submitted CF type information.
   * @param request HttpServletRequest used to make unique IDs for new data.
   * @throws RosettaDataException If unable to lookup the metadata profile.
   */
  public void processCfType(String id, CfTypeData cfTypeData, HttpServletRequest request) throws RosettaDataException ;

  /**
   * Updates persisted CF type data with the information in the provided CFTypeData object.
   *
   * @param cfTypeData  The updated CF type data.
   */
  public void updatePersistedCfTypeData(CfTypeData cfTypeData);

}
