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
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.GatewayQueryFilters;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.spi.data.RequestParameter.ID;
import static com.elvaco.mvp.core.util.CollectionUtils.isNotEmpty;
import static com.elvaco.mvp.database.repository.queryfilters.FilterUtils.isLocationQuery;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinLogicalMetersPhysicalMeter;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinMeterAlarmLogs;
import static com.elvaco.mvp.database.util.JoinIfNeededUtil.joinReportedMeters;
import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class GatewayQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements GatewayJpaRepository {

  @Autowired
  GatewayQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, GatewayEntity.class);
  }

  @Override
  public List<GatewayEntity> findAll(RequestParameters parameters) {
    return createQuery(toPredicate(parameters))
      .select(path)
      .distinct()
      .fetch();
  }

  @Override
  public Page<PagedGateway> findAll(RequestParameters parameters, Pageable pageable) {
    Predicate predicate = toPredicate(parameters);

    JPQLQuery<?> countQuery = createCountQuery(predicate).select(path).distinct();

    applyDefaultJoins(countQuery, parameters);

    JPQLQuery<PagedGateway> selectQuery = createQuery(predicate)
      .select(Projections.constructor(
        PagedGateway.class,
        GATEWAY.id,
        GATEWAY.organisationId,
        GATEWAY.serial,
        GATEWAY.productModel
      ))
      .distinct();

    applyDefaultJoins(selectQuery, parameters);

    List<PagedGateway> pagedGateways = querydsl.applyPagination(pageable, selectQuery).fetch();

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
  public Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    Predicate predicate = GATEWAY.organisationId.eq(organisationId).and(GATEWAY.id.eq(id));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Page<String> findSerials(RequestParameters parameters, Pageable pageable) {
    Predicate predicate = toPredicate(parameters);
    JPQLQuery<String> query = createQuery(predicate).select(GATEWAY.serial).distinct();
    joinLocation(query, parameters);

    JPQLQuery<String> countQuery = createCountQuery(predicate).select(GATEWAY.serial).distinct();
    joinLocation(countQuery, parameters);

    List<String> all = querydsl.applyPagination(pageable, query).fetch();
    return getPage(all, pageable, countQuery::fetchCount);
  }

  @Override
  public Optional<GatewayEntity> findById(UUID id) {
    Predicate predicate = GATEWAY.id.eq(id);
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  private List<PagedGateway> fetchAllLogicalMetersByGatewayIds(
    List<PagedGateway> pagedGateways,
    RequestParameters parameters
  ) {
    if (isNotEmpty(pagedGateways)) {
      parameters.setAll(ID, pagedGateways.stream()
        .map(item -> item.id.toString())
        .collect(toList())
      );

      Map<UUID, Set<LogicalMeterEntity>> gatewayMeters = findGatewayMeters(parameters);

      return pagedGateways.stream()
        .map(pagedGateway -> pagedGateway.toBuilder()
          .meters(gatewayMeters.getOrDefault(pagedGateway.id, emptySet()))
          .build())
        .collect(toList());
    }

    return new ArrayList<>(pagedGateways);
  }

  private Map<UUID, Set<LogicalMeterEntity>> findGatewayMeters(RequestParameters parameters) {
    JPQLQuery<GatewayEntity> query = createQuery(toPredicate(parameters)).select(path);
    applyDefaultJoins(query, parameters);
    return query.transform(groupBy(GATEWAY.id).as(GroupBy.set(LOGICAL_METER)));
  }

  private static void joinLocation(JPQLQuery<String> query, RequestParameters parameters) {
    if (isLocationQuery(parameters)) {
      query.leftJoin(GATEWAY.meters, LOGICAL_METER)
        .leftJoin(LOGICAL_METER.location, LOCATION);
    }
  }

  private static void applyDefaultJoins(JPQLQuery<?> query, RequestParameters parameters) {
    query.leftJoin(GATEWAY.meters, LOGICAL_METER)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(GATEWAY.statusLogs, GATEWAY_STATUS_LOG);

    joinLogicalMetersPhysicalMeter(query, parameters);
    joinReportedMeters(query, parameters);
    joinMeterAlarmLogs(query, parameters);
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new GatewayQueryFilters().toExpression(parameters);
  }
}
