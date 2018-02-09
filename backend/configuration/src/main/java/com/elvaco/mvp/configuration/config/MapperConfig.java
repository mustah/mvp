package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.web.mapper.LogicalMeterMapper;
import com.elvaco.mvp.web.mapper.OrganisationMapper;
import com.elvaco.mvp.web.mapper.UserMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class MapperConfig {

  private final ModelMapper modelMapper;

  @Autowired
  MapperConfig(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  @Bean
  LogicalMeterMapper logicalMeterMapper() {
    return new LogicalMeterMapper(modelMapper);
  }

  @Bean
  OrganisationMapper organisationMapper() {
    return new OrganisationMapper();
  }

  @Bean
  UserMapper userMapper() {
    return new UserMapper(modelMapper);
  }
}
