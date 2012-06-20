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

import java.math.BigDecimal;

/**
 * Class for representing the information in an {@code scp:shipping_weight} XML element.
 *
 * @author Birgit Vera Schmidt
 */
public class ShippingWeight {
  @Key("@unit")
  public String unit;

  @Key("text()")
  public BigDecimal value;

  /**
   * Empty default constructor so that the XML parser can automatically
   * create a new instance of this class.
   */
  public ShippingWeight() {}

  /**
   * Simple constructor that takes type and value and sets them.
   *
   * @param unit Value for {@code unit}.
   * @param value Value for content of the XML element.
   */
  public ShippingWeight(String unit, BigDecimal value) {
    this.unit = unit;
    this.value = value;
  }
}
