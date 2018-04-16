package com.elvaco.mvp.database.repository.jpa;

import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.entity.measurement.QMeasurementEntity;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
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

