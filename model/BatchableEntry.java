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
 * Class for representing an entry in a feed that can handle batching.
 *
 * @author Birgit Vera Schmidt
 */
public class BatchableEntry extends Entry {
  @Key("batch:operation")
  public BatchOperation batchOperation = null;
  
  @Key("batch:id")
  public String batchID = null;
  
  @Key("batch:status")
  public BatchStatus batchStatus = null;
  
  @Key("batch:interrupted")
  public BatchInterrupted batchInterrupted;

  /**
   * Class for representing the batch operation type.
   */
  public static class BatchOperation {
    @Key("@type")
    public String type;
  }
  
  /**
   * Class for representing the batch status.
   */
  public static class BatchStatus {
    @Key("@code")
    public int code;

    @Key("@reason")
    public String reason;
  }
  
  /**
   * Simple inner class for representing a {@code <batch:interrupted>} entry.
   */
  public static class BatchInterrupted {
    @Key ("@error")
    public int error;

    @Key ("@parsed")
    public int parsed;

    @Key ("@reason")
    public String reason;

    @Key ("@success")
    public int success;

    @Key ("@unprocessed")
    public int unprocessed;
  }
}
