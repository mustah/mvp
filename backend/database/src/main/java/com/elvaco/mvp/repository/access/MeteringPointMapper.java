package com.elvaco.mvp.repository.access;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.domainmodels.UserProperty;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;

public class MeteringPointMapper {

  MeteringPoint toDomainModel(MeteringPointEntity meteringPointEntity) {
    PropertyCollection props = meteringPointEntity.propertyCollection;

    UserProperty userProperty =
      props
        .asObject("user", UserProperty.class)
        .orElse(null);

    Location location = new Location(
      props.getDoubleValue("latitude").orElse(null),
      props.getDoubleValue("longitude").orElse(null),
      props.getDoubleValue("confidence").orElse(null)
    );

    return new MeteringPoint(
      meteringPointEntity.id,
      meteringPointEntity.status,
      location,
      meteringPointEntity.created,
      new com.elvaco.mvp.core.domainmodels.PropertyCollection(userProperty)
    );
  }

  public MeteringPointEntity toEntity(MeteringPoint meteringPoint) {
    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.propertyCollection = new PropertyCollection()
      .put("latitude", meteringPoint.location.getLatitude().orElse(null))
      .put("longitude", meteringPoint.location.getLongitude().orElse(null))
      .put("confidence", meteringPoint.location.getConfidence());

    if (meteringPoint.propertyCollection.userProperty != null) {
      meteringPointEntity.propertyCollection.put(
        "user", userPropertyToDto(meteringPoint.propertyCollection.userProperty)
      );
    }

    meteringPointEntity.id = meteringPoint.id;
    meteringPointEntity.created = meteringPoint.created;
    meteringPointEntity.status = meteringPoint.status;

    return meteringPointEntity;
  }

  private UserPropertyDto userPropertyToDto(UserProperty userProperty) {
    return new UserPropertyDto(
      userProperty.externalId,
      userProperty.project
    );
  }
}
