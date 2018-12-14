package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.domainmodels.GeoCoordinate.CONFIDENCE_THRESHOLD;
import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Gateway.GATEWAY;
import static com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog.GATEWAY_STATUS_LOG;
import static com.elvaco.mvp.database.entity.jooq.tables.Location.LOCATION;
import static com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog.METER_ALARM_LOG;

@RequiredArgsConstructor
@Repository
class GatewayMapMarkerJooqJpaRepository implements MapMarkerJpaRepository {

  private final DSLContext dsl;
  private final FilterAcceptor gatewayFilters;

  @Override
  public Set<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    var query = dsl.select(
      GATEWAY.ID,
      GATEWAY_STATUS_LOG.STATUS,
      METER_ALARM_LOG.MASK,
      LOCATION.LATITUDE,
      LOCATION.LONGITUDE
    ).from(GATEWAY);

    Filters filters = toFilters(parameters).add(new LocationConfidenceFilter(CONFIDENCE_THRESHOLD));

    gatewayFilters.apply(filters).applyJoinsOn(query);

    return new HashSet<>(query.fetchInto(MapMarker.class));
  }
}
