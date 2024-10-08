package com.domain_name.next.taxationapi;

import com.domain_name.next.taxationapi.client.request.Address;
import com.domain_name.next.taxationapi.client.request.AvaTaxRequest;
import com.domain_name.next.taxationapi.client.request.ShippingAddress;
import com.domain_name.next.taxationapi.client.response.AvaTaxResponse;
import com.domain_name.next.taxationapi.client.response.ProductLine;
import com.domain_name.next.taxationapi.client.response.ProductLineDetails;
import com.domain_name.next.taxationapi.enums.Provider;
import com.domain_name.next.taxationapi.enums.TaxationPolicy;
import com.domain_name.next.taxationapi.resources.bo.DynamicTaxConfigProperties;
import com.domain_name.next.taxationapi.resources.bo.ItemTaxCodes;
import com.domain_name.next.taxationapi.resources.bo.StateTaxes;
import com.domain_name.next.taxationapi.resources.bo.StaticTaxConfigProperties;
import com.domain_name.next.taxationapi.resources.bo.TaxConfigProperties;
import com.domain_name.next.taxationapi.resources.bo.TaxRates;
import com.domain_name.next.taxationapi.resources.request.DestinationAddress;
import com.domain_name.next.taxationapi.resources.request.ProductTaxRateRequest;
import com.domain_name.next.taxationapi.resources.request.SourceAddress;
import com.domain_name.next.taxationapi.resources.request.TaxableItem;
import com.domain_name.next.taxationapi.resources.response.ProductTaxRateResponse;
import com.domain_name.next.taxationapi.resources.response.StaticTaxRateResponse;
import com.domain_name.next.taxationapi.resources.response.TaxConfigResponse;
import com.domain_name.next.taxationapi.resources.response.TaxedItem;
import com.domain_name.next.taxationapi.util.TaxConfigUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;
import org.zalando.problem.Problem;
import org.zalando.problem.StatusType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class TestHelper {

  private final LocalDateTime date = LocalDateTime.now().plusHours(1);

  protected ProductTaxRateResponse buildProductTaxRateResponse() {
    List<TaxedItem> taxedItemList = new LinkedList<>();
    TaxedItem taxedItem1 = TaxedItem.builder()
        .itemId("Y0001")
        .taxRate(Map.of("Sales",7.25))
        .taxAmount(64.08)
        .build();

    TaxedItem taxedItem2 = TaxedItem.builder()
        .itemId("Y0002")
        .taxRate(Map.of("Sales",7.25))
        .taxAmount(17.47)
        .build();
    taxedItemList.add(taxedItem1);
    taxedItemList.add(taxedItem2);
    return ProductTaxRateResponse.builder()
        .taxedItems(taxedItemList)
        .totalTaxAmount(81.55)
        .taxGotCalculated(true)
        .transactionId("16c8f490-6ea5-49a3-8ab8-e8ec41bc88b9")
        .taxationPolicy(TaxConfigUtil.getDynamicTaxationPolicy("US").getValue())
        .build();
  }
  
  protected ProductTaxRateResponse buildProductTaxRateResponseForCA() {
    List<TaxedItem> taxedItemList = new LinkedList<>();

    TaxedItem taxedItem1 = TaxedItem.builder()
        .itemId("Y0001")
        .taxRate(Map.of("HST", 13.0))
        .taxAmount(64.08)
        .build();

    TaxedItem taxedItem2 = TaxedItem.builder()
        .itemId("Y0002")
        .taxRate(Map.of("HST", 5.0))
        .taxAmount(17.47)
        .build();
    taxedItemList.add(taxedItem1);
    taxedItemList.add(taxedItem2);
    
    return ProductTaxRateResponse.builder()
        .taxedItems(taxedItemList)
        .totalTaxAmount(81.55)
        .taxGotCalculated(true)
        .transactionId("16c8f490-6ea5-49a3-8ab8-e8ec41bc88b9")
        .taxationPolicy(TaxConfigUtil.getDynamicTaxationPolicy("CA").getValue())
        .build();
  }

  protected ProductTaxRateRequest buildProductTaxRateRequest() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParams())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildDestinationAddress())
        .build();
  }

  protected ProductTaxRateRequest buildProductTaxRateRequestPR() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParams())
        .sourceAddress(buildSourceAddressPR())
        .destinationAddress(buildDestinationAddressPR())
        .build();
  }

  protected List<TaxableItem> buildTaxableItemParamsInvalidType() {
    List<TaxableItem> taxableItemList = new LinkedList<>();
    TaxableItem taxableItem1 = TaxableItem.builder().itemType("abc").itemId("Y0001").itemTaxCode("PS081282").quantity(1)
        .itemName("Yarn").unitPrice(883.90).build();
    taxableItemList.add(taxableItem1);
    return taxableItemList;
  }
  protected ProductTaxRateRequest buildProductTaxRateRequestTypenotSupported() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParamsInvalidType())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildDestinationAddress())
        .build();
  }
  
  protected ProductTaxRateRequest buildProductTaxRateRequestWithStateCodeON() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParamsWhenStateCodeON())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildDestinationAddressWithStateCodeON())
        .build();
  }

  protected ProductTaxRateRequest buildProductTaxRateRequestWithoutTaxDate() {
    return ProductTaxRateRequest.builder()
        .taxableItems(buildTaxableItemParams())
        .sourceAddress(buildSourceAddressWithoutTaxDateCase())
        .destinationAddress(buildDestinationAddressWithoutTaxDateCase())
        .build();
  }
  
  protected ProductTaxRateRequest buildProductTaxRateRequestWithInvalidDestination() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParams())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildInvalidDestinationAddress())
        .build();
  }
  protected ProductTaxRateRequest buildProductTaxRateRequestWithInvalidDestinationForDynamic() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParams())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildInvalidDestinationAddressforDynamic())
        .build();
  }
  
  protected ProductTaxRateRequest buildProductTaxRateRequestWithInvalidStateCode() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParams())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildInvalidStateCode())
        .build();
  }
  
  protected ProductTaxRateRequest buildProductTaxRateRequestWithValidTaxCode() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParamsWithValidTaxCode())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildDestinationAddressWithStateCodeON())
        .build();
  }
  
  protected ProductTaxRateRequest buildProductTaxRateRequestWithInValidTaxCode() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParamsWithInvalidTaxCode())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildDestinationAddressWithStateCodeON())
        .build();
  }

  protected List<TaxableItem> buildTaxableItemParams() {
    List<TaxableItem> taxableItemList = new LinkedList<>();
    TaxableItem taxableItem1 = TaxableItem.builder()
        .itemType("ProductItem")
        .itemId("Y0001")
        .itemTaxCode("PS081282")
        .quantity(1)
        .itemName("Yarn")
        .unitPrice(883.90)
        .build();

    TaxableItem taxableItem2 = TaxableItem.builder()
        .itemType("ShippingItem")
        .itemId("Y0002")
        .itemTaxCode("PS081282")
        .quantity(2)
        .itemName("Yarn")
        .unitPrice(39.21)
        .build();

    taxableItemList.add(taxableItem1);
    taxableItemList.add(taxableItem2);

    return taxableItemList;
  }
  
  protected List<TaxableItem> buildTaxableItemParamsWithValidTaxCode() {
    List<TaxableItem> taxableItemList = new LinkedList<>();
    TaxableItem taxableItem1 = TaxableItem.builder()
        .itemType("ProductItem")
        .itemId("Y0001")
        .itemTaxCode("AccTax")
        .quantity(1)
        .itemName("Yarn")
        .unitPrice(100.00)
        .build();
    
    TaxableItem taxableItem2 = TaxableItem.builder()
        .itemType("ShippingItem")
        .itemId("Y0002")
        .itemTaxCode("SLKidsTax")
        .quantity(2)
        .itemName("Yarn")
        .unitPrice(39.21)
        .build();
    
    taxableItemList.add(taxableItem1);
    taxableItemList.add(taxableItem2);
    
    return taxableItemList;
  }
  
  protected List<TaxableItem> buildTaxableItemParamsWithInvalidTaxCode() {
    List<TaxableItem> taxableItemList = new LinkedList<>();
    TaxableItem taxableItem1 = TaxableItem.builder()
        .itemType("ProductItem")
        .itemId("Y0001")
        .itemTaxCode("Acc")
        .quantity(1)
        .itemName("Yarn")
        .unitPrice(100.00)
        .build();

    TaxableItem taxableItem2 = TaxableItem.builder()
        .itemType("ShippingItem")
        .itemId("Y0002")
        .itemTaxCode("SLKidsTax")
        .quantity(2)
        .itemName("Yarn")
        .unitPrice(39.21)
        .build();

    taxableItemList.add(taxableItem1);
    taxableItemList.add(taxableItem2);

    return taxableItemList;
  }
  
  protected List<TaxableItem> buildTaxableItemParamsWhenStateCodeON() {
      List<TaxableItem> taxableItemList = new LinkedList<>();
      TaxableItem taxableItem1 = TaxableItem.builder()
          .itemType("ProductItem")
          .itemId("Y0001")
          .itemTaxCode("AccTax")
          .quantity(1)
          .itemName("Yarn")
          .unitPrice(883.90)
          .build();

      TaxableItem taxableItem2 = TaxableItem.builder()
          .itemType("ShippingItem")
          .itemId("Y0002")
          .itemTaxCode("KidsTax")
          .quantity(2)
          .itemName("Yarn")
          .unitPrice(39.21)
          .build();

      taxableItemList.add(taxableItem1);
      taxableItemList.add(taxableItem2);

      return taxableItemList;
    }

  protected SourceAddress buildSourceAddress() {
    return SourceAddress.builder()
        .addressLine1("2000 Main Street")
        .postalCode("92614")
        .city("Alaska")
        .stateCode("CA")
        .countryCode("US")
        .build();
  }

  protected SourceAddress buildSourceAddressPR() {
    return SourceAddress.builder()
        .addressLine1("2000 Main Street")
        .postalCode("92614")
        .city("Alaska")
        .stateCode("US")
        .countryCode("PR")
        .build();
  }
  protected SourceAddress buildSourceAddressWithoutTaxDateCase() {
    return SourceAddress.builder()
        .addressLine1("2000 Main Street")
        .postalCode("92614")
        .city("Alaska")
        .stateCode("CA")
        .countryCode("CA")
        .build();
  }

  protected DestinationAddress buildDestinationAddress() {
    return DestinationAddress.builder()
        .addressLine1("2000 Main Street")
        .postalCode("92614")
        .city("Alaska")
        .stateCode("CA")
        .countryCode("US")
        .build();
  }

  protected DestinationAddress buildDestinationAddressPR() {
    return DestinationAddress.builder()
        .addressLine1("2000 Main Street")
        .postalCode("92614")
        .city("Alaska")
        .stateCode("US")
        .countryCode("PR")
        .build();
  }

  protected DestinationAddress buildDestinationAddressWithoutTaxDateCase() {
    return DestinationAddress.builder()
        .addressLine1("2000 Main Street")
        .postalCode("92614")
        .city("Alaska")
        .stateCode("CA")
        .countryCode("CA")
        .build();
  }

  protected DestinationAddress buildInvalidDestinationAddress() {
    return DestinationAddress.builder()
        .addressLine1("200 Main Street")
        .postalCode("110045")
        .city("Sydney")
        .stateCode("SD")
        .countryCode("AU")
        .build();
  }
  protected DestinationAddress buildInvalidDestinationAddressforDynamic() {
    return DestinationAddress.builder()
        .addressLine1("200 Main Street")
        .postalCode("110045")
        .city("Sydney")
        .stateCode("SD")
        .countryCode("DE")
        .build();
  }
  
  protected DestinationAddress buildInvalidStateCode() {
    return DestinationAddress.builder()
        .addressLine1("200 Main Street")
        .postalCode("110045")
        .city("Sydney")
        .stateCode("SD")
        .countryCode("CA")
        .build();
  }
  
  protected DestinationAddress buildDestinationAddressWithStateCodeON() {
    return DestinationAddress.builder()
        .addressLine1("2000 Main Street")
        .postalCode("92614")
        .city("Alaska")
        .stateCode("ON")
        .countryCode("CA")
        .build();
  }

  protected AvaTaxRequest buildAvataxRequest() {
    return AvaTaxRequest.builder()
        .lines(buildProductLine())
        .date(date.toString())
        .customerCode("customer")
        .addresses(buildAddress())
        .build();
  }

  protected List<com.domain_name.next.taxationapi.client.request.ProductLine> buildProductLine() {
    List<com.domain_name.next.taxationapi.client.request.ProductLine> taxableItemList = new LinkedList<>();

    com.domain_name.next.taxationapi.client.request.ProductLine productLine1 =
        com.domain_name.next.taxationapi.client.request.ProductLine.builder()
            .number(1)
            .quantity(1)
            .amount(883.90)
            .taxCode("PS081282")
            .itemCode("Y0001")
            .description("Yarn")
            .build();

    com.domain_name.next.taxationapi.client.request.ProductLine productLine2 =
        com.domain_name.next.taxationapi.client.request.ProductLine.builder()
            .number(2)
            .quantity(2)
            .amount(39.21)
            .taxCode("PS081282")
            .itemCode("Y0001")
            .description("Yarn")
            .build();

    taxableItemList.add(productLine1);
    taxableItemList.add(productLine2);

    return taxableItemList;
  }

  protected Address buildAddress() {
    return Address.builder()
        .shipFrom(buildShippingAddress())
        .shipTo(buildShippingAddress())
        .build();
  }

  protected ShippingAddress buildShippingAddress() {
    return ShippingAddress.builder()
        .line1("2000 Main Street")
        .city("Irvine")
        .region("CA")
        .country("US")
        .postalCode("92614")
        .build();
  }

  protected AvaTaxResponse buildAvataxResponse() {
    List<ProductLine> productLines = new LinkedList<>();
    List<ProductLineDetails> productLineDetailsList = new LinkedList<>();
    ProductLineDetails productLineDetails = ProductLineDetails.builder()
        .rate(0.0725)
        .taxType("Sales")
        .build();
    productLineDetailsList.add(productLineDetails);

    ProductLine productLine1 = ProductLine.builder()
        .itemCode("Y0001")
        .lineAmount(883.90)
        .taxCalculated(64.08)
        .quantity(1.0)
        .tax(64.08)
        .details(productLineDetailsList)
        .build();

    ProductLine productLine2 = ProductLine.builder()
        .itemCode("Y0002")
        .lineAmount(241.0)
        .quantity(1.0)
        .tax(17.47)
        .taxCalculated(17.47)
        .details(productLineDetailsList)
        .build();

    productLines.add(productLine1);
    productLines.add(productLine2);

    return AvaTaxResponse.builder()
        .code("16c8f490-6ea5-49a3-8ab8-e8ec41bc88b9")
        .totalTaxCalculated(81.55)
        .lines(productLines)
        .build();
  }
  
  protected TaxConfigProperties buildConfigProperties() {
    DynamicTaxConfigProperties dynamicTaxConfigProperties1 = DynamicTaxConfigProperties.builder()
    .taxationPolicy(TaxationPolicy.NET)
    .taxDate(0)
    .provider(Provider.AVALARA)
    .taxRate(Map.of("Sales", 19.0))
    .build();
    
    DynamicTaxConfigProperties dynamicTaxConfigProperties2 = DynamicTaxConfigProperties.builder()
        .taxationPolicy(TaxationPolicy.NET)
        .taxDate(1)
        .provider(Provider.CONFIG)
        .stateNames(Map.of("AB", "Alberta", "BC", "British Columbia", "MB", "Manitoba", "NB", "New Brunswick", "NL", "Newfoundland and Labrador", "NT", "Northwest Territories", "NS", "Nova Scotia", "NU", "Nunavut", "ON", "Ontario", "PE", "Prince Edward Island"))
        .stateTaxRates(Map.of("AB", Map.of("AccTax", Map.of("GST", 5.0), "DefaultTax", Map.of("GST", 5.0), "FullTax", Map.of("GST", 5.0), "KidsTax", Map.of("GST", 5.0), "ReducedTax", Map.of("GST", 5.0), "SLKidsTax", Map.of("GST", 5.0)), 
            "BC", Map.of("AccTax", Map.of("GST", 5.0, "PST", 7.0), "DefaultTax", Map.of("GST", 5.0, "PST", 7.0), "FullTax", Map.of("GST", 5.0, "PST", 7.0), "KidsTax", Map.of("GST", 5.0), "ReducedTax", Map.of("GST", 5.0), "SLKidsTax", Map.of("GST", 5.0)),
            "MB", Map.of("AccTax", Map.of("GST", 5.0, "PST", 7.0), "DefaultTax", Map.of("GST", 5.0, "PST", 7.0), "FullTax", Map.of("GST", 5.0, "PST", 7.0), "KidsTax", Map.of("GST", 5.0), "ReducedTax", Map.of("GST", 5.0), "SLKidsTax", Map.of("GST", 5.0)),
            "NB", Map.of("AccTax", Map.of("HST", 15.0), "DefaultTax", Map.of("HST", 15.0), "FullTax", Map.of("HST", 15.0), "KidsTax", Map.of("HST", 15.0), "ReducedTax", Map.of("HST", 15.0), "SLKidsTax", Map.of("HST", 15.0)),
            "NL", Map.of("AccTax", Map.of("HST", 15.0), "DefaultTax", Map.of("HST", 15.0), "FullTax", Map.of("HST", 15.0), "KidsTax", Map.of("HST", 15.0), "ReducedTax", Map.of("HST", 15.0), "SLKidsTax", Map.of("HST", 15.0)),
            "NT", Map.of("AccTax", Map.of("GST", 5.0), "DefaultTax", Map.of("GST", 5.0), "FullTax", Map.of("GST", 5.0), "KidsTax", Map.of("GST", 5.0), "ReducedTax", Map.of("GST", 5.0), "SLKidsTax", Map.of("GST", 5.0)),
            "NS", Map.of("AccTax", Map.of("HST", 15.0), "DefaultTax", Map.of("HST", 15.0), "FullTax", Map.of("HST", 15.0), "KidsTax", Map.of("HST", 5.0), "ReducedTax", Map.of("HST", 5.0), "SLKidsTax", Map.of("HST", 5.0)),
            "NU", Map.of("AccTax", Map.of("GST", 5.0), "DefaultTax", Map.of("GST", 5.0), "FullTax", Map.of("GST", 5.0), "KidsTax", Map.of("GST", 5.0), "ReducedTax", Map.of("GST", 5.0), "SLKidsTax", Map.of("GST", 5.0)),
            "ON", Map.of("AccTax", Map.of("HST", 13.0), "DefaultTax", Map.of("HST", 13.0), "FullTax", Map.of("HST", 13.0), "KidsTax", Map.of("HST", 5.0), "ReducedTax", Map.of("HST", 5.0), "SLKidsTax", Map.of("HST", 5.0)),
            "PE", Map.of("AccTax", Map.of("HST", 15.0), "DefaultTax", Map.of("HST", 15.0), "FullTax", Map.of("HST", 15.0), "KidsTax", Map.of("HST", 5.0), "ReducedTax", Map.of("HST", 5.0), "SLKidsTax", Map.of("HST", 5.0))
            ))
        .build();
    
    Map<String, DynamicTaxConfigProperties> dynamicTaxRates = Map.of("US", dynamicTaxConfigProperties1, "CA", dynamicTaxConfigProperties2);
    
    Map<String, StaticTaxConfigProperties> staticTaxRates = Map.of("IT", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 22.0, "ReducedTax", 22.0, "KidsTax", 22.0, "NoTax", 0.0)).build(),
        "ES", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 21.0, "ReducedTax", 21.0, "KidsTax", 21.0, "NoTax", 0.0)).build(),
        "DE", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 19.0, "ReducedTax", 19.0, "KidsTax", 19.0, "NoTax", 0.0)).build(),
        "FR", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 20.0, "ReducedTax", 20.0, "KidsTax", 20.0, "NoTax", 0.0)).build(),
        "NL", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 21.0, "ReducedTax", 21.0, "KidsTax", 21.0, "NoTax", 0.0)).build(),
        "UK", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 20.0, "ReducedTax", 0.0, "KidsTax", 0.0, "NoTax", 0.0)).build(),
        "AT", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 20.0, "ReducedTax", 0.0, "KidsTax", 0.0, "NoTax", 0.0)).build(),
        "IE", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 23.0, "ReducedTax", 0.0, "KidsTax", 0.0, "NoTax", 0.0)).build(),
        "BE", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 21.0, "ReducedTax", 0.0, "KidsTax", 0.0, "NoTax", 0.0)).build(),
        "SE", StaticTaxConfigProperties.builder().taxationPolicy(TaxationPolicy.GROSS).provider(Provider.CONFIG).taxRate(Map.of("FullTax", 25.0, "ReducedTax", 0.0, "KidsTax", 0.0, "NoTax", 0.0)).build()
        );

    return TaxConfigProperties.builder().dynamicTaxRates(dynamicTaxRates).staticTaxRates(staticTaxRates).build();
  }

  protected StaticTaxRateResponse buildProductStaticTaxRateResponse() {
    Map<String, Double> taxRate = new HashMap<>();
    taxRate.put("FullTax", 22.0);
    taxRate.put("ReducedTax", 22.0);
    taxRate.put("KidsTax", 22.0);
    return StaticTaxRateResponse.builder()
        .countryCode("IT")
        .taxRates(taxRate)
        .taxationPolicy(TaxationPolicy.GROSS)
        .build();
  }
 
  protected TaxConfigResponse buildProductTaxRateConfigResponse() {
    return TaxConfigResponse.builder()
        .countryCode("IT")
        .taxationPolicy(TaxationPolicy.GROSS)
        .provider(Provider.CONFIG)
        .itemTaxCodes(List.of(ItemTaxCodes.builder().itemTaxCode("FullTax")
            .taxRates(List.of(TaxRates.builder().taxType(null).taxRate(22.0).build()))
            .build())) 
        .build();
  }

  protected TaxConfigResponse buildProductTaxRateConfigResponseCA() {
    List<ItemTaxCodes> itemTaxCodesList = new ArrayList<>();
    itemTaxCodesList.add(ItemTaxCodes.builder()
        .itemTaxCode("AccTax").taxRates(List.of(TaxRates.builder().taxType("HST").taxRate(13.0).build()))
        .build());
    itemTaxCodesList.add(ItemTaxCodes.builder()
        .itemTaxCode("DefaultTax").taxRates(List.of(TaxRates.builder().taxType("HST").taxRate(13.0).build()))
        .build());
    itemTaxCodesList.add(ItemTaxCodes.builder()
        .itemTaxCode("FullTax").taxRates(List.of(TaxRates.builder().taxType("HST").taxRate(13.0).build()))
        .build());
    itemTaxCodesList.add(ItemTaxCodes.builder()
        .itemTaxCode("KidsTax").taxRates(List.of(TaxRates.builder().taxType("HST").taxRate(5.0).build()))
        .build());
    itemTaxCodesList.add(ItemTaxCodes.builder()
        .itemTaxCode("ReducedTax").taxRates(List.of(TaxRates.builder().taxType("HST").taxRate(5.0).build()))
        .build());
    itemTaxCodesList.add(ItemTaxCodes.builder()
        .itemTaxCode("SLKidsTax").taxRates(List.of(TaxRates.builder().taxType("HST").taxRate(5.0).build()))
        .build());
    return TaxConfigResponse.builder()
        .countryCode("CA")
        .taxationPolicy(TaxationPolicy.NET)
        .provider(Provider.CONFIG)
        .stateTaxes(List.of(StateTaxes.builder()
            .stateCode("ON")
            .stateName("Ontario")
            .itemTaxCodes(itemTaxCodesList)
            .build()))
        .build();
  }
  
  public static Problem buildProblem(URI uri, StatusType status, String details) {
    return Problem.builder()
        .withTitle(status.getReasonPhrase())
        .withType(uri)
        .withStatus(status)
        .withDetail(details)
        .build();
  }
  
  protected TaxConfigResponse buildProductTaxRateConfigResponseStates() {
    return TaxConfigResponse.builder()
        .countryCode("CA")
        .provider(Provider.CONFIG)
        .stateTaxes(List.of(StateTaxes.builder().stateCode("AB").build()))
        .taxationPolicy(TaxationPolicy.NET)
        .build();
  }
  
  protected TaxConfigResponse buildProductTaxRateConfigResponseUSStates() {
    return TaxConfigResponse.builder()
        .countryCode("US")
        .provider(Provider.AVALARA)
        .itemTaxCodes(List.of(ItemTaxCodes.builder().itemTaxCode(null)
            .taxRates(List.of(TaxRates.builder().taxType("Sales").taxRate(12.0).build(),TaxRates.builder().taxType("Default").taxRate(12.0).build()))
            .build()))
        .taxationPolicy(TaxationPolicy.NET)
        .build();
  }

  protected TaxConfigResponse buildProductTaxRateConfigResponsePRStates() {
    return TaxConfigResponse.builder()
        .countryCode("PR")
        .provider(Provider.AVALARA)
        .itemTaxCodes(List.of(ItemTaxCodes.builder().itemTaxCode(null)
            .taxRates(List.of(TaxRates.builder().taxType("Sales").taxRate(12.0).build(),TaxRates.builder().taxType("Default").taxRate(12.0).build()))
            .build()))
        .taxationPolicy(TaxationPolicy.NET)
        .build();
  }
  protected ProductTaxRateRequest buildProductTaxRateRequestWithValidUnitPrice() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParamsWithValidUnitPrice())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildDestinationAddressWithStateCodeON())
        .build();
  }
  
  protected ProductTaxRateRequest buildProductTaxRateRequestWithInValidUnitPrice() {
    return ProductTaxRateRequest.builder()
        .taxDate(date)
        .taxableItems(buildTaxableItemParamsWithInvalidUnitPrice())
        .sourceAddress(buildSourceAddress())
        .destinationAddress(buildDestinationAddressWithStateCodeON())
        .build();
  }
  
  protected List<TaxableItem> buildTaxableItemParamsWithValidUnitPrice() {
    List<TaxableItem> taxableItemList = new LinkedList<>();
    TaxableItem taxableItem1 = TaxableItem.builder()
        .itemType("ProductItem")
        .itemId("Y0001")
        .itemTaxCode("AccTax")
        .quantity(1)
        .itemName("Yarn")
        .unitPrice(10.00)
        .build();
    
    TaxableItem taxableItem2 = TaxableItem.builder()
        .itemType("ShippingItem")
        .itemId("Y0002")
        .itemTaxCode("SLKidsTax")
        .quantity(2)
        .itemName("Yarn")
        .unitPrice(0.0)
        .build();
    
    taxableItemList.add(taxableItem1);
    taxableItemList.add(taxableItem2);
    
    return taxableItemList;
  }
  
  protected List<TaxableItem> buildTaxableItemParamsWithInvalidUnitPrice() {
    List<TaxableItem> taxableItemList = new LinkedList<>();
    TaxableItem taxableItem1 = TaxableItem.builder()
        .itemType("ProductItem")
        .itemId("Y0001")
        .itemTaxCode("AccTax")
        .quantity(1)
        .itemName("Yarn")
        .unitPrice(-10.00)
        .build();

    TaxableItem taxableItem2 = TaxableItem.builder()
        .itemType("ShippingItem")
        .itemId("Y0002")
        .itemTaxCode("SLKidsTax")
        .quantity(2)
        .itemName("Yarn")
        .unitPrice(-39.21)
        .build();

    taxableItemList.add(taxableItem1);
    taxableItemList.add(taxableItem2);

    return taxableItemList;
  }

  public static String readFromFile(String fileName) {
    String xmlString;
    BufferedReader bufferedReader = null;
    StringBuilder lineBuilder = new StringBuilder();
    String line;
    try {
      ClassPathResource classPathResource = new ClassPathResource(fileName);
      InputStream inputStream = classPathResource.getInputStream();

      bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      while ((line = bufferedReader.readLine()) != null) {
        lineBuilder.append(line);
      }
    } catch (IOException ioe) {
      log.error(ioe.getMessage());
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException e) {
          log.error(e.getMessage());
        }
      }
    }
    xmlString = lineBuilder.toString();
    return xmlString;
  }

  protected ProductTaxRateResponse buildProductTaxRateResponsePR() {
    List<TaxedItem> taxedItemList = new LinkedList<>();
    TaxedItem taxedItem1 = TaxedItem.builder()
        .itemId("Y0001")
        .taxRate(Map.of("Sales",12.0))
        .taxAmount(480.0)
        .build();

    TaxedItem taxedItem2 = TaxedItem.builder()
        .itemId("Y0002")
        .taxRate(Map.of("Sales",12.0))
        .taxAmount(120.0)
        .build();
    taxedItemList.add(taxedItem1);
    taxedItemList.add(taxedItem2);
    return ProductTaxRateResponse.builder()
        .taxedItems(taxedItemList)
        .totalTaxAmount(81.55)
        .taxGotCalculated(true)
        .transactionId("16c8f490-6ea5-49a3-8ab8-e8ec41bc88b9")
        .taxationPolicy(TaxConfigUtil.getDynamicTaxationPolicy("PR").getValue())
        .build();
  }

  protected TaxConfigResponse buildProductTaxRateConfigResponseAvalara() {
    return TaxConfigResponse.builder()
        .countryCode("US")
        .provider(Provider.AVALARA)
        .itemTaxCodes(List.of(ItemTaxCodes.builder().itemTaxCode(null)
            .taxRates(List.of(TaxRates.builder().taxType("Sales").taxRate(19.0).build()))
            .build()))
        .taxationPolicy(TaxationPolicy.NET)
        .build();
  }

  protected TaxConfigResponse buildProductTaxRateConfigResponseForUS() {
    return TaxConfigResponse.builder()
        .countryCode("US")
        .taxationPolicy(TaxationPolicy.NET)
        .provider(Provider.AVALARA)
        .itemTaxCodes(List.of(ItemTaxCodes.builder().itemTaxCode(null)
            .taxRates(List.of(TaxRates.builder().taxType("Sales").taxRate(12.0).build(),TaxRates.builder().taxType("Default").taxRate(12.0).build()))
            .build()))
        .build();
  }

}
