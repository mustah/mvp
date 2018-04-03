package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.MeasurementUnit;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.elvaco.mvp.database.util.SqlErrorMapper;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.hibernate.JDBCException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
public class MeasurementJpaRepositoryImpl extends BaseQueryDslRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

  MeasurementJpaRepositoryImpl(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(MeasurementEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public List<MeasurementEntity> findAllScaled(
    String scale,
    Predicate predicate
  ) {
    JPQLQuery<MeasurementEntity> query = getMeasurementEntityJpqlQuery(scale, predicate);

    // TODO: Implement and test sorting...
    // Maybe?: query = querydsl.applySorting(pageable.getSort(), query);

    try {
      return query.fetch();
    } catch (PersistenceException ex) {
      Throwable cause = ex.getCause();
      if (cause instanceof JDBCException) {
        String sqlErrorMessage = ((JDBCException) cause).getSQLException().getMessage();
        throw SqlErrorMapper.mapScalingError(
          scale,
          sqlErrorMessage
        ).orElse(ex);
      }
      throw ex;
    }
  }

  @Override
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

  @Override
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
}

