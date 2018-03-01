package com.elvaco.mvp.web.mapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.web.dto.IdNamedDto.OK;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterMapperTest {

  private LogicalMeterMapper mapper;

  @Before
  public void setUp() {
    ModelMapper modelMapper = new ModelMapper();
    modelMapper
      .getConfiguration()
      .setFieldMatchingEnabled(true)
      .setFieldAccessLevel(AccessLevel.PUBLIC);

    mapper = new LogicalMeterMapper(new MeterStatusLogMapper());
  }

  @Test
  public void mapLogicalMeterToMapMarkerDto() {
    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = 1L;
    mapMarkerDtoExpected.latitude = 3.1;
    mapMarkerDtoExpected.longitude = 2.1;
    mapMarkerDtoExpected.confidence = 1.0;
    mapMarkerDtoExpected.status = OK;
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    Location location = new LocationBuilder()
      .coordinate(new GeoCoordinate(3.1, 2.1, 1.0))
      .build();

    LogicalMeter logicalMeter = new LogicalMeter(
      1L,
      "some-external-id",
      ELVACO.id,
      location,
      new Date(),
      emptyList(),
      null,
      emptyList(),
      emptyList()
    );

    MapMarkerDto mapMarkerDto = mapper.toMapMarkerDto(logicalMeter);

    assertThat(mapMarkerDto).isEqualTo(mapMarkerDtoExpected);
  }

  @Test
  public void toDto() throws ParseException {
    LogicalMeterDto expected = new LogicalMeterDto();
    expected.created = "2018-02-12 15:14:25";
    expected.statusChanged = "2018-02-12 15:14:25";
    expected.medium = "Hot water meter";
    expected.id = 1L;
    expected.status = OK;
    expected.address = new IdNamedDto("Kabelgatan 2T");
    expected.city = new IdNamedDto("Kungsbacka");
    expected.manufacturer = "ELV";
    expected.flags = emptyList();
    expected.position = new GeoPositionDto(57.5052592, 56.123, 1);
    expected.facility = "an-external-id";
    expected.statusChangelog = emptyList();
    expected.gatewayId = 3L;
    expected.gatewaySerial = "123123";
    expected.gatewayStatus = OK;
    expected.gatewayProductModel = "CMi2110";

    UUID organisationId = ELVACO.id;

    assertThat(
      mapper.toDto(
        new LogicalMeter(
          1L,
          "an-external-id",
          organisationId,
          new LocationBuilder()
            .city("Kungsbacka")
            .streetAddress("Kabelgatan 2T")
            .latitude(57.5052592)
            .longitude(56.123)
            .confidence(1.0)
            .build(),
          dateFormat().parse("2018-02-12T14:14:25"),
          singletonList(
            new PhysicalMeter(
              ELVACO,
              "123123",
              "an-external-id",
              "Some device specific medium",
              "ELV"
            )),
          MeterDefinition.HOT_WATER_METER,
          emptyList(),
          singletonList(new Gateway(
            expected.gatewayId,
            organisationId,
            expected.gatewaySerial,
            expected.gatewayProductModel,
            emptyList()
          ))
        ), TimeZone.getTimeZone("Europe/Stockholm")))
      .isEqualTo(expected);
  }

  @Test
  public void dtoCreatedTimeReflectsCallerTimeZone() throws ParseException {
    LogicalMeter logicalMeter = new LogicalMeter(
      0L, "external-id",
      ELVACO.id, Location.UNKNOWN_LOCATION,
      dateFormat().parse("2018-02-12T14:14:25")
    );

    assertThat(mapper.toDto(logicalMeter, TimeZone.getTimeZone("UTC")).created)
      .isEqualTo("2018-02-12 14:14:25");
    assertThat(mapper.toDto(logicalMeter, TimeZone.getTimeZone("America/Los_Angeles")).created)
      .isEqualTo("2018-02-12 06:14:25");
  }

  private static DateFormat dateFormat() {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    return dateFormat;
  }
}
