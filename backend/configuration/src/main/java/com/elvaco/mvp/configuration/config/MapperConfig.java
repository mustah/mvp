package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.web.mapper.GatewayDtoMapper;
import com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper;
import com.elvaco.mvp.web.mapper.MeasurementDtoMapper;
import com.elvaco.mvp.web.mapper.MeterStatusLogDtoMapper;
import com.elvaco.mvp.web.mapper.SelectionTreeDtoMapper;
import com.elvaco.mvp.web.mapper.SelectionsDtoMapper;
import com.elvaco.mvp.web.mapper.UserSelectionDtoMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MapperConfig {

  @Bean
  LogicalMeterDtoMapper logicalMeterMapper() {
    return new LogicalMeterDtoMapper(
      new MeterStatusLogDtoMapper(),
      new GatewayDtoMapper(),
      new MeasurementDtoMapper()
    );
  }

  @Bean
  MeterStatusLogDtoMapper meterStatusLogMapper() {
    return new MeterStatusLogDtoMapper();
  }

  @Bean
  GatewayDtoMapper gatewayMapper() {
    return new GatewayDtoMapper();
  }

  @Bean
  MeasurementDtoMapper measurementMapper() {
    return new MeasurementDtoMapper();
  }

  @Bean
  SelectionsDtoMapper selectionsMapper() {
    return new SelectionsDtoMapper();
  }

  @Bean
  SelectionTreeDtoMapper selectionTreeMapper() {
    return new SelectionTreeDtoMapper();
  }

  @Bean
  UserSelectionDtoMapper userSelectionMapper() {
    return new UserSelectionDtoMapper();
  }
}
