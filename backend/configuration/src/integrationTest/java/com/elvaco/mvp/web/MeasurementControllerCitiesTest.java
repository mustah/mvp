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
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
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

import static com.elvaco.mvp.testing.fixture.LocationTestData.stockholm;
import static com.elvaco.mvp.web.MeasurementControllerCitiesTest.MeasurementPojo.measurement;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
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
  public void oneCityAverage() {
    LocationBuilder locationBuilder = stockholm();

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newConnectedMeterWithMeasurements(
      locationBuilder.address("stora gatan 1").build(),
      measurement(start, "Power", 1.0),
      measurement(start.plusHours(1), "Power", 2.0)
    );

    newConnectedMeterWithMeasurements(
      locationBuilder.address("stora gatan 2").build(),
      measurement(start, "Power", 3.0),
      measurement(start.plusHours(1), "Power", 4.0)
    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsCitiesUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.POWER.name + ":W")
          .parameter("label", "Stockholm")
          .city("sverige,stockholm")
          .resolution("hour")
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactly(
      new MeasurementSeriesDto(
        "average-Power",
        Quantity.POWER.name,
        "W",
        "Stockholm",
        "sverige,stockholm",
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
    var start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newConnectedMeterWithMeasurements(
      stockholm().address("stora gatan 1").build(),
      measurement(start, "Power", 1.0),
      measurement(start.plusHours(1), "Power", 2.0)
    );

    newConnectedMeterWithMeasurements(
      stockholm().address("stora gatan 2").build(),
      measurement(start, "Power", 3.0),
      measurement(start.plusHours(1), "Power", 4.0)
    );

    newConnectedMeterWithMeasurements(
      stockholm().address("stora gatan 1").city("båstad").build(),
      measurement(start, "Power", 10.0),
      measurement(start.plusHours(1), "Power", 10.0)
    );

    var response = asUser()
      .getList(
        measurementsCitiesUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.POWER.name + ":W")
          .city("sverige,stockholm")
          .resolution("hour")
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var measurementSeries = response.getBody();

    assertSoftly(softly -> {
      softly.assertThat(measurementSeries)
        .extracting("city")
        .containsExactly("sverige,stockholm");

      softly.assertThat(measurementSeries)
        .flatExtracting("values")
        .extracting("value")
        .containsExactly(2.0, 3.0);
    });
  }

  @Test
  public void cityAverageCanContainBothMetersWithRequestedQuantitesAndOtherMeters() {
    Location kiruna = new LocationBuilder()
      .country("sverige")
      .city("kiruna")
      .address("stora gatan 1")
      .build();

    PhysicalMeterEntity roomTemperature = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      kiruna,
      MeterDefinition.ROOM_TEMP_METER
    ).getLogicalMeterId());

    PhysicalMeterEntity gas = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      kiruna,
      MeterDefinition.GAS_METER
    ).getLogicalMeterId());

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newMeasurement(roomTemperature, start, Quantity.EXTERNAL_TEMPERATURE.name, 1.0);
    newMeasurement(roomTemperature, start.plusHours(1), Quantity.EXTERNAL_TEMPERATURE.name, 2.0
    );

    newMeasurement(gas, start, Quantity.VOLUME.name, 10.0);
    newMeasurement(gas, start.plusHours(1), Quantity.VOLUME.name, 11.0);

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsCitiesUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", "External temperature")
          .city("sverige,kiruna")
          .resolution("hour")
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        "average-External temperature",
        Quantity.EXTERNAL_TEMPERATURE.name,
        Quantity.EXTERNAL_TEMPERATURE.presentationUnit(),
        "average",
        "sverige,kiruna",
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
  public void cityAverageIsEmptyWhenCityOnlyContainNonMatchingQuantities() {
    LocationBuilder locationBuilder = stockholm().address("stora gatan 1");

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");
    newConnectedMeterWithMeasurements(
      locationBuilder.build(),
      measurement(start, "Power", 1.0)

    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsCitiesUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", "Relative humidity")
          .city("sverige,stockholm")
          .resolution("hour")
          .build(),
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
        measurementsCitiesUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.VOLUME.name)
          .city("sverige,stockholm")
          .resolution("hour").build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void twoCitiesArePartOfSingleAverage() {
    LocationBuilder stockholmStoraGatan1 = stockholm().address("stora gatan 1");

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    newConnectedMeterWithMeasurements(
      stockholmStoraGatan1.build(),
      measurement(start, "Power", 1.0),
      measurement(start.plusHours(1), "Power", 2.0)
    );

    newConnectedMeterWithMeasurements(
      stockholmStoraGatan1.city("båstad").build(),
      measurement(start, "Power", 10.0),
      measurement(start.plusHours(1), "Power", 10.0)
    );

    ResponseEntity<List<MeasurementSeriesDto>> response = asUser()
      .getList(
        measurementsCitiesUrl()
          .period(start, start.plusHours(1))
          .parameter("quantity", Quantity.POWER.name + ":W")
          .city("sverige,stockholm")
          .city("sverige,båstad")
          .resolution("hour")
          .build(),
        MeasurementSeriesDto.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).containsExactly(
      new MeasurementSeriesDto(
        "average-Power",
        Quantity.POWER.name,
        "W",
        "average",
        null,
        null,
        null,
        asList(
          new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 5.5),
          new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 6.0)
        )
      )
    );
  }

  @Test
  public void averageForCityIsSameAsAverageForAllMetersInCity() {
    LocationBuilder locationBuilder = stockholm();

    ZonedDateTime start = ZonedDateTime.parse("2018-09-07T03:00:00Z");

    PhysicalMeterEntity meterOne = newConnectedMeterWithMeasurements(
      locationBuilder.address("stora gatan 1").build(),
      measurement(start, "Power", 1.0),
      measurement(start.plusHours(1), "Power", 2.0)
    );

    PhysicalMeterEntity meterTwo = newConnectedMeterWithMeasurements(
      locationBuilder.address("stora gatan 2").build(),
      measurement(start, "Power", 3.0),
      measurement(start.plusHours(1), "Power", 4.0)
    );

    Url cityAverageUrl = measurementsCitiesUrl()
      .period(start, start.plusHours(1))
      .city("sverige,stockholm")
      .resolution("hour")
      .parameter("quantity", Quantity.POWER.name + ":W")
      .build();

    Url metersAverageUrl = Url
      .builder()
      .path("/measurements/average")
      .period(start, start.plusHours(1))
      .resolution("hour")
      .parameter("quantity", Quantity.POWER.name + ":W")
      .parameter("meters", List.of(meterOne.getLogicalMeterId(), meterTwo.getLogicalMeterId()))
      .build();

    ResponseEntity<List<MeasurementSeriesDto>> cityAverageResponse = asUser()
      .getList(cityAverageUrl, MeasurementSeriesDto.class
      );

    ResponseEntity<List<MeasurementSeriesDto>> metersAverageResponse = asUser()
      .getList(
        metersAverageUrl,
        MeasurementSeriesDto.class
      );

    assertThat(cityAverageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(metersAverageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

    List<MeasurementValueDto> expected = List.of(
      new MeasurementValueDto(Instant.parse("2018-09-07T03:00:00Z"), 2.0),
      new MeasurementValueDto(Instant.parse("2018-09-07T04:00:00Z"), 3.0)
    );

    assertThat(cityAverageResponse.getBody()).extracting("values").containsExactly(expected);
    assertThat(metersAverageResponse.getBody()).extracting("values").containsExactly(expected);
  }

  @Override
  protected void afterRemoveEntitiesHook() {
    if (isPostgresDialect()) {
      measurementJpaRepository.deleteAll();
      organisationJpaRepository.delete(otherOrganisation);
    }
  }

  private PhysicalMeterEntity newConnectedMeterWithMeasurements(
    Location location,
    MeasurementPojo... measurements
  ) {
    PhysicalMeterEntity meter = newPhysicalMeterEntity(newLogicalMeterEntityWithLocation(
      location
    ).getLogicalMeterId());
    Arrays.stream(measurements).forEach(
      m -> newMeasurement(meter, m.created, m.quantity, m.value)
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
    double value
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
    UUID id = randomUUID();

    var meterDefinitionEntity = saveMeterDefinition(meterDefinition);

    var pk = new EntityPk(id, context().organisationId());
    LogicalMeterEntity meter = logicalMeterJpaRepository.save(
      new LogicalMeterEntity(
        pk,
        id.toString(),
        ZonedDateTime.now(),
        meterDefinitionEntity,
        DEFAULT_UTC_OFFSET
      ));

    locationJpaRepository.save(LocationEntity.builder()
      .pk(pk)
      .country(location.getCountry())
      .city(location.getCity())
      .streetAddress(location.getAddress())
      .build());

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
      context().organisationId(),
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

  private Url.UrlBuilder measurementsCitiesUrl() {
    return Url.builder().path("/measurements/average");
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class MeasurementPojo {

    public final ZonedDateTime created;
    public final double value;
    public final String quantity;

    static MeasurementPojo measurement(
      ZonedDateTime created,
      String quantity,
      double value
    ) {
      return new MeasurementPojo(created, value, quantity);
    }
  }
}
