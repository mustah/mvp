package com.elvaco.mvp.core.usecase;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Measurement;
import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.domainmodels.Status;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Measurements;

import static com.elvaco.mvp.core.security.OrganisationFilter.setCurrentUsersOrganisationId;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class LogicalMeterUseCases {

  private static final int DAY_INTERVAL = 1440;
  private static final int HOUR_INTERVAL = 60;

  private final LogicalMeters logicalMeters;
  private final AuthenticatedUser currentUser;
  private final Measurements measurements;

  public LogicalMeterUseCases(
    AuthenticatedUser currentUser,
    LogicalMeters logicalMeters,
    Measurements measurements
  ) {
    this.currentUser = currentUser;
    this.logicalMeters = logicalMeters;
    this.measurements = measurements;
  }

  public List<LogicalMeter> findAll() {
    if (!currentUser.isSuperAdmin()) {
      return logicalMeters.findByOrganisationId(currentUser.getOrganisationId());
    } else {
      return logicalMeters.findAll();
    }
  }

  public Page<LogicalMeter> findAll(RequestParameters parameters, Pageable pageable) {
    return logicalMeters.findAll(setCurrentUsersOrganisationId(currentUser, parameters), pageable)
      .map(logicalMeter ->
             logicalMeter.withCollectionPercentage(
               getCollectionPercent(
                 logicalMeter.physicalMeters, parameters
               ).orElse(null)
             )
      );
  }

  public List<LogicalMeter> findAll(RequestParameters parameters) {
    return logicalMeters.findAll(setCurrentUsersOrganisationId(currentUser, parameters))
      .stream().map(logicalMeter ->
                      logicalMeter.withCollectionPercentage(
                        getCollectionPercent(
                          logicalMeter.physicalMeters, parameters
                        ).orElse(null)
                      )
      ).collect(toList());
  }

  private Optional<Double> getCollectionPercent(
    List<PhysicalMeter> physicalMeters,
    RequestParameters parameters
  ) {
    if (!parameters.hasName("after") || !parameters.hasName("before")) {
      return Optional.empty();
    }

    ZonedDateTime after = ZonedDateTime.parse(parameters.getValues("after").get(0));
    ZonedDateTime before = ZonedDateTime.parse(parameters.getValues("before").get(0));

    double expectedReadouts = 0L;
    double actualReadouts = 0L;

    for (PhysicalMeter physicalMeter : physicalMeters) {
      expectedReadouts = calculateExpectedReadOuts(physicalMeter, after, before);
      actualReadouts += physicalMeter.getMeasurementCount().map(val -> val.longValue()).orElse(0L);
    }

    return Optional.of(actualReadouts / expectedReadouts);
  }

  static Double calculateExpectedReadOuts(
    PhysicalMeter physicalMeter,
    ZonedDateTime after,
    ZonedDateTime before
  ) {
    Double count = 0.0;

    for (int x = 0; x < physicalMeter.statuses.size(); x++) {
      MeterStatusLog status = physicalMeter.statuses.get(x);

      if (Status.ACTIVE.equals(Status.from(status.name))) {
        ZonedDateTime startPoint = getStartPoint(
          status.start,
          after,
          physicalMeter.readIntervalMinutes
        );

        ZonedDateTime endPoint = getEndPoint(
          status.stop == null ? before : status.stop,
          before
        );

        count += calculateExpectedReadOuts(
          physicalMeter.readIntervalMinutes,
          startPoint,
          endPoint
        );
      }
    }

    return count;
  }

  static double calculateExpectedReadOuts(
    long readIntervalMinutes,
    ZonedDateTime after,
    ZonedDateTime before
  ) {
    return Math.floor((double)
                        Duration.between(after, before).toMinutes() / readIntervalMinutes
    );
  }

  /**
   * Decides whether to use status start date or period as starting point.
   */
  private static ZonedDateTime getStartPoint(
    ZonedDateTime statusStart,
    ZonedDateTime periodAfter,
    Long readIntervalMinutes
  ) {
    return getFirstDateMatchingInterval(
      statusStart.isAfter(periodAfter) ? statusStart : periodAfter,
      readIntervalMinutes
    );
  }

  /**
   * Decides whether to use status end date or period as end point.
   */
  private static ZonedDateTime getEndPoint(
    ZonedDateTime statusEnd,
    ZonedDateTime periodBefore
  ) {
    return statusEnd.isBefore(periodBefore) ? statusEnd : periodBefore;
  }

  /**
   * Get next anticipated read of a meter.
   *
   * @param date     Date to start from
   * @param interval Read interval for the meter
   *
   * @return
   */
  static ZonedDateTime getFirstDateMatchingInterval(ZonedDateTime date, Long interval) {
    if (interval == DAY_INTERVAL) {
      if (date.getHour() == 0 && date.getMinute() == 0) {
        return date;
      }
      return date.truncatedTo(ChronoUnit.DAYS).plusDays(1);
    }

    if (interval <= HOUR_INTERVAL) {

      if (date.getMinute() == 0) {
        return ZonedDateTime.ofInstant(date.toInstant(), date.getZone());
      }

      return date.truncatedTo(ChronoUnit.HOURS)
        .plusMinutes(interval * (date.getMinute() / interval) + interval);
    }

    throw new RuntimeException("Unhandled meter interval");
  }

  public LogicalMeter save(LogicalMeter logicalMeter) {
    if (hasTenantAccess(logicalMeter.organisationId)) {
      return logicalMeters.save(logicalMeter);
    }
    throw new Unauthorized("User '" + currentUser.getUsername() + "' is not allowed to "
                           + "create this meter.");
  }

  public List<Measurement> measurements(
    LogicalMeter logicalMeter,
    Supplier<RequestParameters> filter
  ) {
    if (logicalMeter.physicalMeters.isEmpty()
        || logicalMeter.getQuantities().isEmpty()
        || !hasTenantAccess(logicalMeter.organisationId)) {
      return emptyList();
    }
    return measurements.findAll(filter.get());
  }

  public Optional<LogicalMeter> findById(UUID id) {
    if (currentUser.isSuperAdmin()) {
      return logicalMeters.findById(id);
    } else {
      return logicalMeters.findByOrganisationIdAndId(currentUser.getOrganisationId(), id);
    }
  }

  public Optional<LogicalMeter> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    if (currentUser.isWithinOrganisation(organisationId) || currentUser.isSuperAdmin()) {
      return logicalMeters.findByOrganisationIdAndExternalId(
        organisationId,
        externalId
      );
    }
    return Optional.empty();
  }

  private boolean hasTenantAccess(UUID organisationId) {
    return currentUser.isSuperAdmin() || currentUser.isWithinOrganisation(organisationId);
  }
}
