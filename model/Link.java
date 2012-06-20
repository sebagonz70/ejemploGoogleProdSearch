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
 * Class for representing the information in an {@code atom:link} XML element.
 * 
 * @author Birgit Vera Schmidt
 */
public class Link {
  @Key("@href")
  public String href;

  @Key("@rel")
  public String rel;

  @Key("@type")
  public String type;

  /**
   * Finds a link with the given {@code rel} value in a list of links. Returns
   * null if no such link exists. Returns one of the links with the appropriate
   * {@code rel} value if there is more than one such link.
   * 
   * @param links A list of links.
   * @param rel The {@code rel} value to search for.
   * @return The {@code href} value of one of the links from the list with the
   *   given {@code rel} value if at least one such link is in the list,
   *   {@code null} otherwise.
   */
  public static String find(List<Link> links, String rel) {
    if (links != null) {
      for (Link link : links) {
        if (rel.equals(link.rel)) {
          return link.href;
        }
      }
    }
    return null;
  }
}
