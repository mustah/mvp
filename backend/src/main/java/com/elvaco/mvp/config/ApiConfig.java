package com.elvaco.mvp.config;

import com.elvaco.mvp.mappers.MeasurementFilterToPredicateMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

  @Bean
  MeasurementFilterToPredicateMapper predicateMapper() {
    return new MeasurementFilterToPredicateMapper();
  }
}
