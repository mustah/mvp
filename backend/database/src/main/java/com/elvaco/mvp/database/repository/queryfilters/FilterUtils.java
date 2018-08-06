package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static java.util.stream.Collectors.toList;

@UtilityClass
public final class FilterUtils {

  private static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG =
    gatewayStatusLogEntity;

  private static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG =
    physicalMeterStatusLogEntity;

  public static boolean isDateRange(RequestParameters parameters) {
    return parameters.hasName("before") && parameters.hasName("after");
  }

  public static boolean isGatewayQuery(RequestParameters parameters) {
    return parameters.hasName("gatewaySerial") || isGatewayStatusQuery(parameters);
  }

  public static boolean isGatewayStatusQuery(RequestParameters parameters) {
    return parameters.hasName("gatewayStatus");
  }

  public static boolean isMeterStatusQuery(RequestParameters parameters) {
    return parameters.hasName("status") || parameters.hasName("meterStatus");
  }

  public static boolean isOrganisationQuery(RequestParameters parameters) {
    return parameters.hasName("organisation");
  }

  public static boolean isLocationQuery(RequestParameters parameters) {
    return parameters.hasName("city") || parameters.hasName("address");
  }

  static List<StatusType> toStatusTypes(List<String> values) {
    return values.stream()
      .map(StatusType::from)
      .collect(toList());
  }

  static List<UUID> toUuids(List<String> values) {
    return values.stream()
      .map(UUID::fromString)
      .collect(toList());
  }

  @Nullable
  static Predicate gatewayStatusQueryFilter(
    ZonedDateTime start,
    ZonedDateTime stop,
    List<StatusType> statuses
  ) {
    if (start == null || stop == null) {
      return null;
    }
    BooleanExpression dateRangeExpression = GATEWAY_STATUS_LOG.start.before(stop)
      .and(GATEWAY_STATUS_LOG.stop.isNull().or(GATEWAY_STATUS_LOG.stop.after(start)));
    return (statuses == null || statuses.isEmpty())
      ? dateRangeExpression
      : dateRangeExpression.and(GATEWAY_STATUS_LOG.status.in(statuses));
  }

  @Nullable
  static Predicate meterStatusQueryFilter(
    ZonedDateTime start,
    ZonedDateTime stop,
    List<StatusType> statuses
  ) {
    if (start == null || stop == null) {
      return null;
    }
    BooleanExpression dateRangeExpression = METER_STATUS_LOG.start.before(stop)
      .and(METER_STATUS_LOG.stop.isNull().or(METER_STATUS_LOG.stop.after(start)));
    return (statuses == null || statuses.isEmpty())
      ? dateRangeExpression
      : dateRangeExpression.and(METER_STATUS_LOG.status.in(statuses));
  }

  static ZonedDateTime getZonedDateTimeFrom(List<String> values) {
    return ZonedDateTime.parse(values.get(0));
  }
}
