package com.domain_name.fulfillment.configuration.api.shipping.method;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.domain_name.fulfillment.configuration.api.channel.ChannelEnum;
import com.domain_name.fulfillment.configuration.api.exception.NotFoundException;
import com.domain_name.fulfillment.configuration.api.exception.ValidationException;
import com.domain_name.fulfillment.configuration.api.products.type.ProductTypeEnum;
import com.domain_name.fulfillment.configuration.api.sfcc.SFCCService;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringEntity;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringRecord;
import com.domain_name.fulfillment.configuration.api.shipping.carrier.string.CarrierStringService;
import com.domain_name.fulfillment.configuration.api.site.SiteIdEntity;
import com.domain_name.fulfillment.configuration.api.site.SiteIdRepository;


/**
 * @author sangavi.selvaraj
 *
 */
@ExtendWith(SpringExtension.class)
public class ShippingMethodServiceTest extends ShippingMethodTestHelper {

  @Mock
  SiteIdRepository siteIdRepository;

  @Mock
  ShippingMethodRepository shippingMethodRepository;

  @Mock
  ShippingMethodEntityConverter shippingMethodEntityConverter;

  @Mock
  SFCCService sfccService;

  @Mock
  CarrierStringService carrierStringService;

  @Mock
  ShippingMethodResponseConverter responseConverter;

  @InjectMocks
  ShippingMethodService shippingMethodService;

  @Test
  void testGetAllForSiteId() {
    String siteId = "domain_name-AT";
    ShippingMethodEntity shippingMethod = savedShippingMethod();
    ShippingMethodResponse response = buildResponse();
    Mockito.when(siteIdRepository.existsByName(siteId)).thenReturn(true);
    Mockito.when(shippingMethodRepository.findBySiteIdNameOrderByPosition(Mockito.eq(siteId)))
        .thenReturn(Stream.of(shippingMethod)
        );
    Mockito.when(responseConverter.convert(Mockito.eq(shippingMethod)))
        .thenReturn(response);
    List<ShippingMethodResponse> responseList = shippingMethodService.getAllForSiteId("domain_name-AT");
    org.assertj.core.api.Assertions.assertThat(responseList).hasSize(1);
    org.assertj.core.api.Assertions.assertThat(responseList.get(0)).isEqualTo(response);
  }

  @Test
  void testGetAllForSiteId_notFound() {
    String siteId = "domain_name-AT";
    Mockito.when(siteIdRepository.existsByName(siteId)).thenReturn(false);
    assertThrows(NotFoundException.class,
        () -> shippingMethodService.getAllForSiteId(siteId));
  }

  @Test
  void testCreateShippingNotEnabledMethodSuccessFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(false, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);

    ShippingMethodResponse response = shippingMethodService.createShippingMethod(shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).setDefaultSiteIdShippingMethodToNotDefault(Mockito.anyInt());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingEnabledMethodSuccessFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);

    ShippingMethodResponse response = shippingMethodService.createShippingMethod(shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodValidateCarrierStringFailsFlow() {
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.doThrow(new ValidationException(new Exception(""), "", ""))
        .when(carrierStringService)
        .validateCarrierStrings(Mockito.anyList());

    assertThrows(ValidationException.class, () -> {
      shippingMethodService.createShippingMethod(shippingRequest);
    });

    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodSiteIdNoUniqueParametersFlow() {
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any()))
        .thenReturn(buildSiteIdNoUniqueParametersMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(ValidationException.class, () -> {
      shippingMethodService.createShippingMethod(shippingRequest);
    });

    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodEnabledUniqueParameterAlreadyExistFlow() {
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(DataIntegrityViolationException.class, () -> {
      shippingMethodService.createShippingMethod(shippingRequest);
    });

    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodEnabledUniqueParameterWithAvailabilityStatus() {
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequestWithAvailability(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMockedAvailability());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(DataIntegrityViolationException.class, () -> {
      shippingMethodService.createShippingMethod(shippingRequest);
    });

    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodWithoutAvailabilityStatus() {
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMockedAvailability());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(DataIntegrityViolationException.class, () -> {
      shippingMethodService.createShippingMethod(shippingRequest);
    });

    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateDefaultShippingMethodFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, true);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                                                                    Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);

    ShippingMethodResponse response = shippingMethodService.createShippingMethod(shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).setDefaultSiteIdShippingMethodToNotDefault(Mockito.anyInt());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodSaveFailsFlow() {
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodRepository.save(Mockito.any()))
        .thenThrow(new IllegalArgumentException());
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(IllegalArgumentException.class, () -> {
      shippingMethodService.createShippingMethod(shippingRequest);
    });

    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodSfccFailsFlow() {
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(true, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);
    Mockito.doThrow(new ValidationException(new Exception(""), "", ""))
        .when(sfccService)
        .syncShippingMethod(Mockito.any());

    assertThrows(ValidationException.class, () -> {
      shippingMethodService.createShippingMethod(shippingRequest);
    });

    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testCreateShippingMethodWithoutSfccSyncSuccessFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(false, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(buildSiteIdMocked());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(false);

    ShippingMethodResponse response = shippingMethodService.createShippingMethod(shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(siteIdRepository, Mockito.times(1)).findByName(shippingRequest.getSiteId());
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStrings(Mockito.anyList());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodNotEnabledSuccessFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(false,false);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(
        savedShippingMethod(), shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).setDefaultSiteIdShippingMethodToNotDefault(Mockito.anyInt());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodEnabledSuccessFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(true, false);
    Mockito.when(shippingMethodRepository.findByShippingMethodId(Mockito.any()))
        .thenReturn(Optional.of(savedShippingMethod()));
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(
        savedShippingMethod(), shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).setDefaultSiteIdShippingMethodToNotDefault(Mockito.anyInt());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodDefaultSuccessFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(true, true);
    Mockito.when(shippingMethodRepository.findByShippingMethodId(Mockito.any()))
        .thenReturn(Optional.of(savedShippingMethod()));
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
                                                                                   Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(
        savedShippingMethod(), shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
                                                                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).setDefaultSiteIdShippingMethodToNotDefault(Mockito.anyInt());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodValidateCarrierStringFailsFlow() {
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(true, false);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.doThrow(new ValidationException(new Exception(""), "", ""))
        .when(carrierStringService)
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());

    assertThrows(ValidationException.class, () -> {
      shippingMethodService.updateShippingMethod(savedShippingMethod(), shippingRequest);
    });

    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodSiteIdNoUniqueParametersFlow() {
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(true, false);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(ValidationException.class, () -> {
      shippingMethodService.updateShippingMethod(savedShippingMethodWithSiteIdNoUniqueParameters(),
          shippingRequest);
    });

    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodUniqueParametersExistsFlow() {
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(true, false);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(true);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(DataIntegrityViolationException.class, () -> {
      shippingMethodService.updateShippingMethod(savedShippingMethod(), shippingRequest);
    });

    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodSaveFailsFlow() {
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(true, false);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodRepository.save(Mockito.any()))
        .thenThrow(new IllegalArgumentException());
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());

    assertThrows(IllegalArgumentException.class, () -> {
      shippingMethodService.updateShippingMethod(savedShippingMethod(), shippingRequest);
    });

    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodSfccFailsFlow() {
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(true, false);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);
    Mockito.when(sfccService.syncShippingMethod(Mockito.any()))
        .thenThrow(new ValidationException(new Exception(""), "", ""));

    assertThrows(ValidationException.class, () -> {
      shippingMethodService.updateShippingMethod(savedShippingMethod(), shippingRequest);
    });

    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(1)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testUpdateShippingMethodWithoutSfccSyncSuccessFlow() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPatchRequest shippingRequest = createCorrectShippingMethodPatchRequest(false, false);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
            Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPatchRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(false);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(
        savedShippingMethod(), shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
    Mockito.verify(carrierStringService, Mockito.times(1))
        .validateCarrierStringsRecords(Mockito.anyList(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(0))
        .existsShippingMethodsByUniqueParamsAndShippingMethodId(Mockito.any(), Mockito.any(),
            Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    Mockito.verify(shippingMethodRepository, Mockito.times(1)).save(Mockito.any());
    Mockito.verify(sfccService, Mockito.times(0)).syncShippingMethod(Mockito.any());
  }

  @Test
  void testDeleteSuccessFlow() {
    UUID shippingMethodId = UUID.randomUUID();
    Mockito.when(shippingMethodRepository.deleteByShippingMethodId(Mockito.any())).thenReturn(1);
    shippingMethodService.deleteShippingMethod(shippingMethodId);

    Mockito.verify(shippingMethodRepository, Mockito.times(1))
        .deleteByShippingMethodId(shippingMethodId);
  }

  @Test
  void testDeleteWhenShippingMethodIdNotFound() {
    UUID shippingMethodId = UUID.randomUUID();
    Mockito.when(shippingMethodRepository.deleteByShippingMethodId(Mockito.any())).thenReturn(0);

    assertThrows(NotFoundException.class,
        () -> shippingMethodService.deleteShippingMethod(shippingMethodId));
  }

  private ShippingMethodResponse buildResponse() {
    return ShippingMethodResponse.builder().carrierName("someCarrier").build();
  }
  
  @Test
  void testCreateShippingMethodWhenSiteIdIsNull() {
    ShippingMethodPostRequest smPostRequest = buildShippingPostRequest();
    Mockito.when(siteIdRepository.findByName(Mockito.anyString())).thenReturn(null);

    assertThrows(ValidationException.class,
        () -> shippingMethodService.createShippingMethod(smPostRequest));
  }
  
  @Test
  void testCreateShippingMethodWhenUniqueParametersIsNull() {
    ShippingMethodPostRequest smPostRequest = buildShippingPostRequest();
    
    Mockito.when(siteIdRepository.findByName(Mockito.anyString())).thenReturn(SiteIdEntity.builder().build());
    Mockito.doNothing().when(carrierStringService).validateCarrierStrings(smPostRequest.getCarrierStringRecords());
    
    assertThrows(ValidationException.class,
        () -> shippingMethodService.createShippingMethod(smPostRequest));
  }
  
  @Test
  void testCreateShippingMethodvalidatePostRequestIsValid() {
    ShippingMethodPostRequest smPostRequest = buildShippingPostRequest();
    SiteIdEntity siteIdEntity = prepareSiteIdMocked();

    Mockito.when(siteIdRepository.findByName(Mockito.anyString())).thenReturn(siteIdEntity);
    Mockito.doNothing().when(carrierStringService)
        .validateCarrierStrings(smPostRequest.getCarrierStringRecords());
    Mockito.when(shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(),
        Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);

    assertThrows(DataIntegrityViolationException.class,
        () -> shippingMethodService.createShippingMethod(smPostRequest));
  }
  
  @Test
  void testUpdateShippingMethod() {
    List<CarrierStringRecord> carrierStrings = List
        .of(CarrierStringRecord.builder().carrierStringId("b553d8bf-d87b-41f8-8c4c-635b4b735ec5")
            .carrierString("FED000US2000000000").build());

    ShippingMethodEntity shippingMethod = prepareShippingMethodEntity();
    shippingMethod.setSiteId(prepareSiteIdMockedAT());
    ShippingMethodPatchRequest smPatchRequest = prepareSMPatchRequestForCarrierString(
        carrierStrings);
    ShippingMethodResponse shippingMethodResponse = preparePostShippingMethodResponseUS(true);
    shippingMethodResponse.setSiteId("domain_name-AT");

    Mockito.doNothing().when(carrierStringService).validateCarrierStringsRecords(carrierStrings,
        shippingMethod);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
        Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
        Mockito.any())).thenReturn(shippingMethod);

    shippingMethodRepository
        .setDefaultSiteIdShippingMethodToNotDefault(shippingMethod.getSiteId().getId());

    Mockito.when(shippingMethodRepository.save(Mockito.any())).thenReturn(shippingMethod);
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(shippingMethodResponse);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(shippingMethod,
        smPatchRequest);
    Assertions.assertEquals(shippingMethod.getSiteId().getName(), response.getSiteId());
  }
  
  @Test
  void testUpdateShippingMethodWithAvailabilityStatusInUnquieParam() {
    List<CarrierStringRecord> carrierStrings = List
        .of(CarrierStringRecord.builder().carrierStringId("b553d8bf-d87b-41f8-8c4c-635b4b735ec5")
            .carrierString("FED000US2000000000").build());

    ShippingMethodEntity shippingMethod = prepareShippingMethodEntity();
    shippingMethod.setSiteId(prepareSiteIdMockedATWithAvailabilityStatus());
    ShippingMethodPatchRequest smPatchRequest = prepareSMPatchRequestForCarrierString(
        carrierStrings);
    ShippingMethodResponse shippingMethodResponse = preparePostShippingMethodResponseUS(true);
    shippingMethodResponse.setSiteId("domain_name-AT");

    Mockito.doNothing().when(carrierStringService).validateCarrierStringsRecords(carrierStrings,
        shippingMethod);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
        Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
        Mockito.any())).thenReturn(shippingMethod);

    shippingMethodRepository
        .setDefaultSiteIdShippingMethodToNotDefault(shippingMethod.getSiteId().getId());

    Mockito.when(shippingMethodRepository.save(Mockito.any())).thenReturn(shippingMethod);
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(shippingMethodResponse);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(shippingMethod,
        smPatchRequest);
    Assertions.assertEquals(shippingMethod.getSiteId().getName(), response.getSiteId());
  }

  @Test
  void testUpdateShippingMethodWithAvailabilityStatusInRequestAndUniqueParam() {
    List<CarrierStringRecord> carrierStrings = List
        .of(CarrierStringRecord.builder().carrierStringId("b553d8bf-d87b-41f8-8c4c-635b4b735ec5")
            .carrierString("FED000US2000000000").build());

    ShippingMethodEntity shippingMethod = prepareShippingMethodEmpty();
    shippingMethod.setSiteId(prepareSiteIdMockedATWithAvailabilityStatus());
    ShippingMethodPatchRequest smPatchRequest = prepareSMPatchRequestForCarrierStringAvailability(
        carrierStrings);
    ShippingMethodResponse shippingMethodResponse = preparePostShippingMethodResponseUS(true);
    shippingMethodResponse.setSiteId("domain_name-AT");

    Mockito.doNothing().when(carrierStringService).validateCarrierStringsRecords(carrierStrings,
        shippingMethod);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
        Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
        Mockito.any())).thenReturn(shippingMethod);

    shippingMethodRepository
        .setDefaultSiteIdShippingMethodToNotDefault(shippingMethod.getSiteId().getId());

    Mockito.when(shippingMethodRepository.save(Mockito.any())).thenReturn(shippingMethod);
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(shippingMethodResponse);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(shippingMethod,
        smPatchRequest);
    Assertions.assertEquals(shippingMethod.getSiteId().getName(), response.getSiteId());
  }

  @Test
  void testUpdateShippingMethodCheckCarrierService() {
    List<CarrierStringRecord> carrierStrings = List
        .of(CarrierStringRecord.builder().carrierStringId("b553d8bf-d87b-41f8-8c4c-635b4b735ec5")
            .carrierString("FED000US2000000000").build());

    ShippingMethodEntity shippingMethod = prepareShippingMethodEntity();
    shippingMethod.setSiteId(prepareSiteIdMocked());
    ShippingMethodPatchRequest smPatchRequest = prepareSMPatchRequestForCarrierString(
        carrierStrings);
    ShippingMethodResponse shippingMethodResponse = preparePostShippingMethodResponseUS(true);

    Mockito.doNothing().when(carrierStringService).validateCarrierStringsRecords(carrierStrings,
        shippingMethod);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
        Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
        Mockito.any())).thenReturn(shippingMethod);

    shippingMethodRepository
        .setDefaultSiteIdShippingMethodToNotDefault(shippingMethod.getSiteId().getId());

    Mockito.when(shippingMethodRepository.save(Mockito.any())).thenReturn(shippingMethod);
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(shippingMethodResponse);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(shippingMethod,
        smPatchRequest);
    Assertions.assertEquals(shippingMethod.getSiteId().getName(), response.getSiteId());
  }
  
  @Test
  void testGetShippingMethod() {
    String id = "6b854219-85c5-4833-b75e-6af7e51332c6";
    Optional<ShippingMethodEntity> shippingMethod = Optional.of(prepareShippingMethodEntity());

    Mockito.when(shippingMethodRepository.findByShippingMethodId(Mockito.any()))
        .thenReturn(shippingMethod);

    ShippingMethodEntity response = shippingMethodService.getShippingMethod(UUID.fromString(id));
    Assertions.assertEquals(id, response.getShippingMethodId().toString());
  }
  
  @Test
  void testGetShippingMethodNotFoundException() {
    Optional<ShippingMethodEntity> shippingMethod = Optional.empty();
    Mockito.when(shippingMethodRepository.findByShippingMethodId(Mockito.any()))
        .thenReturn(shippingMethod);

    assertThrows(NotFoundException.class, () -> shippingMethodService
        .getShippingMethod(UUID.fromString("d61403d4-38ec-415c-a9c3-b532fd04961a")));
  }
  
  @Test
  void testValidatePostRequestWithUniqueParams() {
    ShippingMethodPostRequest request = buildShippingPostRequest();
    request.setCarrierStringRecords(List.of());
    boolean response = shippingMethodService
        .validatePostRequestWithEnableAndUniqueParams(buildUniqueParamsMap(), request);
    Assertions.assertFalse(response);
  }
  
  @Test
  void testValidatePostRequestWithUniqueParamsDoesNotContainsCarrierStringRecords() {
    Map<String, String> uniqueParams = buildUniqueParamsMap();
    uniqueParams.remove("carrierStringRecords");
    boolean response = shippingMethodService
        .validatePostRequestWithEnableAndUniqueParams(uniqueParams, buildShippingPostRequest());
    Assertions.assertTrue(response);
  }
  
  @Test
  void testUpdateShippingMethodCheckCarrierService1() {
    List<CarrierStringRecord> carrierStrings = List
        .of(CarrierStringRecord.builder().carrierStringId("b553d8bf-d87b-41f8-8c4c-635b4b735ec5")
            .carrierString("FED000US2000000000").build());
    ShippingMethodEntity shippingMethod = prepareShippingMethodEntity();
    shippingMethod.setSiteId(prepareSiteIdMockedWihtoutChannelAndCarrierStringRecords());
    ShippingMethodPatchRequest smPatchRequest = prepareSMPatchRequestForCarrierString(
        carrierStrings);
    ShippingMethodResponse shippingMethodResponse = preparePostShippingMethodResponseUS(true);

    Mockito.doNothing().when(carrierStringService).validateCarrierStringsRecords(carrierStrings,
        shippingMethod);
    Mockito.when(shippingMethodRepository.existsShippingMethodsByUniqueParamsAndShippingMethodId(
        Mockito.anyInt(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
        Mockito.any(), Mockito.any())).thenReturn(false);
    Mockito.when(shippingMethodEntityConverter.mergeShippingMethodWithPatchRequest(Mockito.any(),
        Mockito.any())).thenReturn(shippingMethod);
    shippingMethodRepository
        .setDefaultSiteIdShippingMethodToNotDefault(shippingMethod.getSiteId().getId());

    Mockito.when(shippingMethodRepository.save(Mockito.any())).thenReturn(shippingMethod);
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(shippingMethodResponse);

    ShippingMethodResponse response = shippingMethodService.updateShippingMethod(shippingMethod,
        smPatchRequest);
    Assertions.assertEquals(shippingMethod.getSiteId().getName(), response.getSiteId());
  }
  
  @Test
  void testCreateShippingUniueParamUsecase() {
    ShippingMethodResponse expectedResponse = buildResponse();
    ShippingMethodPostRequest shippingRequest = createCorrectShippingMethodPostRequest(false, false);
    Mockito.when(siteIdRepository.findByName(Mockito.any())).thenReturn(prepareSiteIdMockedWihtoutChannelAndCarrierStringRecords());
    Mockito.when(
            shippingMethodRepository.existsByUniqueSiteIdParameters(Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
        .thenReturn(false);
    Mockito.when(
            shippingMethodEntityConverter.fromShippingPostRequest(Mockito.any(), Mockito.any()))
        .thenReturn(buildShippingEntityMockedFromPostRequest(shippingRequest));
    Mockito.when(responseConverter.convert(Mockito.any())).thenReturn(buildResponse());
    Mockito.when(sfccService.shouldSyncShippingMethod(Mockito.any())).thenReturn(true);

    ShippingMethodResponse response = shippingMethodService.createShippingMethod(shippingRequest);

    Assertions.assertEquals(response, expectedResponse);
  }

  private ShippingMethodEntity buildShippingEntityMockedFromPostRequest(ShippingMethodPostRequest shippingRequest) {
    return ShippingMethodEntity.builder()
        .siteId(buildSiteIdMocked())
        .isDefault(shippingRequest.getIsDefault())
        .enabled(shippingRequest.getEnabled())
        .carrierName("someCarrier")
        .build();
  }

  private ShippingMethodEntity buildShippingEntityMockedFromPatchRequest(ShippingMethodPatchRequest shippingRequest) {
    return ShippingMethodEntity.builder()
        .siteId(buildSiteIdMocked())
        .isDefault(shippingRequest.getIsDefault())
        .enabled(shippingRequest.getEnabled())
        .carrierName("someCarrier")
        .build();
  }

  private ShippingMethodEntity savedShippingMethod() {
    SiteIdEntity siteId = buildSiteIdMocked();
    List<CarrierStringEntity> carriersString = List.of(

        CarrierStringEntity.builder().carrierString("carrier1").build(),
        CarrierStringEntity.builder().carrierString("carrier2").build()
    );
    return ShippingMethodEntity.builder().siteId(siteId).carrierName("someCarrier")
        .carrierStrings(carriersString).build();
  }

  private ShippingMethodEntity savedShippingMethodWithSiteIdNoUniqueParameters() {
    SiteIdEntity siteId = buildSiteIdNoUniqueParametersMocked();
    List<CarrierStringEntity> carriersString = List.of(
        CarrierStringEntity.builder().carrierString("carrier1").build(),
        CarrierStringEntity.builder().carrierString("carrier2").build()
    );
    return ShippingMethodEntity.builder().siteId(siteId).carrierName("someCarrier")
        .carrierStrings(carriersString).build();
  }

  private ShippingMethodPostRequest createCorrectShippingMethodPostRequest(Boolean enabled, Boolean isDefault) {
    return ShippingMethodPostRequest.builder()
        .siteId("domain_name-AT")
        .enabled(enabled)
        .isDefault(isDefault)
        .channels(List.of(ChannelEnum.WEB, ChannelEnum.domain_nameCONFIRMEDAPP))
        .productTypes(List.of(ProductTypeEnum.BACKORDER, ProductTypeEnum.PREORDER))
        .carrierStringRecords(List.of("carrier1", "carrier 2"))
        .carrierService("carrier")
        .build();
  }

  private ShippingMethodPostRequest createCorrectShippingMethodPostRequestWithAvailability(Boolean enabled, Boolean isDefault) {
    return ShippingMethodPostRequest.builder()
        .siteId("domain_name-AT")
        .enabled(enabled)
        .isDefault(isDefault)
        .availabilityStatuses(List.of(AvailabilityStatusEnum.BACKORDER))
        .channels(List.of(ChannelEnum.WEB, ChannelEnum.domain_nameCONFIRMEDAPP))
        .productTypes(List.of(ProductTypeEnum.BACKORDER, ProductTypeEnum.PREORDER))
        .carrierStringRecords(List.of("carrier1", "carrier 2"))
        .carrierService("carrier")
        .build();
  }

  private ShippingMethodPatchRequest createCorrectShippingMethodPatchRequest(Boolean enabled, Boolean isDefault) {
    return ShippingMethodPatchRequest.builder()
        .enabled(enabled)
        .isDefault(isDefault)
        .channels(List.of(ChannelEnum.WEB, ChannelEnum.domain_nameCONFIRMEDAPP))
        .productTypes(List.of(ProductTypeEnum.BACKORDER, ProductTypeEnum.PREORDER))
        .carrierStringRecords(List.of(
            CarrierStringRecord.builder().carrierString("carrier").carrierStringId("id").build()))
        .carrierService("carrier")
        .build();
  }


  private SiteIdEntity buildSiteIdMocked() {
    return SiteIdEntity.builder()
        .id(1)
        .name("domain_name-AT")
        .uniqueParameters(
            Map.of("channels", "channel", "productTypes", "product_type", "carrierStringRecords",
                "shipping_method_carrier_string")
        )
        .build();
  }
  
  private SiteIdEntity buildSiteIdMockedAvailability() {
    return SiteIdEntity.builder()
        .id(1)
        .name("domain_name-AT")
        .uniqueParameters(
            Map.of("channels", "channel", "productTypes", "product_type", "carrierStringRecords",
                "shipping_method_carrier_string", "availabilityStatus", "availability_status")
        )
        .build();
  }


  private SiteIdEntity buildSiteIdNoUniqueParametersMocked() {
    return SiteIdEntity.builder()
        .id(1)
        .name("domain_name-AT")
        .build();
  }

}
