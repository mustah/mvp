package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.MapMarkerDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MapMarkerControllerTest extends IntegrationTest {

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private LogicalMeters logicalMeters;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void locationForMeterNotFound() {
    UUID logicalMeterId = randomUUID();

    ResponseEntity<ErrorMessageDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message)
      .isEqualTo("Unable to find meter with ID '" + logicalMeterId + "'");
  }

  @Test
  public void findMapMarkerWithNoLocation() {
    UUID logicalMeterId = randomUUID();

    logicalMeters.save(new LogicalMeter(
      logicalMeterId,
      randomUUID().toString(),
      context().getOrganisationId(),
      UNKNOWN_LOCATION,
      ZonedDateTime.now()
    ));

    ResponseEntity<MapMarkerDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MapMarkerDto(
      logicalMeterId,
      "unknown",
      null,
      null,
      null
    ));
  }

  @Test
  public void findLogicalMeterWithLocation() {
    UUID logicalMeterId = randomUUID();

    logicalMeters.save(new LogicalMeter(
      logicalMeterId,
      randomUUID().toString(),
      context().getOrganisationId(),
      newLocation(),
      ZonedDateTime.now()
    ));

    ResponseEntity<MapMarkerDto> response = asSuperAdmin()
      .get("/map-markers/meters/" + logicalMeterId, MapMarkerDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MapMarkerDto(
      logicalMeterId,
      "unknown",
      2.1222,
      1.2212,
      1.0
    ));
  }

  @Test
  public void userShouldNotBeAbleToFindLocationForAMeterFromAnotherOrganisation() {
    UUID logicalMeterId = randomUUID();

    logicalMeters.save(new LogicalMeter(
      logicalMeterId,
      randomUUID().toString(),
      context().getOrganisationId(),
      newLocation(),
      ZonedDateTime.now()
    ));

    ResponseEntity<ErrorMessageDto> response = asOtherUser()
      .get("/map-markers/meters/" + logicalMeterId, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().message)
      .isEqualTo("Unable to find meter with ID '" + logicalMeterId + "'");
  }

  private static Location newLocation() {
    return new LocationBuilder()
      .country("sweden")
      .city("kungsbacka")
      .address("kabelgatan 2t")
      .longitude(1.2212)
      .latitude(2.1222)
      .build();
  }
}
