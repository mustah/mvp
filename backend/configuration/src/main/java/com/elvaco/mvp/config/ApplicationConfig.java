package com.elvaco.mvp.config;

import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.LogicalMeterDto;
import com.elvaco.mvp.dto.MapMarkerDto;
import com.elvaco.mvp.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.repository.jpa.mappers.FilterToPredicateMapper;
import com.elvaco.mvp.repository.jpa.mappers.LogicalMeterToPredicateMapper;
import com.elvaco.mvp.repository.jpa.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.security.JpaUserDetailsService;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
class ApplicationConfig {

  private final Users users;

  @Autowired
  ApplicationConfig(@Lazy Users users) {
    this.users = users;
  }

  @Bean
  UserDetailsService userDetailsService() {
    return new JpaUserDetailsService(users);
  }

  @Bean
  FilterToPredicateMapper predicateMapper() {
    return new MeasurementFilterToPredicateMapper();
  }

  @Bean
  LogicalMeterToPredicateMapper logicalMeterToPredicateMapper() {
    return new LogicalMeterToPredicateMapper();
  }

  @Bean
  ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(logicalMeterConverter());
    modelMapper.addConverter(mapMarkerConverter());
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);
    return modelMapper;
  }

  private AbstractConverter<LogicalMeterEntity, MapMarkerDto> mapMarkerConverter() {
    return new AbstractConverter<LogicalMeterEntity, MapMarkerDto>() {
      @Override
      protected MapMarkerDto convert(LogicalMeterEntity source) {
        MapMarkerDto dto = new MapMarkerDto();
        dto.id = source.id;
        dto.status = new IdNamedDto(1L, "ok");
        dto.mapMarkerType = MapMarkerType.Meter;
        return dto;
      }
    };
  }

  private AbstractConverter<LogicalMeterEntity, LogicalMeterDto> logicalMeterConverter() {
    return new AbstractConverter<LogicalMeterEntity, LogicalMeterDto>() {
      @Override
      protected LogicalMeterDto convert(LogicalMeterEntity source) {
        LogicalMeterDto dto = new LogicalMeterDto();
        dto.id = source.id;
        dto.medium = source.medium;
        dto.created = source.created.toString();
        return dto;
      }
    };
  }
}
