package edu.ucar.unidata.rosetta.controller.wizard;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import edu.ucar.unidata.rosetta.config.WebAppContext;
import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.service.wizard.DataManager;
import javax.servlet.http.Cookie;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


@Ignore("No bean named 'cfTypeDataManager' available - update?")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppContext.class})
public class ConvertAndDownloadControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private DataManager dataManagerMock;

  @Autowired
  private WebApplicationContext context;

  @Before
  public void setUp() throws Exception {
    reset(dataManagerMock);

    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .build();
  }

  @Test
  public void displayConvertedFileDownloadPageNoCookieTest() throws Exception {
    mockMvc.perform(get("/convertAndDownload"))
        .andExpect(
            model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
        .andExpect(status().isOk())
        .andExpect(view().name("error"))
        .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
    //.andDo(print());
  }

  @Test
  public void displayConvertedFileDownloadPageTest() throws Exception {

    when(dataManagerMock.lookupPersistedDataById("123456")).thenReturn(mock(Data.class));

    mockMvc.perform(get("/convertAndDownload")
        .cookie(new Cookie("rosetta", "123456"))
    )
        .andExpect(model().attribute("currentStep", equalTo("convertAndDownload")))
        .andExpect(view().name("wizard"))
        .andExpect(forwardedUrl("/WEB-INF/views/wizard.jsp"));
    //.andDo(print());
  }

  @Test
  public void processConvertedFileDownloadPageTest() throws Exception {

    // user clicks previous
    mockMvc.perform(post("/convertAndDownload")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .param("submit", "Previous")
    )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/generalMetadata"));
    //.andDo(print());

  }
}