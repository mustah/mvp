package com.elvaco.geoservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

@Configuration
@ComponentScan("com.elvaco.geoservice")
class MethodValidationConfig {

  @Bean
  MethodValidationPostProcessor methodValidationPostProcessor() {
    return new MethodValidationPostProcessor();
  }
}
