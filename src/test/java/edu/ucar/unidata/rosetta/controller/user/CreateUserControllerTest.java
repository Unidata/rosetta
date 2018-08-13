package edu.ucar.unidata.rosetta.controller.user;

import static org.hamcrest.Matchers.equalTo;
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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Ignore("enable once user managment is working")
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {WebAppContext.class})
public class CreateUserControllerTest {

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
  public void createUserTest() throws Exception {

    mockMvc.perform(get("/user/create"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("action", equalTo("createUser")))
        .andExpect(view().name("user"))
        .andExpect(forwardedUrl("/WEB-INF/views/user.jsp"));
    //.andDo(print());

  }

  @Test
  public void processUserCreationTest() throws Exception {

    User testUserOne = new User();
    testUserOne.setUserName("testUserOne");

    when(userManagerMock.createUser(org.mockito.ArgumentMatchers.isA(User.class)))
        .thenReturn(testUserOne);

    mockMvc.perform(post("/user/create"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/user/view/testUserOne"));
    //.andDo(print());

  }

  @Test
  public void registerTest() throws Exception {
    mockMvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("action", equalTo("register")))
        .andExpect(view().name("user"))
        .andExpect(forwardedUrl("/WEB-INF/views/user.jsp"));
    //.andDo(print());

  }

  @Test
  public void processRegistrationTest() throws Exception {
    mockMvc.perform(post("/register"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/login"));
    //.andDo(print());
  }
}