package edu.ucar.unidata.rosetta.service.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;
import edu.ucar.unidata.rosetta.repository.user.UserDao;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Implements DataManager functionality.
 *
 * @author oxelson@ucar.edu
 */
public class UserManagerImpl implements UserManager {

    protected static Logger logger = Logger.getLogger(UserManagerImpl.class);

    private UserDao userDao;

    /**
     * Creates and saves a new user.
     *
     * @param user  The user to be created.
     * @return  The saved user.
     * @throws RosettaUserException  If unable to create the user.
     */
    public User createUser(User user) throws RosettaUserException {
        String password = new BCryptPasswordEncoder().encode(user.getPassword());
        user.setPassword(password);
        Date now = new Date(System.currentTimeMillis());
        user.setDateCreated(now);
        user.setDateModified(now);

        try {
            return userDao.createUser(user);
        } catch (DataAccessException e) {
            throw new RosettaUserException("Unable to create user with email address '" + user.getEmailAddress() + "' " + e);
        }
    }

    /**
     * Finds and removes the user with the given email address.
     *
     * @param emailAddress The email address of the user to locate (will be unique for each user).
     * @throws RosettaUserException  If unable to find and delete the user.
     */
    public void deleteUser(String emailAddress) throws RosettaUserException {
        try {
            userDao.deleteUser(emailAddress);
        } catch (DataAccessException e) {
            throw new RosettaUserException("Unable to delete user with email address '" + emailAddress + "' " + e);
        }
    }

    /**
     * Requests a list of all available users.
     *
     * @return  A list of users.
     */
    public List<User> getUsers() {
        return userDao.getUsers();
    }

    /**
     * Looks up and retrieves a user with the given user ID.
     *
     * @param userId  The ID of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws RosettaUserException If unable to find the user with the given user ID.
     */
    public User lookupUser(int userId) throws RosettaUserException {
        try {
            return userDao.lookupUser(userId);
        } catch (DataAccessException e) {
            throw new RosettaUserException("Unable to locate user with ID '" + userId + "' " + e);
        }
    }

    /**
     * Looks up and retrieves a user with the given user name.
     *
     * @param userName  The user name of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws RosettaUserException If unable to find the user with the given user name.
     */
    public User lookupUser(String userName) throws RosettaUserException {
        try {
            return userDao.lookupUser(userName);
        } catch (DataAccessException e) {
            throw new RosettaUserException("Unable to locate user with user name '" + userName + "' " + e);
        }
    }

    /**
     * Looks up and retrieves a user with the given email address.
     *
     * @param emailAddress The email address of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws RosettaUserException If unable to find the user with the given email address.
     */
    public User lookupUserByEmailAddress(String emailAddress) throws RosettaUserException {
        try {
            return userDao.lookupUserByEmailAddress(emailAddress);
        } catch (DataAccessException e) {
            throw new RosettaUserException("Unable to locate user with email address '" + emailAddress + "' " + e);
        }
    }

    /**
     * Sets the data access object which will acquire and persist the data
     * passed to it via the methods of this UserManager.
     *
     * @param userDao  The service mechanism data access object representing a user.
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Updates the user's password.
     *
     * @param user  The user whose password needs to be update.
     */
    public void updatePassword(User user) throws RosettaUserException {
        String password = new BCryptPasswordEncoder().encode(user.getPassword());
        user.setPassword(password);
        Date now = new Date(System.currentTimeMillis());
        user.setDateModified(now);
        try {
            userDao.updatePassword(user);
        } catch (DataAccessException e) {
            throw new RosettaUserException("Unable to update password for user with email address '" + user.getEmailAddress() + "' " + e);
        }
    }

    /**
     * Saves changes made to an existing user.
     *
     * @param user   The existing user with changes that needs to be saved.
     * @return  The updated user.
     * @throws RosettaUserException  If unable to update the persisted user.
     */
    public User updateUser(User user) throws RosettaUserException {
        Date now = new Date(System.currentTimeMillis());
        user.setDateModified(now);
        try {
            return userDao.updateUser(user);
        } catch (DataAccessException e) {
            throw new RosettaUserException("Unable to update user with ID '" + user.getUserId() + "' " + e);
        }
    }
}
