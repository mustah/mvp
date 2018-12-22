package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.adapters.spring.RequestParametersAdapter;
import com.elvaco.mvp.core.domainmodels.LogicalMeterCollectionStats;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.After;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static org.assertj.core.api.Assertions.assertThat;

public class MissingMeasurementControllerTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
    missingMeasurementJpaRepository.refreshLocked();
  }

  @Test
  public void findMissingMeterReadings_WithoutPeriod() {
    List<LogicalMeterCollectionStats> missingMeterReadingsCounts =
      logicalMeterJpaRepository.findMissingMeterReadingsCounts(new RequestParametersAdapter());

    assertThat(missingMeterReadingsCounts).isEmpty();
  }

  @Test
  public void findMissingMeterReadings_WhenNoneExists() {
    List<LogicalMeterCollectionStats> missingMeterReadingsCounts =
      logicalMeterJpaRepository.findMissingMeterReadingsCounts(makeParametersWithDateRange());

    assertThat(missingMeterReadingsCounts).isEmpty();
  }

  @Test
  public void refreshAsSuperAdmin() {
    given(logicalMeter());

    ResponseEntity<Void> response = asSuperAdmin()
      .post(
        "/missing/measurement/refresh",
        null,
        Void.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    List<LogicalMeterCollectionStats> missingMeterReadingsCounts =
      logicalMeterJpaRepository.findMissingMeterReadingsCounts(makeParametersWithDateRange());

    assertThat(missingMeterReadingsCounts)
      .extracting(stats -> stats.missingReadingCount)
      .containsExactly(24L);
  }

  @Test
  public void refreshAsUserDenied() {
    given(logicalMeter());

    ResponseEntity<Void> response = asUser()
      .post(
        "/missing/measurement/refresh",
        null,
        Void.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    List<LogicalMeterCollectionStats> missingMeterReadingsCounts = logicalMeterJpaRepository
      .findMissingMeterReadingsCounts(makeParametersWithDateRange());

    assertThat(missingMeterReadingsCounts.size()).isEqualTo(1);
    assertThat(missingMeterReadingsCounts.get(0).missingReadingCount).isEqualTo(0);
  }

  @Test
  public void refreshAsAdminDenied() {
    given(logicalMeter());

    ResponseEntity<Void> response = asAdmin()
      .post(
        "/missing/measurement/refresh",
        null,
        Void.class
      );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    List<LogicalMeterCollectionStats> missingMeterReadingsCounts = logicalMeterJpaRepository
      .findMissingMeterReadingsCounts(makeParametersWithDateRange());

    assertThat(missingMeterReadingsCounts.size()).isEqualTo(1);
    assertThat(missingMeterReadingsCounts.get(0).missingReadingCount).isEqualTo(0);
  }

  private RequestParameters makeParametersWithDateRange() {
    return new RequestParametersAdapter()
      .add(AFTER, context().now().toString())
      .add(BEFORE, context().now().plusDays(1).toString());
  }
}
