package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementJpaRepositoryImpl extends QueryDslJpaRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

  private final EntityManager entityManager;
  private final EntityPath<MeasurementEntity> path;
  private final Querydsl querydsl;

  @Autowired
  MeasurementJpaRepositoryImpl(EntityManager entityManager) {
    this(entityManager, SimpleEntityPathResolver.INSTANCE);
  }

  private MeasurementJpaRepositoryImpl(EntityManager entityManager, EntityPathResolver resolver) {
    this(
      new JpaMetamodelEntityInformation<>(MeasurementEntity.class, entityManager.getMetamodel()),
      entityManager,
      resolver
    );
  }

  private MeasurementJpaRepositoryImpl(
    JpaEntityInformation<MeasurementEntity, Long> entityInformation,
    EntityManager entityManager,
    EntityPathResolver resolver
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
    this.path = resolver.createPath(entityInformation.getJavaType());
    PathBuilder<MeasurementEntity> builder = new PathBuilder<>(path.getType(), path.getMetadata());
    this.querydsl = new Querydsl(entityManager, builder);
  }

  public List<MeasurementEntity> findAllScaled(
    String scale,
    Predicate predicate
  ) {
    JPQLQuery<MeasurementEntity> query = getMeasurementEntityJpqlQuery(scale, predicate);

    // TODO: Implement and test sorting...
    // Maybe?: query = querydsl.applySorting(pageable.getSort(), query);

    return query.fetch();
  }

  public Page<MeasurementEntity> findAllScaled(
    String scale,
    Predicate predicate,
    Pageable pageable
  ) {
    JPQLQuery<MeasurementEntity> query = getMeasurementEntityJpqlQuery(scale, predicate);

    JPQLQuery<?> countQuery = createCountQuery(predicate);
    // TODO: Implement and test sorting...
    // Maybe?: query = querydsl.applySorting(pageable.getSort(), query);
    query = querydsl.applyPagination(pageable, query);

    return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchCount);
  }

  private JPQLQuery<MeasurementEntity> getMeasurementEntityJpqlQuery(
    String scale,
    Predicate predicate
  ) {
    JPQLQuery<MeasurementEntity> query = new JPAQuery<>(entityManager);
    QMeasurementEntity queryMeasurement = QMeasurementEntity.measurementEntity;
    query.select(
      Projections.constructor(
        MeasurementEntity.class,
        queryMeasurement.id,
        queryMeasurement.created,
        queryMeasurement.quantity,
        Expressions.simpleTemplate(
          MeasurementUnit.class,
          "unit_at({0}, {1})",
          queryMeasurement.value,
          scale
        ),
        queryMeasurement.physicalMeter
      ))
      .where(predicate)
      .from(path);
    return query;
  }

  public Map<UUID, Long> countGroupedByPhysicalMeterId(Predicate predicate) {
    JPQLQuery<Void> query = new JPAQuery<>(entityManager);
    QMeasurementEntity queryMeasurement = QMeasurementEntity.measurementEntity;
    return query.from(queryMeasurement)
      .groupBy(queryMeasurement.physicalMeter.id)
      .where(predicate)
      .transform(
        GroupBy.groupBy(
          queryMeasurement.physicalMeter.id).as(queryMeasurement.count()
        )
      );
  }
}
