package com.domain_name.fulfillment.configuration.api.shipping.method;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.domain_name.fulfillment.configuration.api.exception.NotFoundException;
import com.domain_name.fulfillment.configuration.api.logging.MDCManager;

@ExtendWith(SpringExtension.class)
public class ShippingMethodControllerTest extends ShippingMethodTestHelper {

  @Mock
  ShippingMethodService shippingMethodService;

  @Mock
  MDCManager mdcManager;

  @InjectMocks
  ShippingMethodController shippingMethodController;

  @Test
  void testGetShippingMethods() {
    String siteId = "domain_name-US";

    mdcManager.insertSiteIdMDC(siteId);
    Mockito.when(shippingMethodService.getAllForSiteId(siteId))
        .thenReturn(prepareShippingMethodResponseUS());

    List<ShippingMethodResponse> rsponse = shippingMethodController.getShippingMethods(siteId);
    Assertions.assertFalse(rsponse.isEmpty());
    Assertions.assertEquals(siteId, rsponse.get(0).getSiteId());
    Assertions.assertEquals(true, rsponse.get(0).getEnabled());
    Assertions.assertEquals(true, rsponse.get(0).getIsDefault());
    Assertions.assertEquals("FedEx", rsponse.get(0).getCarrierName());
    Assertions.assertEquals("2NDDAY_INLINE", rsponse.get(0).getCarrierService());
    Assertions.assertEquals("FED000US1000000000",
        rsponse.get(0).getCarrierStringRecords().get(0).getCarrierString());
  }

  @Test
  void testCreateShippingMethod() {
    boolean enabled = true;
    ShippingMethodPostRequest smPostRequest = buildShippingPostRequest();
    ShippingMethodResponse smExpectedRsponse = preparePostShippingMethodResponseUS(enabled);

    mdcManager.insertSiteIdMDC(smPostRequest.getSiteId());
    Mockito.when(shippingMethodService.createShippingMethod(Mockito.any()))
        .thenReturn(smExpectedRsponse);

    ResponseEntity<ShippingMethodResponse> smRsponseEntity = shippingMethodController
        .createShippingMethods(smPostRequest);
    Assertions.assertEquals(smRsponseEntity.getStatusCode().value(), HttpStatus.CREATED.value());
    ShippingMethodResponse smRsponse = smRsponseEntity.getBody();
    Assertions.assertEquals(smExpectedRsponse.getSiteId(), smRsponse.getSiteId());
    Assertions.assertEquals(smExpectedRsponse.getEnabled(), smRsponse.getEnabled());
    Assertions.assertEquals(smRsponse.getIsDefault(), smRsponse.getIsDefault());
    Assertions.assertEquals(smRsponse.getCarrierName(), smRsponse.getCarrierName());
    Assertions.assertEquals(smRsponse.getCarrierService(), smRsponse.getCarrierService());
    Assertions.assertEquals(smRsponse.getCarrierStringRecords().get(0).getCarrierString(),
        smRsponse.getCarrierStringRecords().get(0).getCarrierString());
  }

  @Test
  void testCreateShippingMethodThrowsRuntimeException() {
    ShippingMethodPostRequest smPostRequest = buildShippingPostRequest();

    mdcManager.insertSiteIdMDC(smPostRequest.getSiteId());
    Mockito.when(shippingMethodService.createShippingMethod(Mockito.any()))
        .thenThrow(new RuntimeException("Object Already Exist"));

    assertThrows(RuntimeException.class, () -> {
      shippingMethodController.createShippingMethods(smPostRequest);
    });
    Mockito.verify(shippingMethodService, Mockito.times(1)).createShippingMethod(smPostRequest);
  }

  @Test
  void testUpdateShippingMethod() {
    boolean enabled = false;
    String shippingMethodId = "6b854219-85c5-4833-b75e-6af7e51332c6";
    ShippingMethodPatchRequest smPatchRequest = prepareShippingMethodPatchRequest();
    ShippingMethodResponse smExpectedRsponse = preparePostShippingMethodResponseUS(enabled);

    Mockito.when(shippingMethodService.getShippingMethod(Mockito.any()))
        .thenReturn(prepareShippingMethodEntity());
    Mockito.when(shippingMethodService.updateShippingMethod(Mockito.any(), Mockito.any()))
        .thenReturn(smExpectedRsponse);

    ShippingMethodResponse smRsponse = shippingMethodController
        .updateShippingMethod(shippingMethodId, smPatchRequest);
    Assertions.assertEquals(smExpectedRsponse.getEnabled(), smRsponse.getEnabled());
  }

  @Test
  void testUpdateShippingMethodThrowNotFoundException() {
    String shippingMethodId = "6b854219-85c5-4833-b75e-6af7e51332c6";
    ShippingMethodPatchRequest smPatchRequest = prepareShippingMethodPatchRequest();

    Mockito.when(shippingMethodService.getShippingMethod(Mockito.any()))
        .thenThrow(new NotFoundException(""));

    assertThrows(NotFoundException.class, () -> {
      shippingMethodController.updateShippingMethod(shippingMethodId, smPatchRequest);
    });
  }

  @Test
  void testDeleteShippingMethod() {
    String shippingMethodId = "6b854219-85c5-4833-b75e-6af7e51332c6";
    Mockito.doNothing().when(shippingMethodService)
        .deleteShippingMethod(UUID.fromString(shippingMethodId));

    ResponseEntity<String> response = shippingMethodController
        .deleteShippingMethod(shippingMethodId);
    Assertions.assertEquals("204 NO_CONTENT", response.getStatusCode().toString());
  }

  @Test
  void testDeleteShippingMethodThrowsRuntimeException() {
    String shippingMethodId = "6b854219-85c5-4833-b75e-6af7e51332c6";
    Mockito.doThrow(RuntimeException.class).when(shippingMethodService)
        .deleteShippingMethod(UUID.fromString(shippingMethodId));

    assertThrows(RuntimeException.class, () -> {
      shippingMethodController.deleteShippingMethod(shippingMethodId);
    });
  }

}
