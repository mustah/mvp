package com.elvaco.mvp.config;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.core.AbstractEntityLinks;

@Configuration
public class ApplicationConfig {

  @Bean
  @Autowired
  public ModelMapper modelMapper(AbstractEntityLinks entityLinks) {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
        .getConfiguration()
        .setFieldMatchingEnabled(true)
        .setFieldAccessLevel(AccessLevel.PUBLIC);
    modelMapper.addConverter(new AbstractConverter<MeasurementEntity, MeasurementDto>() {
      @Override
      protected MeasurementDto convert(MeasurementEntity source) {
        MeasurementDto dto = new MeasurementDto();
        dto.created = source.created;
        dto.quantity = source.quantity;
        dto.id = source.id;
        dto.unit = source.value.getUnit();
        dto.value = source.value.getValue();
        dto.physicalMeter = entityLinks.linkToSingleResource(
            source.physicalMeter.getClass(), source.physicalMeter.id);
        return dto;
      }
    });
    return modelMapper;
  }
}
