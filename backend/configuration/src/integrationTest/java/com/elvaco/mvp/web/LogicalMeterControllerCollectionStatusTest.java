package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.DoubleStream;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testdata.UrlTemplate;
import com.elvaco.mvp.web.dto.PagedLogicalMeterDto;

import org.assertj.core.util.DoubleComparator;
import org.junit.After;
import org.junit.Test;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DISTRICT_HEATING_METER;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class LogicalMeterControllerCollectionStatusTest extends IntegrationTest {

  @After
  public void tearDown() {
    measurementJpaRepository.deleteAll();
  }

  @Test
  public void nullWhenNoInterval() {
    var districtHeatingMeter = given(logicalMeter(), physicalMeter().readIntervalMinutes(0));

    given(series(districtHeatingMeter, Quantity.ENERGY, 1.0));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusHours(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent()).hasSize(1);
    assertThat(paginatedLogicalMeters.getContent().get(0).collectionPercentage).isNull();
  }

  @Test
  public void zeroPercentWhenNoMeasurements() {
    given(logicalMeter());

    var meters = asUser()
      .getPage(
        metersUrl(context().yesterday(), context().yesterday().plusHours(1)),
        PagedLogicalMeterDto.class
      )
      .getContent();

    assertThat(meters)
      .extracting(m -> m.collectionPercentage)
      .containsExactly(0.0);
  }

  @Test
  public void fiftyPercent_readout() {
    LogicalMeter districtHeatingMeter = given(logicalMeter());

    given(series(districtHeatingMeter, Quantity.RETURN_TEMPERATURE, 1.0));

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusHours(2)),
        PagedLogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent())
      .extracting(m -> m.collectionPercentage)
      .containsExactly(50.0);
  }

  @Test
  public void collectionPercentageOnlyConsidersExpectedMeasurements() {
    LogicalMeter districtHeatingMeter = given(logicalMeter());

    given(series(districtHeatingMeter, Quantity.RETURN_TEMPERATURE, context().now().plusMinutes(5),
      Duration.ofMinutes(5), DoubleStream.iterate(1, d -> d).limit(24 * 12 - 1).toArray()
    ));

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusDays(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent())
      .extracting(m -> m.collectionPercentage)
      .containsExactly(23.0 / 24.0 * 100);
  }

  @Test
  public void fiftyPercent_consumption() {
    LogicalMeter districtHeatingMeter = given(logicalMeter());

    given(series(districtHeatingMeter, Quantity.ENERGY, 1.0));

    Page<PagedLogicalMeterDto> response = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusHours(2)),
        PagedLogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent())
      .extracting(m -> m.collectionPercentage)
      .containsExactly(50.0);
  }

  @Test
  public void fortyPercentWhenMeterHasStatuses() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DISTRICT_HEATING_METER));

    given(
      statusLog(districtHeatingMeter)
        .status(OK)
        .start(context().yesterday().minusMinutes(15))
        .stop(context().yesterday().plusHours(1)),

      statusLog(districtHeatingMeter)
        .status(WARNING)
        .start(context().yesterday().plusHours(1))
    );

    given(
      series(districtHeatingMeter, Quantity.RETURN_TEMPERATURE, context().yesterday(), 1.0, 2.0)
    );

    var response = asUser()
      .getPage(
        metersUrl(context().yesterday(), context().yesterday().plusHours(5)),
        PagedLogicalMeterDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).extracting(m -> m.collectionPercentage).containsExactly(40.0);
  }

  @Test
  public void fiftyPercentWhenMeterHasMultipleActiveStatusesWithinPeriod() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DISTRICT_HEATING_METER));

    given(
      statusLog(districtHeatingMeter)
        .status(OK)
        .start(context().yesterday().minusMinutes(15)),

      statusLog(districtHeatingMeter)
        .status(WARNING)
        .start(context().yesterday().plusHours(1))
    );

    given(
      series(districtHeatingMeter, Quantity.RETURN_TEMPERATURE, context().yesterday(), 1.0, 2.0)
    );

    var content = asUser()
      .getPage(
        metersUrl(context().yesterday(), context().yesterday().plusHours(4)),
        PagedLogicalMeterDto.class
      )
      .getContent();

    assertThat(content).extracting(m -> m.collectionPercentage).containsExactly(50.0);
  }

  @Test
  public void twoOutOfThreeMissing() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DISTRICT_HEATING_METER));

    given(series(districtHeatingMeter, Quantity.RETURN_TEMPERATURE, context().yesterday(), 1.0));

    PagedLogicalMeterDto logicalMeterDto = asUser()
      .getPage(
        metersUrl(context().yesterday(), context().yesterday().plusHours(3)),
        PagedLogicalMeterDto.class
      )
      .getContent()
      .get(0);

    assertThat(logicalMeterDto.collectionPercentage).isEqualTo(33.33333333333333);
  }

  @Test
  public void meterChangeWithIntervalUpdate() {
    var meterId = randomUUID();

    PeriodRange firstMeterActivePeriod = PeriodRange.halfOpenFrom(
      context().yesterday().minusDays(2), context().yesterday().minusDays(1)
    );

    PeriodRange secondMeterActivePeriod = PeriodRange.halfOpenFrom(
      context().yesterday().minusDays(1), null
    );

    var districtHeatingMeter = given(
      logicalMeter().meterDefinition(DISTRICT_HEATING_METER),
      physicalMeter().logicalMeterId(meterId)
        .activePeriod(firstMeterActivePeriod)
        .readIntervalMinutes(0),
      physicalMeter().logicalMeterId(meterId).activePeriod(secondMeterActivePeriod)
    );

    given(series(
      districtHeatingMeter,
      Quantity.RETURN_TEMPERATURE,
      secondMeterActivePeriod.getStartDateTime().get().plusHours(1),
      DoubleStream.iterate(2, d -> d + 1.0).limit(23).toArray()
    ));

    List<PagedLogicalMeterDto> pagedMeters = asUser()
      .getPage(
        metersUrl(
          secondMeterActivePeriod.getStartDateTime().get(),
          secondMeterActivePeriod.getStartDateTime().get().plusDays(1)
        ),
        PagedLogicalMeterDto.class
      )
      .getContent();

    /* NOTE! Only the active meter is considered here; not necessarily since the inactive one is
    inactive, but because it has a zero read interval*/
    assertThat(pagedMeters).extracting(m -> m.collectionPercentage)
      .usingComparatorForType(new DoubleComparator(0.1), Double.class)
      .containsExactlyInAnyOrder(95.8333);
  }

  @Test
  public void oneHundredPercent() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DISTRICT_HEATING_METER));

    given(series(
      districtHeatingMeter,
      Quantity.RETURN_TEMPERATURE,
      context().now(),
      DoubleStream.iterate(1, d -> d + 1.0).limit(24).toArray()
    ));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusDays(1)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(100.0);
  }

  @Test
  public void oneHundredPercentForTwoDays() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DISTRICT_HEATING_METER));

    given(series(
      districtHeatingMeter,
      Quantity.RETURN_TEMPERATURE,
      context().now(),
      DoubleStream.iterate(1, d -> d + 1.0).limit(48).toArray()
    ));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusDays(2)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(100.0);
  }

  @Test
  public void collectionPercentageWithMeterReplacement_100percent() {
    var meterId = randomUUID();

    PeriodRange firstMeterActivePeriod = PeriodRange.halfOpenFrom(
      context().yesterday().minusDays(2), context().yesterday().minusDays(1)
    );

    PeriodRange secondMeterActivePeriod = PeriodRange.halfOpenFrom(
      context().yesterday().minusDays(1), null
    );

    var districtHeatingMeter = given(
      logicalMeter().meterDefinition(DISTRICT_HEATING_METER),
      physicalMeter().logicalMeterId(meterId).activePeriod(firstMeterActivePeriod),
      physicalMeter().logicalMeterId(meterId).activePeriod(secondMeterActivePeriod)
    );

    given(series(
      districtHeatingMeter,
      Quantity.RETURN_TEMPERATURE,
      context().now(),
      DoubleStream.iterate(1, d -> d + 1.0).limit(48).toArray()
    ));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusDays(2)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(100.0);
  }

  @Test
  public void collectionPercentageWithMeterReplacement_75percent() {
    var meterId = randomUUID();

    PeriodRange firstMeterActivePeriod = PeriodRange.halfOpenFrom(
      context().yesterday().minusDays(2), context().yesterday().minusDays(1)
    );

    PeriodRange secondMeterActivePeriod = PeriodRange.halfOpenFrom(
      context().yesterday().minusDays(1), null
    );

    var districtHeatingMeter = given(
      logicalMeter().meterDefinition(DISTRICT_HEATING_METER),
      physicalMeter().logicalMeterId(meterId).activePeriod(firstMeterActivePeriod),
      physicalMeter().logicalMeterId(meterId).activePeriod(secondMeterActivePeriod)
    );

    given(series(
      districtHeatingMeter,
      Quantity.RETURN_TEMPERATURE,
      context().now().plusHours(12),
      DoubleStream.iterate(1, d -> d + 1.0).limit(36).toArray()
    ));

    Page<PagedLogicalMeterDto> paginatedLogicalMeters = asUser()
      .getPage(
        metersUrl(context().now(), context().now().plusDays(2)),
        PagedLogicalMeterDto.class
      );

    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(75.0);
  }

  private static UrlTemplate metersUrl(ZonedDateTime after, ZonedDateTime before) {
    return Url.builder()
      .path("/meters")
      .parameter(AFTER, after)
      .parameter(BEFORE, before)
      .build();
  }
}
