package com.domain_name.fulfillment.configuration.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication(exclude = {
    ManagementWebSecurityAutoConfiguration.class})
@EnableFeignClients
@EnableRetry
public class FulfillmentConfigurationApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(FulfillmentConfigurationApiApplication.class, args);
  }


}
