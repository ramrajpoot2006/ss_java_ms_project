package com.domain_name.fulfillment.configuration.api.shipping.method;

import static com.domain_name.fulfillment.configuration.api.exception.ErrorConstants.INVALID_FIELD_CODE;
import static com.domain_name.fulfillment.configuration.api.exception.ErrorConstants.REQUIRED_FIELD_CODE;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.domain_name.fulfillment.configuration.api.constant.PatternConstants;
import com.domain_name.fulfillment.configuration.api.logging.MDCManager;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/shipping-method")
@Slf4j
@AllArgsConstructor
@Validated
public class ShippingMethodController {

  public final ShippingMethodService shippingMethodService;
  private final MDCManager mdcManager;

  @GetMapping(value = "/{siteId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<ShippingMethodResponse> getShippingMethods(@PathVariable String siteId) {
    mdcManager.insertSiteIdMDC(siteId);
    log.info("Reading all shipping methods");
    return shippingMethodService.getAllForSiteId(siteId);
  }

  /**
   * Creates a new shipping method saved into database and synced with SFCC.
   * 
   * @param request containing method data
   * @return response after creation of the method
   */
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ShippingMethodResponse> createShippingMethods(
      @Valid @RequestBody ShippingMethodPostRequest request) {
    try {
      mdcManager.insertSiteIdMDC(request.getSiteId());
      log.info("Request received to create shipping methods");
      ShippingMethodResponse response = shippingMethodService.createShippingMethod(request);
      log.info("Shipping methods created successfully");
      return ResponseEntity
          .status(HttpStatusCode.valueOf(HttpStatus.CREATED.value()))
          .body(response);
    } catch (RuntimeException ex) {
      log.error("Error in saving shipping methods : {} ", ex);
      throw ex;
    }
  }

  @PatchMapping(value = "/{shippingMethodId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ShippingMethodResponse updateShippingMethod(
      @PathVariable @NotBlank(message = REQUIRED_FIELD_CODE)
      @Pattern(regexp = PatternConstants.UUID_PATTERN,
      message = INVALID_FIELD_CODE) String shippingMethodId,
      @Valid @RequestBody ShippingMethodPatchRequest request) {
    try {
      log.info("Request received to update shipping method for shippingMethodId {}",
          shippingMethodId);
      ShippingMethodEntity shippingMethod = shippingMethodService
          .getShippingMethod(UUID.fromString(shippingMethodId));
      ShippingMethodResponse response = shippingMethodService.updateShippingMethod(shippingMethod,
          request);
      log.info("Shipping method updated successfully shippingMethodId : {}", shippingMethodId);
      return response;
    } catch (RuntimeException ex) {
      log.error("Error occurred while updating shipping methods for shippingMethodId : {} : {}",
          shippingMethodId, ex);
      throw ex;
    }
  }

  /**
   * Deletes a shipping method from database.
   * 
   * @param shippingMethodId to be deleted
   * @return empty response
   */
  @DeleteMapping(value = "/{shippingMethodId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> deleteShippingMethod(
      @PathVariable @NotBlank(message = REQUIRED_FIELD_CODE)
      @Pattern(regexp = PatternConstants.UUID_PATTERN,
      message = INVALID_FIELD_CODE) String shippingMethodId) {
    try {
      shippingMethodService.deleteShippingMethod(UUID.fromString(shippingMethodId));
      return ResponseEntity.noContent().header("Content-Length", "0").build();
    } catch (RuntimeException ex) {
      log.error("Error occurred while deleting shipping method for shippingMethodId : {} : {}",
          shippingMethodId, ex);
      throw ex;
    }
  }

}
