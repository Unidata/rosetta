package edu.ucar.unidata.rosetta.repository.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UserDaoTest {

    private JdbcUserDao userDao;
    private User user1;
    private User user2;

    
    @Before
    public void setUp() throws Exception {
        userDao = mock(JdbcUserDao.class);

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

        when(userDao.getUsers()).thenReturn(Arrays.asList(user1, user2));
        when(userDao.lookupUser(123)).thenReturn(user1);
        when(userDao.lookupUser("userOne")).thenReturn(user1);
        when(userDao.lookupUserByEmailAddress("user2@foobar.baz")).thenReturn(user2);
        when(userDao.sameUser("userOne", "user1@foobar.baz")).thenReturn(true);
        when(userDao.userExists("emailAddress", "user1@foobar.baz")).thenReturn(true);
        doThrow(new RosettaUserException("Unable to find user"));
    }

    @Test
    public void createUserTest() throws Exception {
        userDao.createUser(user1);
        User user = userDao.lookupUser(123);
        assertEquals(user.getEmailAddress(), "user1@foobar.baz");
    }

    @Test(expected = RosettaUserException.class)
    public void deleteUserTest() throws Exception {
        userDao.deleteUser("user1@foo.bar.baz");
        userDao.lookupUserByEmailAddress("user1@foo.bar.baz");
    }

    @Test
    public void getUsersTest() throws Exception {
        List<User> users = userDao.getUsers();
        assertTrue(users.size() == 2);
    }

    @Test
    public void lookupUserByIdTest() throws Exception {
        User user = userDao.lookupUser(123);
        assertEquals(user, user1);
    }

    @Test
    public void lookupUserByUserNameTest() throws Exception {
        User user = userDao.lookupUser("userOne");
        assertEquals(user, user1);
    }

    @Test
    public void lookupUserByEmailAddress() throws Exception {
        User user = userDao.lookupUserByEmailAddress("user2@foobar.baz");
        assertEquals(user, user2);
    }

    @Test
    public void mockCreationTest() throws Exception {
        assertNotNull(userDao);
        assertNotNull(user1);
        assertNotNull(user2);
        assertNotNull(userDao);
    }

    @Test
    public void sameUserTest() throws Exception {
        boolean sameUser = userDao.sameUser("userOne", "user1@foobar.baz");
        assertTrue(sameUser);
    }

    @Test
    public void updatePasswordTest() throws Exception {
        user1.setPassword("newPassword"); // Update password.
        userDao.updatePassword(user1);
        User user = userDao.lookupUser(123);
        assertEquals(user.getPassword(), "newPassword");
    }

    @Test
    public void updateUserTest() throws Exception {
        user1.setEmailAddress("user1@bar.com");// Update email address.
        userDao.updateUser(user1);
        User user = userDao.lookupUser(123);
        assertEquals(user.getEmailAddress(), "user1@bar.com");
    }

    @Test
    public void userExistsTest() throws Exception {
        boolean sameUser = userDao.userExists("userOne", "user1@foobar.baz");
        assertTrue(sameUser);
    }

}