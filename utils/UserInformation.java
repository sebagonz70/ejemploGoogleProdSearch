/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.sample.structuredcontent.utils;

/**
 * Class for storing user specific information.
 *
 * @author Birgit Vera Schmidt
 */
public class UserInformation {
  private final String uid;
  private final String homepage;
  private final ClientLoginCredentials clientLoginCredentials;

  /**
   * Creates a new {@code UserInformation} instance with the given user ID,
   * but without user credentials.
   *
   * @param uid user ID
   * @param homepage the user's registered homepage
   * @throws IllegalArgumentException if the given user ID is {@code null}
   */
  public UserInformation(String uid, String homepage) throws IllegalArgumentException {
    if (uid == null) {
      throw new IllegalArgumentException("uid must not be null");
    }
    this.uid = uid;
    
    this.homepage = homepage;

    this.clientLoginCredentials = null;
  }

  /**
   * Creates a new {@code UserInformation} instance with the given user ID
   * and with user credentials (username and password) for ClientLogin.
   *
   * @param uid user ID
   * @param homepage the user's registered homepage
   * @param username Username for ClientLogin
   * @param password Password for ClientLogin
   * @throws IllegalArgumentException if one of the given arguments is {@code null}
   */
  public UserInformation(String uid, String homepage, String username, String password)
      throws IllegalArgumentException {
    if (uid == null) {
      throw new IllegalArgumentException("uid must not be null");
    }
    this.uid = uid;

    this.homepage = homepage;

    this.clientLoginCredentials = new ClientLoginCredentials(username, password);
  }
  
  /**
   * Returns the user ID.
   *
   * @return the user ID
   */
  public String getUid() {
    return uid;
  }
  
  /**
   * Returns the user's registered homepage.
   *
   * @return the user's registered homepage
   */
  public String getHomepage() {
    return homepage;
  }
  
  /**
   * Returns the user's credentials for ClientLogin, or {@code null} if no
   * credentials are known.
   *
   * @return the user's ClientLogin credentials if they are known, or {@code null}
   *   otherwise
   */
  public ClientLoginCredentials getClientLoginCredentials() {
    return clientLoginCredentials;
  }

  /**
   * User credentials relevant for ClientLogin.
   */
  public static class ClientLoginCredentials {
    private final String username;
    private final String password;
    
    /**
     * Sets username and password to the given values.
     *
     * @param username Username for ClientLogin
     * @param password Password for ClientLogin
     * @throws IllegalArgumentException if one of the given arguments is {@code null}
     */
    public ClientLoginCredentials(String username, String password)
        throws IllegalArgumentException {
      if (username == null) {
        throw new IllegalArgumentException("username must not be null");
      }
      this.username = username;

      if (password == null) {
        throw new IllegalArgumentException("password must not be null");
      }
      this.password = password;
    }
    
    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
      return username;
    }
    
    /**
     * Returns the password.
     *
     * @return the password
     */
    public String getPassword() {
      return password;
    }
  }
}
