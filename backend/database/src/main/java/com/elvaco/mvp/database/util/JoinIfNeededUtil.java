package com.elvaco.mvp.database.util;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.jpa.JPQLQuery;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isDateRange;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isGatewayQuery;

@UtilityClass
public final class JoinIfNeededUtil {

  private static final QLogicalMeterEntity LOGICAL_METER =
    QLogicalMeterEntity.logicalMeterEntity;

  private static final QGatewayEntity GATEWAY
    = QGatewayEntity.gatewayEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  public static <T> void joinStatusLogsFromPhysicalMeter(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isDateRange(parameters)) {
      query.join(LOGICAL_METER.physicalMeters, PHYSICAL_METER);
      query.leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG);
    }
  }

  public static <T> void joinGatewayFromLogicalMeter(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isGatewayQuery(parameters)) {
      query.join(LOGICAL_METER.gateways, GATEWAY);
    }
  }
}
