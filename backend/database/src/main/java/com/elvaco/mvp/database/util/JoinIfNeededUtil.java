package com.elvaco.mvp.database.util;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.querydsl.jpa.JPQLQuery;
import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isDateRange;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isGatewayQuery;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isGatewayStatusQuery;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isLocationQuery;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isMeterStatusQuery;

@UtilityClass
public final class JoinIfNeededUtil {

  private static final QLocationEntity LOCATION =
    QLocationEntity.locationEntity;

  private static final QLogicalMeterEntity LOGICAL_METER =
    QLogicalMeterEntity.logicalMeterEntity;

  private static final QGatewayEntity GATEWAY = QGatewayEntity.gatewayEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER =
    QPhysicalMeterEntity.physicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG =
    QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;

  private static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG =
    QGatewayStatusLogEntity.gatewayStatusLogEntity;

  public static <T> void joinLogicalMetersPhysicalMetersStatusLogs(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isDateRange(parameters)) {
      query.join(LOGICAL_METER.physicalMeters, PHYSICAL_METER);
      query.leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG);
    }
  }

  public static <T> void joinLogicalMeterGateways(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isGatewayQuery(parameters)) {
      query.leftJoin(LOGICAL_METER.gateways, GATEWAY);
    }
  }

  public static <T> void joinMeterStatusLogs(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isMeterStatusQuery(parameters) && isDateRange(parameters)) {
      query.leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG);
    }
  }

  public static <T> void joinGatewayStatusLogs(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isGatewayStatusQuery(parameters)) {
      query.leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG);
    }
  }

  public static <T> void joinLogicalMeterLocation(
    JPQLQuery<T> query,
    RequestParameters parameters
  ) {
    if (isLocationQuery(parameters)) {
      query.leftJoin(LOGICAL_METER.location, LOCATION);
    }
  }
}
