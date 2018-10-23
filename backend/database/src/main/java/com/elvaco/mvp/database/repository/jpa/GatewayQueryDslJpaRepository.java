package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.filter.FilterSet;
import com.elvaco.mvp.core.filter.RequestParametersConverter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.gateway.PagedGateway;
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
  public Page<PagedGateway> findAll(RequestParameters parameters, Pageable pageable) {

    FilterSet filterSet = RequestParametersConverter.toFilterSet(parameters);

    ConstructorExpression<PagedGateway> constructor = Projections.constructor(
      PagedGateway.class,
      GATEWAY.id,
      GATEWAY.organisationId,
      GATEWAY.serial,
      GATEWAY.productModel,
      set(LOGICAL_METER)
    );

    JPQLQuery<PagedGateway> countQuery = createCountQuery().select(constructor)
      .distinct();
    JPQLQuery<PagedGateway> selectQuery = createQuery().select(constructor)
      .distinct();

    querydsl.applyPagination(pageable, selectQuery);

    new GatewayFilterQueryDslJpaVisitor().visitAndApply(
      filterSet,
      countQuery,
      selectQuery
    );

    ResultTransformer<List<PagedGateway>> transformer = GroupBy.groupBy(GATEWAY.id)
      .list(constructor);
    List<PagedGateway> pagedGateways = selectQuery.transform(transformer);

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
  public Page<String> findSerials(FilterSet filterSet, Pageable pageable) {

    GatewayFilterQueryDslJpaVisitor visitor = new GatewayFilterQueryDslJpaVisitor();

    JPQLQuery<String> query = createQuery().select(GATEWAY.serial).distinct();
    JPQLQuery<String> countQuery = createCountQuery().select(GATEWAY.serial).distinct();

    visitor.visitAndApply(filterSet, query, countQuery);
    List<String> all = querydsl.applyPagination(pageable, query).fetch();
    return getPage(all, pageable, countQuery::fetchCount);
  }
}
