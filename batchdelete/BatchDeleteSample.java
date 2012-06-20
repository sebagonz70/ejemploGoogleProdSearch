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

package com.google.api.client.sample.structuredcontent.batchdelete;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.xml.atom.AtomContent;
import com.google.api.client.sample.structuredcontent.model.Link;
import com.google.api.client.sample.structuredcontent.model.Product;
import com.google.api.client.sample.structuredcontent.model.ProductFeed;
import com.google.api.client.sample.structuredcontent.model.ServiceErrors;
import com.google.api.client.sample.structuredcontent.utils.BatchUtils;
import com.google.api.client.sample.structuredcontent.utils.ClientLibraryUtils;
import com.google.api.client.sample.structuredcontent.utils.UserInformation;
import com.google.api.client.xml.XmlNamespaceDictionary;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This class is intended to demonstrate the usage of the
 * <a href="http://code.google.com/p/google-api-java-client/">Google API Client Library for Java</a>
 * with the
 * <a href="http://code.google.com/apis/shopping/content">Content API for Shopping</a>
 * for deleting all stored products. For more information, see
 * <a href="http://code.google.com/apis/shopping/content/developers_guide_java.html">here</a>.</p>
 *
 * @author Birgit Vera Schmidt
 */
public class BatchDeleteSample {
  /**
   * Logger used for logging all messages produced by this class.
   */
  private static final Logger logger = Logger.getLogger(BatchDeleteSample.class.getName());

  /**
   * The root URL of the Content API for Shopping Server.
   */
  private final String rootUrl;

  /**
   * The XML namespace dictionary for sending requests and for parsing answers.
   */
  protected final XmlNamespaceDictionary namespaceDictionary;

  /**
   * HTTP request factory  for sending requests and receiving responses.
   */
  HttpRequestFactory requestFactory;

  /**
   * The user's account ID.
   */
  private final String userId;

  /**
   * Creates an instance of this class and uses it for deleting all products
   * stored for the the user.
   *
   * @param args (none expected)
   * @throws IOException if input or output errors occurred.
   * @throws ServiceErrors if the server reported errors.
   */
  public static void main(String[] args) throws IOException, ServiceErrors {
    UserInformation userInformation = new UserInformation("1234567",
        null,
        "your.username@gmail.com", "yourPassword");
    BatchDeleteSample sample = new BatchDeleteSample(userInformation.getUid(),
        "https://content.googleapis.com/content/v1/",
        createAuthorizedTransport(userInformation));
    sample.deleteAllProducts();
  }

  /**
   * Creates an authorized instance of an HTTP request factory that is ready
   * for sending requests to the Content API for Shopping Server.
   *
   * @param userInformation Information about the user
   * @return A new authenticated HTTP request factory
   * @throws IOException if something went wrong during authentication
   */
  private static HttpRequestFactory createAuthorizedTransport(UserInformation userInformation)
      throws IOException {
    logger.info("== Starting login and setup ==");
    HttpRequestFactory requestFactory = ClientLibraryUtils
        .createBatchingRequestFactory("google-structuredcontentbatchdeletesample-1.0",
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
   * @param rootUrl URL of the Content API for Shopping Server
   * @param requestFactory The HTTP request factory to be used for all requests
   */
  public BatchDeleteSample(String userId, String rootUrl,
      HttpRequestFactory requestFactory) {
    this.userId = userId;
    this.rootUrl = rootUrl;
    this.requestFactory = requestFactory;
    namespaceDictionary = ClientLibraryUtils.createBatchingNamespaceDictionary();
  }

  /**
   * Deletes all products stored by the user specified in {@link UserInformation}.
   * It does this by retrieving the first page of products, sending a batch
   * deletion request for those products, and repeating if necessary. If any
   * problems occur, it throws an exception.
   *
   * @throws IOException if input or output errors occurred.
   * @throws HttpResponseException if the server answered with an HTTP error code.
   * @throws ServiceErrors if the server reported errors.
   */
  public void deleteAllProducts() throws IOException, HttpResponseException, ServiceErrors {
    // Get list of products (i.e. first page of product list), delete all
    // products on it, repeat if necessary.
    boolean finished = false;
    while (!finished) {
      ProductFeed productFeed = getFirstProducts();
      if (productFeed.getEntries().size() == 0) {
        finished = true;
      } else {
        logger.info("== Starting new batch ==");
        logger.info(" = Preparing batch =");
        ProductFeed deletionBatch = createDeletionBatch(productFeed.getEntries());
        logger.info(" = Sending batch =");
        executeProductBatch(deletionBatch);
        logger.info(" = Batch processed =");
      }
    }
    logger.info("== Finished ==");
  }

  /**
   * Creates a batch deletion feed from a list of products.
   *
   * @param products The list of products to be deleted.
   * @return The batch feed to be sent to the server for deleting the products.
   */
  private ProductFeed createDeletionBatch(List<Product> products) {
    ProductFeed deletionBatch = new ProductFeed();
    for (Product p : products) {
      Product productToBeDeleted = new Product();
      logger.info("Batch will try to delete product " + p.externalId
          + " (" + p.title + ")");
      BatchUtils.configureForDelete(productToBeDeleted, p.externalId);
      productToBeDeleted.atomId = Link.find(p.links, "edit");
      deletionBatch.entries.add(productToBeDeleted);
    }
    return deletionBatch;
  }

  /**
   * Sends a batch of products to the Content API for Shopping Server for deleting it.
   * Interprets the results and throws an exception in case of an error.
   *
   * @param batchedProducts The list of products to be deleted.
   * @throws IOException if anything went wrong during the insertion.
   * @throws ServiceErrors if deletion failed.
   */
  private void executeProductBatch(ProductFeed batchedProducts)
      throws IOException, ServiceErrors {
    // create atom feed content
    AtomContent atomFeedContent = AtomContent.forFeed(namespaceDictionary, batchedProducts);

    synchronized (requestFactory) {
      // build HTTP request
      HttpRequest request = requestFactory
          .buildPostRequest(new GoogleUrl(rootUrl + userId + "/items/products/schema/batch"),
              atomFeedContent);

      // execute the HTTP request
      HttpResponse response = request.execute();

      // Interpret the results. Throw an exception if there is any error,
      // because an error means the product could not be deleted, and if we just
      // try again without fixing the problem, we will just end up in an
      // endless loop. (No pun intended.)
      if (HttpResponse.isSuccessStatusCode(response.getStatusCode())) {
        ProductFeed processedProducts = response.parseAs(ProductFeed.class);
        boolean errorsFound = examineReturnedProducts(processedProducts);
        if (errorsFound) {
          logger.log(Level.SEVERE, "One or more errors occured during deletion.");
          throw new ServiceErrors();
        }
      } else {
        reportBug(batchedProducts, response);
        throw new ServiceErrors();
      }
    }
  }

  /**
   * <p>Helper function for examining the status codes returned for the individual
   * products by the Content API for Shopping Server. This function also checks whether
   * the batch has been interrupted.</p>
   *
   * @param processedProducts The {@link ProductFeed} returned by the
   *     Content API for Shopping Server (and parsed by the Google API Client Library).
   * @return {@code true} if one or more errors occurred, {@code false} otherwise.
   */
  private boolean examineReturnedProducts(ProductFeed processedProducts) {
    boolean errorsFound = false;
    for (Product p : processedProducts.getEntries()) {
      if (p.batchInterrupted != null) {
        errorsFound = true;
        logger.log(Level.SEVERE, "Batch was interrupted.");
      } else {
        if (!HttpResponse.isSuccessStatusCode(p.batchStatus.code)) {
          logger.log(Level.SEVERE, "Server error during deletion: \n"
              + "  Product: " + p.batchID + "  \tCode: " + p.batchStatus.code
              + "  \tReason: " + p.batchStatus.reason);
          errorsFound = true;
        }
      }
    }
    return errorsFound;
  }

  /**
   * Creates a bug report file containing the request that was made to the
   * server as well as the response received. Is guaranteed to create a new
   * file and to avoid overwriting an existing one. Will output user explanations
   * using the {@link Logger}.
   *
   * @param sentProducts The {@link ProductFeed} that was sent to the server.
   * @param serverResponse The response received from the server.
   * @throws IOException if anything went wrong during the creation of the
   *   file.
   */
  private void reportBug(ProductFeed sentProducts, HttpResponse serverResponse)
      throws IOException {
    logger.log(Level.SEVERE, "Content API for Shopping unexpectedly returned "
        + "an HTTP error code (" + serverResponse.getStatusCode() + ") for a batch "
        + "request.");
    try {
      File file = ClientLibraryUtils.createBugReportFile(sentProducts, serverResponse,
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
  }

  /**
   * Retrieves the first page of products from the Content API for Shopping Server.
   *
   * @return A list of products returned by the Content API for Shopping Server.
   *   This list is not necessarily complete, it only contains the first
   *   "page" of products, usually the first 25 products.
   * @throws HttpResponseException if the retrieval was not successful.
   * @throws IOException if anything went wrong during the retrieval.
   */
  private ProductFeed getFirstProducts()
    throws IOException, HttpResponseException {
    // URL
    String url = rootUrl + userId + "/items/products/schema?max-results=100";

    // HTTP request
    HttpRequest request = requestFactory.buildGetRequest(new GoogleUrl(url));

    // get ProductFeed as response
    return request.execute().parseAs(ProductFeed.class);
  }
}
