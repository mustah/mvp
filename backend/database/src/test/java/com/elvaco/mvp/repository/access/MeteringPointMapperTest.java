package com.elvaco.mvp.repository.access;

import java.time.Instant;
import java.util.Date;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

public class MeteringPointMapperTest {

  private MeteringPointMapper meteringPointMapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(Configuration.AccessLevel.PUBLIC);

    meteringPointMapper = new MeteringPointMapper();
  }

  @Test
  public void mapMeterPointEntityToDomainModelWithPosition() {
    long id = 1;
    String status = "Ok";
    double latitude = 3.1;
    double longitude = 2.1;
    double confidence = 1.1;
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.id = id;
    meteringPointEntity.status = status;
    meteringPointEntity.propertyCollection = new PropertyCollection();
    meteringPointEntity.propertyCollection.put("latitude", latitude);
    meteringPointEntity.propertyCollection.put("longitude", longitude);
    meteringPointEntity.propertyCollection.put("confidence", confidence);
    meteringPointEntity.created = created;

    MeteringPoint meteringPoint = meteringPointMapper.toDomainModel(meteringPointEntity);

    assertThat(meteringPoint).isEqualTo(
      new MeteringPoint(
        id,
        status,
        new Location(latitude, longitude, confidence),
        created,
        new com.elvaco.mvp.core.domainmodels.PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapMeterPointEntityToDomainModelOutPosition() {
    long id = 1;
    String status = "Ok";
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.id = id;
    meteringPointEntity.status = status;
    meteringPointEntity.created = created;


    MeteringPoint meteringPoint = meteringPointMapper.toDomainModel(meteringPointEntity);

    assertThat(meteringPoint).isEqualTo(
      new MeteringPoint(
        id,
        status,
        new Location(),
        created,
        new com.elvaco.mvp.core.domainmodels.PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapMeterPointDomainModelToEntity() {
    long id = 1;
    String status = "Ok";
    double latitude = 3.1;
    double longitude = 2.1;
    double confidence = 1.1;
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));
    com.elvaco.mvp.core.domainmodels.PropertyCollection propertyCollection =
      new com.elvaco.mvp.core.domainmodels.PropertyCollection(null);

    MeteringPoint meteringPoint = new MeteringPoint(
      id,
      status,
      latitude,
      longitude,
      confidence,
      created,
      propertyCollection
    );

    final MeteringPointEntity meteringPointEntityActual =
      meteringPointMapper.toEntity(meteringPoint);

    MeteringPointEntity meteringPointEntityExpected = new MeteringPointEntity();
    meteringPointEntityExpected.id = id;
    meteringPointEntityExpected.status = status;
    meteringPointEntityExpected.propertyCollection = new PropertyCollection();
    meteringPointEntityExpected.propertyCollection.put("latitude", latitude);
    meteringPointEntityExpected.propertyCollection.put("longitude", longitude);
    meteringPointEntityExpected.propertyCollection.put("confidence", confidence);
    meteringPointEntityExpected.created = created;

    assertThat(meteringPointEntityActual).isEqualTo(meteringPointEntityExpected);
  }
}
