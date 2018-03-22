package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
public class LogicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  @Autowired
  public LogicalMeterQueryDslJpaRepository(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(LogicalMeterEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public List<LogicalMeterEntity> findAll(Predicate predicate) {
    return queryOf(predicate).fetch();
  }

  @Override
  public List<LogicalMeterEntity> findAll(Predicate predicate, Sort sort) {
    return querydsl.applySorting(sort, queryOf(predicate)).fetch();
  }

  @Override
  public Page<LogicalMeterEntity> findAll(Predicate predicate, Pageable pageable) {
    JPQLQuery<?> countQuery = createCountQuery(predicate);
    List<LogicalMeterEntity> all = querydsl.applyPagination(pageable, queryOf(predicate)).fetch();
    return getPage(all, pageable, countQuery::fetchCount);
  }

  @Override
  public List<LogicalMeterEntity> findByOrganisationId(UUID organisationId) {
    return findAll(Q.organisationId.eq(organisationId));
  }

  @Override
  public Optional<LogicalMeterEntity> findById(UUID id) {
    return Optional.ofNullable(findOne(id));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(
    UUID organisationId,
    String externalId
  ) {
    Predicate predicate = Q.organisationId.eq(organisationId).and(Q.externalId.eq(externalId));
    return Optional.ofNullable(fetchOne(predicate));
  }

  @Override
  public Optional<LogicalMeterEntity> findBy(
    UUID organisationId,
    UUID id
  ) {
    Predicate predicate = Q.organisationId.eq(organisationId).and(Q.id.eq(id));
    return Optional.ofNullable(fetchOne(predicate));
  }

  private LogicalMeterEntity fetchOne(Predicate predicate) {
    return queryOf(predicate).fetchOne();
  }

  private JPQLQuery<LogicalMeterEntity> queryOf(Predicate predicate) {
    return createQuery(predicate)
      .select(path)
      .innerJoin(Q.location)
      .fetchJoin();
  }
}
