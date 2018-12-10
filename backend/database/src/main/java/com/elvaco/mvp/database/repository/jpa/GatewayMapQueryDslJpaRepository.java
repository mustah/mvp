package com.elvaco.mvp.database.repository.jpa;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.MapMarker;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.filter.LocationConfidenceFilter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.jooq.tables.Gateway;
import com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog;
import com.elvaco.mvp.database.entity.jooq.tables.Location;
import com.elvaco.mvp.database.entity.jooq.tables.MeterAlarmLog;
import com.elvaco.mvp.database.repository.jooq.JooqFilterVisitor;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.domainmodels.GeoCoordinate.CONFIDENCE_THRESHOLD;
import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;

@Repository
class GatewayMapQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements MapMarkerJpaRepository {

  private final DSLContext dsl;
  private final JooqFilterVisitor gatewayJooqConditions;

  @Autowired
  GatewayMapQueryDslJpaRepository(
    EntityManager entityManager,
    DSLContext dsl,
    JooqFilterVisitor gatewayJooqConditions
  ) {
    super(entityManager, GatewayEntity.class);
    this.dsl = dsl;
    this.gatewayJooqConditions = gatewayJooqConditions;
  }

  @Override
  public Set<MapMarker> findAllMapMarkers(RequestParameters parameters) {
    var gateway = Gateway.GATEWAY;
    var location = Location.LOCATION;
    var gatewayStatusLog = GatewayStatusLog.GATEWAY_STATUS_LOG;
    var alarmLog = MeterAlarmLog.METER_ALARM_LOG;
    var query = dsl.select(
      gateway.ID,
      gatewayStatusLog.STATUS,
      alarmLog.MASK,
      location.LATITUDE,
      location.LONGITUDE
    ).from(gateway);

    Filters filters = toFilters(parameters);

    filters.add(new LocationConfidenceFilter(CONFIDENCE_THRESHOLD));

    gatewayJooqConditions.apply(filters, query);
    List<MapMarker> mapMarkers = query.fetchInto(MapMarker.class);
    return new HashSet<>(mapMarkers);
  }
}
