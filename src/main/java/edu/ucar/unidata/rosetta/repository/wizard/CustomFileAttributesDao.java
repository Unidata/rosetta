package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.domain.wizard.CustomFileAttributes;
import org.springframework.dao.DataRetrievalFailureException;

public interface CustomFileAttributesDao {

  public CustomFileAttributes lookupById(String id) throws DataRetrievalFailureException;

  public void updatePersistedData(String id, CustomFileAttributes customFileAttributes) throws DataRetrievalFailureException;

}
