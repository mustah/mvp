package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.MeasurementDto;
import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.hateoas.core.ControllerEntityLinks;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/api/measurements")
public class MeasurementController {

  private final MeasurementRepository repository;
  private final ModelMapper modelMapper;
  private final ControllerEntityLinks entityLinks;

  @Autowired
  MeasurementController(MeasurementRepository repository, ModelMapper modelMapper,
                        ControllerEntityLinks entityLinks) {
    this.repository = repository;
    this.modelMapper = modelMapper;
    this.entityLinks = entityLinks;
  }

  @RequestMapping("{id}")
  public MeasurementDto measurement(@PathVariable("id") Long id) {
    return toDto(repository.findOne(id));
  }

  @RequestMapping("")
  public Page<MeasurementDto> measurements(@Param("quantity") String quantity, @Param("scale")
      String scale, Pageable pageable) {
    Page<MeasurementEntity> entityPage = null;
    if (quantity != null) {
      if (scale != null) {
        entityPage = repository.findByQuantityScaled(quantity, scale, pageable);
      } else {
        entityPage = repository.findByQuantity(quantity, pageable);
      }
    } else {
      entityPage = repository.findAll(pageable);
    }
    return entityPage.map(source -> toDto(source));
  }

  private MeasurementDto toDto(MeasurementEntity measurementEntity) {
    MeasurementDto dto = modelMapper.map(measurementEntity, MeasurementDto.class);
    dto.unit = measurementEntity.value.getUnit();
    dto.value = measurementEntity.value.getValue();
    dto.physicalMeter = entityLinks.linkToSingleResource(
        measurementEntity.physicalMeter.getClass(), measurementEntity.physicalMeter.id);
    return dto;
  }

}
