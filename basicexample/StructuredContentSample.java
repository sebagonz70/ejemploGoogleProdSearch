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

package com.google.api.client.sample.structuredcontent.basicexample;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.xml.atom.AtomContent;
import com.google.api.client.sample.structuredcontent.model.AppControl;
import com.google.api.client.sample.structuredcontent.model.Content;
import com.google.api.client.sample.structuredcontent.model.Link;
import com.google.api.client.sample.structuredcontent.model.Price;
import com.google.api.client.sample.structuredcontent.model.Product;
import com.google.api.client.sample.structuredcontent.model.ProductFeed;
import com.google.api.client.sample.structuredcontent.model.ServiceErrors;
import com.google.api.client.sample.structuredcontent.utils.ClientLibraryUtils;
import com.google.api.client.sample.structuredcontent.utils.UserInformation;
import com.google.api.client.xml.XmlNamespaceDictionary;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is intended to demonstrate the usage of the
 * <a href="http://code.google.com/p/google-api-java-client/">Google API Client Library for Java</a>
 * with the
 * <a href="http://code.google.com/apis/shopping/content">Content API for Shopping</a>.
 * For more information, see
 * <a href="http://code.google.com/apis/shopping/content/developers_guide_java.html">here</a>.
 *
 * @author Birgit Vera Schmidt
 */
public class StructuredContentSample {
  /**
   * Logger used for logging all messages produced by this class.
   */
  private static final Logger logger = Logger.getLogger(StructuredContentSample.class.getName());

  /**
   * The root URL of the Content API for Shopping Server.
   */
  private final String rootUrl;

  /**
   * The XML namespace dictionary for sending requests and for parsing
   * answers.
   */
  protected final XmlNamespaceDictionary namespaceDictionary
      = ClientLibraryUtils.createNamespaceDictionary();

  /**
   * The XML namespace dictionary used for parsing error messages returned
   * by the Content API for Shopping.
   */
  protected final XmlNamespaceDictionary errorNamespaceDictionary
      = ClientLibraryUtils.createErrorNamespaceDictionary();

  /**
   * HTTP request factory  for sending requests and receiving responses.
   */
  HttpRequestFactory requestFactory;

  /**
   * The user's account ID.
   */
  private final String userId;

  /**
   * The user's homepage.
   */
  private final String homepage;

  /**
   * Main function for simple demonstration of the usage of the
   * Google API Client Library for Java with the
   * Content API for Shopping.
   *
   * @param args (none expected)
   * @throws IOException if anything went seriously wrong during input or
   *   output
   */
  public static void main(String[] args) throws IOException {
    UserInformation userInformation = new UserInformation("1234567",
        "http://my.supercool.com/homepage/",
        "your.username@gmail.com", "yourPassword");
    StructuredContentSample sample = new StructuredContentSample(userInformation,
        "https://content.googleapis.com/content/v1/",
        createAuthorizedTransport(userInformation));
    sample.run();
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
        .createStandardRequestFactory(
            "google-structuredcontentbasicsample-1.0",
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
   * @param userId the user's account ID
   * @param homepage the user's registered homepage
   * @param rootUrl URL of the Content API for Shopping Server
   * @param requestFactory The HTTP request factory to be used for all requests
   */
  public StructuredContentSample(String userId, String homepage, String rootUrl,
      HttpRequestFactory requestFactory) {
    this.userId = userId;
    this.homepage = homepage;
    this.rootUrl = rootUrl;
    this.requestFactory = requestFactory;
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
  public StructuredContentSample(UserInformation userInformation, String rootUrl,
      HttpRequestFactory requestFactory) {
    this(userInformation.getUid(), userInformation.getHomepage(), rootUrl, requestFactory);
  }

  /**
   * This function will perform a number of arbitrary get, insert, update and
   * delete actions in order to demonstrate how they work.
   *
   * @throws IOException if anything went seriously wrong during input or
   *   output
   */
  public void run() throws IOException {
    // generate arbitrary product IDs
    Integer[] ids = new Integer[30];
    ids[0] = Integer.valueOf(1234567);
    for (int i = 1; i < ids.length; i++) {
      ids[i] = ids[i - 1] + 1;
    }

    try {
      // insert all products
      logger.info("== Start inserting products ==");
      for (Integer id : ids) {
        Product product = insertProduct(createProduct(id.toString()));
        logger.info("  * Inserted " + product.externalId.toString());
      }
      logger.info("== Product insertion ok ==");

      // list all products
      displayAllProducts();
      // show only first product page
      displayFirstProducts();
      // retrieve one product by its ID
      String productId = "online:de:DE:" + ids[2].toString();
      Product thirdProduct = getProduct(productId);
      logger.info("Retrieved product " + productId + ": " + thirdProduct.condition + " "
          + thirdProduct.title);

      // update all products
      logger.info("== Start updating products ==");
      for (Integer id : ids) {
        Product product = updateProduct(createChangedProduct(id.toString()));
        logger.info("  * Updated " + product.externalId.toString());
      }
      logger.info("== Product update ok ==");

      // list all products
      displayAllProducts();

      // changing the title of one product by retrieving it, changing the
      // title and returning the updated product
      logger.info("== Changing title of one product ==");
      Product aProduct = getProduct("online:de:DE:" + ids[7].toString());
      aProduct.title = "Silk scarf";
      updateProduct(aProduct);

      // list all products
      displayAllProducts();

      // delete all products
      logger.info("== Start deleting products ==");
      for (Integer id : ids) {
        productId = "online:de:DE:" + id.toString();
        deleteProduct(productId);
        logger.info("  * Deleted " + productId);
      }
      logger.info("== Product deletion ok ==");

      // list all products
      displayAllProducts();
    } catch (HttpResponseException e) {
      if ("application/vnd.google.gdata.error+xml".equals(e.getResponse().getContentType())) {
        // parse returned error messages and write them to the standard output
        ServiceErrors errors = e.getResponse().parseAs(ServiceErrors.class);
        logger.log(Level.SEVERE, errors.toString());
      } else {
        logger.log(Level.SEVERE, e.toString());
      }
      throw e;
    }
  }

  /**
   * Creates a {@link Product} instance using dummy values for all fields
   * except ID. Uses dummy values that are different from those
   * in {@link #createChangedProduct(String)}.
   *
   * @param id The ID to be set in the created {@link Product} instance.
   * @return the created {@link Product} instance.
   */
  private Product createProduct(String id) {
    Product product = new Product();
    product.title = "Red wool sweater";
    product.content =
        new Content(
            "text",
            "Comfortable and soft, this sweater will keep you warm on those "
            + "cold winter nights. Red and blue stripes.");
    product.appControl = new AppControl();
    product.appControl.addRequiredDestination("ProductAds");

    product.externalId = id;
    product.lang = "de";
    product.country = "DE";
    product.condition = "new";
    product.price = new Price("EUR", new BigDecimal("12.99"));

    // add a link
    Link link = new Link();
    link.rel = "alternate";
    link.href = homepage + "item1-info-page.html";
    link.type = "text/html";
    product.links.add(link);

    // set image links
    List<String> imageLinks = new ArrayList<String>();
    imageLinks.add("http://www.example.com/image1.jpg");
    imageLinks.add("http://www.example.com/image2.jpg");
    product.imageLinks = imageLinks;

    return product;
  }

  /**
   * Creates a {@link Product} instance using dummy values for all fields
   * except ID. Uses dummy values that are different from those
   * in {@link #createProduct(String)}.
   *
   * @param id The ID to be set in the created {@link Product} instance.
   * @return the created {@link Product} instance.
   */
  private Product createChangedProduct(String id) {
    Product product = new Product();
    product.title = "Old wool sweater";
    product.content =
        new Content(
            "text",
            "This sweater is very old and torn. "
            + "But it was worn by Marilyn Monroe!");
    product.appControl = new AppControl();
    product.appControl.addRequiredDestination("ProductAds");
    product.externalId = id;
    product.lang = "en";
    product.country = "US";
    product.condition = "used";
    product.price = new Price("USD", new BigDecimal("129.99"));

    // add a link
    Link link = new Link();
    link.rel = "alternate";
    link.href = homepage + "new-item1-info-page.html";
    link.type = "text/html";
    product.links.add(link);

    return product;
  }

  /**
   * Sends a product to the Content API for Shopping Server for inserting it.
   *
   * @param product The product to be inserted.
   * @return The response of the Content API for Shopping Server parsed into a
   *   new {@link Product} instance.
   * @throws HttpResponseException if the insertion was not successful.
   * @throws IOException if anything went wrong during the insertion.
   */
  private Product insertProduct(Product product)
      throws IOException, HttpResponseException {
    // URL
    String url = rootUrl + userId + "/items/products/schema";

    // Atom content
    AtomContent atomContent = AtomContent.forEntry(namespaceDictionary, product);

    // HTTP request
    HttpRequest request = requestFactory.buildPostRequest(new GoogleUrl(url), atomContent);

    // execute and interpret result
    return request.execute().parseAs(Product.class);
  }

  /**
   * Sends a product to the Content API for Shopping Server for updating it.
   *
   * @param product The product to be updated.
   * @return The response of the Content API for Shopping Server parsed into a
   *   new {@link Product} instance.
   * @throws HttpResponseException if the update was not successful.
   * @throws IOException if anything went wrong during the update.
   */
  private Product updateProduct(Product product)
      throws IOException, HttpResponseException {
    // URL
    String url = rootUrl + userId + "/items/products/schema/online:" + product.lang + ":"
        + product.country + ":" + product.externalId;

    // Atom content
    AtomContent atomContent = AtomContent.forEntry(namespaceDictionary, product);

    // HTTP request
    HttpRequest request = requestFactory.buildPutRequest(new GoogleUrl(url), atomContent);

    // execute and interpret result
    return request.execute().parseAs(Product.class);
  }

  /**
   * Sends a product ID to the Content API for Shopping Server for deleting
   * the corresponding product.
   *
   * @param productId The ID of the product to be deleted.
   * @throws HttpResponseException if the deletion was not successful.
   * @throws IOException if anything went wrong during the deletion.
   */
  private void deleteProduct(String productId)
      throws IOException, HttpResponseException {
    // URL
    String url = rootUrl + userId + "/items/products/schema/" + productId;

    // HTTP request
    HttpRequest request = requestFactory.buildDeleteRequest(new GoogleUrl(url));

    // execute and ignore result
    request.execute().ignore();
  }

  /**
   * Retrieves the product with the specified product ID from the Content API for Shopping Server.
   *
   * @param productId The ID of the product to be retrieved.
   * @return The requested product.
   * @throws HttpResponseException if the retrieval was not successful.
   * @throws IOException if anything went wrong during the retrieval.
   */
  private Product getProduct(String productId)
      throws IOException, HttpResponseException {
    // URL
    String url = rootUrl + userId + "/items/products/schema/" + productId;

    // HTTP request
    HttpRequest request = requestFactory.buildGetRequest(new GoogleUrl(url));

    // execute and interpret result
    return request.execute().parseAs(Product.class);
  }

  /**
   * Retrieves the first page of products from the Content API for Shopping Server.
   *
   * @return A list of products returned by the Content API for Shopping Server.
   *   This list is not necessarily complete, but rather contains the first
   *   "page" of products, usually the first 25 products.
   * @throws HttpResponseException if the retrieval was not successful.
   * @throws IOException if anything went wrong during the retrieval.
   */
  private List<Product> getProducts()
    throws IOException, HttpResponseException {
    // URL
    String url = rootUrl + userId + "/items/products/schema";

    // HTTP request
    HttpRequest request = requestFactory.buildGetRequest(new GoogleUrl(url));

    // get ProductFeed as response and return its entries
    ProductFeed feed = request.execute().parseAs(ProductFeed.class);
    return feed.getEntries();
  }

  /**
   * Retrieves a list of products from the Content API for Shopping Server using
   * the URL given as a parameter.
   *
   * @param url The URL to send the {@code GET} request to.
   * @return A list of products returned by the Content API for Shopping Server.
   *   This content of the list depends on the given URL.
   * @throws HttpResponseException if the retrieval was not successful.
   * @throws IOException if anything went wrong during the retrieval.
   */
  private ProductFeed getProducts(String url)
      throws IOException, HttpResponseException {
    // HTTP request using given URL
    HttpRequest request = requestFactory.buildGetRequest(new GoogleUrl(url));

    // execute and interpret result
    return request.execute().parseAs(ProductFeed.class);
  }

  /**
   * Retrieves the complete list of products from the Content API for Shopping
   * Server by navigating the {@code next} links given in the server responses.
   *
   * @return A list of products returned by the Content API for Shopping Server.
   * @throws HttpResponseException if the retrieval was not successful.
   * @throws IOException if anything went wrong during the retrieval.
   */
  private List<Product> getAllProducts() throws IOException {
    // get first page
    ProductFeed feed = getProducts(rootUrl + userId + "/items/products/schema");
    List<Product> list = feed.getEntries();

    // If the last page that was retrieved had a "next" link, get items from
    // that link and add them to the list. Repeat if necessary.
    String nextUrl = null;
    while ((nextUrl = findNextLink(feed.links)) != null) {
      feed = getProducts(nextUrl);
      list.addAll(feed.getEntries());
    }
    return list;
  }

  /**
   * Gets the "next"-link from a list of links.
   *
   * @param links The list of links that should be searched for a "next"-link
   * @return The {@code href} value of one of the links from the list with the
   *   "next" as {@code rel} value if at least one such link is in the list,
   *   {@code null} otherwise.
   */
  protected String findNextLink(List<Link> links) {
    return Link.find(links, "next");
  }

  /**
   * Retrieves the complete list of products from the Content API for Shopping
   * Server using the {@link #getAllProducts()} function and displays them
   * on the standard output.
   *
   * @throws HttpResponseException if the retrieval was not successful.
   * @throws IOException if anything went wrong during the retrieval.
   */
  private void displayAllProducts()
      throws IOException, HttpResponseException {
    logger.info("All products:");
    List<Product> products = getAllProducts();
    displayProducts(products);
  }

  /**
   * Retrieves the first page of products from the Content API for Shopping
   * Server using the {@link #getProducts()} function and displays them
   * on the standard output.
   *
   * @throws HttpResponseException if the retrieval was not successful.
   * @throws IOException if anything went wrong during the retrieval.
   */
  private void displayFirstProducts()
      throws IOException, HttpResponseException {
    logger.info("First product page:");
    List<Product> products = getProducts();
    displayProducts(products);
  }

  /**
   * Outputs the products in the given list to the standard output.
   *
   * @param products A list of products to be displayed on the standard output.
   */
  private static void displayProducts(List<Product> products) {
    for (Product p : products) {
      logger.info("  Product: " + p.title + "(" + p.externalId + ")");
    }
  }
}
