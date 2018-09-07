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

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterDtoMapperTest {

  @Test
  public void mapLogicalMeterToMapMarkerDto() {
    UUID logicalMeterId = randomUUID();

    PhysicalMeter physicalMeter = PhysicalMeter.builder()
      .organisation(ELVACO)
      .logicalMeterId(logicalMeterId)
      .status(StatusLogEntry.<UUID>builder()
        .status(StatusType.OK)
        .start(ZonedDateTime.now())
        .build())
      .build();

    LogicalMeter logicalMeter = new LogicalMeter(
      logicalMeterId,
      "some-external-id",
      ELVACO.id,
      null,
      ZonedDateTime.now(),
      singletonList(physicalMeter),
      emptyList(),
      new LocationBuilder()
        .latitude(3.1)
        .longitude(2.1)
        .build()
    );

    assertThat(LogicalMeterDtoMapper.toMapMarkerDto(logicalMeter))
      .isEqualTo(new MapMarkerWithStatusDto(
        logicalMeterId,
        StatusType.OK.name,
        3.1,
        2.1
      ));
  }

  @Test
  public void toDto() {
    UUID meterId = randomUUID();
    LogicalMeterDto expected = new LogicalMeterDto();
    expected.id = meterId;
    expected.created = "2018-02-12T14:14:25Z";
    expected.statusChanged = "2018-02-12T14:14:25Z";
    expected.medium = "Hot water";
    expected.status = StatusType.OK;
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
        "2018-02-12T14:14:25Z",
        ""
      )
    );
    ZonedDateTime statusChanged = ZonedDateTime.parse("2018-02-12T14:14:25Z");
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
    List<PhysicalMeter> physicalMeters = singletonList(
      PhysicalMeter.builder()
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
    );
    assertThat(
      LogicalMeterDtoMapper.toDto(
        new LogicalMeter(
          meterId,
          "an-external-id",
          organisationId,
          MeterDefinition.HOT_WATER_METER,
          statusChanged,
          physicalMeters,
          gateways,
          emptyList(),
          new LocationBuilder()
            .city("kungsbacka")
            .address("kabelgatan 2t")
            .latitude(57.5052592)
            .longitude(56.123)
            .confidence(1.0)
            .build(),
          100L,
          25L,
          null
        )))
      .isEqualTo(expected);
  }

  @Test
  public void nullCollectionStatusIsMappedToNull() {
    LogicalMeterDto logicalMeterDto = LogicalMeterDtoMapper.toDto(new LogicalMeter(
      randomUUID(),
      "external-id",
      ELVACO.id,
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.parse("2018-02-12T14:14:25Z"),
      emptyList(),
      emptyList(),
      emptyList(),
      UNKNOWN_LOCATION,
      null,
      null,
      null
    ));

    assertThat(logicalMeterDto.collectionPercentage).isNull();
  }

  @Test
  public void dtoCreatedTimeReflectsCallerTimeZone() {
    LogicalMeterDto logicalMeterDto = LogicalMeterDtoMapper.toDto(new LogicalMeter(
      randomUUID(),
      "external-id",
      ELVACO.id,
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.parse("2018-02-12T14:14:25Z"),
      emptyList(),
      emptyList(),
      emptyList(),
      UNKNOWN_LOCATION,
      null,
      null,
      null
    ));

    assertThat(logicalMeterDto.created).isEqualTo("2018-02-12T14:14:25Z");
  }
}
