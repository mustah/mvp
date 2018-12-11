package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.user.OrganisationEntity;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
class OrganisationQueryDslJpaRepository
  extends BaseQueryDslRepository<OrganisationEntity, UUID>
  implements OrganisationJpaRepository {

  @Autowired
  OrganisationQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, OrganisationEntity.class);
  }

  @Override
  public Optional<OrganisationEntity> findBySlug(String slug) {
    return Optional.ofNullable(fetchOne(ORGANISATION.slug.eq(slug)));
  }

  @Override
  public Optional<OrganisationEntity> findByExternalId(String externalId) {
    return Optional.ofNullable(fetchOne(ORGANISATION.externalId.eq(externalId)));
  }

  @Override
  public List<OrganisationEntity> findAllByOrderByNameAsc() {
    return createQuery().select(path).fetch();
  }

  @Override
  public Page<OrganisationEntity> findAllMainOrganisations(Predicate predicate, Pageable pageable) {
    Predicate withoutParentOrganisation = ORGANISATION.parent.isNull().and(predicate);

    var countQuery = createCountQuery(withoutParentOrganisation).select(path);
    var query = createQuery(withoutParentOrganisation).select(path);
    var all = querydsl.applyPagination(pageable, query).fetch();

    return getPage(all, pageable, countQuery::fetchCount);
  }

  private OrganisationEntity fetchOne(Predicate predicate) {
    return createQuery(predicate).select(path).fetchOne();
  }
}
