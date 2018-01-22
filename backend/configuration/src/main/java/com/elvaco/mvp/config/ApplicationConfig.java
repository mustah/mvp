package com.elvaco.mvp.config;

import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.repository.jpa.mappers.FilterToPredicateMapper;
import com.elvaco.mvp.repository.jpa.mappers.MeasurementFilterToPredicateMapper;
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
  ModelMapper modelMapper(AbstractEntityLinks entityLinks) {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper.addConverter(measurementConverter(entityLinks));
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);
    return modelMapper;
  }

  private AbstractConverter<MeasurementEntity, MeasurementDto> measurementConverter(
    AbstractEntityLinks entityLinks
  ) {
    return new AbstractConverter<MeasurementEntity, MeasurementDto>() {
      @Override
      protected MeasurementDto convert(MeasurementEntity source) {
        MeasurementDto dto = new MeasurementDto();
        dto.created = source.created;
        dto.quantity = source.quantity;
        dto.id = source.id;
        dto.unit = source.value.getUnit();
        dto.value = source.value.getValue();
        dto.physicalMeter = entityLinks.linkToSingleResource(
          source.physicalMeter.getClass(),
          source.physicalMeter.id
        );
        return dto;
      }
    };
  }
}
