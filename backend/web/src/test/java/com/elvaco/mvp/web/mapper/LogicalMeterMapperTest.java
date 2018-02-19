package com.elvaco.mvp.web.mapper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.PropertyCollection;
import com.elvaco.mvp.core.dto.MapMarkerType;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LogicalMeterDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import org.junit.Before;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
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
    mapMarkerDtoExpected.status = new IdNamedDto("Ok");
    mapMarkerDtoExpected.mapMarkerType = MapMarkerType.Meter;

    Location location = new LocationBuilder()
      .coordinate(new GeoCoordinate(3.1, 2.1, 1.0))
      .build();

    LogicalMeter logicalMeter = new LogicalMeter(
      1L,
      location,
      new Date(),
      null,
      Collections.emptyList(),
      null,
      Collections.emptyList()
    );

    MapMarkerDto mapMarkerDto = mapper.toMapMarkerDto(logicalMeter);

    assertThat(mapMarkerDto).isEqualTo(mapMarkerDtoExpected);
  }

  @Test
  public void toDto() throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

    LogicalMeter logicalMeter = new LogicalMeter(
      1L,
      new LocationBuilder().city("Kungsbacka")
        .streetAddress("Kabelgatan 2T")
        .latitude(57.5052592)
        .longitude(12.0683196)
        .build(),
      dateFormat.parse("2018-02-12T14:14:25"),
      PropertyCollection.empty(),
      Collections.singletonList(new PhysicalMeter(
        ELVACO, "123123", "Some device specific medium", "ELV"
      )),
      MeterDefinition.HOT_WATER_METER,
      Collections.emptyList()
    );
    LogicalMeterDto actual = mapper.toDto(logicalMeter, TimeZone.getTimeZone("Europe/Stockholm"));
    assertThat(actual.created).isEqualTo("2018-02-12 15:14:25");
    assertThat(actual.medium).isEqualTo("Hot water meter");
    assertThat(actual.id).isEqualTo(1L);
    assertThat(actual.address.name).isEqualTo("Kabelgatan 2T");
    assertThat(actual.city.name).isEqualTo("Kungsbacka");
    assertThat(actual.manufacturer).isEqualTo("ELV");
    assertThat(actual.position.confidence).isEqualTo(1.0);
    assertThat(actual.position.latitude).isEqualTo(57.5052592);
    assertThat(actual.position.longitude).isEqualTo(12.0683196);
  }

  @Test
  public void dtoCreatedTimeReflectsCallerTimeZone() throws ParseException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    LogicalMeter logicalMeter = new LogicalMeter(0L,
                                                 Location.UNKNOWN_LOCATION,
                                                 dateFormat.parse("2018-02-12T14:14:25"),
                                                 PropertyCollection.empty()
    );

    assertThat(mapper.toDto(logicalMeter, TimeZone.getTimeZone("UTC")).created).isEqualTo(
      "2018-02-12 14:14:25");
    assertThat(mapper.toDto(
      logicalMeter,
      TimeZone.getTimeZone("America/Los_Angeles")
    ).created).isEqualTo("2018-02-12 06:14:25");

  }
}
