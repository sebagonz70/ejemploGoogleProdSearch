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
 * Class for representing a datafeed.
 *
 * @author Birgit Vera Schmidt
 */
public class DataFeed extends Entry {
  @Key("sc:feed_file_name")
  public String feedFileName;

  @Key("sc:target_country")
  public String targetCountry;

  @Key("sc:file_format")
  public FileFormat fileFormat;

  /**
   * Inner class for representing a file format.
   */
  public static class FileFormat {
    @Key("@format")
    public String format;

    @Key("sc:delimiter")
    public String delimiter;

    @Key("sc:encoding")
    public String encoding;

    @Key("sc:use_quoted_fields")
    public String useQuotedFields;

  }
}
