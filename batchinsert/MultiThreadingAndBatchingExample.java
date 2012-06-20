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

package com.google.api.client.sample.structuredcontent.batchinsert;

import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.sample.structuredcontent.batchinsert.CsvInputAdapter.ParsingError;
import com.google.api.client.sample.structuredcontent.model.ServiceError;
import com.google.api.client.sample.structuredcontent.model.ServiceErrors;
import com.google.api.client.sample.structuredcontent.utils.ClientLibraryUtils;
import com.google.api.client.sample.structuredcontent.utils.UserInformation;
import com.google.api.client.xml.XmlNamespaceDictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This class is intended to demonstrate the usage of the
 * <a href="http://code.google.com/p/google-api-java-client/">Google API Client Library for Java</a>
 * with the
 * <a href="http://code.google.com/apis/shopping/content">Content API for Shopping</a>
 * using batch processing and parallel threads. For more information, see
 * <a href="http://code.google.com/apis/shopping/content/developers_guide_java.html">here</a>.
 *
 * <p>The main function of the class takes a .csv file and a string to be
 * interpreted as a field delimiter, expecting the format described in
 * {@link CsvInputAdapter}.
 *
 * <p>The first line of the .csv file may be used for headers or left empty;
 * it will be ignored during the parsing. This is just for making the .csv file
 * human readable when opened in a spreadsheet program.
 *
 * <p>The .csv file should be encoded in UTF-8.
 *
 * <p>This example can be used "out of the box" for just submitting your data
 * using a spreadsheet. All you need to do is put the data into a spreadsheet
 * adhering to the described format and add your username, password and uid
 * to {@link UserInformation}. Then, start the program with the parameters
 * described in the documentation of the {@link #main(String[])} function.
 *
 * <p>Internally, the program will create a number of worker threads. Each thread
 * will accumulate products from the input file up to a certain number, and then
 * send all of these products as one batch.
 *
 * @author Birgit Vera Schmidt
 */
public class MultiThreadingAndBatchingExample {
  /**
   * Logger used for logging all messages produced by this class.
   */
  private static final Logger logger
      = Logger.getLogger(MultiThreadingAndBatchingExample.class.getName());

  /**
   * The root URL of the Content API for Shopping Server.
   */
  private final String rootUrl;

  /**
   * The XML namespace dictionary for sending requests and for parsing answers.
   */
  private final XmlNamespaceDictionary namespaceDictionary;

  /**
   * The HTTP request factory instance used for sending requests and receiving
   * responses.
   */
  private final HttpRequestFactory requestFactory;

  /**
   * The user's account ID.
   */
  private final String userId;

  /**
   * The user's homepage.
   */
  private final String homepage;

  /**
   * This function will create a number of worker threads as specified by the
   * arguments, then start all of them, wait for them to finish and report errors
   * that occurred. All worker threads will read from the same file, using an
   * instance of the (thread-safe) class {@link CsvInputAdapter} for accessing
   * it and parsing the products therein.
   *
   * @param args Exactly 4 arguments are expected in this order:
   *   <ol>
   *     <li>The file name of the .csv file to be parsed</li>
   *     <li>The string used as a separator in the .csv file</li>
   *     <li>The number of worker threads to be used</li>
   *     <li>The maximum number of products that can be sent in one batch
   *         request</li>
   *   </ol>
   * @throws IOException if anything went seriously wrong during input or output
   * @throws InterruptedException if anything went wrong with the multi-threading
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    // parse commandline arguments
    if (args.length != 4) {
      logger.log(Level.SEVERE, "Wrong number of arguments."
          + " Expected: file.csv <separator string> <number_of_worker_threads>"
          + " <max_products_in_batch>");
      System.exit(-1);
    }

    File file = new File(args[0]);

    String separator = args[1];

    int numberOfWorkerThreads = 0;
    try {
      numberOfWorkerThreads = Integer.parseInt(args[2]);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Number of worker threads could not be parsed.");
      System.exit(-1);
    }
    logger.info("Using " + numberOfWorkerThreads + " threads.");

    int maxProductsInBatch = 0;
    try {
      maxProductsInBatch = Integer.parseInt(args[3]);
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Maximum number of products in one batch could not be parsed.");
      System.exit(-1);
    }
    logger.info("Sending up to " + maxProductsInBatch + " in one batch.");

    // start the actual program
    UserInformation userInformation = new UserInformation("1234567",
        "http://my.supercool.com/homepage/",
        "your.username@gmail.com", "yourPassword");
    MultiThreadingAndBatchingExample sample
        = new MultiThreadingAndBatchingExample(
            userInformation.getUid(),
            userInformation.getHomepage(),
            "https://content.googleapis.com/content/v1/",
            createAuthorizedTransport(userInformation));
    sample.insertAllProductsFromFile(file, separator, numberOfWorkerThreads, maxProductsInBatch);
  }

  /**
   * Creates an authorized HTTP request factory that is ready
   * for sending requests to the Content API for Shopping Server.
   *
   * @param userInformation Information about the user
   * @return A new authenticated HTTP request factory instance
   * @throws IOException if something went wrong during authentication
   */
  private static HttpRequestFactory createAuthorizedTransport(UserInformation userInformation)
      throws IOException {
    logger.info("== Starting login and setup ==");
    HttpRequestFactory requestFactory = ClientLibraryUtils
        .createBatchingRequestFactory("google-structuredcontentbatchingsample-1.0",
            userInformation.getClientLoginCredentials().getUsername(),
            userInformation.getClientLoginCredentials().getPassword());
    logger.info("== Login and setup done ==");
    return requestFactory;
  }

  /**
   * Constructor for the class. Will initialize necessary internal variables,
   * and will set the class up to use the given HTTP request factory for all
   * requests. This transport is assumed to be already authenticated.
   *
   * @param userId The user's account ID
   * @param homepage the user's registered homepage
   * @param rootUrl URL of the Content API for Shopping Server
   * @param requestFactory The HTTP request factory to be used for all requests
   */
  public MultiThreadingAndBatchingExample(String userId, String homepage, String rootUrl,
      HttpRequestFactory requestFactory) {
    this.userId = userId;
    this.homepage = homepage;
    this.rootUrl = rootUrl;
    this.requestFactory = requestFactory;
    namespaceDictionary = ClientLibraryUtils.createBatchingNamespaceDictionary();
  }

  /**
   * Constructor for the class. Will initialize necessary internal variables,
   * and will set the class up to use the given HTTP request factory for all
   * requests. This transport is assumed to be already authenticated.
   *
   * @param userInformation information about the user
   * @param rootUrl URL of the Content API for Shopping Server
   * @param requestFactory The HTTP request factory to be used for all requests
   */
  public MultiThreadingAndBatchingExample(UserInformation userInformation, String rootUrl,
      HttpRequestFactory requestFactory) {
    this(userInformation.getUid(), userInformation.getHomepage(), rootUrl, requestFactory);
  }

  /**
   * Reads all products from the given file and send them in batches to the
   * Content API for Shopping Server, using the specified number of parallel worker threads.
   * Errors are reported using a {@link Logger}.
   *
   * @param file The .csv file containing the product information
   * @param separator The string used as a separator in the .csv file
   * @param numberOfWorkerThreads The number of worker threads to be used
   * @param maxProductsInBatch The maximum number of products that can be sent in one thread
   * @throws IOException if anything went seriously wrong during input or output
   * @throws InterruptedException if anything went wrong with the multi-threading
   */
  public void insertAllProductsFromFile(File file, String separator,
      int numberOfWorkerThreads, int maxProductsInBatch) throws IOException, InterruptedException {
    logger.info("== Preparing file reading and error reporting ==");

    // creating an InputAdapter that will read and translate our input file
    CsvInputAdapter inputAdapter = createCsvInputAdapter(file, separator, true);

    logger.info("== File reading and error reporting ready ==");

    // insert products using the created InputAdapter
    Collection<BatchError> serviceErrorList
        = insertAllProducts(inputAdapter, maxProductsInBatch, numberOfWorkerThreads);

    // output errors if applicable
    reportParsingErrors(inputAdapter.getParsingErrors());
    reportServiceErrors(serviceErrorList);
  }

  /**
   * Retrieves all products from the given {@link CsvInputAdapter} and sends them
   * in batches to the Content API for Shopping Server, using the specified number
   * of parallel worker threads. Errors are reported to the given error collecting
   * lists.
   *
   * @param inputAdapter The input adapter from which the products to be inserted
   *   are retrieved
   * @param numberOfWorkerThreads The number of worker threads to be used
   * @param maxProductsInBatch The maximum number of products that can be sent in one thread
   * @throws InterruptedException if anything went wrong with the multi-threading
   */
  public Collection<BatchError> insertAllProducts(CsvInputAdapter inputAdapter,
      int maxProductsInBatch,
      int numberOfWorkerThreads) throws InterruptedException {
    // creating empty error lists where service errors and parsing errors
    // will be added to
    Collection<BatchError> serviceErrorList
        = Collections.synchronizedCollection(new ArrayList<BatchError>());

    // create worker threads
    logger.info("== Create worker threads ==");
    List<Thread> workerThreads = new ArrayList<Thread>();
    for (int i = 0; i < numberOfWorkerThreads; i++) {
      workerThreads.add(new WorkerThread(requestFactory, namespaceDictionary, inputAdapter,
          maxProductsInBatch, serviceErrorList, userId, rootUrl));
    }

    // start all worker threads
    logger.info("== Start worker threads ==");
    for (Thread t : workerThreads) {
      t.start();
    }

    // wait for all worker threads to finish
    logger.info("== Wait for worker threads to finish ==");
    for (Thread t : workerThreads) {
      t.join();
    }

    return serviceErrorList;
  }

  /**
   * Uses the given file for creating a {@link CsvInputAdapter} that will read
   * from that file. Will use UTF-8 encoding, and will drop the first line
   * (which contains the headers that make the file human readable if opened
   * in a normal spreadsheet editor).
   *
   * @param file The file containing the product data
   * @param separator The string used as a separator in the .csv file
   * @return a new CsvInputAdapter that will read from the given file
   * @throws IOException if anything went wrong during creation of the
   *   {@code CsvInputAdapter}
   */
  private CsvInputAdapter createCsvInputAdapter(File file, String separator,
      boolean recordParsingErrors)
      throws IOException {
    BufferedReader fileInput = new BufferedReader(new InputStreamReader(
        new FileInputStream(file), "UTF8"));
    fileInput.readLine(); // first line of .csv file is ignored
    return new CsvInputAdapter(fileInput, separator, homepage);
  }

  /**
   * Reports parsing errors that occurred to the logger.
   *
   * @param parsingErrorList The list of parsing errors
   */
  private static void reportParsingErrors(Collection<ParsingError> parsingErrorList) {
    logger.info("== Status report ==");
    if (parsingErrorList.size() > 0) {
      logger.info("There were " + parsingErrorList.size() + " parsing error(s):");
      for (CsvInputAdapter.ParsingError e : parsingErrorList) {
        logger.info("  Product: " + e.productId + "  \tError: " + e.errorMessage
            + "\n      Complete product description: " + e.completeProductDescription);
      }
    } else {
      logger.info("Finished without parsing errors.");
    }
  }

  /**
   * Reports service errors that occurred to the logger.
   *
   * @param serviceErrorList The list of service errors
   */
  private static void reportServiceErrors(Collection<BatchError> serviceErrorList) {
    if (serviceErrorList.size() > 0) {
      logger.info("There were " + serviceErrorList.size() + " service error(s):");
      for (BatchError e : serviceErrorList) {
        StringBuilder s = new StringBuilder("  Product: ")
            .append(e.id)
            .append("  \tCode: ")
            .append(e.code)
            .append("  \tReason: ")
            .append(e.reason);
        if (e.errors != null && e.errors.errors != null) {
          s.append("[");
          for (ServiceError error : e.errors.errors) {
            s.append("{")
                .append(error.code)
                .append(" ; ")
                .append(error.name)
                .append(" ; ")
                .append(error.domain)
                .append(" ; ")
                .append(error.internalReason)
                .append("} ");
          }
          s.append("]");
        }
        logger.info(s.toString());
      }
    } else {
      logger.info("Finished without service errors.");
    }
  }

  /**
   * Simple immutable class for representing a server error during processing
   * of the batch.
   */
  public static class BatchError {
    public final String id;
    public final int code;
    public final String reason;
    public final ServiceErrors errors;

    /**
     * Constructor for setting product ID, error code and given error reason.
     *
     * @param id Product ID
     * @param code Error code
     * @param reason Given internal reason for the error
     */
    public BatchError (String id, int code, String reason, ServiceErrors errors) {
      this.id = id;
      this.code = code;
      this.reason = reason;
      this.errors = errors;
    }

    @Override
    public String toString() {
      return "Code " + code + " in item " + id + ": " + reason;
    }
  }
}
