package com.elvaco.mvp.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
  @Bean
  MeasurementFilterToPredicateMapper measurementFilterToPredicateMapper() {
    return new MeasurementFilterToPredicateMapper();
  }
}
