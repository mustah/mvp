package com.elvaco.mvp.database.repository.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.PagedGateway;
import com.elvaco.mvp.database.entity.gateway.QGatewayEntity;
import com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.GatewayQueryFilters;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.gateway.QGatewayEntity.gatewayEntity;
import static com.elvaco.mvp.database.entity.gateway.QGatewayStatusLogEntity.gatewayStatusLogEntity;
import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinGatewayStatusLogs;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class GatewayQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements GatewayJpaRepository {

  private static final QGatewayEntity GATEWAY = gatewayEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;
  private static final QLocationEntity LOCATION = locationEntity;
  private static final QGatewayStatusLogEntity GATEWAY_STATUS_LOG = gatewayStatusLogEntity;

  @Autowired
  GatewayQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, GatewayEntity.class);
  }

  @Override
  public List<GatewayEntity> findAll(RequestParameters parameters) {
    JPQLQuery<GatewayEntity> query = createQuery(toPredicate(parameters)).select(path);
    joinGatewayStatusLogs(query, parameters);
    return query.distinct().fetch();
  }

  @Override
  public Page<PagedGateway> findAll(RequestParameters parameters, Pageable pageable) {
    Predicate predicate = toPredicate(parameters);

    JPQLQuery<?> countQuery = createCountQuery(predicate)
      .select(path)
      .leftJoin(GATEWAY.meters, LOGICAL_METER)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG);

    JPQLQuery<PagedGateway> selectQuery = createQuery(predicate)
      .select(Projections.constructor(
        PagedGateway.class,
        GATEWAY.id,
        GATEWAY.organisationId,
        GATEWAY.serial,
        GATEWAY.productModel
      ))
      .leftJoin(GATEWAY.meters, LOGICAL_METER)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG);

    List<PagedGateway> pagedGateways = querydsl.applyPagination(pageable, selectQuery)
      .fetch();

    List<PagedGateway> content =
      fetchAllLogicalMetersByGatewayIds(pagedGateways, parameters.shallowCopy());

    return getPage(content, pageable, countQuery::fetchCount);
  }

  @Override
  public List<GatewayEntity> findAllByOrganisationId(UUID organisationId) {
    Predicate predicate = GATEWAY.organisationId.eq(organisationId);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndProductModelAndSerial(
    UUID organisationId,
    String productModel,
    String serial
  ) {
    Predicate predicate = GATEWAY.organisationId.eq(organisationId)
      .and(GATEWAY.productModel.equalsIgnoreCase(productModel))
      .and(GATEWAY.serial.equalsIgnoreCase(serial));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndSerial(UUID organisationId, String serial) {
    Predicate predicate = GATEWAY.organisationId.eq(organisationId)
      .and(GATEWAY.serial.equalsIgnoreCase(serial));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Optional<GatewayEntity> findById(UUID id) {
    Predicate predicate = GATEWAY.id.eq(id);
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    Predicate predicate = GATEWAY.organisationId.eq(organisationId).and(GATEWAY.id.eq(id));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  private List<PagedGateway> fetchAllLogicalMetersByGatewayIds(
    List<PagedGateway> pagedGateways,
    RequestParameters parameters
  ) {
    if (!pagedGateways.isEmpty()) {
      parameters.setAll("id", pagedGateways.stream()
        .map(item -> item.id.toString())
        .collect(toList())
      );

      Map<UUID, Set<LogicalMeterEntity>> gatewayMeters =
        findGatewayMeters(toPredicate(parameters));

      return pagedGateways.stream()
        .map(pagedGateway ->
          pagedGateway.withMeters(gatewayMeters.getOrDefault(pagedGateway.id, emptySet())))
        .collect(toList());
    }

    return new ArrayList<>(pagedGateways);
  }

  private Map<UUID, Set<LogicalMeterEntity>> findGatewayMeters(Predicate predicate) {
    return createQuery(predicate)
      .select(path)
      .leftJoin(GATEWAY.meters, LOGICAL_METER)
      .leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG)
      .join(LOGICAL_METER.location, LOCATION)
      .transform(groupBy(GATEWAY.id).as(GroupBy.set(LOGICAL_METER)));
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new GatewayQueryFilters().toExpression(parameters);
  }
}
