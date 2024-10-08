package com.domain_name.fulfillment.configuration.api.shipping.method;

import static com.domain_name.fulfillment.configuration.api.exception.ErrorConstants.RECORD_NOT_FOUND_CODE_SHIPPING;
import static com.domain_name.fulfillment.configuration.api.exception.ErrorConstants.SITEID_NOT_FOUND_CODE;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpClientErrorException;

import com.domain_name.fulfillment.configuration.api.channel.ChannelEntity;
import com.domain_name.fulfillment.configuration.api.channel.ChannelEnum;
import com.domain_name.fulfillment.configuration.api.constant.DbConstants;
import com.domain_name.fulfillment.configuration.api.exception.ErrorConstants;
import com.domain_name.fulfillment.configuration.api.exception.NotFoundException;
import com.domain_name.fulfillment.configuration.api.exception.ValidationException;
import com.domain_name.fulfillment.configuration.api.products.type.ProductTypeEntity;
import com.domain_name.fulfillment.configuration.api.products.type.ProductTypeEnum;
import com.domain_name.fulfillment.configuration.api.sfcc.SFCCService;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringEntity;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringRecord;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringService;
import com.domain_name.fulfillment.configuration.api.site.SiteIdEntity;
import com.domain_name.fulfillment.configuration.api.site.SiteIdRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@Service
public class ShippingMethodService {

  private static final String EMPTY = "";

  public final SiteIdRepository siteIdRepository;
  public final ShippingMethodRepository shippingMethodRepository;
  public final ShippingMethodEntityConverter shippingMethodEntityConverter;
  public final SFCCService sfccService;
  public final CarrierStringService carrierStringService;
  public final ShippingMethodResponseConverter responseConverter;

  @Transactional(readOnly = true)
  public List<ShippingMethodResponse> getAllForSiteId(String siteId) {
    if (!siteIdRepository.existsByName(siteId)) {
      throw new NotFoundException(new HttpClientErrorException(HttpStatus.NOT_FOUND),
          SITEID_NOT_FOUND_CODE, siteId);
    }
    return shippingMethodRepository.findBySiteIdNameOrderByPosition(siteId)
        .map(responseConverter::convert).toList();
  }

  @Transactional
  public ShippingMethodResponse createShippingMethod(ShippingMethodPostRequest shippingRequest) {
    SiteIdEntity siteId = siteIdRepository.findByName(shippingRequest.getSiteId());
    if (siteId == null) {
      throw new ValidationException(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY),
          ErrorConstants.INVALID_FIELD_CODE, "siteId");
    }
    carrierStringService.validateCarrierStrings(shippingRequest.getCarrierStringRecords());
    validateUniqueParametersExists(siteId.getUniqueParameters(), shippingRequest.getSiteId());
    validatePostRequestIsValid(shippingRequest, siteId);
    ShippingMethodEntity shippingMethod = saveShippingMethod(shippingRequest, siteId);
    return responseConverter.convert(shippingMethod);
  }

  @Transactional
  public ShippingMethodResponse updateShippingMethod(ShippingMethodEntity shippingMethod,
      ShippingMethodPatchRequest request) {

    carrierStringService.validateCarrierStringsRecords(request.getCarrierStringRecords(),
        shippingMethod);
    validateUniqueParametersExists(shippingMethod.getSiteId().getUniqueParameters(),
        shippingMethod.getSiteId().getName());
    validatePatchRequestIsValid(request, shippingMethod);
    ShippingMethodEntity updatedShippingMethod = saveUpdatedShippingMethod(request, shippingMethod);
    return responseConverter.convert(updatedShippingMethod);
  }

  @Transactional
  public void deleteShippingMethod(UUID shippingMethodId) {
    int queryCount = shippingMethodRepository.deleteByShippingMethodId(shippingMethodId);
    if (queryCount == 0) {
      throw new NotFoundException(new HttpClientErrorException(HttpStatus.NOT_FOUND),
          RECORD_NOT_FOUND_CODE_SHIPPING, shippingMethodId.toString());
    }
  }

  private void validateUniqueParametersExists(Map<String, String> uniqueParameters, String siteId) {
    if (uniqueParameters == null) {
      throw new ValidationException(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY),
          ErrorConstants.INVALID_FIELD_CODE, siteId);
    }
  }

  private void validatePostRequestIsValid(ShippingMethodPostRequest shippingRequest,
      SiteIdEntity siteId) {
    if (validatePostRequest(shippingRequest, siteId)) {
      throw new DataIntegrityViolationException(ErrorConstants.CONSTRAINT_VIOLATION_CODE,
          new Exception());
    }
  }

  private ShippingMethodEntity saveShippingMethod(ShippingMethodPostRequest shippingRequest,
      SiteIdEntity siteId) {
    ShippingMethodEntity shippingMethod = shippingMethodEntityConverter
        .fromShippingPostRequest(shippingRequest, siteId);
    checkDisableDefaultSiteIdShippingMethod(shippingMethod);
    shippingMethodRepository.save(shippingMethod);
    if (sfccService.shouldSyncShippingMethod(shippingMethod)) {
      sfccService.syncShippingMethod(shippingMethod);
    }
    return shippingMethod;
  }

  private void checkDisableDefaultSiteIdShippingMethod(ShippingMethodEntity shippingMethod) {
    if (shippingMethod.getEnabled() && shippingMethod.getIsDefault()) {
      shippingMethodRepository.setDefaultSiteIdShippingMethodToNotDefault(
          shippingMethod.getSiteId().getId());
    }
  }

  private boolean validatePostRequest(ShippingMethodPostRequest shippingRequest,
      SiteIdEntity siteId) {
    Map<String, String> uniqueParams = siteId.getUniqueParameters();
    List<String> channels = List.of(EMPTY);
    List<String> productTypes = List.of(EMPTY);
    List<String> carrierStrings = List.of(EMPTY);
    List<String> availabilityStatusList = Collections.<String>emptyList();

    if (uniqueParams.containsKey(DbConstants.CHANNELS) && shippingRequest.getChannels() != null) {
      channels = shippingRequest.getChannels().stream().map(ChannelEnum::getValue)
          .map(String::toLowerCase).toList();
    }
    if (uniqueParams.containsKey(DbConstants.PRODUCT_TYPES)) {
      productTypes = shippingRequest.getProductTypes().stream().map(ProductTypeEnum::getValue)
          .toList();
    }
    if (uniqueParams.containsKey(DbConstants.CARRIER_STRING_RECORDS)) {
      carrierStrings = shippingRequest.getCarrierStringRecords();
    }
    if (uniqueParams.containsKey(DbConstants.AVAILABILITY_STATUS)) {
      availabilityStatusList = Optional.ofNullable(shippingRequest.getAvailabilityStatuses())
          .map(availabilityStatus -> availabilityStatus.stream()
              .map(AvailabilityStatusEnum::getValue).collect(Collectors.toList()))
          .orElse(List.of(EMPTY));
    }
    return validatePostRequestWithEnableAndUniqueParams(uniqueParams, shippingRequest)
        && shippingMethodRepository.existsByUniqueSiteIdParameters(siteId.getId(), channels, 
            productTypes, carrierStrings, 
            shippingRequest.getCarrierService(), availabilityStatusList.toArray(String[]::new));
  }

  public boolean validatePostRequestWithEnableAndUniqueParams(Map<String, String> uniqueParams,
      ShippingMethodPostRequest shippingRequest) {

    return shippingRequest.getEnabled() && !(uniqueParams.containsKey("carrierStringRecords")
        && CollectionUtils.isEmpty(shippingRequest.getCarrierStringRecords()));
  }

  public ShippingMethodEntity getShippingMethod(UUID id) {
    return shippingMethodRepository.findByShippingMethodId(id)
        .orElseThrow(() -> new NotFoundException(new HttpClientErrorException(HttpStatus.NOT_FOUND),
            RECORD_NOT_FOUND_CODE_SHIPPING, id.toString()));
  }

  private void validatePatchRequestIsValid(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    if (isPatchShippingMethodEnabled(request, shippingMethod)
        && validatePatchRequestWithUniqueParams(request, shippingMethod)) {
      throw new DataIntegrityViolationException(ErrorConstants.CONSTRAINT_VIOLATION_CODE,
          new Exception());
    }
  }

  private static Boolean isPatchShippingMethodEnabled(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    if (request.getEnabled() != null) {
      return request.getEnabled();
    }
    return shippingMethod.getEnabled();
  }

  private Boolean validatePatchRequestWithUniqueParams(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    SiteIdEntity siteId = shippingMethod.getSiteId();
    Map<String, String> uniqueParameters = siteId.getUniqueParameters();
    List<String> channels = uniqueParameters.containsKey(DbConstants.CHANNELS)
        ? getPatchChannels(request, shippingMethod)
        : List.of(EMPTY);
    List<String> productTypes = uniqueParameters.containsKey(DbConstants.PRODUCT_TYPES)
        ? getPatchProductTypes(request, shippingMethod)
        : List.of(EMPTY);
    List<String> carrierStrings = uniqueParameters.containsKey(DbConstants.CARRIER_STRING_RECORDS)
        ? getPatchProductCarrierStrings(request, shippingMethod)
        : List.of(EMPTY);
    String carrierService = uniqueParameters.containsKey(DbConstants.CARRIER_SERVICE)
        ? getPathCarrierService(request, shippingMethod)
        : null;
    List<String> availabilityStatus = uniqueParameters.containsKey(DbConstants.AVAILABILITY_STATUS)
        ? getPatchAvailabilityStatus(request, shippingMethod)
        : Collections.<String>emptyList();
    return shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
        siteId.getId(), channels, productTypes, carrierStrings, carrierService,
        shippingMethod.getId(), availabilityStatus.toArray(String[]::new));
  }

  private static List<String> getPatchChannels(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    if (request.getChannels() == null) {
      return shippingMethod.getChannels().stream().map(ChannelEntity::getChannelName)
          .map(ChannelEnum::getValue).map(String::toLowerCase).toList();
    }
    return request.getChannels().stream().map(ChannelEnum::getValue).map(String::toLowerCase)
        .toList();
  }

  private static List<String> getPatchProductTypes(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    if (request.getProductTypes() == null) {
      return shippingMethod.getProductTypes().stream().map(ProductTypeEntity::getProductTypeName)
          .map(ProductTypeEnum::getValue).toList();
    }
    return request.getProductTypes().stream().map(ProductTypeEnum::getValue).toList();
  }

  private static List<String> getPatchProductCarrierStrings(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    List<String> patchCarriers = Optional.ofNullable(request.getCarrierStringRecords())
        .orElse(List.of()).stream().map(CarrierStringRecord::getCarrierString).toList();
    List<String> shippingMethodCarriers = shippingMethod.getCarrierStrings().stream()
        .map(CarrierStringEntity::getCarrierString).toList();
    return Stream.concat(patchCarriers.stream(), shippingMethodCarriers.stream()).toList();
  }

  private static String getPathCarrierService(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    return Optional.ofNullable(request.getCarrierService())
        .orElse(shippingMethod.getCarrierService());
  }

  private static List<String> getPatchAvailabilityStatus(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    if (request.getAvailabilityStatus() == null) {
      return Optional.ofNullable(shippingMethod.getAvailabilityStatus())
          .map(availabilityStatusList -> availabilityStatusList.stream()
              .map(AvailabilityStatusEnum::getValue).toList())
          .orElse(Collections.<String>emptyList());
    }
    return request.getAvailabilityStatus().stream().map(AvailabilityStatusEnum::getValue).toList();
  }

  private ShippingMethodEntity saveUpdatedShippingMethod(ShippingMethodPatchRequest request,
      ShippingMethodEntity shippingMethod) {
    ShippingMethodEntity mergedShippingMethod = shippingMethodEntityConverter
        .mergeShippingMethodWithPatchRequest(shippingMethod, request);
    checkDisableDefaultSiteIdShippingMethod(mergedShippingMethod);
    ShippingMethodEntity updatedShippingMethod = shippingMethodRepository
        .save(mergedShippingMethod);
    if (sfccService.shouldSyncShippingMethod(mergedShippingMethod)) {
      sfccService.syncShippingMethod(mergedShippingMethod);
    }
    return updatedShippingMethod;
  }
}

