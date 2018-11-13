package com.elvaco.mvp.web;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.web.MeasurementControllerCitiesTest.MeasurementPojo.measurement;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MeasurementControllerCitiesTest extends IntegrationTest {

  @Autowired
  private LocationJpaRepository locationJpaRepository;

  @Autowired
  private MeterDefinitions meterDefinitions;

  private OrganisationEntity otherOrganisation;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());

    otherOrganisation = organisationJpaRepository.save(
      OrganisationEntity.builder()
        .id(randomUUID())
        .name("Wayne Industries")
        .slug("wayne-industries")
        .externalId("wayne-industries")
        .build()
    );
  }

  @After
  public void tearDown() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
      organisationJpaRepository.delete(otherOrganisation);
    }
  }

  @Test
  public void averagesForDifferentCitiesHaveUniqueIds() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .address("street 1")
      .city("stockholm");

    newConnectedMeterWithMeasurements(
      locationBuilder.country("sweden").build(),
      measurement(ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W"),
      measurement(ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W")
    );

    newConnectedMeterWithMeasurements(
      locationBuilder.country("england").build(),
      measurement(ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W"),
      measurement(ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W")
    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser().getList(
      "/measurements/cities"
        + "?after=2018-03-06T05:00:00.000Z"
        + "&before=2018-03-06T06:59:59.999Z"
        + "&quantities=" + Quantity.POWER.name
        + "&city=sweden,stockholm"
        + "&city=england,stockholm"
        + "&resolution=hour",
      MeasurementSeriesDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
      .extracting("id")
      .containsExactlyInAnyOrder(
        "city-sweden,stockholm-Power",
        "city-england,stockholm-Power"
      );
  }

  @Test
  public void oneCityAverage() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .city("stockholm");

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newConnectedMeterWithMeasurements(
      locationBuilder.address("stora gatan 1").build(),
      measurement(start, "Power", 1.0, "W"),
      measurement(start.plusHours(1), "Power", 2.0, "W")
    );

    newConnectedMeterWithMeasurements(
      locationBuilder.address("stora gatan 2").build(),
      measurement(start, "Power", 3.0, "W"),
      measurement(start.plusHours(1), "Power", 4.0, "W")
    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        "/measurements/cities"
          + "?after=" + start
          + "&before=" + start.plusHours(1)
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&city=sweden,stockholm"
          + "&meters=123"
          + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactly(
      new MeasurementSeriesDto(
        "city-sweden,stockholm-Power",
        Quantity.POWER.name,
        "W",
        "sweden,stockholm",
        "stockholm",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 2.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 3.0)
        )
      )
    );
  }

  @Test
  public void cityAverageOnlyIncludesRequestedCity() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("stora gatan 1");

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newConnectedMeterWithMeasurements(
      locationBuilder.build(),
      measurement(start, "Power", 1.0, "W"),
      measurement(start.plusHours(1), "Power", 2.0, "W")
    );

    newConnectedMeterWithMeasurements(
      locationBuilder.address("stora gatan 2").build(),
      measurement(start, "Power", 3.0, "W"),
      measurement(start.plusHours(1), "Power", 4.0, "W")
    );

    newConnectedMeterWithMeasurements(
      locationBuilder.city("båstad").build(),
      measurement(start, "Power", 10.0, "W"),
      measurement(start.plusHours(1), "Power", 10.0, "W")

    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        "/measurements/cities"
          + "?after=" + start
          + "&before=" + start.plusHours(1)
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&city=sweden,stockholm"
          + "&meters=123"
          + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody())
      .extracting("city")
      .hasSize(1)
      .allMatch("stockholm"::equals);
  }

  @Test
  public void cityAverageCanContainBothMetersWithRequestedQuantitesAndOtherMeters() {
    Location kiruna = new LocationBuilder()
      .country("sweden")
      .city("kiruna")
      .address("stora gatan 1")
      .build();

    PhysicalMeterEntity roomTemperature = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      kiruna,
      MeterDefinition.ROOM_TEMP_METER
    ).id);

    PhysicalMeterEntity gas = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      kiruna,
      MeterDefinition.GAS_METER
    ).id);

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newMeasurement(roomTemperature, start, Quantity.EXTERNAL_TEMPERATURE.name, 1.0, Quantity
      .EXTERNAL_TEMPERATURE.presentationUnit());
    newMeasurement(roomTemperature, start.plusHours(1), Quantity.EXTERNAL_TEMPERATURE.name, 2.0,
      Quantity.EXTERNAL_TEMPERATURE.presentationUnit()
    );

    newMeasurement(gas, start, Quantity.VOLUME.name, 10.0, Quantity.VOLUME.presentationUnit());
    newMeasurement(gas, start.plusHours(1), Quantity.VOLUME.name, 11.0, Quantity.VOLUME
      .presentationUnit());

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        "/measurements/cities"
          + "?after=" + start
          + "&before=" + start.plusHours(1)
          + "&quantities=External+temperature"
          + "&city=sweden,kiruna"
          + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        "city-sweden,kiruna-External temperature",
        Quantity.EXTERNAL_TEMPERATURE.name,
        Quantity.EXTERNAL_TEMPERATURE.presentationUnit(),
        "sweden,kiruna",
        "kiruna",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 1.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 2.0)
        )
      )
    );
  }

  @Test
  public void cityAverageOfTwoQuantitiesBelongingToDifferentMedia() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .address("stora gatan 1");

    PhysicalMeterEntity roomTemperature = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      locationBuilder.city("knivsta").build(),
      MeterDefinition.ROOM_TEMP_METER
    ).id);

    PhysicalMeterEntity gas = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      locationBuilder.city("umeå").build(),
      MeterDefinition.GAS_METER
    ).id);

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newMeasurement(roomTemperature, start, Quantity.EXTERNAL_TEMPERATURE.name, 1.0, Quantity
      .EXTERNAL_TEMPERATURE.presentationUnit());
    newMeasurement(roomTemperature, start.plusHours(1), Quantity.EXTERNAL_TEMPERATURE.name, 2.0,
      Quantity.EXTERNAL_TEMPERATURE.presentationUnit()
    );

    newMeasurement(gas, start, Quantity.VOLUME.name, 10.0, Quantity.VOLUME.presentationUnit());
    newMeasurement(gas, start.plusHours(1), Quantity.VOLUME.name, 11.0, Quantity.VOLUME
      .presentationUnit());

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        "/measurements/cities"
          + "?after=" + start
          + "&before=" + start.plusHours(1)
          + "&quantities=External+temperature,Volume"
          + "&city=sweden,knivsta"
          + "&city=sweden,umeå"
          + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        "city-sweden,knivsta-External temperature",
        Quantity.EXTERNAL_TEMPERATURE.name,
        Quantity.EXTERNAL_TEMPERATURE.presentationUnit(),
        "sweden,knivsta",
        "knivsta",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 1.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 2.0)
        )
      ),
      new MeasurementSeriesDto(
        "city-sweden,umeå-Volume",
        Quantity.VOLUME.name,
        Quantity.VOLUME.presentationUnit(),
        "sweden,umeå",
        "umeå",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 1.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), null)
        )
      )
    );
  }

  @Test
  public void cityAverageIsEmptyWhenCityOnlyContainNonMatchingQuantities() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("stora gatan 1");

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");
    newConnectedMeterWithMeasurements(
      locationBuilder.build(),
      measurement(start, "Power", 1.0, "W")

    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        "/measurements/cities"
          + "?after=" + start
          + "&before=" + start.plusHours(1)
          + "&quantities=Relative+humidity"
          + "&city=sweden,stockholm"
          + "&meters=123"
          + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void cityAverageIsEmptyWhenNoMetersExistsInCity() {
    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        "/measurements/cities"
          + "?after=" + start
          + "&before=" + start.plusHours(1)
          + "&quantities=" + Quantity.VOLUME.name
          + "&city=sweden,stockholm"
          + "&meters=123"
          + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void twoCityAverages() {
    LocationBuilder locationBuilder = new LocationBuilder()
      .country("sweden")
      .city("stockholm")
      .address("stora gatan 1");

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newConnectedMeterWithMeasurements(
      locationBuilder.build(),
      measurement(start, "Power", 1.0, "W"),
      measurement(start.plusHours(1), "Power", 2.0, "W")
    );

    newConnectedMeterWithMeasurements(
      locationBuilder.city("båstad").build(),
      measurement(start, "Power", 10.0, "W"),
      measurement(start.plusHours(1), "Power", 10.0, "W")
    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        "/measurements/cities"
          + "?after=" + start
          + "&before=" + start.plusHours(1)
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&city=sweden,stockholm"
          + "&city=sweden,båstad"
          + "&meters=123"
          + "&resolution=hour",
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        "city-sweden,stockholm-Power",
        Quantity.POWER.name,
        "W",
        "sweden,stockholm",
        "stockholm",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 1.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 2.0)
        )
      ),
      new MeasurementSeriesDto(
        "city-sweden,båstad-Power",
        Quantity.POWER.name,
        "W",
        "sweden,båstad",
        "båstad",
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 10.0),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 10.0)
        )
      )
    );
  }

  @Override
  protected void afterRemoveEntitiesHook() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
      organisationJpaRepository.delete(otherOrganisation);
    }
  }

  PhysicalMeterEntity newConnectedMeterWithMeasurements(
    Location location,
    MeasurementPojo... measurements
  ) {
    PhysicalMeterEntity meter = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      location
    ).id);
    Arrays.stream(measurements).forEach(
      m -> newMeasurement(meter, m.created, m.quantity, m.value, m.unit)
    );
    return meter;
  }

  private MeterDefinitionEntity saveMeterDefinition(MeterDefinition meterDefinition) {
    return MeterDefinitionEntityMapper.toEntity(meterDefinitions.save(meterDefinition));
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime created,
    String quantity,
    double value,
    String unit
  ) {
    measurementJpaRepository.save(new MeasurementEntity(
      created,
      QuantityEntityMapper.toEntity(QuantityAccess.singleton().getByName(quantity)),
      value,
      meter
    ));
  }

  private LogicalMeterEntity newLogicalMeterEntityWithLocation(
    Location location,
    MeterDefinition meterDefinition
  ) {
    UUID uuid = randomUUID();

    MeterDefinitionEntity meterDefinitionEntity = saveMeterDefinition(meterDefinition);

    LogicalMeterEntity meter = logicalMeterJpaRepository.save(new LogicalMeterEntity(
      uuid,
      uuid.toString(),
      context().organisationEntity.id,
      ZonedDateTime.now(),
      meterDefinitionEntity
    ));

    locationJpaRepository.save(new LocationEntity(
      meter.id,
      location.getCountry(),
      location.getCity(),
      location.getAddress()
    ));

    return meter;
  }

  private LogicalMeterEntity newLogicalMeterEntityWithLocation(
    Location location
  ) {
    return newLogicalMeterEntityWithLocation(
      location,
      MeterDefinition.DISTRICT_HEATING_METER
    );
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(UUID logicalMeterId) {
    UUID uuid = randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationEntity,
      "",
      uuid.toString(),
      "",
      "",
      logicalMeterId,
      0,
      1,
      1,
      emptySet(),
      emptySet()
    ));
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class MeasurementPojo {

    public final String unit;
    public final ZonedDateTime created;
    public final double value;
    public final String quantity;

    static MeasurementPojo measurement(
      ZonedDateTime created,
      String quantity,
      double value,
      String unit
    ) {
      return new MeasurementPojo(unit, created, value, quantity);
    }
  }
}
