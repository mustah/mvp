package com.elvaco.mvp.web;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.GeoCoordinate;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
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
    UUID logicalMeterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId("test-123")
      .organisationId(context().organisationId())
      .utcOffset(DEFAULT_UTC_OFFSET)
      .build());

    var geoResponse = new GeoResponseDto(
      new AddressDto(
        "sweden",
        "kungsbacka",
        "kabelgatan 1"
      ),
      new GeoPositionDto(11.23332, 12.12323, 1.0)
    );

    var response = asSuperAdmin()
      .post("/geocodes/callback/" + logicalMeterId, geoResponse, GeoResponseDto.class);

    var pk = new EntityPk(logicalMeterId, context().organisationId());

    var expected = toLocationWithId(geoResponse, pk);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(findLocationBy(pk)).isEqualTo(expected);
  }

  @Test
  public void saveLocationAsLowercaseStringForLogicalMeter() {
    UUID logicalMeterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId("test-123")
      .organisationId(context().organisationId())
      .utcOffset(DEFAULT_UTC_OFFSET)
      .build());

    var geoResponse = new GeoResponseDto(
      new AddressDto(
        "Sweden",
        "Växjö",
        "Drottninggatan 3"
      ),
      new GeoPositionDto(11.23332, 12.12323, 1.0)
    );

    var response = asSuperAdmin()
      .post("/geocodes/callback/" + logicalMeterId, geoResponse, GeoResponseDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    var pk = new EntityPk(logicalMeterId, context().organisationId());

    LocationWithId expected = new LocationBuilder()
      .id(pk.getId())
      .organisationId(pk.getOrganisationId())
      .coordinate(new GeoCoordinate(11.23332, 12.12323, 1.0))
      .country("sweden")
      .city("växjö")
      .address("drottninggatan 3")
      .buildLocationWithId();

    assertThat(findLocationBy(pk)).isEqualTo(expected);
  }

  @Test
  public void doesNotSaveLocationWithNoCountry() {
    UUID logicalMeterId = randomUUID();

    logicalMeters.save(LogicalMeter.builder()
      .id(logicalMeterId)
      .externalId("test-123")
      .organisationId(context().organisationId())
      .utcOffset(DEFAULT_UTC_OFFSET)
      .build());

    var geoResponse = new GeoResponseDto(
      new AddressDto(
        "  ",
        " ",
        "Drottninggatan 3"
      ),
      new GeoPositionDto(11.23332, 12.12323, 1.0)
    );

    var response = asSuperAdmin()
      .post("/geocodes/callback/" + logicalMeterId, geoResponse, GeoResponseDto.class);

    var pk = new EntityPk(logicalMeterId, context().organisationId());

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
        "kabelgatan 1"
      )
    );
    var response = asSuperAdmin()
      .post("/geocodes/error/" + logicalMeterId, payload, GeoResponseErrorDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private LocationWithId findLocationBy(EntityPk id) {
    return locationJpaRepository.findById(id)
      .map(LocationEntityMapper::toLocationWithId)
      .get();
  }
}
