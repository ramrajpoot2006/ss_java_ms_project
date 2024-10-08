package com.domain_name.fulfillment.configuration.api.logging;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.jboss.logging.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MDCManager {

  private static final int MAX_PAYLOAD_LENGTH = 50_000;
  public static final String REQUEST_URL = "request.url";
  public static final String REQUEST_HEADERS = "request.headers";
  public static final String REQUEST_BODY = "request.body";
  public static final String REQUEST_METHOD = "request.method";

  public static final String RESPONSE_STATUS_CODE = "response.statusCode";
  public static final String RESPONSE_BODY = "response.body";
  public static final String RESPONSE_HEADERS = "response.headers";

  public static final String REQUEST_SITE_ID = "siteId";

  @Value("${logging.sanitize.headers}")
  private String sanitizedHeaders;

  @Value("${logging.logBody}")
  boolean isLogBody;

  public void insertRequestMDC(HttpServletRequest request) {
    putRequestHeaders(request);
    MDC.put(REQUEST_URL, request.getRequestURI());
    MDC.put(REQUEST_METHOD, request.getMethod());
  }

  public void insertRequestBodyMDC(ContentCachingRequestWrapper requestWrapper) {
    if (isLogBody) {
      String requestBody = convertPayloadToString(requestWrapper.getContentAsByteArray(),
          requestWrapper.getCharacterEncoding());
      MDC.put(REQUEST_BODY, requestBody);
    }
  }

  public void insertResponseMDC(HttpServletResponse response) {
    putResponseHeaders(response);
    MDC.put(RESPONSE_STATUS_CODE, response.getStatus());
  }

  public void insertResponseBodyMDC(ContentCachingResponseWrapper responseWrapper) {
    if (isLogBody) {
      String responseBody = convertPayloadToString(responseWrapper.getContentAsByteArray(),
          responseWrapper.getCharacterEncoding());
      MDC.put(RESPONSE_BODY, responseBody);
    }
  }

  private void putRequestHeaders(HttpServletRequest request) {
    request.getHeaderNames().asIterator().forEachRemaining(header -> {
      if (!sanitizedHeaders.contains(header.toLowerCase(Locale.ENGLISH))) {
        MDC.put(REQUEST_HEADERS + "." + header, request.getHeader(header));
      }
    });
  }

  private void putResponseHeaders(HttpServletResponse response) {
    response.getHeaderNames().stream()
        .forEach(header -> MDC.put(RESPONSE_HEADERS + "." + header, response.getHeader(header)));
  }

  private String convertPayloadToString(byte[] buf, String characterEncoding) {
    String payload = "";
    if (buf.length > 0) {
      int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
      try {
        payload = new String(buf, 0, length, characterEncoding);
      } catch (UnsupportedEncodingException ex) {
        log.warn("conversion of payload to string failed : {}", ex);
        payload = "[unknown]";
      }
    }
    return payload;
  }

  public void insertSiteIdMDC(String siteId) {
    MDC.put(REQUEST_SITE_ID, siteId);
  }

}