package com.domain_name.fulfillment.configuration.api.shipping.method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.domain_name.fulfillment.configuration.api.channel.ChannelEntity;
import com.domain_name.fulfillment.configuration.api.channel.ChannelEnum;
import com.domain_name.fulfillment.configuration.api.channel.ChannelRepository;
import com.domain_name.fulfillment.configuration.api.products.type.ProductTypeEntity;
import com.domain_name.fulfillment.configuration.api.products.type.ProductTypeEnum;
import com.domain_name.fulfillment.configuration.api.products.type.ProductTypeRepository;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringEntity;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringRecord;
import com.domain_name.fulfillment.configuration.api.site.SiteIdEntity;

@ExtendWith(SpringExtension.class)
public class ShippingMethodEntityConverterTest {

  @Mock
  ChannelRepository channelRepository;

  @Mock
  ProductTypeRepository productTypeRepository;

  @InjectMocks
  ShippingMethodEntityConverter shippingMethodEntityConverter;

  public static ShippingMethodEntity getEntity() {
    List<CarrierStringEntity> carrierStrings = List.of(
        CarrierStringEntity.builder().carrierString("carrier1")
            .carrierStringId(UUID.fromString("a47e1dd0-3a9a-44bc-ad3c-a5e7ed6d2b94")).build(),
        CarrierStringEntity.builder().carrierString("carrier2")
            .carrierStringId(UUID.fromString("de538b8f-de7c-4391-95df-351bd910edfe")).build()
    );
    List<ProductTypeEntity> productTypes = getProductTypes();
    List<ChannelEntity> channels = getChannels();

    return ShippingMethodEntity.builder()
        .shippingMethodId(UUID.randomUUID())
        .name(Map.of("de-AT", "Standardlieferung"))
        .description(Map.of("de-AT", "Standardlieferung"))
        .prices(getPrice())
        .fulfillmentTypes(List.of("HOMEDELIVERY"))
        .enabled(true)
        .isDefault(true)
        .customId("customId")
        .carrierName("carrierName")
        .carrierString("carrierString")
        .carrierService("Express")
        .taxClassId("taxId")
        .position((short) 0)
        .maxDaysToDeliver((short) 2.0)
        .minDaysToDeliver((short) 2.0)
        .siteId(getSiteId())
        .channels(channels)
        .productTypes(productTypes)
        .carrierStrings(carrierStrings)
        .createdBy("someone")
        .createdDate(LocalDateTime.of(2000, 9, 22, 2, 22, 22))
        .modifiedBy("someonte")
        .modifiedDate(LocalDateTime.of(2000, 9, 22, 2, 22, 22))
        .rules(List.of())
        .build();
  }

  private static List<ChannelEntity> getChannels() {
    return List.of(
        ChannelEntity.builder().channelName(ChannelEnum.WEB).build(),
        ChannelEntity.builder().channelName(ChannelEnum.domain_nameCONFIRMEDAPP).build()
    );
  }

  private static List<ProductTypeEntity> getProductTypes() {
    return List.of(
        ProductTypeEntity.builder().productTypeName(ProductTypeEnum.INLINE).build(),
        ProductTypeEntity.builder().productTypeName(ProductTypeEnum.BACKORDER).build()
    );
  }

  private static SiteIdEntity getSiteId() {
    return SiteIdEntity.builder()
        .name("domain_name-AT")
        .build();
  }

  private static ShippingMethodPrice getPrice() {
    return ShippingMethodPrice.builder()
        .threshold(3.0)
        .baseFixedPrice(3.0)
        .shipmentUpsell(3.0)
        .memberFixedPrices(MemberFixedPrices.builder()
            .tier1(1.0)
            .tier2(2.0)
            .tier3(3.0)
            .tier4(3.0)
            .build())
        .build();
  }

  @Test
  void testFromShippingPostRequestConversion() {
    ShippingMethodPostRequest postRequest = getPostRequest();
    SiteIdEntity siteId = getSiteId();
    List<ChannelEntity> channels = getChannels();
    List<ProductTypeEntity> productTypes = getProductTypes();
    Mockito.when(channelRepository.findByChannelNameIn(Mockito.anyList())).thenReturn(channels);
    Mockito.when(productTypeRepository.findByProductTypeNameIn(Mockito.anyList()))
        .thenReturn(productTypes);

    ShippingMethodEntity response = shippingMethodEntityConverter.fromShippingPostRequest(
        postRequest, siteId);

    Assertions.assertEquals(response.getName(), postRequest.getName());
    Assertions.assertEquals(response.getDescription(), postRequest.getDescription());
    Assertions.assertEquals(response.getEnabled(), postRequest.getEnabled());
    Assertions.assertEquals(response.getCustomId(), postRequest.getCustomId());
    Assertions.assertEquals(response.getIsDefault(), postRequest.getIsDefault());
    Assertions.assertEquals(response.getCarrierName(), postRequest.getCarrierName());
    Assertions.assertEquals(response.getCarrierString(), postRequest.getCarrierString());
    Assertions.assertEquals(response.getCarrierService(), postRequest.getCarrierService());
    Assertions.assertEquals(response.getTaxClassId(), postRequest.getTaxClassId());
    Assertions.assertEquals(response.getPosition(), postRequest.getPosition());
    Assertions.assertEquals(response.getMaxDaysToDeliver(), getPostRequest().getMaxDaysToDeliver());
    Assertions.assertEquals(response.getMinDaysToDeliver(), postRequest.getMinDaysToDeliver());
    Assertions.assertEquals(response.getCreatedBy(), postRequest.getCreatedBy());
    Assertions.assertEquals(response.getSiteId(), siteId);
    Assertions.assertEquals(response.getChannels(), channels);
    Assertions.assertEquals(response.getProductTypes(), productTypes);
    Assertions.assertEquals(response.getPrices(), postRequest.getShippingMethodPrice());
    Assertions.assertTrue(
        response.getFulfillmentTypes().contains(FulfillmentTypeEnum.HOMEDELIVERY.toString()));
  }

  @Test
  void testMergeShippingMethodsPatchRequestBasicParameters() {
    ShippingMethodPatchRequest patchRequest = getPatchRequest();
    ShippingMethodEntity existingShippingMethod = getEntity();

    ShippingMethodEntity modifiedEntity = shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(
        existingShippingMethod, patchRequest);

    Assertions.assertEquals(modifiedEntity.getName(), patchRequest.getName());
    Assertions.assertEquals(modifiedEntity.getCustomId(), patchRequest.getCustomId());
    Assertions.assertEquals(modifiedEntity.getDescription(), patchRequest.getDescription());
    Assertions.assertEquals(modifiedEntity.getEnabled(), patchRequest.getEnabled());
    Assertions.assertEquals(modifiedEntity.getIsDefault(), patchRequest.getIsDefault());
    Assertions.assertEquals(modifiedEntity.getCarrierName(), patchRequest.getCarrierName());
    Assertions.assertEquals(modifiedEntity.getCarrierString(), patchRequest.getCarrierString());
    Assertions.assertEquals(modifiedEntity.getCarrierService(), patchRequest.getCarrierService());
    Assertions.assertEquals(modifiedEntity.getPosition(), patchRequest.getPosition());
    Assertions.assertEquals(modifiedEntity.getMinDaysToDeliver(),
        patchRequest.getMinDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getMaxDaysToDeliver(),
        patchRequest.getMaxDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getPrices(), patchRequest.getShippingMethodPrice());
    Assertions.assertEquals(modifiedEntity.getModifiedBy(), patchRequest.getModifiedBy());
    Assertions.assertTrue(
        modifiedEntity.getFulfillmentTypes().contains(FulfillmentTypeEnum.PUDO.toString()));
    Assertions.assertEquals(modifiedEntity.getChannels(), existingShippingMethod.getChannels());
    Assertions.assertEquals(modifiedEntity.getProductTypes(),
        existingShippingMethod.getProductTypes());
    Assertions.assertEquals(modifiedEntity.getCarrierStrings(),
        existingShippingMethod.getCarrierStrings());

    Mockito.verify(channelRepository, Mockito.times(0)).findByChannelNameIn(Mockito.any());
    Mockito.verify(productTypeRepository, Mockito.times(0)).findByProductTypeNameIn(Mockito.any());
  }

  @Test
  void testMergeShippingMethodsPatchRequestChannels() {
    ShippingMethodPatchRequest patchRequest = ShippingMethodPatchRequest.builder().channels(List.of(
        ChannelEnum.YEEZYSUPPLY)).build();
    ShippingMethodEntity existingShippingMethod = getEntity();
    List<ChannelEntity> channels = List.of(
        ChannelEntity.builder().channelName(ChannelEnum.YEEZYSUPPLY).build()
    );
    Mockito.when(channelRepository.findByChannelNameIn(Mockito.anyList())).thenReturn(channels);

    ShippingMethodEntity modifiedEntity = shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(
        existingShippingMethod, patchRequest);
    Assertions.assertEquals(modifiedEntity.getChannels(), channels);

    Assertions.assertEquals(modifiedEntity.getName(), existingShippingMethod.getName());
    Assertions.assertEquals(modifiedEntity.getCustomId(), existingShippingMethod.getCustomId());
    Assertions.assertEquals(modifiedEntity.getDescription(),
        existingShippingMethod.getDescription());
    Assertions.assertEquals(modifiedEntity.getEnabled(), existingShippingMethod.getEnabled());
    Assertions.assertEquals(modifiedEntity.getIsDefault(), existingShippingMethod.getIsDefault());
    Assertions.assertEquals(modifiedEntity.getCarrierName(),
        existingShippingMethod.getCarrierName());
    Assertions.assertEquals(modifiedEntity.getCarrierString(),
        existingShippingMethod.getCarrierString());
    Assertions.assertEquals(modifiedEntity.getCarrierService(),
        existingShippingMethod.getCarrierService());
    Assertions.assertEquals(modifiedEntity.getPosition(), existingShippingMethod.getPosition());
    Assertions.assertEquals(modifiedEntity.getMinDaysToDeliver(),
        existingShippingMethod.getMinDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getMaxDaysToDeliver(),
        existingShippingMethod.getMaxDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getPrices(), existingShippingMethod.getPrices());
    Assertions.assertEquals(modifiedEntity.getFulfillmentTypes(),
        existingShippingMethod.getFulfillmentTypes());
    Assertions.assertEquals(modifiedEntity.getProductTypes(),
        existingShippingMethod.getProductTypes());
    Assertions.assertEquals(modifiedEntity.getCarrierStrings(),
        existingShippingMethod.getCarrierStrings());

    Mockito.verify(channelRepository, Mockito.times(1)).findByChannelNameIn(Mockito.any());
    Mockito.verify(productTypeRepository, Mockito.times(0)).findByProductTypeNameIn(Mockito.any());
  }

  @Test
  void testMergeShippingMethodsPatchRequestBasicProductTypes() {
    ShippingMethodPatchRequest patchRequest = ShippingMethodPatchRequest.builder()
        .productTypes(List.of(ProductTypeEnum.BACKORDER, ProductTypeEnum.PERSONALIZED))
        .build();
    ShippingMethodEntity existingShippingMethod = getEntity();
    List<ProductTypeEntity> productTypes = List.of(
        ProductTypeEntity.builder().productTypeName(ProductTypeEnum.BACKORDER).build(),
        ProductTypeEntity.builder().productTypeName(ProductTypeEnum.PERSONALIZED).build()
    );
    Mockito.when(productTypeRepository.findByProductTypeNameIn(Mockito.anyList()))
        .thenReturn(productTypes);

    ShippingMethodEntity modifiedEntity = shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(
        existingShippingMethod, patchRequest);
    Assertions.assertEquals(modifiedEntity.getProductTypes(), productTypes);

    Assertions.assertEquals(modifiedEntity.getName(), existingShippingMethod.getName());
    Assertions.assertEquals(modifiedEntity.getCustomId(), existingShippingMethod.getCustomId());
    Assertions.assertEquals(modifiedEntity.getDescription(),
        existingShippingMethod.getDescription());
    Assertions.assertEquals(modifiedEntity.getEnabled(), existingShippingMethod.getEnabled());
    Assertions.assertEquals(modifiedEntity.getIsDefault(), existingShippingMethod.getIsDefault());
    Assertions.assertEquals(modifiedEntity.getCarrierName(),
        existingShippingMethod.getCarrierName());
    Assertions.assertEquals(modifiedEntity.getCarrierString(),
        existingShippingMethod.getCarrierString());
    Assertions.assertEquals(modifiedEntity.getCarrierService(),
        existingShippingMethod.getCarrierService());
    Assertions.assertEquals(modifiedEntity.getPosition(), existingShippingMethod.getPosition());
    Assertions.assertEquals(modifiedEntity.getMinDaysToDeliver(),
        existingShippingMethod.getMinDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getMaxDaysToDeliver(),
        existingShippingMethod.getMaxDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getPrices(), existingShippingMethod.getPrices());
    Assertions.assertEquals(modifiedEntity.getFulfillmentTypes(),
        existingShippingMethod.getFulfillmentTypes());
    Assertions.assertEquals(modifiedEntity.getChannels(), existingShippingMethod.getChannels());
    Assertions.assertEquals(modifiedEntity.getCarrierStrings(),
        existingShippingMethod.getCarrierStrings());

    Mockito.verify(channelRepository, Mockito.times(0)).findByChannelNameIn(Mockito.any());
    Mockito.verify(productTypeRepository, Mockito.times(1)).findByProductTypeNameIn(Mockito.any());
  }

  @Test
  void testMergeShippingMethodsPatchRequestBasicCarrierString() {
    List<String> expectedCarrierStrings = List.of("carrier1", "updated", "notExist", "new");
    ShippingMethodPatchRequest patchRequest = ShippingMethodPatchRequest.builder()
        .carrierStringRecords(List.of(
            CarrierStringRecord.builder().carrierStringId("de538b8f-de7c-4391-95df-351bd910edfe")
                .carrierString("updated").build(),
            CarrierStringRecord.builder().carrierStringId("afeba1e8-bbdb-4cbc-bb68-517365667990")
                .carrierString("notExist").build(),
            CarrierStringRecord.builder().carrierString("new").build()
        ))
        .build();

    ShippingMethodEntity existingShippingMethod = getEntity();
    ShippingMethodEntity modifiedEntity = shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(
        existingShippingMethod, patchRequest);

    Assertions.assertEquals(modifiedEntity.getCarrierStrings().size(), 4);
    Assertions.assertEquals(
        modifiedEntity.getCarrierStrings().stream().map(CarrierStringEntity::getCarrierString)
            .toList(), expectedCarrierStrings);

    Assertions.assertEquals(modifiedEntity.getName(), existingShippingMethod.getName());
    Assertions.assertEquals(modifiedEntity.getCustomId(), existingShippingMethod.getCustomId());
    Assertions.assertEquals(modifiedEntity.getDescription(),
        existingShippingMethod.getDescription());
    Assertions.assertEquals(modifiedEntity.getEnabled(), existingShippingMethod.getEnabled());
    Assertions.assertEquals(modifiedEntity.getIsDefault(), existingShippingMethod.getIsDefault());
    Assertions.assertEquals(modifiedEntity.getCarrierName(),
        existingShippingMethod.getCarrierName());
    Assertions.assertEquals(modifiedEntity.getCarrierString(),
        existingShippingMethod.getCarrierString());
    Assertions.assertEquals(modifiedEntity.getCarrierService(),
        existingShippingMethod.getCarrierService());
    Assertions.assertEquals(modifiedEntity.getPosition(), existingShippingMethod.getPosition());
    Assertions.assertEquals(modifiedEntity.getMinDaysToDeliver(),
        existingShippingMethod.getMinDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getMaxDaysToDeliver(),
        existingShippingMethod.getMaxDaysToDeliver());
    Assertions.assertEquals(modifiedEntity.getPrices(), existingShippingMethod.getPrices());
    Assertions.assertEquals(modifiedEntity.getFulfillmentTypes(),
        existingShippingMethod.getFulfillmentTypes());
    Assertions.assertEquals(modifiedEntity.getChannels(), existingShippingMethod.getChannels());
    Assertions.assertEquals(modifiedEntity.getProductTypes(),
        existingShippingMethod.getProductTypes());

    Mockito.verify(channelRepository, Mockito.times(0)).findByChannelNameIn(Mockito.any());
    Mockito.verify(productTypeRepository, Mockito.times(0)).findByProductTypeNameIn(Mockito.any());
  }

  private ShippingMethodPostRequest getPostRequest() {
    return ShippingMethodPostRequest.builder()
        .siteId("domain_name-AT")
        .fulfillmentTypes(List.of(FulfillmentTypeEnum.HOMEDELIVERY))
        .channels(List.of(ChannelEnum.WEB, ChannelEnum.domain_nameCONFIRMEDAPP))
        .name(Map.of("de-AT", "Standardlieferung"))
        .description(Map.of("de-AT", "Standardlieferung"))
        .enabled(true)
        .isDefault(true)
        .productTypes(List.of(ProductTypeEnum.INLINE, ProductTypeEnum.BACKORDER))
        .carrierStringRecords(List.of("carrier1", "carrier2"))
        .carrierName("carrierName")
        .carrierString("carrierString")
        .carrierService("Express")
        .position((short) 0)
        .shippingMethodPrice(ShippingMethodPrice.builder()
            .threshold(3.0)
            .baseFixedPrice(3.0)
            .shipmentUpsell(3.0)
            .memberFixedPrices(MemberFixedPrices.builder()
                .tier1(1.0)
                .tier2(2.0)
                .tier3(3.0)
                .tier4(3.0)
                .build())
            .build())
        .createdBy("someone")
        .customId("customId")
        .taxClassId("taxId")
        .maxDaysToDeliver((short) 2)
        .minDaysToDeliver((short) 2)

        .build();
  }

  private ShippingMethodPatchRequest getPatchRequest() {
    return ShippingMethodPatchRequest.builder()
        .name(Map.of("de-AT", "otherName"))
        .customId("otherId")
        .description(Map.of("de-AT", "OtherDescription"))
        .enabled(false)
        .isDefault(false)
        .fulfillmentTypes(List.of(FulfillmentTypeEnum.PUDO))
        .carrierName("otherCarrier")
        .carrierString("otherCarrierString")
        .carrierService("otherCarrierService")
        .taxClassId("taxId")
        .position((short) 1)
        .minDaysToDeliver((short) 44)
        .maxDaysToDeliver((short) 55)
        .shippingMethodPrice(getPrice())
        .modifiedBy("someuser")
        .build();
  }
  
  @Test
  void testFromShippingPostRequestConversionWhenCustomIdIsNull() {
    ShippingMethodPostRequest postRequest = getPostRequest();
    postRequest.setCustomId(null);
    SiteIdEntity siteId = getSiteId();
    List<ChannelEntity> channels = getChannels();
    List<ProductTypeEntity> productTypes = getProductTypes();
    Mockito.when(channelRepository.findByChannelNameIn(Mockito.anyList())).thenReturn(channels);
    Mockito.when(productTypeRepository.findByProductTypeNameIn(Mockito.anyList()))
        .thenReturn(productTypes);

    ShippingMethodEntity response = shippingMethodEntityConverter
        .fromShippingPostRequest(postRequest, siteId);

    Assertions.assertNull(response.getCustomId());
  }
}