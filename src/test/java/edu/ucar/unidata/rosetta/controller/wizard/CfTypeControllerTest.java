/*
 * Copyright (c) 2012-2020 University Corporation for Atmospheric Research/Unidata.
 * See LICENSE for license information.
 */

package edu.ucar.unidata.rosetta.controller.wizard;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import edu.ucar.unidata.rosetta.config.WebAppContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppContext.class})
public class CfTypeControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext context;

  @Before
  public void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
  }

  @Test
  public void displayCFTypeSelectionFormTest() throws Exception {
    mockMvc.perform(get("/cfType")).andExpect(status().isOk())
        .andExpect(model().attribute("currentStep", equalTo("cfType"))).andExpect(view().name("wizard"))
        .andExpect(forwardedUrl("/WEB-INF/views/wizard.jsp"));
    // .andDo(print());
  }

  @Test
  public void processCFTypeTest() throws Exception {
    mockMvc.perform(post("/cfType")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/fileUpload"));
    // .andDo(print());
  }
}
