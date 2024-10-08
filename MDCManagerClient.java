package com.domain_name.fulfillment.configuration.api.logging;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import feign.Request;
import feign.Response;
import feign.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class MDCManagerClient {

  public static final String OUTGOING_REQUEST_HEADERS = "outgoingRequest.request.headers";
  public static final String OUTGOING_REQUEST_BODY = "outgoingRequest.request.body";
  public static final String OUTGOING_REQUEST_METHOD = "outgoingRequest.request.method";
  public static final String OUTGOING_REQUEST_URL = "outgoingRequest.request.url";

  public static final String OUTGOING_RESPONSE_HEADERS = "outgoingRequest.response.headers";
  public static final String OUTGOING_RESPONSE_BODY = "outgoingRequest.response.body";
  public static final String OUTGOING_RESPONSE_STATUS = "outgoingRequest.response.status";

  @Value("${logging.sanitize.clientheaders}")
  private String sanitizedHeaders;

  @Value("${logging.logBody}")
  boolean isLogBody;

  private final ObjectMapper mapper;

  public void insertOutgoingRequestMDC(Request request) {
    MDC.put(OUTGOING_REQUEST_URL, request.url());
    MDC.put(OUTGOING_REQUEST_METHOD, request.httpMethod());
    putRequestHeaders(request);
    if (isLogBody) {
      Optional.ofNullable(request.body()).ifPresent(
          body -> MDC.put(OUTGOING_REQUEST_BODY, new String(body, StandardCharsets.UTF_8)));
    }
  }

  private void putRequestHeaders(Request request) {
    Map<String, Collection<String>> headers = request.headers();
    headers.entrySet().stream()
        .filter(entry -> !sanitizedHeaders.contains(entry.getKey().toLowerCase(Locale.ENGLISH)))
        .forEach(entry -> {
          try {
            MDC.put(OUTGOING_REQUEST_HEADERS + "." + entry.getKey(),
                mapper.writeValueAsString(entry.getValue()));
          } catch (JsonProcessingException e) {
            log.warn("cannot log headers: {}", e);
          }
        });
  }

  public Response insertOutgoingResponseMDC(Response response) throws IOException {
    byte[] responseCopy = new byte[0];
    putResponseHeaders(response);
    MDC.put(OUTGOING_RESPONSE_STATUS, Integer.toString(response.status()));
    if (Objects.nonNull(response.body())) {
      responseCopy = Util.toByteArray(response.body().asInputStream());
    }
    if (isLogBody) {
      MDC.put(OUTGOING_RESPONSE_BODY, new String(responseCopy, StandardCharsets.UTF_8));
    }
    return response.toBuilder().body(responseCopy).build();
  }

  private void putResponseHeaders(Response response) {
    Map<String, Collection<String>> headers = response.headers();
    headers.entrySet().stream().forEach(entry -> {
      try {
        MDC.put(OUTGOING_RESPONSE_HEADERS + "." + entry.getKey(),
            mapper.writeValueAsString(entry.getValue()));
      } catch (JsonProcessingException e) {
        log.warn("cannot log headers: {}", e);
      }
    });
  }

}
