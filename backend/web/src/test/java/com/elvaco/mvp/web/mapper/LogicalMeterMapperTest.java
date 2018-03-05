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
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import com.elvaco.mvp.web.dto.MeterStatusLogDto;
import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static com.elvaco.mvp.web.dto.IdNamedDto.OK;
import static com.elvaco.mvp.web.util.IdHelper.uuidOf;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class LogicalMeterMapperTest {

  private LogicalMeterMapper mapper;

  @Before
  public void setUp() {
    mapper = new LogicalMeterMapper(new MeterStatusLogMapper());
  }

  @Test
  public void mapLogicalMeterToMapMarkerDto() {
    UUID meterId = randomUUID();
    MapMarkerDto mapMarkerDtoExpected = new MapMarkerDto();
    mapMarkerDtoExpected.id = meterId.toString();
    mapMarkerDtoExpected.latitude = 3.1;
    mapMarkerDtoExpected.longitude = 2.1;
    mapMarkerDtoExpected.confidence = 1.0;
    mapMarkerDtoExpected.status = OK;
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    Location location = new LocationBuilder()
      .coordinate(new GeoCoordinate(3.1, 2.1, 1.0))
      .build();

    LogicalMeter logicalMeter = new LogicalMeter(
      meterId,
      "some-external-id",
      ELVACO.id,
      location,
      new Date(),
      emptyList(),
      null,
      singletonList(
        new MeterStatusLog(null, randomUUID(), 1, "Ok", new Date(), new Date())
      ),
      emptyList()
    );

    MapMarkerDto mapMarkerDto = mapper.toMapMarkerDto(logicalMeter);

    assertThat(mapMarkerDto).isEqualTo(mapMarkerDtoExpected);
  }

  @Test
  public void toDto() throws ParseException {
    UUID meterId = randomUUID();
    LogicalMeterDto expected = new LogicalMeterDto();
    expected.id = meterId.toString();
    expected.created = "2018-02-12 15:14:25";
    expected.statusChanged = "2018-02-12 15:14:25";
    expected.medium = "Hot water meter";
    expected.status = OK;
    expected.address = new IdNamedDto("Kabelgatan 2T");
    expected.city = new IdNamedDto("Kungsbacka");
    expected.manufacturer = "ELV";
    expected.flags = emptyList();
    expected.position = new GeoPositionDto(57.5052592, 56.123, 1);
    expected.facility = "an-external-id";
    expected.statusChangelog = singletonList(
      new MeterStatusLogDto(
        1L,
        "Ok",
        "Ok",
        "2018-02-12 15:14:25",
        "2018-02-13 15:14:25"
      )
    );
    expected.gatewayId = randomUUID().toString();
    expected.gatewaySerial = "123123";
    expected.gatewayStatus = OK;
    expected.gatewayProductModel = "CMi2110";

    UUID organisationId = ELVACO.id;

    assertThat(
      mapper.toDto(
        new LogicalMeter(
          meterId,
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
              randomUUID(),
              "123123",
              "an-external-id",
              "Some device specific medium",
              "ELV",
              ELVACO
            )),
          MeterDefinition.HOT_WATER_METER,
          singletonList(
            new MeterStatusLog(
              1L,
              randomUUID(),
              2,
              "Ok",
              dateFormat().parse("2018-02-12T14:14:25"),
              dateFormat().parse("2018-02-13T14:14:25")
            )
          ),
          singletonList(new Gateway(
            uuidOf(expected.gatewayId),
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
      randomUUID(),
      "external-id",
      ELVACO.id,
      Location.UNKNOWN_LOCATION,
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
