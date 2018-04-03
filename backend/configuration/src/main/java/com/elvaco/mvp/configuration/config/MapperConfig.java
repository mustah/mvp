package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.web.mapper.GatewayMapper;
import com.elvaco.mvp.web.mapper.LogicalMeterMapper;
import com.elvaco.mvp.web.mapper.MeasurementMapper;
import com.elvaco.mvp.web.mapper.MeterStatusLogMapper;
import com.elvaco.mvp.web.mapper.OrganisationMapper;
import com.elvaco.mvp.web.mapper.SelectionTreeMapper;
import com.elvaco.mvp.web.mapper.SelectionsMapper;
import com.elvaco.mvp.web.mapper.UserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MapperConfig {

  @Bean
  LogicalMeterMapper logicalMeterMapper() {
    return new LogicalMeterMapper(new MeterStatusLogMapper(), new GatewayMapper(), new MeasurementMapper());
  }

  @Bean
  OrganisationMapper organisationMapper() {
    return new OrganisationMapper();
  }

  @Bean
  UserMapper userMapper() {
    return new UserMapper(new OrganisationMapper());
  }

  @Bean
  MeterStatusLogMapper meterStatusLogMapper() {
    return new MeterStatusLogMapper();
  }

  @Bean
  GatewayMapper gatewayMapper() {
    return new GatewayMapper();
  }

  @Bean
  MeasurementMapper measurementMapper() {
    return new MeasurementMapper();
  }

  @Bean
  SelectionsMapper selectionsMapper() {
    return new SelectionsMapper();
  }

  @Bean
  SelectionTreeMapper selectionTreeMapper() {
    return new SelectionTreeMapper();
  }
}
