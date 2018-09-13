package com.elvaco.mvp.web.mapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.web.dto.GatewayMandatoryDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper.toDto;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterDtoMapperTest {

  private static final String CREATED_DATE_STRING = "2018-02-12T14:14:25Z";

  @Test
  public void mapLogicalMeterToMapMarkerDto() {
    UUID logicalMeterId = randomUUID();

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .organisation(ELVACO)
      .status(StatusLogEntry.<UUID>builder()
        .start(ZonedDateTime.now())
        .status(StatusType.OK)
        .build())
      .logicalMeterId(logicalMeterId)
      .build();

    LogicalMeter logicalMeter = logicalMeter()
      .id(logicalMeterId)
      .physicalMeter(physicalMeter)
      .location(new LocationBuilder()
        .latitude(3.1)
        .longitude(2.1)
        .build())
      .build();

    assertThat(LogicalMeterDtoMapper.toMapMarkerDto(logicalMeter))
      .isEqualTo(new MapMarkerWithStatusDto(
        logicalMeterId,
        StatusType.OK.name,
        3.1,
        2.1
      ));
  }

  @Test
  public void domainModelToDto() {
    UUID meterId = randomUUID();
    LogicalMeterDto expected = new LogicalMeterDto();
    expected.id = meterId;
    expected.created = CREATED_DATE_STRING;
    expected.statusChanged = CREATED_DATE_STRING;
    expected.medium = "Hot water";
    expected.isReported = false;
    expected.location = new LocationDto(
      new IdNamedDto("kungsbacka"),
      new IdNamedDto("kabelgatan 2t"),
      new GeoPositionDto(57.5052592, 56.123, 1.0)
    );
    expected.manufacturer = "ELV";
    expected.flags = emptyList();
    expected.facility = "an-external-id";
    expected.address = "123123";
    expected.statusChangelog = singletonList(
      new MeterStatusLogDto(
        null,
        StatusType.OK.name,
        CREATED_DATE_STRING,
        ""
      )
    );
    ZonedDateTime statusChanged = ZonedDateTime.parse(CREATED_DATE_STRING);
    expected.gateway = new GatewayMandatoryDto(
      randomUUID(),
      "CMi2110",
      "123123",
      StatusType.OK.name,
      formatUtc(statusChanged)
    );
    expected.collectionPercentage = 75.0;
    expected.readIntervalMinutes = 15L;

    expected.measurements = emptyList();
    expected.organisationId = ELVACO.id;

    UUID organisationId = ELVACO.id;

    List<Gateway> gateways = singletonList(Gateway.builder()
      .id(expected.gateway.id)
      .organisationId(organisationId)
      .serial(expected.gateway.serial)
      .productModel(expected.gateway.productModel)
      .statusLogs(singletonList(
        new StatusLogEntry<>(
          1L,
          randomUUID(),
          StatusType.OK,
          statusChanged,
          statusChanged.plusDays(1)
        )))
      .build());

    assertThat(
      toDto(
        LogicalMeter.builder()
          .id(meterId)
          .externalId("an-external-id")
          .organisationId(organisationId)
          .meterDefinition(MeterDefinition.HOT_WATER_METER)
          .created(statusChanged)
          .physicalMeter(PhysicalMeter.builder()
            .organisation(ELVACO)
            .address("123123")
            .externalId("an-external-id")
            .medium("Gas")
            .manufacturer("ELV")
            .logicalMeterId(meterId)
            .readIntervalMinutes(15)
            .status(StatusLogEntry.<UUID>builder()
              .status(StatusType.OK)
              .start(statusChanged)
              .build())
            .build()
          )
          .gateways(gateways)
          .location(new LocationBuilder()
            .city("kungsbacka")
            .address("kabelgatan 2t")
            .latitude(57.5052592)
            .longitude(56.123)
            .confidence(1.0)
            .build())
          .expectedMeasurementCount(100L)
          .missingMeasurementCount(25L)
          .build()
      ))
      .isEqualTo(expected);
  }

  @Test
  public void meterIsNotReported() {
    LogicalMeter logicalMeter = logicalMeter()
      .physicalMeter(PhysicalMeter.builder().organisation(ELVACO)
        .status(StatusLogEntry.<UUID>builder()
          .start(ZonedDateTime.now())
          .status(StatusType.OK)
          .build())
        .build())
      .build();

    LogicalMeterDto logicalMeterDto = toDto(logicalMeter);

    assertThat(logicalMeterDto.isReported).isFalse();
  }

  @Test
  public void meterIsReported() {
    LogicalMeter logicalMeter = logicalMeter()
      .physicalMeter(PhysicalMeter.builder().organisation(ELVACO)
        .status(StatusLogEntry.<UUID>builder()
          .start(ZonedDateTime.now())
          .status(StatusType.ERROR)
          .build())
        .build())
      .build();

    LogicalMeterDto logicalMeterDto = toDto(logicalMeter);

    assertThat(logicalMeterDto.isReported).isTrue();
  }

  @Test
  public void nullCollectionStatusIsMappedToNull() {
    LogicalMeter logicalMeter = logicalMeter().build();

    LogicalMeterDto logicalMeterDto = toDto(logicalMeter);

    assertThat(logicalMeterDto.collectionPercentage).isNull();
  }

  @Test
  public void dtoCreatedTimeReflectsCallerTimeZone() {
    LogicalMeter logicalMeter = logicalMeter().build();

    LogicalMeterDto logicalMeterDto = toDto(logicalMeter);

    assertThat(logicalMeterDto.created).isEqualTo(CREATED_DATE_STRING);
  }

  private static LogicalMeter.LogicalMeterBuilder logicalMeter() {
    return LogicalMeter.builder()
      .externalId("external-id")
      .organisationId(ELVACO.id)
      .created(ZonedDateTime.parse(CREATED_DATE_STRING));
  }
}
