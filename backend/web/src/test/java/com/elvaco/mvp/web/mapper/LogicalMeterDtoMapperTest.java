package com.elvaco.mvp.web.mapper;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.web.dto.EventLogDto;
import com.elvaco.mvp.web.dto.EventType;
import com.elvaco.mvp.web.dto.GatewayMandatoryDto;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerWithStatusDto;

import org.junit.Test;

import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static com.elvaco.mvp.web.mapper.LogicalMeterDtoMapper.toDto;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterDtoMapperTest {

  private static final String CREATED_DATE_STRING = "2018-02-12T14:14:25Z";

  @Test
  public void mapLogicalMeterToMapMarkerDto() {
    UUID logicalMeterId = randomUUID();

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .organisationId(ELVACO.id)
      .status(StatusLogEntry.builder()
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
      "sverige",
      "kungsbacka",
      "kabelgatan 2t",
      "43437",
      new GeoPositionDto(57.5052592, 56.123, 1.0)
    );
    expected.manufacturer = "ELV";
    expected.facility = "an-external-id";
    expected.address = "123123";

    expected.eventLog = List.of(
      EventLogDto.builder()
        .type(EventType.newMeter)
        .name("123123")
        .start(CREATED_DATE_STRING)
        .build(),
      EventLogDto.builder()
        .type(EventType.statusChange)
        .name(StatusType.OK.name)
        .start(CREATED_DATE_STRING)
        .build()
    );

    expected.alarms = List.of();

    ZonedDateTime statusChanged = ZonedDateTime.parse(CREATED_DATE_STRING);
    expected.gateway = GatewayMandatoryDto.builder()
      .id(randomUUID())
      .productModel("CMi2110")
      .serial("123123")
      .status(new IdNamedDto(StatusType.OK.name))
      .statusChanged(formatUtc(statusChanged))
      .build();
    expected.collectionPercentage = 75.0;
    expected.readIntervalMinutes = 15L;

    expected.organisationId = ELVACO.id;

    UUID organisationId = ELVACO.id;

    assertThat(
      toDto(
        LogicalMeter.builder()
          .id(meterId)
          .externalId("an-external-id")
          .organisationId(organisationId)
          .meterDefinition(MeterDefinition.DEFAULT_HOT_WATER)
          .created(statusChanged)
          .physicalMeter(PhysicalMeter.builder()
            .organisationId(ELVACO.id)
            .address("123123")
            .externalId("an-external-id")
            .medium("Gas")
            .manufacturer("ELV")
            .logicalMeterId(meterId)
            .readIntervalMinutes(15)
            .activePeriod(PeriodRange.from(statusChanged))
            .status(StatusLogEntry.builder()
              .status(StatusType.OK)
              .start(statusChanged)
              .build())
            .build()
          )
          .gateway(Gateway.builder()
            .id(expected.gateway.id)
            .organisationId(organisationId)
            .serial(expected.gateway.serial)
            .productModel(expected.gateway.productModel)
            .statusLog(StatusLogEntry.builder()
              .id(1L)
              .status(StatusType.OK)
              .start(statusChanged)
              .stop(statusChanged.plusDays(1))
              .build())
            .build())
          .location(new LocationBuilder()
            .city("kungsbacka")
            .address("kabelgatan 2t")
            .country("sverige")
            .zip("43437")
            .latitude(57.5052592)
            .longitude(56.123)
            .confidence(1.0)
            .build())
          .collectionPercentage(75.0)
          .build()
      ))
      .isEqualTo(expected);
  }

  @Test
  public void meterIsNotReported() {
    LogicalMeter logicalMeter = logicalMeter()
      .physicalMeter(PhysicalMeter.builder().organisationId(ELVACO.id)
        .status(StatusLogEntry.builder()
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
      .physicalMeter(PhysicalMeter.builder().organisationId(ELVACO.id)
        .status(StatusLogEntry.builder()
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
