// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.api.client.sample.structuredcontent.model;

import com.google.api.client.util.Key;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a SC API specific app:control element for an entry.
 *
 * @author tfrey@google.com (Thomas Frey)
 */
public class AppControl {

  @Key("sc:required_destination")
  public List<Destination> requiredDestinations = new ArrayList<Destination>();

  @Key("sc:excluded_destination")
  public List<Destination> excludedDestinations = new ArrayList<Destination>();

  /**
   * Class for representing a SCAPI specific app:control destination element.
   */
  public static class Destination {

    public Destination() {
    }

    public Destination(String destination) {
      dest = destination;
    }

    @Key("@dest")
    public String dest;
  }

  public AppControl() {
  }

  public void addRequiredDestination(String destination) {
    requiredDestinations.add(new Destination(destination));
  }

  public void addExcludedDestination(String destination) {
    excludedDestinations.add(new Destination(destination));
  }

}
