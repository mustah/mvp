package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.SelectionTreeDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.testing.fixture.UserTestData.DAILY_PLANET;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionTreeControllerTest extends IntegrationTest{

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private LogicalMeters logicalMeters;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void getResponseOk() {
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1"));
    logicalMeters.save(newLogicalMeter("sweden", "gothenburg", "kabelgatan 3", "extId2"));
    logicalMeters.save(newLogicalMeter("finland", "kungsbacka", "joksigatan 2", "extId3"));

    ResponseEntity<SelectionTreeDto> response = as(context().superAdmin)
      .get("/selection-tree", SelectionTreeDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void getFilteredCity() {
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1"));
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2"));
    logicalMeters.save(newLogicalMeter("sweden", "gothenburg", "kabelgatan 3", "extId3"));
    logicalMeters.save(newLogicalMeter("finland", "kungsbacka", "joksigatan 2", "extId3"));

    ResponseEntity<SelectionTreeDto> response = as(context().superAdmin)
      .get("/selection-tree?city=sweden,kungsbacka", SelectionTreeDto.class);

    assertThat(response.getBody().cities).hasSize(1);
    assertThat(response.getBody().cities.get(0).addresses).hasSize(2);
  }

  @Test
  public void getFilteredAddress() {
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1"));
    logicalMeters.save(newLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2"));
    logicalMeters.save(newLogicalMeter("sweden", "gothenburg", "kabelgatan 3", "extId3"));
    logicalMeters.save(newLogicalMeter("finland", "kungsbacka", "joksigatan 2", "extId3"));

    ResponseEntity<SelectionTreeDto> response = as(context().superAdmin)
      .get("/selection-tree?address=sweden,kungsbacka,kabelgatan 2", SelectionTreeDto.class);

    assertThat(response.getBody().cities).hasSize(1);
    assertThat(response.getBody().cities.get(0).addresses).hasSize(1);
  }

  private static LogicalMeter newLogicalMeter(
    String country,
    String city,
    String address,
    UUID id,
    String externalId
  ) {
    Location location = new LocationBuilder()
      .country(country)
      .city(city)
      .streetAddress(address)
      .build();
    return new LogicalMeter(
      id,
      externalId,
      context().getOrganisationId(),
      location,
      ZonedDateTime.now()
    );
  }

  private static LogicalMeter newLogicalMeter(
    String country,
    String city,
    String address,
    String externalId
  ) {
    return newLogicalMeter(
      country,
      city,
      address,
      randomUUID(),
      externalId
    );

  }

}
