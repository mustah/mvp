package com.elvaco.mvp.repository.access;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
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
    UserProperty userProperty = null;

    if (props != null) {
      latitude = props.get("latitude") == null ? null : props.get("latitude").doubleValue();
      longitude = props.get("longitude") == null ? null : props.get("longitude").doubleValue();
      confidence = props.get("confidence") == null ? null : props.get("confidence").doubleValue();
      userProperty = props.get("user") == null ? null : props.asObject("user", UserProperty.class);
    }

    return new MeteringPoint(
      meteringPointEntity.id,
      meteringPointEntity.status,
      latitude,
      longitude,
      confidence,
      new com.elvaco.mvp.core.domainmodels.PropertyCollection(userProperty)
    );
  }

  public MeteringPointEntity toEntity(MeteringPoint meteringPoint) {
    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.propertyCollection = new PropertyCollection()
      .put("user", userPropertyToDto(meteringPoint.propertyCollection.userProperty))
      .put("latitude", meteringPoint.latitude)
      .put("longitude", meteringPoint.longitude)
      .put("confidence", meteringPoint.confidence);

    return meteringPointEntity;
  }

  private UserPropertyDto userPropertyToDto(UserProperty userProperty) {
    return new UserPropertyDto(
      userProperty.externalId,
      userProperty.project);
  }
}
