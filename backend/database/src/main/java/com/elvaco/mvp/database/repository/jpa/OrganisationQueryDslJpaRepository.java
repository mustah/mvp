package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.querydsl.core.types.ExpressionUtils;
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

  private static final Predicate[] NO_PREDICATE = new Predicate[0];

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
    return createQuery(NO_PREDICATE).select(path).fetch();
  }

  @Override
  public Page<OrganisationEntity> findAllParentOrganisations(
    Predicate predicate,
    Pageable pageable
  ) {
    Predicate withoutParentOrganisatin = ExpressionUtils.allOf(
      predicate,
      ORGANISATION.parent.isNull()
    );
    var countQuery = createCountQuery(withoutParentOrganisatin).select(path);
    var query = createQuery(withoutParentOrganisatin).select(path);
    var all = querydsl.applyPagination(pageable, query).fetch();

    return getPage(all, pageable, countQuery::fetchCount);
  }

  private OrganisationEntity fetchOne(Predicate predicate) {
    return createQuery(predicate).select(path).fetchOne();
  }
}
