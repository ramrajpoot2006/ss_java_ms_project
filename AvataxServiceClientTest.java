package com.domain_name.next.taxationapi.client;

import com.domain_name.next.taxationapi.TestHelper;
import com.domain_name.next.taxationapi.client.request.AvaTaxRequest;
import com.domain_name.next.taxationapi.client.response.AvaTaxResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class AvataxServiceClientTest extends TestHelper {

  @InjectMocks
  AvaTaxServiceClient avataxServiceClient;
  
  AvaTaxRequest avataxRequest;

  private final WebClient.Builder webClientBuilder = Mockito.mock(WebClient.Builder.class);
  private final WebClient webClient = Mockito.mock(WebClient.class);
  private final WebClient.RequestBodyUriSpec requestBodyUriSpec = Mockito.mock(WebClient.RequestBodyUriSpec.class);
  private final WebClient.RequestBodySpec requestBodySpec = Mockito.mock(WebClient.RequestBodySpec.class);
  @SuppressWarnings("rawtypes")
  private final WebClient.RequestHeadersSpec requestHeadersSpec = Mockito.mock(WebClient.RequestHeadersSpec.class);
  private final WebClient.ResponseSpec responseSpec = Mockito.mock(WebClient.ResponseSpec.class);

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(avataxServiceClient, "uri", "/api/v2/transactions/create");
    ReflectionTestUtils.setField(avataxServiceClient, "username", "username");
    ReflectionTestUtils.setField(avataxServiceClient, "password", "password");

    webClientBuilder.baseUrl("https://sandbox-rest.avatax.com");
    Mockito. when(webClientBuilder.baseUrl(Mockito.anyString())).thenReturn(webClientBuilder);
    Mockito. when(webClientBuilder.build()).thenReturn(webClient);
    avataxRequest = buildAvataxRequest();
  }

  @Test
  @SuppressWarnings("unchecked")
  void getTaxSuccessFlow() {
    AvaTaxResponse avataxResponse = buildAvataxResponse();
    Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
    Mockito.when(requestBodyUriSpec.uri("/api/v2/transactions/create")).thenReturn(requestBodySpec);
    Mockito.when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
    Mockito.when(requestBodySpec.headers(Mockito.any())).thenReturn(requestBodySpec);
    Mockito.when(requestBodySpec.body(Mockito.any(), Mockito.eq(AvaTaxRequest.class))).thenReturn(requestHeadersSpec);
    Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
    Mockito.when(responseSpec.bodyToMono(AvaTaxResponse.class)).thenReturn(Mono.just(avataxResponse));
    StepVerifier
        .create(avataxServiceClient.getTax(avataxRequest))
        .thenConsumeWhile(avataxRes -> {
          Assertions.assertEquals(avataxResponse.getTotalTaxCalculated(), avataxRes.getTotalTaxCalculated());
          return true;
        }).verifyComplete();
  }

  @Test
  @SuppressWarnings("unchecked")
  void getTaxFailureFlow() {
    Mockito.when(webClient.post()).thenReturn(requestBodyUriSpec);
    Mockito.when(requestBodyUriSpec.uri("/api/v2/transactions/create")).thenReturn(requestBodySpec);
    Mockito.when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
    Mockito.when(requestBodySpec.headers(Mockito.any())).thenReturn(requestBodySpec);
    Mockito.when(requestBodySpec.body(Mockito.any(Mono.class), Mockito.eq(AvaTaxRequest.class))).thenReturn(requestHeadersSpec);
    Mockito.when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    Mockito.when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
    Mockito.when(responseSpec.bodyToMono(AvaTaxResponse.class)).thenReturn(Mono.error(new Exception()));
    StepVerifier.create(avataxServiceClient.getTax(avataxRequest))
        .expectErrorMatches(throwable -> throwable instanceof Exception)
        .verify();
  }
}
