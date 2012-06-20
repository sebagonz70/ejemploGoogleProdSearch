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

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.xml.atom.AtomContent;
import com.google.api.client.sample.structuredcontent.batchinsert.MultiThreadingAndBatchingExample.BatchError;
import com.google.api.client.sample.structuredcontent.model.Product;
import com.google.api.client.sample.structuredcontent.model.ProductFeed;
import com.google.api.client.sample.structuredcontent.utils.BatchUtils;
import com.google.api.client.sample.structuredcontent.utils.ClientLibraryUtils;
import com.google.api.client.xml.XmlNamespaceDictionary;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A worker thread that retrieves products from the given {@link CsvInputAdapter},
 * collects them and sends them to the server as batches.
 *
 * <p>The parameter {@code maxProductsInBatch} that is set in the constructor
 * determines up to how many products will be sent in one batch. Any errors
 * that are encountered are inserted into the error lists that are also given
 * to the constructor.
 *
 * <p>All requests to the server will be made using the given HTTP request factory.
 *
 * @author Birgit Vera Schmidt
 */
public final class WorkerThread extends Thread {
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
   * HTTP request factory  for sending requests and receiving responses.
   */
  HttpRequestFactory requestFactory;

  /**
   * The XML namespace dictionary that is sent with all requests to the server.
   */
  private final XmlNamespaceDictionary namespaceDictionary;

  /**
   * A {@code CsvInputAdapter} instance from which the products are retrieved
   * that should be inserted.
   */
  private final CsvInputAdapter inputAdapter;

  /**
   * A list where service and batching errors should be added to. If it is
   * {@code null}, these errors will not be recorded.
   */
  private final Collection<BatchError> serviceErrorList;

  /**
   * Maximum number of products that can be sent in one batch.
   */
  private final int maxProductsInBatch;

  /**
   * The user's account ID.
   */
  private final String userId;

  /**
   * Constructor setting up all required (final) parameters.
   *
   * @param requestFactory The HTTP request factory to be used for all requests
   * @param inputAdapter An input adapter that will supply the products to
   *   be inserted. Must not be {@code null}.
   * @param maxProductsInBatch The maximum number of products that can be sent
   *   in one batch
   * @param serviceErrorList A list where service and batching errors should
   *   be added to. If it is {@code null}, these errors will not be recorded.
   * @param userId Account ID of the user who is sending the products
   */
  public WorkerThread(HttpRequestFactory requestFactory,
      XmlNamespaceDictionary namespaceDictionary,
      CsvInputAdapter inputAdapter,
      int maxProductsInBatch,
      Collection<BatchError> serviceErrorList,
      String userId,
      String rootUrl) {
    if (requestFactory == null) {
      throw new IllegalArgumentException("transport must not be null");
    }
    this.requestFactory = requestFactory;

    this.namespaceDictionary = namespaceDictionary;

    if (inputAdapter == null) {
      throw new IllegalArgumentException("inputAdapter must not be null");
    }
    this.inputAdapter = inputAdapter;

    this.maxProductsInBatch = maxProductsInBatch;

    this.serviceErrorList = serviceErrorList;

    if (userId == null) {
      throw new IllegalArgumentException("userId must not be null");
    }
    this.userId = userId;

    if (rootUrl == null) {
      throw new IllegalArgumentException("rootUrl must not be null");
    }
    this.rootUrl = rootUrl;
  }

  /**
   * The actual working part of the worker thread. When started, the thread
   * will retrieve products until it reaches the maximum number of products
   * that can be sent in one batch. It will then send that batch and repeat
   * this process, until there are no more products to be inserted. If
   * necessary, it will send a last batch with less than the maximum allowed
   * number of products.
   *
   * @throws RuntimeException in case of an unexpected exception in input or
   *   output
   */
  @Override
  public void run() {
    try {
      while (true) {
        List<Product> products = inputAdapter.getNextProducts(maxProductsInBatch);
        if (products.size() > 0) {
          sendBatch(products);
        } else {
          break; // no more products available
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Sends all products in the list to the server, adding batch information
   * as appropriate. Warning: Will modify the products by adding/overwriting
   * batch information.
   *
   * @param products The list of products to be inserted
   * @throws IOException if anything went wrong with input/output
   */
  private void sendBatch(List<Product> products) throws IOException {
    ProductFeed feed = new ProductFeed();
    for (Product p : products) {
      // adding an "insert" batch operation, and
      // setting the batch ID to the product ID so that we can identify
      // products that had problems
      BatchUtils.configureForInsert(p, p.externalId);
    }
    feed.entries = products;
    executeProductBatch(feed);
  }

  /**
   * Sends a batch of products to the Content API for Shopping Server for inserting it.
   *
   * @param batchedProducts The list of products to be inserted
   * @throws IOException if anything went wrong during the insertion
   */
  private void executeProductBatch(ProductFeed batchedProducts)
      throws IOException {
    // create atom feed content
    AtomContent atomFeedContent = AtomContent.forFeed(namespaceDictionary, batchedProducts);

    // build HTTP request
    HttpRequest request = requestFactory
        .buildPostRequest(new GoogleUrl(rootUrl + userId + "/items/products/schema/batch"),
           atomFeedContent);

    // execute the HTTP request
    HttpResponse response = request.execute();

    // interpret the results
    if (HttpResponse.isSuccessStatusCode(response.getStatusCode())) {
      ProductFeed processedProducts = response.parseAs(ProductFeed.class);
      boolean batchWasInterrupted = processReturnedProducts(processedProducts);
      if (batchWasInterrupted) {
        reportUnprocessedProducts(batchedProducts, processedProducts);
      }
    } else {
      // According to the batch processing reference on
      // http://code.google.com/apis/gdata/docs/batch.html
      // this should never happen. If it does, create a bug report and
      // ask the user to send it to the Google Content API for Shopping team.
      logger.log(Level.SEVERE, "Content API for Shopping unexpectedly returned "
          + "an HTTP error code (" + response.getStatusCode() + ") for a batch "
          + "request.");
      try {
        File file = ClientLibraryUtils.createBugReportFile(batchedProducts, response,
            namespaceDictionary, userId);
        logger.log(Level.SEVERE, "A bug report file has been created here:\n"
            + file.getAbsolutePath() + "\n"
            + "Please send this file to "
            + "structured-content-api@googlegroups.com so that we can "
            + "investigate this error.\n"
            + "Note: The file contains the request you sent and "
            + "the server response you received, as well as the current time "
            + "and your UID.");
      } catch (IOException e) {
        logger.log(Level.SEVERE, "A bug report file could not be created.");
      }
      throw new RuntimeException();
    }
  }

  /**
   * <p>Helper function for examining the status codes returned for the individual
   * products by the Content API for Shopping Server. Products with an error status
   * code will be reported in the list for service errors.
   *
   * @param processedProducts The {@link ProductFeed} returned by the Content API for Shopping
   *   API Server (and parsed by the Google API Client Library)
   * @return {@code true} if the batch aborted with a {@code batch:interrupted}
   *   error code, {@code false} otherwise
   */
  private boolean processReturnedProducts(ProductFeed processedProducts) {
    boolean batchWasInterrupted = false;
    Iterator<Product> productIterator = processedProducts.getEntries().iterator();
    while (productIterator.hasNext()) {
      Product p = productIterator.next();
      if (p.batchInterrupted != null) {
        batchWasInterrupted = true;
      } else {
        if (!HttpResponse.isSuccessStatusCode(p.batchStatus.code)) {
          if (serviceErrorList != null) {
            serviceErrorList.add(new BatchError(p.batchID, p.batchStatus.code,
                p.batchStatus.reason, p.content.errors));
          }
        }
      }
    }
    return batchWasInterrupted;
  }

  /**
   * Helper function for reporting products that were not processed because
   * of an interrupted batch.
   *
   * @param batchedProducts The feed of products that were sent to the server
   * @param processedProducts The feed of products that was returned by the
   *   server, not containing {@code batch:interrupted} error code entries any
   *   more
   */
  private void reportUnprocessedProducts(ProductFeed batchedProducts,
      ProductFeed processedProducts) {
    // collect list of processed products
    List<String> processed = new ArrayList<String>();
    for (Product p : processedProducts.getEntries()) {
      if (p.batchInterrupted == null) {
        processed.add(p.batchID);
      }
    }

    // add products that were not processed to the error list
    for (Product p : batchedProducts.entries) {
      if (processed.contains(p.batchID)) {
        processed.remove(p.batchID);
      } else {
        serviceErrorList.add(new BatchError(p.batchID, 500,
            "Not processed because batch was interrupted", null));
      }
    }
  }
}
