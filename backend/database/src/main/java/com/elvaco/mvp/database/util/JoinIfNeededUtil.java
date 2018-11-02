package com.elvaco.mvp.database.util;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.MeterAlarmLogQueryFilters;
import com.querydsl.jpa.JPQLQuery;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.database.entity.meter.QMeterAlarmLogEntity.meterAlarmLogEntity;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isAlarmQuery;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isGatewayQuery;

@UtilityClass
public final class JoinIfNeededUtil {

  private static final QLogicalMeterEntity LOGICAL_METER =
    QLogicalMeterEntity.logicalMeterEntity;

  private static final QGatewayEntity GATEWAY = QGatewayEntity.gatewayEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  private static final QMeterAlarmLogEntity ALARM_LOG = meterAlarmLogEntity;

  public static <T> void joinLogicalMeterGateways(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isGatewayQuery(parameters)) {
      query.leftJoin(LOGICAL_METER.gateways, GATEWAY);
    }
  }

  public static <T> void joinMeterAlarmLogs(JPQLQuery<T> query, RequestParameters parameters) {
    if (isAlarmQuery(parameters)) {
      query.leftJoin(PHYSICAL_METER.alarms, ALARM_LOG)
        .on(new MeterAlarmLogQueryFilters().toPredicate(parameters));
    }
  }
}
