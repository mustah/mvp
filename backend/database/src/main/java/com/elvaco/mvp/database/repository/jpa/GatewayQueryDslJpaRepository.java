package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.dto.GatewaySummaryDto;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.jooq.tables.Gateway;
import com.elvaco.mvp.database.entity.jooq.tables.GatewayStatusLog;
import com.elvaco.mvp.database.entity.jooq.tables.Location;
import com.elvaco.mvp.database.repository.jooq.GatewayJooqConditions;
import com.elvaco.mvp.database.repository.querydsl.GatewayFilterQueryDslJpaVisitor;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class GatewayQueryDslJpaRepository
  extends BaseQueryDslRepository<GatewayEntity, UUID>
  implements GatewayJpaRepository {

  private final DSLContext dsl;

  @Autowired
  GatewayQueryDslJpaRepository(EntityManager entityManager, DSLContext dsl) {
    super(entityManager, GatewayEntity.class);
    this.dsl = dsl;
  }

  @Override
  public Optional<GatewayEntity> findById(UUID id) {
    var predicate = GATEWAY.pk.id.eq(id);
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Page<GatewaySummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    var gateway = Gateway.GATEWAY;
    var location = Location.LOCATION;
    var gatewayStatusLog = GatewayStatusLog.GATEWAY_STATUS_LOG;

    var selectQuery = dsl.select(
      gateway.ID,
      gateway.ORGANISATION_ID,
      gateway.SERIAL,
      gateway.PRODUCT_MODEL,
      gatewayStatusLog.ID,
      gatewayStatusLog.STATUS,
      gatewayStatusLog.START,
      gatewayStatusLog.STOP,
      location.LOGICAL_METER_ID,
      location.LATITUDE,
      location.LONGITUDE,
      location.CONFIDENCE,
      location.COUNTRY,
      location.CITY,
      location.STREET_ADDRESS
    ).distinctOn(gateway.ID)
      .from(gateway);

    var countQuery = dsl.select(gateway.ID).from(gateway);

    var filters = toFilters(parameters);

    new GatewayJooqConditions(dsl).apply(filters, selectQuery);
    new GatewayJooqConditions(dsl).apply(filters, countQuery);

    List<GatewaySummaryDto> pagedGateways = selectQuery
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue())
      .fetchInto(GatewaySummaryDto.class);

    return getPage(pagedGateways, pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public List<GatewayEntity> findAllByOrganisationId(UUID organisationId) {
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndProductModelAndSerial(
    UUID organisationId,
    String productModel,
    String serial
  ) {
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId)
      .and(GATEWAY.productModel.equalsIgnoreCase(productModel))
      .and(GATEWAY.serial.equalsIgnoreCase(serial));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndSerial(UUID organisationId, String serial) {
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId)
      .and(GATEWAY.serial.equalsIgnoreCase(serial));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    Predicate predicate = GATEWAY.pk.organisationId.eq(organisationId)
      .and(GATEWAY.pk.id.eq(id));
    return Optional.ofNullable(createQuery(predicate).select(path).fetchOne());
  }

  @Override
  public Page<String> findSerials(Filters filters, Pageable pageable) {
    JPQLQuery<String> query = createQuery().select(GATEWAY.serial).distinct();
    JPQLQuery<String> countQuery = createCountQuery().select(GATEWAY.serial).distinct();

    new GatewayFilterQueryDslJpaVisitor().visitAndApply(filters, query, countQuery);
    List<String> all = querydsl.applyPagination(pageable, query).fetch();
    return getPage(all, pageable, countQuery::fetchCount);
  }
}
