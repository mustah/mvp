package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.dto.GatewaySummaryDto;
import com.elvaco.mvp.core.dto.LogicalMeterLocation;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.filter.RequestParametersMapper;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.querydsl.core.ResultTransformer;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.querydsl.core.group.GroupBy.set;
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
  public Page<GatewaySummaryDto> findAll(RequestParameters parameters, Pageable pageable) {
    Filters filters = RequestParametersMapper.toFilters(parameters);

    ConstructorExpression<GatewaySummaryDto> constructor = Projections.constructor(
      GatewaySummaryDto.class,
      GATEWAY.id,
      GATEWAY.organisationId,
      GATEWAY.serial,
      GATEWAY.productModel,
      set(Projections.constructor(
        StatusLogEntry.class,
        GATEWAY_STATUS_LOG.id,
        GATEWAY_STATUS_LOG.gatewayId,
        GATEWAY_STATUS_LOG.status,
        GATEWAY_STATUS_LOG.start,
        GATEWAY_STATUS_LOG.stop
        ).skipNulls()
      ),
      set(
        Projections.constructor(
          LogicalMeterLocation.class,
          LOGICAL_METER.id,
          LOCATION.latitude,
          LOCATION.longitude,
          LOCATION.confidence,
          LOCATION.country,
          LOCATION.city,
          LOCATION.streetAddress
        ).skipNulls()
      )
    );

    JPQLQuery<GatewaySummaryDto> countQuery = createCountQuery().select(constructor)
      .distinct();
    JPQLQuery<GatewaySummaryDto> selectQuery = createQuery().select(constructor)
      .distinct();

    querydsl.applyPagination(pageable, selectQuery);

    new GatewayFilterQueryDslJpaVisitor().visitAndApply(
      filters,
      countQuery,
      selectQuery
    );

    ResultTransformer<List<GatewaySummaryDto>> transformer = GroupBy.groupBy(
      GATEWAY.id
    ).list(constructor);
    List<GatewaySummaryDto> pagedGateways = selectQuery.transform(transformer);

    return getPage(pagedGateways, pageable, countQuery::fetchCount);
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
  public Page<String> findSerials(Filters filters, Pageable pageable) {

    GatewayFilterQueryDslJpaVisitor visitor = new GatewayFilterQueryDslJpaVisitor();

    JPQLQuery<String> query = createQuery().select(GATEWAY.serial).distinct();
    JPQLQuery<String> countQuery = createCountQuery().select(GATEWAY.serial).distinct();

    visitor.visitAndApply(filters, query, countQuery);
    List<String> all = querydsl.applyPagination(pageable, query).fetch();
    return getPage(all, pageable, countQuery::fetchCount);
  }
}
