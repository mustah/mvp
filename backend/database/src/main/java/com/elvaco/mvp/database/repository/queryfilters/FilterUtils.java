package com.elvaco.mvp.database.repository.queryfilters;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;

import com.querydsl.core.types.Predicate;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ADDRESS;
import static com.elvaco.mvp.core.spi.data.RequestParameter.CITY;
import static com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity.meterAlarmLogEntity;
import static java.util.stream.Collectors.toList;

@UtilityClass
public final class FilterUtils {

  private static final QMeterAlarmLogEntity ALARM_LOG = meterAlarmLogEntity;

  public static boolean isLocationQuery(RequestParameters parameters) {
    return parameters.hasParam(CITY) || parameters.hasParam(ADDRESS);
  }

  static List<UUID> toUuids(List<String> values) {
    return values.stream()
      .map(UUID::fromString)
      .collect(toList());
  }

  public static Predicate alarmQueryFilter(Collection<String> values) {
    return values.stream().anyMatch(FilterUtils::isYes)
      ? ALARM_LOG.mask.isNotNull()
      : ALARM_LOG.mask.isNull();
  }

  static ZonedDateTime getZonedDateTimeFrom(List<String> values) {
    return ZonedDateTime.parse(values.get(0));
  }

  public static boolean isYes(String v) {
    return "yes".equalsIgnoreCase(v);
  }
}
