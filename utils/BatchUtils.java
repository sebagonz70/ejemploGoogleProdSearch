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

import com.google.api.client.sample.structuredcontent.model.BatchableEntry;

/**
 * Utility methods for entries that are part of a batch response.
 *
 * @author Alex Dovlecel
 */
public class BatchUtils {
  public static final String INSERT = "insert";
  public static final String UPDATE = "update";
  public static final String DELETE = "delete";
  
  /**
   * Configure the batch operation for the {@code entry} to insert.
   */
  public static <T extends BatchableEntry> T configureForInsert(T entry,
      String batchId) {
    return configureEntry(entry, INSERT, batchId);
  }

  /**
   * Configure the batch operation for the {@code entry} to update.
   */
  public static <T extends BatchableEntry> T configureForUpdate(T entry,
      String batchId) {
    return configureEntry(entry, UPDATE, batchId);
  }

  /**
   * Configure the batch operation for the {@code entry} to delete.
   */
  public static <T extends BatchableEntry> T configureForDelete(T entry,
      String batchId) {
    return configureEntry(entry, DELETE, batchId);
  }

  private static <T extends BatchableEntry> T configureEntry(T entry,
      String operationType,
      String batchId) {
    entry.batchOperation = new BatchableEntry.BatchOperation();
    entry.batchOperation.type = operationType;
    if (batchId != null) {
      entry.batchID = batchId;
    }
    return entry;
  }
}
