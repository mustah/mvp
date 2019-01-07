package com.elvaco.mvp.consumers.rabbitmq.helpers;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import javax.annotation.Nullable;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CronHelper {

  private static final CronParser CRON_PARSER = new CronParser(cronDefinition());
  private static final ZonedDateTime PREDICTABLE_TIME = ZonedDateTime.parse("1970-02-02T00:00:00Z")
    .truncatedTo(ChronoUnit.SECONDS);

  public static Optional<Duration> toReportInterval(@Nullable String cronExpression) {
    return Optional.ofNullable(cronExpression)
      .filter(expression -> !expression.trim().isEmpty())
      .map(CRON_PARSER::parse)
      .map(CronHelper::durationBetweenExecutions);
  }

  private static Duration durationBetweenExecutions(Cron cron) {
    ExecutionTime executionTime = ExecutionTime.forCron(cron);
    ZonedDateTime lastExecution = executionTime.lastExecution(PREDICTABLE_TIME.plusSeconds(1))
      .orElseThrow(() -> new RuntimeException(String.format(
        "No previous execution time found for cron '%s' at %s",
        cron.asString(),
        PREDICTABLE_TIME
      )));
    ZonedDateTime nextExecution = executionTime.nextExecution(lastExecution)
      .orElseThrow(() -> new RuntimeException(String.format(
        "No next execution time found for cron '%s' at %s",
        cron.asString(),
        PREDICTABLE_TIME
      )));

    return Duration.between(lastExecution, nextExecution);
  }

  private static CronDefinition cronDefinition() {
    /* We only support a subset of cron fields, namely the ones that describe time.
     * The reason for this is two-fold:
     *  - We can't calculate a regular interval from date-based fields
     *  - There exists a bug in cron-utils version 7.0.1 (version in use ATTOW) which might
     *    trigger an infinite loop for invalid dates
     *    (reported here: https://github.com/jmrozanec/cron-utils/issues/310
     *    and duplicated here: https://github.com/jmrozanec/cron-utils/issues/329)
     * We might need to expand support in the future, but for now, this is good enough.
     **/
    return CronDefinitionBuilder.defineCron()
      .withMinutes().and()
      .withHours().and()
      .withDayOfWeek().withValidRange(-1, -1).and()
      .withMonth().withValidRange(-1, -1).and()
      .withDayOfMonth().withValidRange(-1, -1).and()
      .enforceStrictRanges()
      .instance();
  }
}
