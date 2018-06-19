package edu.ucar.unidata.rosetta.controller.wizard;

import edu.ucar.unidata.rosetta.config.WebAppContext;
import edu.ucar.unidata.rosetta.domain.Data;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppContext.class})
public class CustomFileTypeAttributesControllerTest {

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

}