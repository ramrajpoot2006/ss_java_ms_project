package com.domain_name.fulfillment.configuration.api.shipping.method;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface ShippingMethodRepository extends CrudRepository<ShippingMethodEntity, Integer> {

  Optional<ShippingMethodEntity> findByShippingMethodId(UUID id);

  Stream<ShippingMethodEntity> findBySiteIdNameOrderByPosition(String name);

  @Query(nativeQuery = true, value =
  "select count(smc.shipping_method_id) > 0 FROM site_shipping_method_configuration smc"
      + "   inner join shipping_method_channel_mapping smcm on (smc.id = smcm.shipping_method_id and smcm.channel_id in (select channel_id from channel where LOWER(channel_name) in (:channels)))  or length(concat(:channels)) = 0"
      + "   inner join shipping_method_product_type_mapping smpm on (smc.id = smpm.shipping_method_id and smpm.product_type_id in (select product_type_id from product_type where product_type_name in (:productTypes))) or length(concat(:productTypes)) = 0"
      + "   inner join shipping_method_carrier_string smcs on (smc.id = smcs.shipping_method_id and smcs.carrier_string in (:carrierStringRecords)) or length(concat(:carrierStringRecords)) = 0 "
      + " where smc.site_id = :siteId and smc.enabled = true and (smc.carrier_service=:carrierService or :carrierService is null) and ((:availabilityStatus\\:\\:varchar[] && availability_status) or (''=  ANY(:availabilityStatus) and availability_status is null) "
      + " or cardinality (:availabilityStatus) = 0)")
      Boolean existsByUniqueSiteIdParameters(@Param("siteId") Integer siteId,
          @Param("channels") List<String> channels,
          @Param("productTypes") List<String> productTypes,
          @Param("carrierStringRecords") List<String> carrierStringRecords,
          @Param("carrierService") String carrierService,
          @Param("availabilityStatus") String[] availabilityStatus);

  @Query(nativeQuery = true, value = "select count(smc.id) > 0" +
      " from site_shipping_method_configuration smc " +
      "         inner join shipping_method_channel_mapping smcm on smc.id = smcm.shipping_method_id inner join channel c on (c.channel_id = smcm.channel_id and LOWER(c.channel_name) in (:channels)) or length(concat(:channels)) = 0 "
      +
      "         inner join shipping_method_product_type_mapping smp on smc.id = smp.shipping_method_id inner join product_type p on (p.product_type_id = smp.product_type_id and p.product_type_name in (:productTypes)) or length(concat(:productTypes)) = 0 "
      +
      "         inner join shipping_method_carrier_string smcs on (smc.id = smcs.shipping_method_id and smcs.carrier_string in (:carrierStringRecords)) or length(concat(:carrierStringRecords)) = 0 "
      +
      "where smc.site_id = :siteId " +
      "and (smc.carrier_service =:carrierService or :carrierService is null) " +
      "and smc.id <> :shippingMethodId and smc.enabled=true and ((:availabilityStatus\\:\\:varchar[] && availability_status) or cardinality (:availabilityStatus) = 0 )")
  Boolean existsShippingMethodsByUniqueParamsAndShippingMethodId(@Param("siteId") Integer siteId,
      @Param("channels") List<String> channels,
      @Param("productTypes") List<String> productTypes,
      @Param("carrierStringRecords") List<String> carrierStringRecords,
      @Param("carrierService") String carrierService,
      @Param("shippingMethodId") Integer shippingMethodId,
      @Param("availabilityStatus") String[] availabilityStatus);

  Integer deleteByShippingMethodId(UUID id);

  @Modifying
  @Query(nativeQuery = true, value = "update site_shipping_method_configuration set default_shipping_method = false where site_id = :siteId and enabled = true and default_shipping_method = true")
  Integer setDefaultSiteIdShippingMethodToNotDefault(@Param("siteId") Integer siteId);
}
