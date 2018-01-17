package com.elvaco.mvp.repository.access;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import org.modelmapper.ModelMapper;

public class MeteringPointMapper {
  private final ModelMapper modelMapper;

  public MeteringPointMapper(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  MeteringPoint toDomainModel(MeteringPointEntity meteringPointEntity) {
    PropertyCollection props = meteringPointEntity.propertyCollection;

    Double latitude = null;
    Double longitude = null;
    Double confidence = null;

    if (props != null) {
      latitude = props.get("latitude") == null ? null : props.get("latitude").doubleValue();
      longitude = props.get("longitude") == null ? null : props.get("longitude").doubleValue();
      confidence = props.get("confidence") == null ? null : props.get("confidence").doubleValue();
    }

    return new MeteringPoint(
        meteringPointEntity.id,
        meteringPointEntity.status,
        latitude,
        longitude,
        confidence
    );
  }
}
