package edu.ucar.unidata.rosetta.service.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;
import edu.ucar.unidata.rosetta.repository.user.UserDao;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class UserManagerTest {

  private UserManagerImpl userManager;
  private UserDao userDao;
  private User user1;
  private User user2;

  @Before
  public void setUp() throws Exception {
    userManager = mock(UserManagerImpl.class);
    userDao = mock(UserDao.class);
    userManager.setUserDao(userDao);

    user1 = new User();
    user1.setUserId(123);
    user1.setEmailAddress("user1@foobar.baz");
    user1.setUserName("userOne");
    user1.setPassword("password1");

    user2 = new User();
    user2.setUserId(456);
    user2.setEmailAddress("user2@foobar.baz");
    user2.setUserName("userTwo");
    user2.setPassword("password2");

    when(userManager.getUsers()).thenReturn(Arrays.asList(user1, user2));
    when(userManager.lookupUser(123)).thenReturn(user1);
    when(userManager.lookupUser("userOne")).thenReturn(user1);
    when(userManager.lookupUserByEmailAddress("user2@foobar.baz")).thenReturn(user2);
    when(userManager.sameUser("userOne", "user1@foobar.baz")).thenReturn(true);
    when(userManager.userExists("emailAddress", "user1@foobar.baz")).thenReturn(true);
    doThrow(new RosettaUserException("Unable to find user"));

  }

  @Test
  public void createUserTest() throws Exception {
    userManager.createUser(user1);
    User user = userManager.lookupUser(123);
    assertEquals(user.getEmailAddress(), "user1@foobar.baz");
  }

  @Test(expected = RosettaUserException.class)
  public void deleteUserTest() throws Exception {
    userManager.deleteUser("user1@foo.bar.baz");
    userManager.lookupUserByEmailAddress("user1@foo.bar.baz");
  }

  @Test
  public void getUsersTest() throws Exception {
    List<User> users = userManager.getUsers();
    assertTrue(users.size() == 2);
  }

  @Test
  public void lookupUserByIdTest() throws Exception {
    User user = userManager.lookupUser(123);
    assertEquals(user, user1);
  }

  @Test
  public void lookupUserByUserNameTest() throws Exception {
    User user = userManager.lookupUser("userOne");
    assertEquals(user, user1);
  }

  @Test
  public void lookupUserByEmailAddress() throws Exception {
    User user = userManager.lookupUserByEmailAddress("user2@foobar.baz");
    assertEquals(user, user2);
  }

  @Test
  public void mockCreationTest() throws Exception {
    assertNotNull(userManager);
    assertNotNull(user1);
    assertNotNull(user2);
    assertNotNull(userDao);
  }

  @Test
  public void sameUserTest() throws Exception {
    boolean sameUser = userManager.sameUser("userOne", "user1@foobar.baz");
    assertTrue(sameUser);
  }

  @Test
  public void updatePasswordTest() throws Exception {
    user1.setPassword("newPassword"); // Update password.
    userManager.updatePassword(user1);
    User user = userManager.lookupUser(123);
    assertEquals(user.getPassword(), "newPassword");
  }

  @Test
  public void updateUserTest() throws Exception {
    user1.setEmailAddress("user1@bar.com");// Update email address.
    userManager.updateUser(user1);
    User user = userManager.lookupUser(123);
    assertEquals(user.getEmailAddress(), "user1@bar.com");
  }

  @Test
  public void userExistsTest() throws Exception {
    boolean sameUser = userManager.userExists("userOne", "user1@foobar.baz");
    assertTrue(sameUser);
  }

}