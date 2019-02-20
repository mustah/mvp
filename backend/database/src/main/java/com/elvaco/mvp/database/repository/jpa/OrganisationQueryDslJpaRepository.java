package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
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

import com.querydsl.core.types.Predicate;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record5;
import org.jooq.SelectForUpdateStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.jooq.tables.Organisation.ORGANISATION;
import static com.elvaco.mvp.database.entity.jooq.tables.OrganisationUserSelection.ORGANISATION_USER_SELECTION;
import static com.elvaco.mvp.database.entity.user.QOrganisationEntity.organisationEntity;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.levenshtein;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class OrganisationQueryDslJpaRepository
  extends BaseQueryDslRepository<OrganisationEntity, UUID>
  implements OrganisationJpaRepository {

  private final DSLContext dsl;

  @Autowired
  OrganisationQueryDslJpaRepository(
    EntityManager entityManager,
    DSLContext dsl
  ) {
    super(entityManager, OrganisationEntity.class);

    this.dsl = dsl;
  }

  @Override
  public Optional<OrganisationEntity> findBySlug(String slug) {
    return Optional.ofNullable(fetchOne(organisationEntity.slug.eq(slug)));
  }

  @Override
  public Optional<OrganisationEntity> findByExternalId(String externalId) {
    return Optional.ofNullable(fetchOne(organisationEntity.externalId.eq(externalId)));
  }

  @Override
  public List<OrganisationEntity> findAllByOrderByNameAsc() {
    return createQuery().select(path).fetch();
  }

  @Override
  public List<OrganisationEntity> findAllSubOrganisations(UUID organisationId) {
    return nativeQuery(dsl.select().from(ORGANISATION)
      .leftJoin(ORGANISATION_USER_SELECTION)
      .on(ORGANISATION_USER_SELECTION.ORGANISATION_ID.equal(ORGANISATION.ID))
      .where(
        ORGANISATION.ID.equal(organisationId).or(ORGANISATION.PARENT_ID.equal(organisationId))
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
      .orderBy(editDistance.asc())
      .limit(pageable.getPageSize())
      .offset(Long.valueOf(pageable.getOffset()).intValue());

    var all = select
      .fetch()
      .stream()
      .map(record -> new Organisation(
        record.value1(),
        record.value2(),
        record.value3(),
        record.value4()
      ))
      .collect(toList());

    return getPage(all, pageable, () -> dsl.fetchCount(countQuery));
  }

  private OrganisationEntity fetchOne(Predicate predicate) {
    return createQuery(predicate).select(path).fetchOne();
  }
}
