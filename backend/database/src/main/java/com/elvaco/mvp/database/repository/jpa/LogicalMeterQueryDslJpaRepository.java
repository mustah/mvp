package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@NoRepositoryBean
public class LogicalMeterQueryDslJpaRepository
  extends QueryDslJpaRepository<LogicalMeterEntity, UUID>
  implements LogicalMeterJpaRepository {

  private static final QLogicalMeterEntity Q = QLogicalMeterEntity.logicalMeterEntity;

  private final EntityPath<LogicalMeterEntity> path;
  private final Querydsl querydsl;

  public LogicalMeterQueryDslJpaRepository(EntityManager entityManager) {
    this(entityManager, SimpleEntityPathResolver.INSTANCE);
  }

  private LogicalMeterQueryDslJpaRepository(
    EntityManager entityManager,
    EntityPathResolver resolver
  ) {
    super(
      new JpaMetamodelEntityInformation<>(LogicalMeterEntity.class, entityManager.getMetamodel()),
      entityManager
    );
    this.path = resolver.createPath(LogicalMeterEntity.class);
    this.querydsl = new Querydsl(
      entityManager,
      new PathBuilder<>(path.getType(), path.getMetadata())
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
