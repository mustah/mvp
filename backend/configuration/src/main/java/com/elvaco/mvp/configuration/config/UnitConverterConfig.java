package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.unitconverter.UomUnitConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UnitConverterConfig {

  @Bean
  UnitConverter unitConverter() {
    return UomUnitConverter.singleton();
  }
}
