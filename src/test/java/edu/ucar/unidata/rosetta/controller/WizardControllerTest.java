package edu.ucar.unidata.rosetta.controller;

import edu.ucar.unidata.rosetta.config.WebAppContext;
import edu.ucar.unidata.rosetta.domain.Data;
import edu.ucar.unidata.rosetta.domain.GeneralMetadata;
import edu.ucar.unidata.rosetta.service.DataManager;

import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppContext.class})
public class WizardControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private DataManager dataManagerMock;

    @Autowired
    private WebApplicationContext context;

    @Test
    public void displayCFTypeSelectionFormTest() throws Exception {

        mockMvc.perform(get("/cfType"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentStep", equalTo("cfType")))
                .andExpect(view().name("wizard"))
                .andExpect(forwardedUrl("/WEB-INF/views/wizard.jsp"));
        //.andDo(print());
    }

    @Test
    public void displayConvertedFileDownloadPageNoCookieTest() throws Exception {
        mockMvc.perform(get("/convertAndDownload"))
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
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
    public void displayCustomFileTypeAttributesFormNoCookieTest() throws Exception {

        mockMvc.perform(get("/customFileTypeAttributes"))
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void displayCustomFileTypeAttributesFormTest() throws Exception {

        when(dataManagerMock.lookupPersistedDataById("123456")).thenReturn(mock(Data.class));

        mockMvc.perform(get("/customFileTypeAttributes")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(model().attribute("currentStep", equalTo("customFileTypeAttributes")))
                .andExpect(view().name("wizard"))
                .andExpect(forwardedUrl("/WEB-INF/views/wizard.jsp"));
        //.andDo(print());
    }

    @Test
    public void displayFileUploadFormNoCookieTest() throws Exception {
        mockMvc.perform(get("/fileUpload"))
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void displayFileUploadFormTest() throws Exception {
        mockMvc.perform(get("/fileUpload")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(model().attribute("currentStep", equalTo("fileUpload")))
                .andExpect(view().name("wizard"))
                .andExpect(forwardedUrl("/WEB-INF/views/wizard.jsp"));
        //.andDo(print());
    }

    @Test
    public void displayGeneralMetadataFormNoCookieTest() throws Exception {

        mockMvc.perform(get("/generalMetadata"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void displayGeneralMetadataFormTest() throws Exception {

        when(dataManagerMock.lookupPersistedDataById("123456")).thenReturn(mock(Data.class));
        when(dataManagerMock.getUploadDir()).thenReturn("/dev/null");
        when(dataManagerMock.getMetadataFromKnownFile("/dev/null", "eTuff", mock(GeneralMetadata.class))).thenReturn(mock(GeneralMetadata.class));

        mockMvc.perform(get("/generalMetadata")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(model().attribute("currentStep", equalTo("generalMetadata")))
                .andExpect(view().name("wizard"))
                .andExpect(forwardedUrl("/WEB-INF/views/wizard.jsp"));
        //.andDo(print());

    }

    @Test
    public void displayVariableMetadataFormNoCookieTest() throws Exception {

        mockMvc.perform(get("/variableMetadata"))
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void displayVariableMetadataFormTest() throws Exception {

        when(dataManagerMock.lookupPersistedDataById("123456")).thenReturn(mock(Data.class));

        mockMvc.perform(get("/variableMetadata")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(model().attribute("currentStep", equalTo("variableMetadata")))
                .andExpect(view().name("wizard"))
                .andExpect(forwardedUrl("/WEB-INF/views/wizard.jsp"));
        //.andDo(print());

    }

    @Test
    public void processCFTypeTest() throws Exception {
        mockMvc.perform(post("/cfType"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/fileUpload"));
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

    @Test
    public void processCustomFileTypeAttributesNoCookieTest() throws Exception {

        mockMvc.perform(post("/customFileTypeAttributes")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "next")
        )
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void processCustomFileTypeAttributesSubmitNextTest() throws Exception {

        mockMvc.perform(post("/customFileTypeAttributes")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Next")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/variableMetadata"));
        //.andDo(print());
    }

    @Test
    public void processCustomFileTypeAttributesSubmitPreviousTest() throws Exception {

        mockMvc.perform(post("/customFileTypeAttributes")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Previous")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/fileUpload"));
        //.andDo(print());

    }

    @Test
    public void processFileUploadNoCookieTest() throws Exception {

        mockMvc.perform(post("/fileUpload")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Next")
        )
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void processFileUploadSubmitNextCustomDataFileTypeTest() throws Exception {

        when(dataManagerMock.processNextStep("123456")).thenReturn("/customFileTypeAttributes");

        mockMvc.perform(post("/fileUpload")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Next")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customFileTypeAttributes"));
        // .andDo(print());

    }

    @Test
    public void processFileUploadSubmitNextKnownDataFileTypeTest() throws Exception {

        when(dataManagerMock.processNextStep("123456")).thenReturn("/generalMetadata");

        mockMvc.perform(post("/fileUpload")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Next")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/generalMetadata"));
        // .andDo(print());

    }

    @Test
    public void processFileUploadSubmitPreviousTest() throws Exception {

        mockMvc.perform(post("/fileUpload")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Previous")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cfType"));
        //.andDo(print());
    }

    @Test
    public void processGeneralMetadataNoCookieTest() throws Exception {

        // user clicks next
        mockMvc.perform(post("/generalMetadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "next")
        )
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void processGeneralMetadataSubmitNextTest() throws Exception {

        mockMvc.perform(post("/generalMetadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "next")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/convertAndDownload"));
        //.andDo(print());
    }

    @Test
    public void processGeneralMetadataSubmitPreviousCustomDataFileTypeTest() throws Exception {

        when(dataManagerMock.processPreviousStep("123456")).thenReturn("/variableMetadata");

        mockMvc.perform(post("/generalMetadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Previous")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/variableMetadata"));
                //.andDo(print());
    }

    @Test
    public void processGeneralMetadataSubmitPreviousKnownDataFileTypeTest() throws Exception {

        when(dataManagerMock.processPreviousStep("123456")).thenReturn("/fileUpload");

        mockMvc.perform(post("/generalMetadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Previous")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/fileUpload"));
        //.andDo(print());
    }

    @Test
    public void processVariableMetadataNoCookieTest() throws Exception {

        mockMvc.perform(post("/variableMetadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "next")
        )
                .andExpect(model().attribute("exception", org.hamcrest.Matchers.isA(IllegalStateException.class)))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(forwardedUrl("/WEB-INF/views/error.jsp"));
        //.andDo(print());
    }

    @Test
    public void processVariableMetadataSubmitNextTest() throws Exception {

        // user clicks previous
        mockMvc.perform(post("/variableMetadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Next")
                .cookie(new Cookie("rosetta", "123456"))
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/generalMetadata"));
        //.andDo(print());
    }

    @Test
    public void processVariableMetadataSubmitPreviousTest() throws Exception {

        mockMvc.perform(post("/variableMetadata")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("submit", "Previous")
        )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customFileTypeAttributes"));
        //.andDo(print());

    }

    @Before
    public void setUp() throws Exception {
        reset(dataManagerMock);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }
}