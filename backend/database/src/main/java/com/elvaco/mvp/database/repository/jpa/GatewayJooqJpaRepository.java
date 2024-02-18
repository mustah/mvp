package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import com.elvaco.mvp.database.entity.jooq.tables.Gateway;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Gateway.GATEWAY;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.levenshtein;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class GatewayJooqJpaRepository
  extends BaseJooqRepository<GatewayEntity, UUID>
  implements GatewayJpaRepository {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
    "serial", GATEWAY.SERIAL
  );

  private final DSLContext dsl;
  private final FilterAcceptor gatewayFilters;

  @Autowired
  GatewayJooqJpaRepository(
    EntityManager entityManager,
    DSLContext dsl,
    FilterAcceptor gatewayFilters
  ) {
    super(entityManager, GatewayEntity.class);
    this.dsl = dsl;
    this.gatewayFilters = gatewayFilters;
  }

  @Override
  public Optional<GatewayEntity> findById(UUID id) {
    return fetchOne(GATEWAY.ID.equal(id));
  }

  @Override
  public Page<String> findSerials(RequestParameters parameters, Pageable pageable) {
    Field<Integer> editDistance = levenshtein(
      GATEWAY.SERIAL,
      parameters.getFirst(RequestParameter.Q_SERIAL)
    );

    var query = dsl.selectDistinct(GATEWAY.SERIAL, editDistance).from(GATEWAY);
    var countQuery = dsl.selectDistinct(GATEWAY.SERIAL).from(GATEWAY);

    gatewayFilters.accept(toFilters(parameters))
      .andJoinsOn(query)
      .andJoinsOn(countQuery);

    List<String> gatewaySerials = query
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP, editDistance.asc()))
      .limit(pageable.getPageSize())
      .offset((int) pageable.getOffset())
      .stream().map(Record2::value1)
      .toList();

    return getPage(gatewaySerials, pageable, () -> dsl.fetchCount(countQuery));
  }

  @Override
  public List<GatewayEntity> findAllByOrganisationId(UUID organisationId) {
    return nativeQuery(dsl.select().from(GATEWAY)
      .where(GATEWAY.ORGANISATION_ID.equal(organisationId)));
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndProductModelAndSerial(
    UUID organisationId,
    String productModel,
    String serial
  ) {
    return fetchOne(GATEWAY.ORGANISATION_ID.equal(organisationId)
      .and(GATEWAY.PRODUCT_MODEL.equal(productModel)
        .and(GATEWAY.SERIAL.upper().equal(serial.toUpperCase()))));
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndSerial(UUID organisationId, String serial) {
    return fetchOne(GATEWAY.ORGANISATION_ID.equal(organisationId)
      .and(GATEWAY.SERIAL.upper().equal(serial.toUpperCase())));
  }

  @Override
  public List<GatewayEntity> findBySerial(String serial) {
    return nativeQuery(dsl.select().from(GATEWAY)
      .where(GATEWAY.SERIAL.upper().equal(serial.toUpperCase())));
  }

  @Override
  public Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id) {
    return fetchOne(GATEWAY.ORGANISATION_ID.equal(organisationId).and(GATEWAY.ID.equal(id)));
  }

  private Optional<GatewayEntity> fetchOne(Condition... conditions) {
    return nativeQuery(dsl.select().from(GATEWAY).where(conditions).limit(1)).stream()
      .findAny();
  }
}
