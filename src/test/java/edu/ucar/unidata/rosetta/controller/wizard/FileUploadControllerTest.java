package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.config.WebAppContext;
import edu.ucar.unidata.rosetta.service.wizard.DataManager;
import org.junit.Before;
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

import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppContext.class})
public class FileUploadControllerTest {

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


}