package edu.ucar.unidata.rosetta.repository.user;

import edu.ucar.unidata.rosetta.domain.user.User;

import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * The data access object representing a user.
 * 
 * @author oxelson@ucar.edu
 */
public interface UserDao {

    /**
     * Persists a new user.
     *
     * @param user  The user to be created.
     * @return  The persisted user.
     * @throws DataAccessException  If unable to persists the user.
     */
    public User createUser(User user) throws DataAccessException;

    /**
     * Finds and removes the persisted user with the given email address.
     *
     * @param emailAddress The email address of the user to locate (will be unique for each user).
     * @throws DataAccessException  If unable to find and delete the user.
     */
    public void deleteUser(String emailAddress) throws DataAccessException;

    /**
     * Requests a list of all persisted users.
     *
     * @return  A list of users.
     */
    public List<User> getUsers();
    /**
     * Looks up and retrieves a persisted user with the given user ID.
     *
     * @param userId  The ID of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws DataAccessException If unable to find the user with the given user ID.
     */
    public User lookupUser(int userId) throws DataAccessException;

    /**
     * Looks up and retrieves a persisted user with the given user name.
     *
     * @param userName  The user name of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws DataAccessException If unable to find the user with the given user name.
     */
    public User lookupUser(String userName) throws DataAccessException;

    /**
     * Looks up and retrieves a persisted user with the given email address.
     *
     * @param emailAddress The email address of the user to locate (will be unique for each user).
     * @return  The user.
     * @throws DataAccessException If unable to find the user with the given email address.
     */
    public User lookupUserByEmailAddress(String emailAddress) throws DataAccessException;

    /**
     * Updates the persisted user's password .
     *
     * @param user  The user whose password needs to be update.
     */
    public void updatePassword(User user) throws DataAccessException;

    /**
     * Saves changes made to an existing persisted User.
     *
     * @param user   The existing user with changes that needs to be saved.
     * @return  The updated user.
     * @throws DataAccessException  If unable to update the persisted user.
     */
    public User updateUser(User user) throws DataAccessException;
}