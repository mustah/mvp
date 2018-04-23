package com.elvaco.mvp.web.mapper;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
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
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.core.util.Dates.formatUtc;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterMapperTest {

  private LogicalMeterMapper mapper;

  @Before
  public void setUp() {
    mapper = new LogicalMeterMapper(
      new MeterStatusLogMapper(),
      new GatewayMapper(),
      new MeasurementMapper()
    );
  }

  @Test
  public void mapLogicalMeterToMapMarkerDto() {
    UUID meterId = randomUUID();
    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = meterId;
    mapMarkerDtoExpected.latitude = 3.1;
    mapMarkerDtoExpected.longitude = 2.1;
    mapMarkerDtoExpected.confidence = 1.0;
    mapMarkerDtoExpected.status = StatusType.OK.name;

    Location location = new LocationBuilder()
      .coordinate(new GeoCoordinate(3.1, 2.1))
      .build();

    PhysicalMeter physicalMeter = new PhysicalMeter(
      null,
      ELVACO,
      "",
      "",
      "",
      "",
      meterId,
      0,
      null,
      singletonList(
        new StatusLogEntry<>(
          null,
          randomUUID(),
          StatusType.OK,
          ZonedDateTime.now(),
          ZonedDateTime.now()
        )
      )
    );

    LogicalMeter logicalMeter = new LogicalMeter(
      meterId,
      "some-external-id",
      ELVACO.id,
      location,
      ZonedDateTime.now(),
      singletonList(physicalMeter),
      null,
      emptyList()
    );

    MapMarkerDto mapMarkerDto = mapper.toMapMarkerDto(logicalMeter);

    assertThat(mapMarkerDto).isEqualTo(mapMarkerDtoExpected);
  }

  @Test
  public void toDto() {
    UUID meterId = randomUUID();
    LogicalMeterDto expected = new LogicalMeterDto();
    expected.id = meterId;
    expected.created = "2018-02-12 14:14:25";
    expected.statusChanged = "2018-02-12 14:14:25";
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
        1L,
        StatusType.OK.name,
        "2018-02-12 14:14:25",
        "2018-02-13 14:14:25"
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
    expected.collectionStatus = "";
    expected.readIntervalMinutes = 15L;

    expected.measurements = emptyList();

    UUID organisationId = ELVACO.id;

    assertThat(
      mapper.toDto(
        new LogicalMeter(
          meterId,
          "an-external-id",
          organisationId,
          new LocationBuilder()
            .city("kungsbacka")
            .address("kabelgatan 2t")
            .latitude(57.5052592)
            .longitude(56.123)
            .confidence(1.0)
            .build(),
          statusChanged,
          singletonList(
            new PhysicalMeter(
              randomUUID(),
              ELVACO,
              "123123",
              "an-external-id",
              "Some device specific medium",
              "ELV",
              meterId,
              15,
              null,
              singletonList(
                new StatusLogEntry<>(
                  1L,
                  randomUUID(),
                  StatusType.OK,
                  statusChanged,
                  statusChanged.plusDays(1)
                )
              )
            )),
          MeterDefinition.HOT_WATER_METER,
          singletonList(new Gateway(
            expected.gateway.id,
            organisationId,
            expected.gateway.serial,
            expected.gateway.productModel,
            emptyList(),
            singletonList(
              new StatusLogEntry<>(
                1L,
                randomUUID(),
                StatusType.OK,
                statusChanged,
                statusChanged.plusDays(1)
              )
            )
          ))
        )))
      .isEqualTo(expected);
  }

  @Test
  public void dtoCreatedTimeReflectsCallerTimeZone() {
    LogicalMeterDto logicalMeterDto = mapper.toDto(new LogicalMeter(
      randomUUID(),
      "external-id",
      ELVACO.id,
      UNKNOWN_LOCATION,
      ZonedDateTime.parse("2018-02-12T14:14:25Z")
    ));

    assertThat(logicalMeterDto.created).isEqualTo("2018-02-12 14:14:25");
  }
}
