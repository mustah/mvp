package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.SelectionPeriod;
import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.core.types.Predicate;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.AFTER;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ALARM;
import static com.elvaco.mvp.core.spi.data.RequestParameter.BEFORE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_ID;
import static com.elvaco.mvp.core.spi.data.RequestParameter.GATEWAY_SERIAL;
import static com.elvaco.mvp.core.spi.data.RequestParameter.MAX_VALUE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.MIN_VALUE;
import static com.elvaco.mvp.core.spi.data.RequestParameter.ORGANISATION;
import static com.elvaco.mvp.core.spi.data.RequestParameter.QUANTITY;
import static com.elvaco.mvp.core.spi.data.RequestParameter.REPORTED;
import static com.elvaco.mvp.core.spi.data.RequestParameter.SECONDARY_ADDRESS;
import static com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity.meterAlarmLogEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static java.util.stream.Collectors.toList;

@UtilityClass
public final class FilterUtils {

  private static final QPhysicalMeterStatusLogEntity METER_STATUS_LOG =
    physicalMeterStatusLogEntity;

  private static final QMeterAlarmLogEntity ALARM_LOG = meterAlarmLogEntity;

  public static boolean isDateRange(RequestParameters parameters) {
    return parameters.hasParam(BEFORE) && parameters.hasParam(AFTER);
  }

  public static boolean isGatewayQuery(RequestParameters parameters) {
    return parameters.hasParam(GATEWAY_SERIAL) || parameters.hasParam(GATEWAY_ID);
  }

  public static boolean isReportedQuery(RequestParameters parameters) {
    return parameters.hasParam(REPORTED);
  }

  public static boolean isAlarmQuery(RequestParameters parameters) {
    return parameters.hasParam(ALARM);
  }

  public static boolean isOrganisationQuery(RequestParameters parameters) {
    return parameters.hasParam(ORGANISATION);
  }

  public static boolean isLocationQuery(RequestParameters parameters) {
    return parameters.hasParam(CITY) || parameters.hasParam(ADDRESS);
  }

  public static boolean isPhysicalMeterQuery(RequestParameters parameters) {
    return parameters.hasParam(SECONDARY_ADDRESS)
      || isAlarmQuery(parameters)
      || isReportedQuery(parameters);
  }

  public static boolean isMeasurementsQuery(RequestParameters parameters) {
    return (parameters.hasParam(MIN_VALUE) || parameters.hasParam(MAX_VALUE))
      && parameters.hasParam(QUANTITY);
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
  static Predicate meterStatusQueryFilter(
    SelectionPeriod selectionPeriod,
    List<StatusType> statuses
  ) {
    if (statuses == null || statuses.isEmpty()) {
      return null;
    }
    return METER_STATUS_LOG.start.before(selectionPeriod.stop)
      .and(METER_STATUS_LOG.stop.isNull().or(METER_STATUS_LOG.stop.after(selectionPeriod.start)))
      .and(METER_STATUS_LOG.status.in(statuses));
  }

  static Predicate alarmQueryFilter(List<String> values) {
    return values.stream().anyMatch(FilterUtils::isYes)
      ? ALARM_LOG.mask.isNotNull()
      : ALARM_LOG.mask.isNull();
  }

  static ZonedDateTime getZonedDateTimeFrom(List<String> values) {
    return ZonedDateTime.parse(values.get(0));
  }

  private static boolean isYes(String v) {
    return "yes".equalsIgnoreCase(v);
  }
}
