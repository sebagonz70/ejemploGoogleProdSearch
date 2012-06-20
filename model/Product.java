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

import com.google.api.client.util.DateTime;
import com.google.api.client.util.Key;

import java.util.List;

/**
 * Class for representing a product entry.
 *
 * @author Birgit Vera Schmidt
 */
public class Product extends BatchableEntry {
  @Key("sc:additional_image_link")
  public List<String> additionalImageLinks;

  @Key("sc:adult")
  public boolean adult;

  @Key("sc:attribute")
  public List<Attribute> attributes;

  @Key("sc:channel")
  public String channel;

  @Key("sc:content_language")
  public String lang = "en";

  @Key("sc:expiration_date")
  public DateTime expirationDate;

  @Key("sc:group")
  public List<AttributeGroup> attributeGroups;

  @Key("sc:id")
  public String externalId;

  @Key("sc:image_link")
  public List<String> imageLinks;

  @Key("sc:target_country")
  public String country = "US";

  @Key("scp:adwords_grouping")
  public String adwordsGrouping;

  @Key("scp:adwords_labels")
  public List<String> adwordsLabels;

  @Key("scp:adwords_redirect")
  public String adwordsRedirect;

  @Key("scp:adwords_queryparam")
  public List<String> adwordsQueryparam;

  @Key("scp:age_group")
  public String ageGroup;

  @Key("scp:author")
  public String author;

  @Key("scp:availability")
  public String availability;

  @Key("scp:brand")
  public String brand;

  @Key("scp:color")
  public List<String> colors;

  @Key("scp:condition")
  public String condition;

  @Key("scp:edition")
  public String edition;

  @Key("scp:feature")
  public List<String> feature;

  @Key("scp:featured_product")
  public boolean featuredProduct;

  @Key("scp:gender")
  public String gender;

  @Key("scp:genre")
  public String genre;

  @Key("scp:google_product_category")
  public String googleProductCategory;

  @Key("scp:gtin")
  public String gtin;

  @Key("scp:item_group_id")
  public String itemGroupId;

  @Key("scp:manufacturer")
  public String manufacturer;

  @Key("scp:material")
  public String material;

  @Key("scp:mpn")
  public String mpn;

  @Key("scp:pattern")
  public String pattern;

  @Key("scp:price")
  public Price price;

  @Key("scp:product_review_average")
  public String productReviewAverage;

  @Key("scp:product_review_count")
  public String productReviewCount;

  @Key("scp:product_type")
  public String productType;

  @Key("scp:quantity")
  public Integer quantity;

  @Key("scp:shipping")
  public List<Shipping> shippingRules;

  @Key("scp:shipping_weight")
  public ShippingWeight shippingWeight;

  @Key("scp:size")
  public String size;

  @Key("scp:tax")
  public List<Tax> taxes;

  @Key("scp:year")
  public String year;
}
