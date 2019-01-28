package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.database.entity.meter.EntityPk;
import com.elvaco.mvp.database.repository.jpa.LocationJpaRepository;
import com.elvaco.mvp.database.repository.mappers.LocationEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseDto;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseErrorDto;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static com.elvaco.mvp.web.mapper.LocationDtoMapper.toLocationWithId;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class GeocodeControllerTest extends IntegrationTest {

  @Autowired
  private LocationJpaRepository locationJpaRepository;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void saveLocationForLogicalMeter() {
    var meter = given(logicalMeter());

    var geoResponse = new GeoResponseDto(
      new AddressDto(
        "sweden",
        "kungsbacka",
        "kabelgatan 1",
        "43437"
      ),
      new GeoPositionDto(11.23332, 12.12323, 1.0)
    );

    var response = restClient()
      .post("/geocodes/callback/" + meter.id, geoResponse, GeoResponseDto.class);

    var pk = new EntityPk(meter.id, meter.organisationId);

    var expected = toLocationWithId(geoResponse, meter);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(findLocationBy(pk)).isEqualTo(expected);
  }

  @Test
  public void saveLocationAsLowercaseStringForLogicalMeter() {
    var meter = given(logicalMeter().location(new LocationBuilder().country("Sweden")
      .city("Växjö").address("Drottninggatan 3").zip("").build()));

    var geoResponse = new GeoResponseDto(
      new AddressDto(
        "Sweden",
        "Växjö",
        "Drottninggatan 3",
        ""
      ),
      new GeoPositionDto(11.23332, 12.12323, 1.0)
    );

    var response = restClient()
      .post("/geocodes/callback/" + meter.id, geoResponse, GeoResponseDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var pk = new EntityPk(meter.id, meter.organisationId);

    LocationWithId expected = new LocationBuilder()
      .id(pk.getId())
      .organisationId(pk.getOrganisationId())
      .coordinate(new GeoCoordinate(11.23332, 12.12323, 1.0))
      .country("sweden")
      .city("växjö")
      .address("drottninggatan 3")
      .zip("")
      .buildLocationWithId();
    var actual = findLocationBy(pk);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void doesNotSaveLocationWithNoCountry() {
    var meter = given(logicalMeter());

    var geoResponse = new GeoResponseDto(
      new AddressDto(
        "  ",
        " ",
        "Drottninggatan 3",
        ""
      ),
      new GeoPositionDto(11.23332, 12.12323, 1.0)
    );

    var response = restClient()
      .post("/geocodes/callback/" + meter.id, geoResponse, GeoResponseDto.class);

    var pk = new EntityPk(meter.id, meter.organisationId);

    LocationWithId actual = findLocationBy(pk);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(actual.getCountry()).isNull();
    assertThat(actual.getCity()).isNull();
  }

  @Test
  public void justLogAndReturnsStatusOk() {
    var logicalMeterId = randomUUID();
    var payload = new GeoResponseErrorDto(
      1,
      "No geolocation found",
      new AddressDto(
        "sweden",
        "kungsbacka",
        "kabelgatan 1",
        "43437"
      )
    );
    var response = restClient()
      .post("/geocodes/error/" + logicalMeterId, payload, GeoResponseErrorDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private LocationWithId findLocationBy(EntityPk id) {
    return locationJpaRepository.findById(id)
      .map(LocationEntityMapper::toLocationWithId)
      .get();
  }
}
