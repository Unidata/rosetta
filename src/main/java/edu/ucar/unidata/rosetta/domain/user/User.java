package edu.ucar.unidata.rosetta.domain.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * Object representing a User.
 *
 * A User is person with an account in the web app. User attributes, with the exception of the
 * confirmPassword attribute, correspond to database columns.
 */
public class User implements UserDetails, Serializable {

  private int userId;
  private String userName;
  private String fullName;
  private String password;
  private String confirmPassword;
  private int accessLevel = 1;   // default access level is user (not admin).
  private int accountStatus = 1; // default account status is enabled.
  private String emailAddress;
  private Date dateCreated;
  private Date dateModified;

  /**
   * Returns the userId of the User (immutable/unique to each User).
   *
   * @return The userId.
   */
  public int getUserId() {
    return userId;
  }

  /**
   * Sets the userId of the User (immutable/unique to each User).
   *
   * @param userId The userId.
   */
  public void setUserId(int userId) {
    this.userId = userId;
  }

  /**
   * Returns the username of the User (immutable/unique to each User).
   *
   * @return The User's username.
   */
  public String getUserName() {
    return userName;
  }

  /**
   * Sets the username of the User (immutable/unique to each User).
   *
   * @param userName The User's username.
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  /**
   * Returns the User's password.
   *
   * @return The User's password.
   */
  @Override
  public String getPassword() {
    return password;
  }

  /**
   * Sets the User's password.
   *
   * @param password The User's password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns the User's confirmation of the password.
   *
   * @return The User's confirmation of the password.
   */
  public String getConfirmPassword() {
    return confirmPassword;
  }

  /**
   * Sets the User's confirmation of the password.
   *
   * @param confirmPassword The Users's confirmation of the password.
   */
  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  /**
   * Returns the access level of the User.
   *
   * @return The User's access level.
   */
  public int getAccessLevel() {
    return accessLevel;
  }

  /**
   * Sets the access level of the User.
   *
   * @param accessLevel The User's access level.
   */
  public void setAccessLevel(int accessLevel) {
    this.accessLevel = accessLevel;
  }

  /**
   * Returns the status of the User's account.
   *
   * @return The status of the User's account.
   */
  public int getAccountStatus() {
    return accountStatus;
  }

  /**
   * Sets the status of the User's account.
   *
   * @param accountStatus The status of the User's account.
   */
  public void setAccountStatus(int accountStatus) {
    this.accountStatus = accountStatus;
  }


  /**
   * Returns the email address of the User.
   *
   * @return The User's email address.
   */
  public String getEmailAddress() {
    return emailAddress;
  }

  /**
   * Sets the email address of the User.
   *
   * @param emailAddress The User's email address.
   */
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  /**
   * Returns the full name of the User.
   *
   * @return The User's full name.
   */
  public String getFullName() {
    return fullName;
  }

  /**
   * Sets the full name of the User.
   *
   * @param fullName The User's full name.
   */
  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  /**
   * Returns date the User was created.
   *
   * @return The User's creation date.
   */
  public Date getDateCreated() {
    return dateCreated;
  }

  /**
   * Sets the date the User was created.
   *
   * @param dateCreated The User's creation date.
   */
  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  /**
   * Returns date the User's account was last modified.
   *
   * @return The User account last modified date.
   */
  public Date getDateModified() {
    return dateModified;
  }

  /**
   * Sets the date the User's account was last modified.
   *
   * @param dateModified The User account last modified date.
   */
  public void setDateModified(Date dateModified) {
    this.dateModified = dateModified;
  }

  /**
   * Indicates whether the user account is enabled or disabled.
   *
   * @return True if user account is enabled; otherwise false.
   */
  @Override
  public boolean isEnabled() {
    if (getAccountStatus() > 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Indicates whether the user's credentials (password) have expired. Always true (credential
   * expiration not currently supported.)
   *
   * @return true
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  /**
   * Indicates whether the user is locked or unlocked. Always true (Account locking not supported at
   * this time.)  See isEnabled().
   *
   * @return true
   */
  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  /**
   * Indicates whether the user's account has expired. Always true (Account expiration not supported
   * at this time.)  See isEnabled().
   *
   * @return true
   */
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  /**
   * Returns the username used to authenticate the user.
   *
   * @return The username.
   */
  @Override
  public String getUsername() {
    return getUserName();
  }

  /**
   * Returns the authorities or roles granted to the user.
   *
   * @return The list of roles for the user.
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
    // User access
    if (getAccessLevel() == 1) {
      roles.add(new SimpleGrantedAuthority("ROLE_USER"));
    }
    // Admin access
    if (getAccessLevel() == 2) {
      roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
    return roles;
  }

  /**
   * Returns a string representation of this object.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}