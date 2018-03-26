package com.elvaco.mvp.web;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeasurementJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MeasurementSeriesDto;
import com.elvaco.mvp.web.dto.MeasurementValueDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class MeasurementControllerAggregatesTest extends IntegrationTest {

  @Autowired
  private MeasurementJpaRepository measurementJpaRepository;
  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;
  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  private MeterDefinitionMapper meterDefinitionMapper;

  @Before
  public void setUp() {
    assumeTrue(isPostgresDialect());
    meterDefinitionMapper = new MeterDefinitionMapper();
  }

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void averageOfOneMeterTwoHours() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T06:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(new MeasurementSeriesDto(
        Quantity.POWER.name,
        Quantity.POWER.unit,
        "average",
        asList(
          new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            1.0
          ),
          new MeasurementValueDto(
            Instant.parse("2018-03-06T06:00:00Z"),
            2.0
          )
        )
      )));
  }

  @Test
  public void averageOfTwoMetersTwoHours() {
    LogicalMeterEntity logicalMeter1 = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter1 = newPhysicalMeterEntity(logicalMeter1.id);
    newMeasurement(meter1, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter1, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 2.0, "W");

    LogicalMeterEntity logicalMeter2 = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter2 = newPhysicalMeterEntity(logicalMeter2.id);
    newMeasurement(meter2, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 3.0, "W");
    newMeasurement(meter2, ZonedDateTime.parse("2018-03-06T06:00:01Z"), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T06:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        String.join(",", asList(logicalMeter1.id.toString(), logicalMeter2.id.toString()))
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name,
          Quantity.POWER.unit,
          "average",
          asList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T05:00:00Z"),
              2.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-03-06T06:00:00Z"),
              3.0
            )
          )
        )));
  }

  @Test
  public void averageForMultipleQuantities() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:02Z"), "Power", 4.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:03Z"), "Power", 6.0, "W");

    newMeasurement(
      meter,
      ZonedDateTime.parse("2018-03-06T05:00:01Z"),
      "Difference temperature",
      20.0,
      "K"
    );
    newMeasurement(
      meter,
      ZonedDateTime.parse("2018-03-06T05:00:02Z"),
      "Difference temperature",
      40.0,
      "K"
    );
    newMeasurement(
      meter,
      ZonedDateTime.parse("2018-03-06T05:00:03Z"),
      "Difference temperature",
      60.0,
      "K"
    );

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T05:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name + "," + Quantity.DIFFERENCE_TEMPERATURE.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).containsExactlyInAnyOrder(
      new MeasurementSeriesDto(
        Quantity.POWER.name,
        Quantity.POWER.unit,
        "average",
        singletonList(
          new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            4.0
          )
        )
      ),
      new MeasurementSeriesDto(
        Quantity.DIFFERENCE_TEMPERATURE.name,
        Quantity.DIFFERENCE_TEMPERATURE.unit,
        "average",
        singletonList(
          new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            40.0
          )
        )
      )
    );
  }

  @Test
  public void averageOfOneMeterOneHour() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:02Z"), "Power", 4.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:03Z"), "Power", 6.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T05:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name,
          Quantity.POWER.unit,
          "average",
          singletonList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T05:00:00Z"),
              4.0
            )
          )
        )));
  }

  @Test
  public void timeZoneInformationIsConsidered() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T03:00:01-02:00"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T04:00:01-01:00"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 4.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T06:00:01+01:00"), "Power", 8.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T05:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);
    ResponseEntity<List<MeasurementSeriesDto>> responseForNonZuluRequest =
      as(context().user).getList(
        String.format(
          "/measurements/average"
            + "?after=2018-03-06T03:00:00.000-02:00"
            + "&before=2018-03-06T06:59:59.999+01:00"
            + "&quantities=" + Quantity.POWER.name
            + "&meters=%s&resolution=hour",
          logicalMeter.id.toString()
        ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name,
          Quantity.POWER.unit,
          "average",
          singletonList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T05:00:00Z"),
              3.75
            )
          )
        )
      ));
    assertThat(response.getBody()).isEqualTo(responseForNonZuluRequest.getBody());

  }

  @Test
  public void averageForNoPhysicalMeters() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );

    ResponseEntity<ErrorMessageDto> response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T05:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void averageForUndefinedQuantity() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<ErrorMessageDto> response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T05:59:59.999Z"
          + "&quantities=SomeUnknownQuantity"
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void averageWithNoMatchingMeasurements() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    newPhysicalMeterEntity(logicalMeter.id);

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T05:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name, "W",
          "average",
          singletonList(new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            null
          ))
        )));
  }

  @Test
  public void allowsOverridingDefinitionsPresentationUnit() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 40000.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-06T05:59:59.999Z"
          + "&quantities=" + Quantity.POWER.name + ":kW"
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name, "kW", "average",
          singletonList(new MeasurementValueDto(
            Instant.parse("2018-03-06T05:00:00Z"),
            40.0
          ))
        )));
  }

  @Test
  public void averageWithDayResolution() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-03-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T05:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T05:00:01Z"), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-03-06T05:00:00.000Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=day",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name, "W", "average",
          asList(
            new MeasurementValueDto(
              Instant.parse("2018-03-06T00:00:00Z"),
              1.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-03-07T00:00:00Z"),
              3.0
            )
          )
        )
      ));
  }

  @Test
  public void averageWithMonthResolution() {
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, ZonedDateTime.parse("2018-01-06T05:00:01Z"), "Power", 1.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-02-07T05:00:01Z"), "Power", 2.0, "W");
    newMeasurement(meter, ZonedDateTime.parse("2018-03-07T05:00:01Z"), "Power", 4.0, "W");

    ResponseEntity<List<MeasurementSeriesDto>> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=2018-01-01T05:00:00.000Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=month",
        logicalMeter.id.toString()
      ), MeasurementSeriesDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(
      singletonList(
        new MeasurementSeriesDto(
          Quantity.POWER.name, "W", "average",
          asList(
            new MeasurementValueDto(
              Instant.parse("2018-01-01T00:00:00Z"),
              1.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-02-01T00:00:00Z"),
              2.0
            ),
            new MeasurementValueDto(
              Instant.parse("2018-03-01T00:00:00Z"),
              4.0
            )
          )
        )
      ));
  }

  @Test
  public void invalidAverageParameterValuesReturnsHttp400() {

    ResponseEntity<ErrorMessageDto> response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?after=thisIsNotAValidTimestamp"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=month",
        UUID.randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'after' timestamp: 'thisIsNotAValidTimestamp'.");

    response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=thisIsNotAValidTimestamp"
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=month",
        UUID.randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'before' timestamp: 'thisIsNotAValidTimestamp'.");

    response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=month",
        "NotAValidUUID"
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo("Invalid 'meters' list: 'NotAValidUUID'.");

    response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=NotAValidResolution",
        UUID.randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Invalid 'resolution' parameter: 'NotAValidResolution'.");
  }

  @Test
  public void missingParametersReturnsHttp400() {
    ResponseEntity<ErrorMessageDto> response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?to=2018-03-07T12:32:05.999Z"
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=hour",
        UUID.randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Missing 'after' parameter.");

    response = as(context().user).get(
      String.format(
        "/measurements/average"
          + "?after=2018-03-07T12:32:05.999Z"
          + "&before=2018-03-07T12:32:05.999Z"
          + "&meters=%s"
          + "&resolution=hour",
        UUID.randomUUID().toString()
      ), ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody().message).isEqualTo(
      "Missing 'quantities' parameter.");
  }

  @Test
  public void toTimestampDefaultsToNow() {
    ZonedDateTime now = ZonedDateTime.now();
    LogicalMeterEntity logicalMeter = newLogicalMeterEntity(
      meterDefinitionMapper.toEntity(MeterDefinition.DISTRICT_HEATING_METER)
    );
    PhysicalMeterEntity meter = newPhysicalMeterEntity(logicalMeter.id);
    newMeasurement(meter, now, "Power", 1.0, "W");

    List<MeasurementSeriesDto> response = as(context().user).getList(
      String.format(
        "/measurements/average"
          + "?after=" + now.toString()
          + "&quantities=" + Quantity.POWER.name + ":W"
          + "&meters=%s"
          + "&resolution=hour",
        logicalMeter.getId()
      ), MeasurementSeriesDto.class).getBody();

    assertThat(response.get(0)).isEqualTo(
      new MeasurementSeriesDto(
        Quantity.POWER.name,
        "W",
        "average",
        singletonList(new MeasurementValueDto(now.truncatedTo(ChronoUnit.HOURS).toInstant(), 1.0))
      )
    );
  }

  private void newMeasurement(
    PhysicalMeterEntity meter,
    ZonedDateTime when,
    String quantity,
    double value,
    String unit
  ) {
    measurementJpaRepository.save(new MeasurementEntity(
      when,
      quantity,
      value,
      unit,
      meter
    ));

  }

  private LogicalMeterEntity newLogicalMeterEntity(MeterDefinitionEntity meterDefinitionEntity) {
    UUID uuid = UUID.randomUUID();
    return logicalMeterJpaRepository.save(new LogicalMeterEntity(
      UUID.randomUUID(),
      uuid.toString(),
      context().organisationEntity.id,
      ZonedDateTime.now(),
      meterDefinitionEntity
    ));
  }

  private PhysicalMeterEntity newPhysicalMeterEntity(UUID logicalMeterId) {
    UUID uuid = UUID.randomUUID();
    return physicalMeterJpaRepository.save(new PhysicalMeterEntity(
      uuid,
      context().organisationEntity,
      "",
      uuid.toString(),
      "",
      "",
      logicalMeterId,
      0
    ));
  }

}
