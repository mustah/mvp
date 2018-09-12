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

import static com.elvaco.mvp.core.spi.data.RequestParameter.ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_STATUS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.METER_STATUS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SECONDARY_ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.STATUS;
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
    return parameters.hasParam(BEFORE) && parameters.hasParam(AFTER);
  }

  public static boolean isGatewayQuery(RequestParameters parameters) {
    return parameters.hasParam(GATEWAY_SERIAL) || isGatewayStatusQuery(parameters);
  }

  public static boolean isGatewayStatusQuery(RequestParameters parameters) {
    return parameters.hasParam(GATEWAY_STATUS);
  }

  public static boolean isMeterStatusQuery(RequestParameters parameters) {
    return parameters.hasParam(STATUS) || parameters.hasParam(METER_STATUS);
  }

  public static boolean isOrganisationQuery(RequestParameters parameters) {
    return parameters.hasParam(ORGANISATION);
  }

  public static boolean isLocationQuery(RequestParameters parameters) {
    return parameters.hasParam(CITY) || parameters.hasParam(ADDRESS);
  }

  public static boolean isPhysicalMeterQuery(RequestParameters parameters) {
    return parameters.hasParam(SECONDARY_ADDRESS);
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
    if (start == null || stop == null || statuses == null || statuses.isEmpty()) {
      return null;
    }
    return METER_STATUS_LOG.start.before(stop)
      .and(METER_STATUS_LOG.stop.isNull().or(METER_STATUS_LOG.stop.after(start)))
      .and(METER_STATUS_LOG.status.in(statuses));
  }

  static ZonedDateTime getZonedDateTimeFrom(List<String> values) {
    return ZonedDateTime.parse(values.get(0));
  }
}
