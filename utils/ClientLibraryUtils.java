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

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.http.HttpParser;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.xml.XmlHttpParser;
import com.google.api.client.http.xml.atom.AtomParser;
import com.google.api.client.sample.structuredcontent.model.Feed;
import com.google.api.client.xml.Xml;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.google.api.client.xml.atom.Atom;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Various utility functions for creating and authenticating HttpTransports.
 *
 * @author Birgit Vera Schmidt
 */
public class ClientLibraryUtils {

  /**
   * Creates and returns a new XML namespace dictionary for normal requests to the Structured
   * Content API Server.
   *
   * @return a new XML namespace dictionary for normal requests to the Structured Content API
   *         Server
   */
  public static XmlNamespaceDictionary createNamespaceDictionary() {
    return new XmlNamespaceDictionary()
        .set("", "http://www.w3.org/2005/Atom")
        .set("app", "http://www.w3.org/2007/app")
        .set("gd", "http://schemas.google.com/g/2005")
        .set("sc", "http://schemas.google.com/structuredcontent/2009")
        .set("scp", "http://schemas.google.com/structuredcontent/2009/products")
        .set("xml", "http://www.w3.org/XML/1998/namespace");
  }

  /**
   * Creates and returns a new XML namespace dictionary for batch requests to the Structured Content
   * API Server.
   *
   * @return a new XML namespace dictionary for batch requests to the Structured Content API Server
   */
  public static XmlNamespaceDictionary createBatchingNamespaceDictionary() {
    return createNamespaceDictionary().set("batch", "http://schemas.google.com/gdata/batch");
  }

  /**
   * Creates and returns a new XML namespace dictionary for parsing error responses from the
   * Structured Content API Server.
   *
   * @return a new XML namespace dictionary for parsing error responses from the Structured Content
   *         API Server
   */
  public static XmlNamespaceDictionary createErrorNamespaceDictionary() {
    return new XmlNamespaceDictionary().set("", "http://schemas.google.com/g/2005");
  }

  /**
   * Creates a new {@link HttpRequestFactory} instance, authorizes the user using ClientLogin, and
   * sets some default parameters.
   *
   * @param applicationName The name of the application that accesses the Structured Content API
   *                        Server
   * @param username        Username
   * @param password        Password
   * @return a new HttpRequestFactory
   * @throws IOException if something went wrong during authentication
   */
  public static HttpRequestFactory createBareRequestFactory(final String applicationName,
      String username, String password, final HttpParser... parsers) throws IOException {
    // create new transport
    HttpTransport transport = new NetHttpTransport();
    // ClientLogin
    ClientLogin authenticator = new ClientLogin();
    authenticator.transport = transport;
    authenticator.authTokenType = "structuredcontent";
    authenticator.username = username;
    authenticator.password = password;
    final ClientLogin.Response response = authenticator.authenticate();
    // request factory
    return transport.createRequestFactory(new HttpRequestInitializer() {

      @Override
      public void initialize(HttpRequest request) {
        GoogleHeaders headers = new GoogleHeaders();
        headers.setApplicationName(applicationName);
        headers.gdataVersion = "1";
        request.setHeaders(headers);
        for (HttpParser parser : parsers) {
          request.addParser(parser);
        }
        response.initialize(request);
      }
    });
  }

  /**
   * Does the same as {@link #createBareRequestFactory}, but additionally adds a standard and an
   * error parser.
   *
   * @param applicationName The name of the application that accesses the Structured Content API
   *                        Server
   * @param username        Username
   * @param password        Password
   * @return a new HttpRequestFactory
   * @throws IOException if something went wrong during authentication
   */
  public static HttpRequestFactory createStandardRequestFactory(String applicationName,
      String username,
      String password)
      throws IOException {
    return createBareRequestFactory(applicationName, username, password, newStandardParser(),
        newErrorParser());
  }

  /**
   * Does the same as {@link #createBareRequestFactory}, but additionally adds a batching parser.
   *
   * @param applicationName The name of the application that accesses the Structured Content API
   *                        Server
   * @return a new HttpTransport
   */
  public static HttpRequestFactory createBatchingRequestFactory(final String applicationName,
      String username, String password) throws IOException {
    return createBareRequestFactory(applicationName, username, password, newBatchingParser());
  }

  /**
   * Returns a new standard parser.
   */
  private static AtomParser newStandardParser() {
    AtomParser parser = new AtomParser(createNamespaceDictionary());
    return parser;
  }

  /**
   * Returns a new batching parser.
   */
  private static AtomParser newBatchingParser() {
    return new AtomParser(createBatchingNamespaceDictionary());
  }

  /**
   * Returns a new error parser.
   */
  private static XmlHttpParser newErrorParser() {
    return XmlHttpParser.builder(createErrorNamespaceDictionary())
        .setContentType("application/vnd.google.gdata.error+xml")
        .build();
  }

  /**
   * Creates a bug report file containing the request that was made to the server as well as the
   * response received. Is guaranteed to create a new file and to avoid overwriting an existing
   * one.
   *
   * @param sentFeed            The {@link Feed} that was sent to the server
   * @param serverResponse      The response received from the server
   * @param namespaceDictionary The XML namespace dictionary that was used for sending the request
   * @param userId              Account ID of the user for whom the request was sent
   * @return the {@link File} containing the bug report
   * @throws IOException if anything went wrong during the creation of the file
   */
  public static File createBugReportFile(Feed<?> sentFeed, HttpResponse serverResponse,
      XmlNamespaceDictionary namespaceDictionary, String userId)
      throws IOException {
    File file = new File("bugreport.scapi.txt");
    int count = 1;
    while (file.exists()) {
      file = new File("bugreport" + (count++) + ".scapi.txt");
    }
    file.createNewFile();
    PrintStream out = new PrintStream(new FileOutputStream(file));

    try {
      out.println("Time: " + System.currentTimeMillis());
      out.println("UID: " + userId);
      out.println();

      out.println();
      out.println("== Request ==");
      XmlSerializer serializer = Xml.createSerializer();
      serializer.setOutput(out, "UTF-8");
      namespaceDictionary.serialize(serializer, Atom.ATOM_NAMESPACE,
          "feed", sentFeed);
      out.println();

      out.println();
      out.println("== Response ==");
      out.println(serverResponse.parseAsString());
      out.println();
      out.close();
    } catch (IOException e) {
      out.close();
    }

    return file;
  }
}
