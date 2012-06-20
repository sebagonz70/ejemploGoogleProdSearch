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

package com.google.api.client.sample.structuredcontent.model;

/**
 * Exception to be thrown when the service was not reachable at all.
 *
 * @author Birgit Vera Schmidt
 */
public class ServiceNotReachableError extends ServiceErrors {
  private int errorCode;
  
  /**
   * Simple constructor for setting the error code.
   *
   * @param errorCode The HTTP error code returned by the server.
   */
  public ServiceNotReachableError(int errorCode) {
    this.errorCode = errorCode;
  }
  
  @Override
  public String toString() {
    return String.valueOf("Service not reachable: " + errorCode);
  }
}
