package com.elvaco.mvp.consumers.rabbitmq.helpers;

import java.time.Duration;
import java.util.Optional;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CronHelperTest {

  /*
   * Cron format:
   *
   * * * * * *
   * | | | | |
   * | | | | |
   * | | | | +---- Day of the Week   (range: 1-7, 1 standing for Monday)
   * | | | +------ Month of the Year (range: 1-12)
   * | | +-------- Day of the Month  (range: 1-31)
   * | +---------- Hour              (range: 0-23)
   * +------------ Minute            (range: 0-59)
   */

  @Test
  public void oneHourInterval() {
    assertThat(CronHelper.toReportInterval("0 * * * *").get()).isEqualTo(Duration.ofHours(1));
  }

  @Test
  public void fifteenMinuteInterval() {
    assertThat(CronHelper.toReportInterval("*/15 * * * *").get()).isEqualTo(Duration.ofMinutes(15));
  }

  @Test
  public void oneMinuteInterval() {
    assertThat(CronHelper.toReportInterval("* * * * *").get()).isEqualTo(Duration.ofMinutes(1));
  }

  @Test
  public void dayInterval() {
    assertThat(CronHelper.toReportInterval("0 0 * * *").get()).isEqualTo(Duration.ofDays(1));
  }

  @Test
  public void monthIntervalIsInvalid() {
    assertThatThrownBy(() -> CronHelper.toReportInterval("0 0 1 * *"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Failed to parse");
  }

  @Test
  public void febThirtyIsInvalid() {
    assertThatThrownBy(() -> CronHelper.toReportInterval("0 0 30 2 *"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Failed to parse");
  }

  @Test
  public void febFirstIsInvalid() {
    assertThatThrownBy(() -> CronHelper.toReportInterval("0 0 1 2 *"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Failed to parse");
  }

  @Test
  public void allZerosAreInvalid() {
    assertThatThrownBy(() -> CronHelper.toReportInterval("0 0 0 0 0"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Failed to parse");
  }

  @Test
  public void zerothDayOfMonthIsInvalid() {
    assertThatThrownBy(() -> CronHelper.toReportInterval("0 0 0 * *"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Failed to parse");
  }

  @Test
  public void negativeMonthIsInvalid() {
    assertThatThrownBy(() -> CronHelper.toReportInterval("0 0 * -1 *"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("Failed to parse");
  }

  @Test
  public void emptyCronString() {
    assertThat(CronHelper.toReportInterval("")).isEqualTo(Optional.empty());
  }
}
