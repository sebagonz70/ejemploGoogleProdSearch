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

import com.google.api.client.util.Key;

import java.util.List;

/**
 * Class for representing the list of service errors returned by the
 * Structured Content API Server.
 * 
 * @author Birgit Vera Schmidt
 */
public class ServiceErrors extends Exception {
  @Key("gd:error")
  public List<ServiceError> errors;
  
  /**
   * Returns a formatted string for displaying all encountered service
   * errors.
   */
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer("Service Errors:\n");
    if (errors != null) {
      for (ServiceError s : errors) {
        if (s != null) {
          result.append(s.toString());
        } else {
          result.append("[unrecognized error syntax]");
        }
      }
    } else {
      result.append("no errors");
    }
    return result.toString();
  }
}
