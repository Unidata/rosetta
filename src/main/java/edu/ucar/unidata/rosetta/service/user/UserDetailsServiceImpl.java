package edu.ucar.unidata.rosetta.service.user;

import edu.ucar.unidata.rosetta.domain.user.User;
import edu.ucar.unidata.rosetta.repository.user.UserDao;

import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


/**
 * Service for processing User objects with respect to authentication and access control.
 *
 * @author oxelson@ucar.edu
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserDao userDao;

    /**
     * Sets the data access object.
     *
     * @param userDao  The service mechanism data access object representing a user.
     */
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Retrieves a User record containing the User's credentials and access.
     *
     * @param userName  The user name of the authenticating user.
     * @return  The Spring UserDetails.
     * @throws UsernameNotFoundException  If unable to find the persisted user.
     */
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = null;
        try {
            user = userDao.lookupUser(userName);
        } catch (DataAccessException e) {
            throw new UsernameNotFoundException("No user found with user name '" + userName +"'" + e);
        }
        return user;
    }
}
