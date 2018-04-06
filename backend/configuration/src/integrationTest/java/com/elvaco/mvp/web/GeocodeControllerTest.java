package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationWithId;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.GeoPositionDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseDto;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseErrorDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static com.elvaco.mvp.web.mapper.LocationMapper.toLocationWithId;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class GeocodeControllerTest extends IntegrationTest {

  @Autowired
  private Locations locations;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void saveLocationForLogicalMeter() {
    UUID logicalMeterId = randomUUID();
    logicalMeters.save(new LogicalMeter(
      logicalMeterId,
      "test-123",
      context().getOrganisationId(),
      UNKNOWN_LOCATION,
      ZonedDateTime.now()
    ));

    GeoResponseDto geoResponse = new GeoResponseDto(
      new AddressDto(
        "sweden",
        "kungsbacka",
        "kabelgatan 1"
      ),
      new GeoPositionDto(11.23332, 12.12323, 1.0)
    );
    ResponseEntity<?> response = restClient()
      .post("/geocodes/callback/" + logicalMeterId, geoResponse, GeoResponseDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    LocationWithId expected = toLocationWithId(geoResponse, logicalMeterId);
    assertThat(locations.findById(logicalMeterId).get()).isEqualTo(expected);
  }

  @Test
  public void justLogAndReturnsStatusOk() {
    UUID logicalMeterId = randomUUID();
    GeoResponseErrorDto payload = new GeoResponseErrorDto(
      1,
      "No geolocation found",
      new AddressDto(
        "sweden",
        "kungsbacka",
        "kabelgatan 1"
      )
    );
    ResponseEntity<?> response = restClient()
      .post("/geocodes/error/" + logicalMeterId, payload, GeoResponseErrorDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}