package com.elvaco.mvp.web;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.DoubleStream;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.PeriodRange;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.dto.CollectionStatsDto;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.Url;
import com.elvaco.mvp.testdata.UrlTemplate;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.DoubleComparator;
import org.junit.After;
import org.junit.Test;
import org.springframework.data.domain.Page;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.StatusType.OK;
import static com.elvaco.mvp.core.domainmodels.StatusType.WARNING;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.COLLECTION_BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.THRESHOLD;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
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

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .withQuantity(Quantity.ENERGY)
      .startingAt(context().now())
      .withValues(1.0));

    Page<CollectionStatsDto> paginatedLogicalMeters = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusHours(1)),
        CollectionStatsDto.class
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
        statsFacilityUrl(context().yesterday(), context().yesterday().plusHours(1)),
        CollectionStatsDto.class
      )
      .getContent();

    assertThat(meters)
      .extracting(m -> m.collectionPercentage)
      .containsExactly(0.0);
  }

  @Test
  public void fiftyPercent_readout() {
    LogicalMeter districtHeatingMeter = given(logicalMeter());

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .startingAt(context().now())
      .withValues(1.0));

    Page<CollectionStatsDto> response = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusHours(2)),
        CollectionStatsDto.class
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

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .startingAt(context().now().plusMinutes(5))
      .withInterval(Duration.ofMinutes(5))
      .withValues(DoubleStream.iterate(1, d -> d).limit(24 * 12 - 1).toArray()));

    Page<CollectionStatsDto> response = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusDays(1)),
        CollectionStatsDto.class
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

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .withQuantity(Quantity.ENERGY)
      .startingAt(context().now())
      .withValues(1.0));

    Page<CollectionStatsDto> response = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusHours(2)),
        CollectionStatsDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent())
      .extracting(m -> m.collectionPercentage)
      .containsExactly(50.0);
  }

  @Test
  public void fortyPercentWhenMeterHasStatuses() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));

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
      measurementSeries()
        .forMeter(districtHeatingMeter)
        .startingAt(context().yesterday())
        .withQuantity(Quantity.RETURN_TEMPERATURE)
        .withValues(1.0, 2.0)
    );

    var response = asUser()
      .getPage(
        statsFacilityUrl(context().yesterday(), context().yesterday().plusHours(5)),
        CollectionStatsDto.class
      );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).extracting(m -> m.collectionPercentage).containsExactly(40.0);
  }

  @Test
  public void fiftyPercentWhenMeterHasMultipleActiveStatusesWithinPeriod() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));

    given(
      statusLog(districtHeatingMeter)
        .status(OK)
        .start(context().yesterday().minusMinutes(15)),

      statusLog(districtHeatingMeter)
        .status(WARNING)
        .start(context().yesterday().plusHours(1))
    );

    given(
      measurementSeries()
        .forMeter(districtHeatingMeter)
        .startingAt(context().yesterday())
        .withQuantity(Quantity.RETURN_TEMPERATURE)
        .withValues(1.0, 2.0)
    );

    var content = asUser()
      .getPage(
        statsFacilityUrl(context().yesterday(), context().yesterday().plusHours(4)),
        CollectionStatsDto.class
      )
      .getContent();

    assertThat(content).extracting(m -> m.collectionPercentage).containsExactly(50.0);
  }

  @Test
  public void twoOutOfThreeMissing() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(context().yesterday())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0));

    CollectionStatsDto logicalMeterDto = asUser()
      .getPage(
        statsFacilityUrl(context().yesterday(), context().yesterday().plusHours(3)),
        CollectionStatsDto.class
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
      logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING),
      physicalMeter().logicalMeterId(meterId)
        .activePeriod(firstMeterActivePeriod)
        .readIntervalMinutes(0),
      physicalMeter().logicalMeterId(meterId).activePeriod(secondMeterActivePeriod)
    );

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(secondMeterActivePeriod.getStartDateTime().get().plusHours(1))
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(2, d -> d + 1.0).limit(23).toArray()));

    List<CollectionStatsDto> pagedMeters = asUser()
      .getPage(
        statsFacilityUrl(
          secondMeterActivePeriod.getStartDateTime().get(),
          secondMeterActivePeriod.getStartDateTime().get().plusDays(1)
        ),
        CollectionStatsDto.class
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
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(context().now())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(1, d -> d + 1.0).limit(24).toArray()));

    Page<CollectionStatsDto> paginatedLogicalMeters = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusDays(1)),
        CollectionStatsDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(100.0);

    var listedPercentage = asUser()
      .getList(
        statsDateUrl(context().now(), context().now().plusDays(2)),
        CollectionStatsPerDateDto.class
      );
    assertThat(listedPercentage.getBody()).hasSize(2);
    assertThat(listedPercentage.getBody().get(0).collectionPercentage).isEqualTo(100.0);
    assertThat(listedPercentage.getBody().get(1).collectionPercentage).isEqualTo(0.0);
  }

  @Test
  public void oneHundredPercentForTwoDays() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(context().now())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(1, d -> d + 1.0).limit(48).toArray()));

    Page<CollectionStatsDto> paginatedLogicalMeters = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusDays(2)),
        CollectionStatsDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(100.0);

    var listedPercentage = asUser().getList(
      statsDateUrl(
        context().now(),
        context().now().plusDays(2)
      ),
      CollectionStatsPerDateDto.class
    );
    assertThat(listedPercentage.getBody()).hasSize(2);
    assertThat(listedPercentage.getBody().get(0).collectionPercentage).isEqualTo(100.0);
    assertThat(listedPercentage.getBody().get(1).collectionPercentage).isEqualTo(100.0);
  }

  @Test
  public void oneHundredPercentForDayWhenPreviousDayHasExtraQuantity() {
    var districtHeatingMeter = given(logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING));

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(context().now())
      .withQuantity(Quantity.VOLUME)
      .withValues(DoubleStream.iterate(1, d -> d + 1.0).limit(24).toArray()));
    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(context().now())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(1, d -> d + 1.0).limit(48).toArray()));

    Page<CollectionStatsDto> paginatedLogicalMeters = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusDays(2)),
        CollectionStatsDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(100.0);

    var listedPercentage = asUser().getList(
      statsDateUrl(
        context().now().plusDays(1),
        context().now().plusDays(2)
      ),
      CollectionStatsPerDateDto.class
    );
    assertThat(listedPercentage.getBody()).hasSize(1);
    assertThat(listedPercentage.getBody().get(0).collectionPercentage).isEqualTo(100.0);
  }

  @Test
  public void fiftyPercentWithThresholdFilter() {
    ZonedDateTime now = context().now();
    var meterMatchingThreshold = given(logicalMeter()
      .meterDefinition(DEFAULT_DISTRICT_HEATING)
      .physicalMeter(physicalMeter().readIntervalMinutes(60).build())
    );

    given(measurementSeries()
      .forMeter(meterMatchingThreshold)
      .startingAt(now)
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(1, d -> d + 1.0).limit(24).toArray()));

    var otherMeter = given(logicalMeter()
      .meterDefinition(DEFAULT_DISTRICT_HEATING)
      .physicalMeter(physicalMeter().build())
    );

    given(measurementSeries()
      .forMeter(otherMeter)
      .startingAt(now)
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(0, d -> d - 1.0).limit(12).toArray()));

    Page<CollectionStatsDto> paginatedLogicalMeters = asUser()
      .getPage(
        Url.builder()
          .path("/meters/stats/facility")
          .parameter(AFTER, now)
          .parameter(BEFORE, now.plusDays(1))
          .parameter(THRESHOLD, "Return temperature > 10 °C")
          .parameter(COLLECTION_AFTER, now)
          .parameter(COLLECTION_BEFORE, now.plusDays(2))
          .build(),
        CollectionStatsDto.class
      );

    assertThat(paginatedLogicalMeters.getTotalElements()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getTotalPages()).isEqualTo(1);
    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(50.0);

    var listedPercentage = asUser()
      .getList(
        Url.builder()
          .path("/meters/stats/date")
          .parameter(AFTER, now)
          .parameter(BEFORE, now.plusDays(1))
          .parameter(THRESHOLD, "Return temperature > 10 °C")
          .parameter(COLLECTION_AFTER, now)
          .parameter(COLLECTION_BEFORE, now.plusDays(2))
          .build(),
        CollectionStatsPerDateDto.class
      );

    assertThat(listedPercentage.getBody())
      .extracting(e -> e.collectionPercentage).containsOnly(100.0, 0.0);
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
      logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING),
      physicalMeter().logicalMeterId(meterId).activePeriod(firstMeterActivePeriod),
      physicalMeter().logicalMeterId(meterId).activePeriod(secondMeterActivePeriod)
    );

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(context().now())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(1, d -> d + 1.0).limit(48).toArray()));

    Page<CollectionStatsDto> paginatedLogicalMeters = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusDays(2)),
        CollectionStatsDto.class
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
      logicalMeter().meterDefinition(DEFAULT_DISTRICT_HEATING),
      physicalMeter().logicalMeterId(meterId).activePeriod(firstMeterActivePeriod),
      physicalMeter().logicalMeterId(meterId).activePeriod(secondMeterActivePeriod)
    );

    given(measurementSeries()
      .forMeter(districtHeatingMeter)
      .startingAt(context().now().plusHours(12))
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(1, d -> d + 1.0).limit(36).toArray()));

    Page<CollectionStatsDto> paginatedLogicalMeters = asUser()
      .getPage(
        statsFacilityUrl(context().now(), context().now().plusDays(2)),
        CollectionStatsDto.class
      );

    assertThat(paginatedLogicalMeters.getContent())
      .extracting(m -> m.collectionPercentage)
      .contains(75.0);
  }

  @Test
  public void sortsByCollectionPercentage() {
    PhysicalMeter phys = physicalMeter().readIntervalMinutes(0).build();
    List<LogicalMeter> meters = new ArrayList<>(given(
      logicalMeter().externalId("0002"),
      logicalMeter().externalId("0001"),
      logicalMeter().externalId("0003"),
      logicalMeter().externalId("0004"),
      logicalMeter().externalId("0005").physicalMeters(List.of(phys))
    ));
    given(measurementSeries()
      .forMeter(meters.get(0))
      .startingAt(context().yesterday())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0, 1, 1, 1, 1, 1));
    given(measurementSeries()
      .forMeter(meters.get(2))
      .startingAt(context().yesterday())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0, 1, 1, 1, 1));
    given(measurementSeries()
      .forMeter(meters.get(1))
      .startingAt(context().yesterday().plusHours(6))
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0, 1, 1, 1));

    testSorting(
      "collectionPercentage,asc",
      meter -> meter.facility,
      List.of("0004", "0001", "0003", "0002", "0005")
    );

    testSorting(
      "collectionPercentage,desc",
      meter -> meter.facility,
      List.of("0005", "0002", "0003", "0001", "0004")
    );
  }

  @Test
  public void sortsByLastData() {
    List<LogicalMeter> meters = new ArrayList<>(given(
      logicalMeter().externalId("0002"),
      logicalMeter().externalId("0001"),
      logicalMeter().externalId("0003"),
      logicalMeter().externalId("0004")
    ));
    given(measurementSeries()
      .forMeter(meters.get(0))
      .startingAt(context().yesterday())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0, 1, 1, 1, 1, 1));
    given(measurementSeries()
      .forMeter(meters.get(2))
      .startingAt(context().yesterday())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0, 1, 1, 1, 1));
    given(measurementSeries()
      .forMeter(meters.get(1))
      .startingAt(context().yesterday().plusHours(6))
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0, 1, 1, 1));

    testSorting(
      "lastData,asc",
      meter -> meter.facility,
      List.of("0003", "0002", "0001", "0004")
    );

    testSorting(
      "lastData,desc",
      meter -> meter.facility,
      List.of("0004", "0001", "0002", "0003")
    );
  }

  @Test
  public void findAll_SortsByFacility() {
    given(
      logicalMeter().externalId("0005"),
      logicalMeter().externalId("0001"),
      logicalMeter().externalId("0003"),
      logicalMeter().externalId("0004"),
      logicalMeter().externalId("0002")
    );

    testSorting(
      "facility,asc",
      meter -> meter.facility,
      List.of("0001", "0002", "0003", "0004", "0005")
    );

    testSorting(
      "facility,desc",
      meter -> meter.facility,
      List.of("0005", "0004", "0003", "0002", "0001")
    );
  }

  @Test
  public void collectionPercentageDifferentReadIntervall() {
    PhysicalMeter phys0 = physicalMeter().readIntervalMinutes(0).build();
    PhysicalMeter phys24 = physicalMeter().readIntervalMinutes(1440).build();
    List<LogicalMeter> meters = new ArrayList<>(given(
      logicalMeter(),
      logicalMeter().physicalMeters(List.of(phys0)),
      logicalMeter().physicalMeters(List.of(phys24))
    ));
    given(measurementSeries()
      .forMeter(meters.get(0))
      .startingAt(context().yesterday())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(DoubleStream.iterate(0, d -> d + 1.0).limit(24).toArray()));
    given(measurementSeries()
      .forMeter(meters.get(1))
      .startingAt(context().yesterday())
      .withQuantity(Quantity.RETURN_TEMPERATURE)
      .withValues(1.0, 1, 1, 1, 1));

    var listedPercentage = asUser()
      .getList(
        statsDateUrl(context().yesterday(), context().yesterday().plusDays(2)),
        CollectionStatsPerDateDto.class
      );
    assertThat(listedPercentage.getBody()).hasSize(2);
    assertThat(listedPercentage.getBody().get(0).collectionPercentage).isEqualTo(50.0);
    assertThat(listedPercentage.getBody().get(1).collectionPercentage).isEqualTo(0.0);
  }

  private void testSorting(
    String sort,
    Function<CollectionStatsDto, String> actual,
    List<String> expectedProperties
  ) {
    Url url = Url.builder()
      .path("/meters/stats/facility")
      .collectionPeriod(context().yesterday(), context().yesterday().plusDays(1))
      .size(expectedProperties.size())
      .page(0)
      .sortBy(sort)
      .build();

    Page<CollectionStatsDto> response = asUser()
      .getPage(url, CollectionStatsDto.class);

    assertThat(response)
      .extracting(actual)
      .containsExactlyElementsOf(
        expectedProperties.stream().map(Assertions::tuple).collect(toList())
      );
  }

  private static UrlTemplate statsFacilityUrl(ZonedDateTime after, ZonedDateTime before) {
    return Url.builder()
      .path("/meters/stats/facility")
      .parameter(COLLECTION_AFTER, after)
      .parameter(COLLECTION_BEFORE, before)
      .build();
  }

  private static UrlTemplate statsDateUrl(ZonedDateTime after, ZonedDateTime before) {
    return Url.builder()
      .path("/meters/stats/date")
      .parameter(COLLECTION_AFTER, after)
      .parameter(COLLECTION_BEFORE, before)
      .build();
  }
}
