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
 * Class for representing a managed account feed.
 *
 * @author Birgit Vera Schmidt
 */
public class ManagedAccountFeed extends Feed<ManagedAccount> {
  @Key("entry")
  public List<ManagedAccount> entries = new ArrayList<ManagedAccount>();

  @Override
  public List<ManagedAccount> getEntries() {
    return entries;
  }
}
