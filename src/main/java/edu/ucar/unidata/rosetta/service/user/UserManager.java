package edu.ucar.unidata.rosetta.service.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.exceptions.RosettaUserException;

import java.util.List;

/**
 * Service for handling collected user information.
 *
 * @author oxelson@ucar.edu
 */
public interface UserManager {

    /**
     * Creates and saves a new user.
     *
     * @param user  The user to be created.
     * @return  The saved user.
     * @throws RosettaUserException  If unable to create the user.
     */
    public User createUser(User user) throws RosettaUserException;

    /**
     * Finds and removes the user with the given email address.
     *
     * @param emailAddress The email address of the user to locate (will be unique for each user).
     * @throws RosettaUserException  If unable to find and delete the user.
     */
    public void deleteUser(String emailAddress) throws RosettaUserException;

    /**
     * Requests a list of all available users.
     *
     * @return  A list of users.
     */
    public List<User> getUsers();
    /**
     * Looks up and retrieves a user with the given user ID.
     *
     * @param userId  The ID of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws RosettaUserException If unable to find the user with the given user ID.
     */
    public User lookupUser(int userId) throws RosettaUserException;

    /**
     * Looks up and retrieves a user with the given user name.
     *
     * @param userName  The user name of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws RosettaUserException If unable to find the user with the given user name.
     */
    public User lookupUser(String userName) throws RosettaUserException;

    /**
     * Looks up and retrieves a user with the given email address.
     *
     * @param emailAddress The email address of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws RosettaUserException If unable to find the user with the given email address.
     */
    public User lookupUserByEmailAddress(String emailAddress) throws RosettaUserException;

    /**
     * Updates the user's password.
     *
     * @param user  The user whose password needs to be update.
     */
    public void updatePassword(User user) throws RosettaUserException;

    /**
     * Saves changes made to an existing user.
     *
     * @param user   The existing user with changes that needs to be saved.
     * @return  The updated user.
     * @throws RosettaUserException  If unable to update the persisted user.
     */
    public User updateUser(User user) throws RosettaUserException;
}
