package com.elvaco.mvp.config;

import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.MapMarkerDto;
import com.elvaco.mvp.dto.MeteringPointDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.jpa.mappers.FilterToPredicateMapper;
import com.elvaco.mvp.repository.jpa.mappers.MeasurementFilterToPredicateMapper;
import com.elvaco.mvp.repository.jpa.mappers.MeteringPointToPredicateMapper;
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
  MeteringPointToPredicateMapper meteringPointToPredicateMapper() {
    return new MeteringPointToPredicateMapper();
  }

  @Bean
  ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(meteringPointConverter());
    modelMapper.addConverter(mapMarkerConverter());
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);
    return modelMapper;
  }

  private AbstractConverter<MeteringPointEntity, MapMarkerDto> mapMarkerConverter() {
    return new AbstractConverter<MeteringPointEntity, MapMarkerDto>() {
      @Override
      protected MapMarkerDto convert(MeteringPointEntity source) {
        MapMarkerDto dto = new MapMarkerDto();
        dto.id = source.id;
        dto.status = new IdNamedDto(1L, "ok");
        dto.mapMarkerType = MapMarkerType.Meter;
        return dto;
      }
    };
  }

  private AbstractConverter<MeteringPointEntity, MeteringPointDto> meteringPointConverter() {
    return new AbstractConverter<MeteringPointEntity, MeteringPointDto>() {
      @Override
      protected MeteringPointDto convert(MeteringPointEntity source) {
        MeteringPointDto dto = new MeteringPointDto();
        dto.id = source.id;
        dto.medium = source.medium;
        dto.created = source.created.toString();
        return dto;
      }
    };
  }
}
