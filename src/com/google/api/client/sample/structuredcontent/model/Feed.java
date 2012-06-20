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

import java.util.ArrayList;
import java.util.List;


/**
 * Class for representing an XML feed.
 * 
 * @author Birgit Vera Schmidt
 * 
 * @param <T> The class of the entries contained in the feed.
 */
public abstract class Feed<T extends Entry> {
  @Key("link")
  public List<Link> links = new ArrayList<Link>();

  @Key
  public String id;

  @Key
  public String updated;

  /**
   * @return the list of entries.
   */
  public abstract List<T> getEntries();
}
