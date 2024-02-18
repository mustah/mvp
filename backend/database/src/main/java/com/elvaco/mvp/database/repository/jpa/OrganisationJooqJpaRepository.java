package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.filter.Filters;
import com.elvaco.mvp.core.filter.OrganisationParentFilter;
import com.elvaco.mvp.core.filter.RequestParametersMapper;
import com.elvaco.mvp.core.spi.data.RequestParameter;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.SelectForUpdateStep;
import org.jooq.SelectOnConditionStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.jooq.tables.Organisation.ORGANISATION;
import static com.elvaco.mvp.database.entity.jooq.tables.OrganisationUserSelection.ORGANISATION_USER_SELECTION;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.levenshtein;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class OrganisationJooqJpaRepository
  extends BaseJooqRepository<OrganisationEntity, UUID>
  implements OrganisationJpaRepository {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
    "name", ORGANISATION.NAME
  );

  private final DSLContext dsl;

  @Autowired
  OrganisationJooqJpaRepository(
    EntityManager entityManager,
    DSLContext dsl
  ) {
    super(entityManager, OrganisationEntity.class);

    this.dsl = dsl;
  }

  @Override
  public Optional<OrganisationEntity> findBySlug(String slug) {
    return fetchOne(ORGANISATION.SLUG.eq(slug));
  }

  @Override
  public Optional<OrganisationEntity> findByExternalId(String externalId) {
    return fetchOne(ORGANISATION.EXTERNAL_ID.eq(externalId));
  }

  @Override
  public List<OrganisationEntity> findAllByOrderByNameAsc() {
    return nativeQuery(selectNativeOrganisation()
      .orderBy(ORGANISATION.NAME.asc()));
  }

  @Override
  public List<OrganisationEntity> findOrganisationAndSubOrganisations(UUID organisationId) {
    return nativeQuery(selectNativeOrganisation().where(
      ORGANISATION.ID.equal(organisationId).or(ORGANISATION.PARENT_ID.equal(organisationId))
    ));
  }

  @Override
  public List<OrganisationEntity> findAllSubOrganisations(UUID organisationId) {
    return nativeQuery(selectNativeOrganisation().where(
      ORGANISATION.PARENT_ID.equal(organisationId)
    ));
  }

  @Override
  public Page<Organisation> findAllMainOrganisations(
    RequestParameters parameters,
    Pageable pageable
  ) {
    Filters filters = RequestParametersMapper
      .toFilters(parameters)
      .add(new OrganisationParentFilter());

    Field<Integer> editDistance = levenshtein(
      ORGANISATION.NAME,
      parameters.getFirst(RequestParameter.Q_ORGANISATION)
    );

    var selectQuery = dsl.select(
      ORGANISATION.ID,
      ORGANISATION.NAME,
      ORGANISATION.SLUG,
      ORGANISATION.EXTERNAL_ID,
      editDistance
    ).from(ORGANISATION);

    var countQuery = dsl.select(ORGANISATION.ID)
      .from(ORGANISATION);

    FilterVisitors.organisation().accept(filters)
      .andJoinsOn(selectQuery)
      .andJoinsOn(countQuery);

    SelectForUpdateStep<Record5<UUID, String, String, String, Integer>> select = selectQuery
      .orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP, editDistance.asc()))
      .limit(pageable.getPageSize())
      .offset((int) pageable.getOffset());

    var all = select.fetch().stream()
      .map(record5 -> Organisation.builder()
        .id(record5.value1())
        .name(record5.value2())
        .slug(record5.value3())
        .externalId(record5.value4())
        .build())
      .toList();

    return getPage(all, pageable, () -> dsl.fetchCount(countQuery));
  }

  private Optional<OrganisationEntity> fetchOne(Condition condition) {
    return nativeQuery(selectNativeOrganisation()
      .where(condition)
      .limit(1)).stream()
      .findAny();
  }

  private SelectOnConditionStep<Record> selectNativeOrganisation() {
    return dsl.select()
      .from(ORGANISATION)
      .leftJoin(ORGANISATION_USER_SELECTION)
      .on(ORGANISATION.ID.eq(ORGANISATION_USER_SELECTION.ORGANISATION_ID));
  }
}
