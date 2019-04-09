package com.elvaco.mvp.database.repository.access;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeasurementValue;
import com.elvaco.mvp.core.domainmodels.TemporalResolution;

import org.junit.Test;

import static com.elvaco.mvp.database.repository.access.MeasurementRepository.fillMissing;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeasurementRepositoryTest {
  private static final ZonedDateTime NOW = ZonedDateTime.now()
    .withZoneSameInstant(ZoneId.of("UTC"))
    .truncatedTo(ChronoUnit.HOURS);

  @Test
  public void fillMissing_empty() {
    assertThat(fillMissing(
      emptyList(),
      NOW,
      NOW.minusDays(1),
      TemporalResolution.hour
    )).isEqualTo(List.of());
  }

  @Test
  public void fillMissing_doesntFillWhenNotNeeded() {
    List<MeasurementValue> values = List.of(measurement(NOW));
    assertThat(fillMissing(
      values,
      NOW,
      NOW,
      TemporalResolution.hour
    )).containsExactly(measurement(NOW));
  }

  @Test
  public void fillMissing_fillsBeginning() {
    List<MeasurementValue> values = List.of(measurement(NOW.plusHours(1)));
    assertThat(fillMissing(
      values,
      NOW,
      NOW.plusHours(1),
      TemporalResolution.hour
    )).containsExactly(nullMeasurement(NOW), measurement(NOW.plusHours(1)));
  }

  @Test
  public void fillMissing_fillsEnd() {
    List<MeasurementValue> values = List.of(measurement(NOW));

    assertThat(fillMissing(
      values,
      NOW,
      NOW.plusHours(1),
      TemporalResolution.hour
    )).containsExactly(measurement(NOW), nullMeasurement(NOW.plusHours(1)));
  }

  @Test
  public void fillMissing_fillsMiddle() {
    List<MeasurementValue> values = List.of(measurement(NOW), measurement(NOW.plusHours(2)));

    assertThat(fillMissing(
      values,
      NOW,
      NOW.plusHours(2),
      TemporalResolution.hour
    )).containsExactly(
      measurement(NOW),
      nullMeasurement(NOW.plusHours(1)),
      measurement(NOW.plusHours(2))
    );
  }

  @Test
  public void fillMissing_fillsWhenMeasurementsExistBetweenResolutionPointsWithMissing() {
    List<MeasurementValue> values = List.of(
      measurement(NOW),
      measurement(NOW.plusMinutes(12)),
      measurement(NOW.plusHours(2))
    );

    assertThat(fillMissing(
      values,
      NOW,
      NOW.plusHours(2),
      TemporalResolution.hour
    )).containsExactly(
      measurement(NOW),
      nullMeasurement(NOW.plusHours(1)),
      measurement(NOW.plusHours(2))
    );
  }

  @Test
  public void fillMissing_fillsWhenMeasurementsExistBetweenResolutionPointsNoMissing() {
    List<MeasurementValue> values = List.of(
      measurement(NOW),
      measurement(NOW.plusMinutes(30)),
      measurement(NOW.plusHours(1))
    );

    assertThat(fillMissing(
      values,
      NOW,
      NOW.plusHours(1),
      TemporalResolution.hour
    )).containsExactly(
      measurement(NOW),
      measurement(NOW.plusHours(1))
    );
  }

  @Test
  public void fillMissing_respectsOffsetDifferences() {
    List<MeasurementValue> values = List.of(
      measurement(NOW),
      measurement(NOW.plusHours(1))
    );

    assertThat(fillMissing(
      values,
      NOW.withZoneSameInstant(ZoneId.of("CET")),
      NOW.plusHours(1).withZoneSameInstant(ZoneId.of("UTC")),
      TemporalResolution.hour
    )).containsExactly(
      measurement(NOW),
      measurement(NOW.plusHours(1))
    );
  }

  @Test
  public void fillMissing_roundsStartUpToClosesIntervalTimestamp() {
    List<MeasurementValue> values = List.of(
      measurement(NOW),
      measurement(NOW.plusHours(1))
    );

    assertThat(fillMissing(
      values,
      NOW.minusMinutes(1),
      NOW.plusHours(1),
      TemporalResolution.hour
    )).containsExactly(
      measurement(NOW),
      measurement(NOW.plusHours(1))
    );
  }

  @Test
  public void fillMissing_roundsStartUpToClosesIntervalTimestamp_MonthResolution() {
    ZonedDateTime start = NOW.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
    List<MeasurementValue> values = List.of(
      measurement(start),
      measurement(start.plusMonths(1))
    );

    assertThat(fillMissing(
      values,
      start.minusMinutes(1),
      start.plusMonths(1),
      TemporalResolution.month
    )).containsExactly(
      measurement(start),
      measurement(start.plusMonths(1))
    );
  }

  private MeasurementValue nullMeasurement(ZonedDateTime now) {
    return new MeasurementValue(
      null,
      now.toInstant()
    );
  }

  private MeasurementValue measurement(ZonedDateTime now) {
    return new MeasurementValue(
      0.0,
      now.toInstant()
    );
  }
}
