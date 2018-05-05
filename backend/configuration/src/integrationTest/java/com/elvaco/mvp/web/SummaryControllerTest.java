package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.MeterSummaryDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SummaryControllerTest extends IntegrationTest {

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private Organisations organisations;

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
  }

  @Test
  public void whenNoMetersGetEmptySummaryInfo() {
    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto());
  }

  @Test
  public void getSummaryOfMetersShouldHaveOneResult() {
    logicalMeters.save(newMeter());

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(1, 1, 1));
  }

  @Test
  public void metersWithSameAddress() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeter());

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 1));
  }

  @Test
  public void metersWithDifferentAddresses() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 1")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 2, 2));
  }

  @Test
  public void metersWithSameCityDifferentStreetAddress() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(3, 2, 3));
  }

  @Test
  public void metersWithNoLocation() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeterWithLocation(null));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(3, 2, 2));
  }

  @Test
  public void userCanOnlyGetSummaryForMetersWithinTheOrganisation() {
    logicalMeters.save(newMeter());
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asOtherUser()
      .get("/summary/meters", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(0, 0, 0));
  }

  @Test
  public void fetchOnlyMetersInKungsbacka() {
    logicalMeters.save(newMeterWith(sweden()
                                      .city("stockholm")
                                      .address("kungsgatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 1")));
    logicalMeters.save(newMeterWith(sweden()
                                      .city("kungsbacka")
                                      .address("drottninggatan 2")));

    ResponseEntity<MeterSummaryDto> response = asSuperAdmin()
      .get("/summary/meters?city=sweden,kungsbacka", MeterSummaryDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new MeterSummaryDto(2, 1, 2));
  }

  private LogicalMeter newMeterWith(LocationBuilder locationBuilder) {
    return newMeterWithLocation(locationBuilder.build());
  }

  private LogicalMeter newMeterWithLocation(@Nullable Location location) {
    UUID meterId = randomUUID();
    return new LogicalMeter(
      meterId,
      "externalId-" + meterId,
      context().getOrganisationId(),
      location,
      ZonedDateTime.now()
    );
  }

  private LogicalMeter newMeter() {
    return newMeterWith(sweden()
                          .city("stockholm")
                          .address("kungsgatan 1"));
  }

  private static LocationBuilder sweden() {
    return new LocationBuilder().country("sweden");
  }
}
