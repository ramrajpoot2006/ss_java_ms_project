package com.domain_name.fulfillment.configuration.api.logging;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {

  private final MDCManager mdcManager;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    if (request.getRequestURI().contains("/actuator")) {
      filterChain.doFilter(request, response);
    } else {
      processLoggingRequest(request, response, filterChain);
    }
  }

  private void processLoggingRequest(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws IOException {
    mdcManager.insertRequestMDC(request);
    mdcManager.insertResponseMDC(response);
    ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
    try {
      filterChain.doFilter(requestWrapper, responseWrapper);
    } catch (ServletException e) {
      log.error("error while executing the request:  {}", e);
    } finally {
      mdcManager.insertRequestBodyMDC(requestWrapper);
      mdcManager.insertResponseBodyMDC(responseWrapper);
      log.info("Request Completed");
      MDC.clear();
      responseWrapper.copyBodyToResponse();
    }
  }

}
