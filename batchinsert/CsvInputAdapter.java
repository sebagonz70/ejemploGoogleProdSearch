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

import com.google.api.client.sample.structuredcontent.model.Content;
import com.google.api.client.sample.structuredcontent.model.Link;
import com.google.api.client.sample.structuredcontent.model.Price;
import com.google.api.client.sample.structuredcontent.model.Product;
import com.google.api.client.sample.structuredcontent.model.ShippingWeight;
import com.google.api.client.util.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>This class reads and parses product data stored as CSV in the
 * following format (newlines inserted for readability, ';' used as field
 * delimiter):<br/>
 * [ID];<br/>
 * [content language];[target country];<br/>
 * [title];<br/>
 * [description];<br/>
 * [condition];<br/>
 * [price];[currency];<br/>
 * [weight];[unit];<br/>
 * [quantity];<br/>
 * [expiration date (format: "yyyy-MM-dd HH:mm")];<br/>
 * [product type];<br/>
 * [brand];<br/>
 * [GTIN];<br/>
 * [MPN];<br/>
 * [product information homepage relative to registered homepage];<br/>
 * [product image URL]<br/>
 *
 * <p>The field separator to be used is given to the constructor together
 * with the {@link BufferedReader} from which the CSV lines will be retrieved.
 * It may not appear within any of the fields. (For example, if ';' is used as
 * separator, the description must not contain any ';'.)
 *
 * <p>Any optional fields will be omitted from the insert request if
 * they are empty.
 *
 * <p>This class is thread-safe, i.e. multiple threads can retrieve products
 * from it at the same time. The class guarantees to return each product exactly
 * once. (Unless the product appears more than once in the underlying CSV; In
 * that case, it will be returned as often as it appears there.) 
 *
 * @author Birgit Vera Schmidt
 */
public class CsvInputAdapter {
  private final BufferedReader input;
  private final Pattern separator;

  /**
   * The user's homepage.
   */
  private final String homepage;
  
  /**
   * The parser used for date formats.
   */
  private static final DateFormat dateFormat8601 =
      new SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US);

  /**
   * A list where parsing errors should be added to. If it is {@code null},
   * these errors will not be recorded.
   */
  private final Collection<CsvInputAdapter.ParsingError> parsingErrorList;

  /**
   * Simple constructor for setting up the input reader and the separator string.
   *
   * @param input An input reader that will return CSV lines containing exactly
   *   one product per line
   * @param separator The string used as a separator in the CSV
   */
  public CsvInputAdapter(BufferedReader input, String separator) {
    this(input, separator, null);
  }

  /**
   * Simple constructor for setting up the input reader and the separator string.
   *
   * @param input An input reader that will return CSV lines containing exactly
   *   one product per line
   * @param separator The string used as a separator in the CSV
   * @param homepage The user's registered homepage. This homepage will be used
   *   as a prefix for all homepage links.
   */
  public CsvInputAdapter(BufferedReader input, String separator, String homepage) {
    this.input = input;
    this.separator = Pattern.compile(Pattern.quote(separator));
    
    this.homepage = (homepage == null ? "" : homepage);
    this.parsingErrorList = Collections.synchronizedCollection(
        new ArrayList<ParsingError>());
  }

  /**
   * Parses and returns the next product.
   *
   * @return A product, or {@code null} if no more products are available
   * @throws IOException if anything went wrong during reading the product
   */
  public synchronized Product getNextProduct() throws IOException {
    while (true) {
      String line = input.readLine();
      if (line != null) {
        try {
          return parseProduct(line);
        } catch (ParsingError e) {
          parsingErrorList.add(e);
        }
      } else {
        return null;
      }
    }
  }
  
  /**
   * Parses and returns the next up to {@code maxNumOfProducts} products.
   *
   * @return A list of products, containing the next {@code maxNumOfProducts} if
   *   that many products were still available, or as many products as were
   *   available otherwise. In particular it returns an empty list if no more
   *   products are available.
   * @throws IOException if anything went wrong during reading the products
   */
  public synchronized List<Product> getNextProducts(int maxNumOfProducts) throws IOException {
    List<Product> products = new ArrayList<Product>(maxNumOfProducts);
    for (int i = 0; i < maxNumOfProducts; i++) {
      Product nextProduct = getNextProduct();
      if (nextProduct != null) {
        products.add(nextProduct);
      } else {
        break; // no more products available
      }
    }
    return products;
  }
  
  /**
   * Returns a list of parsing errors that occurred during the processing of
   * the input file.
   *
   * @return a list of parsing errors that occurred.
   */
  public Collection<ParsingError> getParsingErrors() {
    return parsingErrorList;
  }
  
  /**
   * Parses the values of one CSV line and creates a new product from them. Also
   * checks that all required parameters are given. Optional parameters that are
   * not given in the CSV will be set to {@code null} so that they aren't sent
   * in the insert request to the server.
   * 
   * @param line A CSV line describing exactly one product in the format given
   *   above
   * @return A product representing the information of the given CSV product
   *   description
   * @throws ParsingError if the product could not be parsed
   */
  protected Product parseProduct(String line) throws ParsingError {
    String[] parts = separator.split(line, -1);
    try {
      Product product = new Product();
      product.externalId = parseString(parts[0], "ID", true);
      product.lang = parseString(parts[1], "Content language", true);
      product.country = parseString(parts[2], "Target country", true);
      product.title = parseString(parts[3], "Title", true);
      product.content = new Content("text", parseString(parts[4], "Description", true));
      product.condition = parseString(parts[5], "Condition", true);
      product.price = new Price(parseString(parts[7], "Currency", true),
          parseDecimal(parts[6], "Price", true));
      product.shippingWeight = parseWeight(parts[9], parts[8]);
      product.quantity = parseInteger(parts[10], "Quantity", false).intValue();
      product.expirationDate = parseDate(parts[11], "Expiration date", false);
      product.productType = parseString(parts[12], "Product type", false);
      product.brand = parseString(parts[13], "Brand", false);
      product.gtin = parseString(parts[14], "GTIN", false);
      product.mpn = parseString(parts[15], "MPN", false);

      Link link = new Link();
      link.rel = "alternate";
      link.href = homepage + parts[16];
      link.type = "text/html";
      product.links.add(link);

      String imageLink = parseString(parts[17], "Image link", false);
      product.imageLinks = new ArrayList<String>();
      if (imageLink != null) {
        product.imageLinks.add(imageLink);
      }

      return product;
    } catch (ParsingError e) {
      throw new ParsingError(parts[0], line, e.errorMessage);
    }
  }
  
  /**
   * Parses a string argument.
   *
   * @param input The string to be parsed
   * @param attributeName The name of the attribute. This is only used for error
   *   reporting and may be {@code null}.
   * @param required Whether the attribute is required or not
   * @return The trimmed input string if it is not empty. Otherwise a
   *   {@code ParsingError} if the attribute is required, or {@code null} if
   *   it is optional.
   * @throws ParsingError if a required attribute is missing
   */
  private String parseString(String input, String attributeName, boolean required)
      throws ParsingError {
    if ("".equals(input)) {
      if (required) {
        throw new ParsingError(null, null, "Required argument missing: " + attributeName);
      } else {
        return null;
      }
    } else {
      return input.trim();
    }
  }

  /**
   * Parses an integer argument.
   *
   * @param input The integer to be parsed
   * @param attributeName The name of the attribute. This is only used for error
   *   reporting and may be {@code null}.
   * @param required Whether the attribute is required or not
   * @return The parsed integer if it is not empty. Otherwise a
   *   {@code ParsingError} if the attribute is required, or {@code null} if
   *   it is optional.
   * @throws ParsingError if a required attribute is missing
   */
  private BigInteger parseInteger(String input, String attributeName, boolean required)
      throws ParsingError {
    String parsedInput = parseString(input, attributeName, required);
    if (parsedInput == null) {
      return null;
    } else {
      try {
        return new BigInteger(input);
      } catch (NumberFormatException e) {
        throw new ParsingError(null, null, "Could not parse \"" + input
            + "\" as " + attributeName);
      }
    }
  }
  
  /**
   * Parses a decimal number argument.
   *
   * @param input The decimal number to be parsed
   * @param attributeName The name of the attribute. This is only used for error
   *   reporting and may be {@code null}.
   * @param required Whether the attribute is required or not
   * @return The parsed decimal if it is not empty. Otherwise a
   *   {@code ParsingError} if the attribute is required, or {@code null} if
   *   it is optional.
   * @throws ParsingError if a required attribute is missing
   */
  private BigDecimal parseDecimal(String input, String attributeName, boolean required)
      throws ParsingError {
    String parsedInput = parseString(input, attributeName, required);
    if (parsedInput == null) {
      return null;
    } else {
      try {
        return new BigDecimal(parsedInput);
      } catch (NumberFormatException e) {
        throw new ParsingError(null, null, "Could not parse \"" + input
            + "\" as " + attributeName);
      }
    }
  }
  
  /**
   * Parses a date argument.
   *
   * @param input The date to be parsed
   * @param attributeName The name of the attribute. This is only used for error
   *   reporting and may be {@code null}.
   * @param required Whether the attribute is required or not
   * @return The parsed date if it is not empty. Otherwise a
   *   {@code ParsingError} if the attribute is required, or {@code null} if
   *   it is optional.
   * @throws ParsingError if a required attribute is missing
   */
  private DateTime parseDate(String input, String attributeName, boolean required)
      throws ParsingError {
    String parsedInput = parseString(input, attributeName, required);
    if (parsedInput == null) {
      return null;
    } else {
      try {
        return new DateTime(dateFormat8601.parse(input));
      } catch (ParseException e) {
        throw new ParsingError(null, null, "Date (" + input + ") could not be parsed");
      }
    }
  }
  
  /**
   * Parses the weight of the product.
   *
   * @param unit The given weight unit
   * @param weight The given weight
   * @return The parsed weight if it is a valid weight, {@code null} if it is
   *   empty. In case of an invalid weight, a {@code ParsingError} will
   *   be thrown.
   * @throws ParsingError if the weight is given without a weight
   *   unit
   */
  private ShippingWeight parseWeight(String unit, String weight) throws ParsingError {
    String parsedUnit = parseString(unit, "weight unit", false);
    BigDecimal parsedWeight = parseDecimal(weight, "weight", false);
    if (parsedWeight == null) {
      return null;
    } else {
      if (parsedUnit == null) {
        throw new ParsingError(null, null, "Weight given without unit");
      } else {
        return new ShippingWeight(parsedUnit, parsedWeight);
      }
    }
  }
  
  /**
   * Convenience class for representing a parsing error. It contains the product
   * ID, the complete CSV description that could not be parsed, and the error
   * message itself.
   */
  static class ParsingError extends Exception {
    public final String productId;
    public final String completeProductDescription;
    public final String errorMessage;
    
    /**
     * Simple constructor for setting the fields.
     *
     * @param productId The product ID
     * @param completeProductDescription The complete CSV line that should have
     *   been parsed
     * @param errorMessage The error message that occured
     */
    public ParsingError(String productId, String completeProductDescription, String errorMessage) {
      this.productId = productId;
      this.completeProductDescription = completeProductDescription;
      this.errorMessage = errorMessage;
    }
  }
}
