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

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Class for retrieving debug information produced by the {@link Logger} for
 * {@link com.google.api.client.http.HttpTransport} and printing it to a given
 * output stream.
 *
 * @author Birgit Vera Schmidt
 */
public class DebugHelper {
  /**
   * @param out The PrintStream where the debug output should be written to.
   */
  public static void startDebugging(PrintStream out, Class<?> clazz) {
    Logger temp = Logger.getLogger(clazz.getName());
    temp.setLevel(Level.ALL);
    temp.addHandler(new PrintingHandler(out));
  }

  private static class PrintingHandler extends Handler {
    private final PrintStream out;

    public PrintingHandler(PrintStream out) {
      this.out = out;
    }

    @Override
    public void close() throws SecurityException {
    }

    @Override
    public void flush() {
    }

    @Override
    public void publish(LogRecord record) {
      out.println("DEBUG OUTPUT: " + record.getMessage());
    }
  }
}
