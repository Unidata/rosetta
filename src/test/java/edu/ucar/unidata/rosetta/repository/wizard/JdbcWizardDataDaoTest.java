package edu.ucar.unidata.rosetta.repository.wizard;

import edu.ucar.unidata.rosetta.config.WebAppContext;
import edu.ucar.unidata.rosetta.domain.wizard.WizardData;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {WebAppContext.class})
public class JdbcWizardDataDaoTest {

    private WizardDataDao wizardDataDao;

    private WizardData wizardData;

    @Before
    public void setUp() throws Exception {
        wizardDataDao = mock(JdbcWizardDataDao.class);

        wizardData = new WizardData();
        wizardData.setId("1270011146053096");

        // What is expected.
        when(wizardDataDao.lookupWizardDataById("1270011146053096")).thenReturn(wizardData);
    }


    @Test
    public void lookupWizardDataByIdTest() throws Exception {
        wizardData.setCfType("eTuff");
        wizardData.setCommunity("Bio-Logging");
        wizardData.setMetadataProfile(null);
        wizardData.setPlatform("eTag");

        WizardData wizardData = wizardDataDao.lookupWizardDataById("1270011146053096");
        assertEquals(wizardData.getCfType(), "eTuff");
        assertEquals(wizardData.getCommunity(), "Bio-Logging");
        assertEquals(wizardData.getPlatform(), "eTag");
        assertNull(wizardData.getMetadataProfile());
    }

    @Test
    public void persistWizardDataTest() throws Exception {
        wizardData.setPlatform(null);
        wizardData.setCommunity(null);
        wizardData.setMetadataProfile("CF,eTuff,NCEI");
        wizardDataDao.persistWizardData(wizardData);
        WizardData wizardData = wizardDataDao.lookupWizardDataById("1270011146053096");
        assertEquals(wizardData.getMetadataProfile(), "CF,eTuff,NCEI");
        assertNull(wizardData.getPlatform());
        assertNull(wizardData.getCommunity());
    }

    @Test
    public void updatePersistedWizardDataTest() throws Exception {
        wizardData.setPlatform(null);
        wizardData.setCommunity(null);
        wizardData.setMetadataProfile("CF,eTuff,NCEI");
        wizardDataDao.updatePersistedWizardData(wizardData);
        WizardData wizardData = wizardDataDao.lookupWizardDataById("1270011146053096");
        assertEquals(wizardData.getMetadataProfile(), "CF,eTuff,NCEI");
        assertNull(wizardData.getPlatform());
        assertNull(wizardData.getCommunity());
    }

}