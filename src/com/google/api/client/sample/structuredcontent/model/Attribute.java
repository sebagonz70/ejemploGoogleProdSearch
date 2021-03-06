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

/**
 * Class for representing the information in an {@code sc:attribute} XML element.
 * 
 * @author Birgit Vera Schmidt
 */
public class Attribute {
  @Key("@name")
  public String name;

  @Key("@type")
  public String type;

  @Key("@unit")
  public String unit;

  @Key("text()")
  public String value;
}
