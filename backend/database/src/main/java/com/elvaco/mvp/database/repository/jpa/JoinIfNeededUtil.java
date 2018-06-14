package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.FilterUtils;
import com.querydsl.jpa.JPQLQuery;
import lombok.experimental.UtilityClass;

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

  public static <T> void joinPhysicalMeterFromLogicalMeter(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (FilterUtils.isPhysicalQuery(parameters)) {
      query.innerJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER);
    }
  }

  public static <T> void joinStatusLogsFromPhysicalMeter(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (FilterUtils.isStatusQuery(parameters)) {
      joinPhysicalMeterFromLogicalMeter(query, parameters);
      query.leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG);
    }
  }

  public static <T> void joinGatewayFromLogicalMeter(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (FilterUtils.isGatewayQuery(parameters)) {
      query.innerJoin(LOGICAL_METER.gateways, GATEWAY);
    }
  }
}
