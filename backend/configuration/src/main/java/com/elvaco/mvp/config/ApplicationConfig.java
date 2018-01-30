package com.elvaco.mvp.config;

import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.dto.IdNamedDto;
import com.elvaco.mvp.dto.MapMarkerDto;
import com.elvaco.mvp.dto.MeasurementDto;
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
import org.springframework.hateoas.core.AbstractEntityLinks;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
class ApplicationConfig {

  private final UserUseCases userUseCases;

  @Autowired
  ApplicationConfig(@Lazy UserUseCases userUseCases) {
    this.userUseCases = userUseCases;
  }

  @Bean
  UserDetailsService userDetailsService() {
    return new JpaUserDetailsService(userUseCases);
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
  ModelMapper modelMapper(AbstractEntityLinks entityLinks) {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(measurementConverter(entityLinks));
    modelMapper.addConverter(meteringPointConverter(entityLinks));
    modelMapper.addConverter(mapMarkerConverter(entityLinks));
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);
    return modelMapper;
  }

  private AbstractConverter<MeteringPointEntity, MapMarkerDto> mapMarkerConverter(
    AbstractEntityLinks entityLinks
  ) {
    return new AbstractConverter<MeteringPointEntity, MapMarkerDto>() {
      @Override
      protected MapMarkerDto convert(MeteringPointEntity source) {
        IdNamedDto status = new IdNamedDto();
        status.id = 1L;
        status.name = "ok";

        MapMarkerDto dto = new MapMarkerDto();
        dto.id = source.id;
        dto.status = status;
        dto.mapMarkerType = MapMarkerType.Meter;
        return dto;
      }
    };
  }

  private AbstractConverter<Measurement, MeasurementDto> measurementConverter(
    AbstractEntityLinks entityLinks
  ) {
    return new AbstractConverter<Measurement, MeasurementDto>() {
      @Override
      protected MeasurementDto convert(Measurement source) {
        MeasurementDto dto = new MeasurementDto();
        dto.created = source.created;
        dto.quantity = source.quantity;
        dto.id = source.id;
        dto.unit = source.unit;
        dto.value = source.value;
        dto.physicalMeter = entityLinks.linkToSingleResource(
          source.physicalMeter.getClass(),
          source.physicalMeter.id
        );
        return dto;
      }
    };
  }

  private AbstractConverter<MeteringPointEntity, MeteringPointDto> meteringPointConverter(
    AbstractEntityLinks entityLinks
  ) {
    return new AbstractConverter<MeteringPointEntity, MeteringPointDto>() {
      @Override
      protected MeteringPointDto convert(MeteringPointEntity source) {
        MeteringPointDto dto = new MeteringPointDto();
        dto.id = source.id;
        dto.medium = source.medium;
        return dto;
      }
    };
  }
}
