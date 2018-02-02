package com.elvaco.mvp.repository.access;

import java.time.Instant;
import java.util.Date;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.modelmapper.config.Configuration.AccessLevel;

public class MeteringPointMapperTest {

  private MeteringPointMapper meteringPointMapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);

    meteringPointMapper = new MeteringPointMapper();
  }

  @Test
  public void mapMeterPointEntityToDomainModelWithPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.id = (long) 1;
    meteringPointEntity.status = "Ok";
    meteringPointEntity.created = created;
    meteringPointEntity.propertyCollection
      .put("latitude", 3.1)
      .put("longitude", 2.1)
      .put("confidence", 1.1);

    MeteringPoint meteringPoint = meteringPointMapper.toDomainModel(meteringPointEntity);

    assertThat(meteringPoint).isEqualTo(
      new MeteringPoint(
        (long) 1,
        "Ok",
        new Location(3.1, 2.1, 1.1),
        created,
        new PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapMeterPointEntityToDomainModelOutPosition() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    MeteringPointEntity meteringPointEntity = new MeteringPointEntity();
    meteringPointEntity.id = (long) 1;
    meteringPointEntity.status = "Ok";
    meteringPointEntity.created = created;

    MeteringPoint meteringPoint = meteringPointMapper.toDomainModel(meteringPointEntity);

    assertThat(meteringPoint).isEqualTo(
      new MeteringPoint(
        (long) 1,
        "Ok",
        new Location(),
        created,
        new PropertyCollection(null)
      )
    );
  }

  @Test
  public void mapMeterPointDomainModelToEntity() {
    Date created = Date.from(Instant.parse("2001-01-01T10:14:00.00Z"));

    MeteringPoint meteringPoint = new MeteringPoint(
      (long) 1,
      "Ok",
      new Location(3.1, 2.1, 1.1),
      created,
      new PropertyCollection(null)
    );

    MeteringPointEntity meteringPointEntityExpected = new MeteringPointEntity();
    meteringPointEntityExpected.id = (long) 1;
    meteringPointEntityExpected.status = "Ok";
    meteringPointEntityExpected.created = created;
    meteringPointEntityExpected.propertyCollection
      .put("latitude", 3.1)
      .put("longitude", 2.1)
      .put("confidence", 1.1);

    assertThat(meteringPointMapper.toEntity(meteringPoint)).isEqualTo(meteringPointEntityExpected);
  }
}
