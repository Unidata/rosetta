package edu.ucar.unidata.rosetta.controller.user;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.doNothing;
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
import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.service.user.UserManager;
import java.util.Arrays;
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
public class DeleteUserControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private UserManager userManagerMock;

  @Autowired
  private WebApplicationContext context;

  @Before
  public void setUp() throws Exception {
    reset(userManagerMock);

    mockMvc = MockMvcBuilders
        .webAppContextSetup(context)
        .build();
  }

  @Test
  public void deleteUserTest() throws Exception {

    User testUserOne = new User();
    testUserOne.setUserName("testUserOne");
    testUserOne.setEmailAddress("user1@foo.bar");

    when(userManagerMock.lookupUserByEmailAddress("user1@foo.bar")).thenReturn(testUserOne);

    mockMvc.perform(get("/user/delete/testUserOne"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("action", equalTo("deleteUser")))
        .andExpect(view().name("user"))
        .andExpect(forwardedUrl("/WEB-INF/views/user.jsp"));
    //.andDo(print());
  }

  @Test
  public void processUserDeletionTest() throws Exception {

    User testUserOne = new User();
    testUserOne.setUserName("testUserOne");
    testUserOne.setEmailAddress("user1@foo.bar");

    User testUserTwo = new User();
    testUserOne.setUserName("testUserTwo");
    testUserOne.setEmailAddress("user2@foo.bar");

    when(userManagerMock.getUsers()).thenReturn(Arrays.asList(testUserOne, testUserTwo));
    doNothing().when(userManagerMock).deleteUser(testUserOne.getEmailAddress());

    mockMvc.perform(post("/user/delete/testUserOne"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("action", equalTo("listUser")))
        .andExpect(redirectedUrl("/user"));
    //.andDo(print());

  }
}