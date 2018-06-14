package com.elvaco.mvp.database.repository.jpa;

import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity;
import com.elvaco.mvp.database.repository.queryfilters.LogicalMeterQueryFilters;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.meter.QLocationEntity.locationEntity;
import static com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity.logicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;
import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterStatusLogEntity.physicalMeterStatusLogEntity;
import static com.querydsl.core.types.ExpressionUtils.allOf;
import static com.querydsl.core.types.ExpressionUtils.isNotNull;

@Repository
class SummeryQueryDslJpaRepository
  extends BaseQueryDslRepository<LogicalMeterEntity, UUID>
  implements SummaryJpaRepository {

  private static final QLocationEntity LOCATION = locationEntity;

  private static final QLogicalMeterEntity LOGICAL_METER = logicalMeterEntity;

  private static final QPhysicalMeterStatusLogEntity STATUS_LOG = physicalMeterStatusLogEntity;

  private static final QPhysicalMeterEntity PHYSICAL_METER = physicalMeterEntity;

  @Autowired
  SummeryQueryDslJpaRepository(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(LogicalMeterEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  /**
   * NOTE: we calculate .size() in Java land which causes extra memory usage.
   * JQL does not support multiple distinct values ("select count(distinct a, b)..."),
   * which forces us to count outside of the database.
   */
  @Override
  public MeterSummary summary(RequestParameters parameters, Predicate predicate) {
    long meters = createCountQuery(predicate)
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .distinct()
      .fetchCount();

    long cities = createQuery(predicate)
      .select(Expressions.list(LOCATION.country, LOCATION.city))
      .where(
        allOf(
          isNotNull(LOCATION.country), isNotNull(LOCATION.city)
        )
      )
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .distinct()
      .fetch()
      .size();

    long addresses = createQuery(predicate)
      .select(Expressions.list(LOCATION.country, LOCATION.city, LOCATION.streetAddress))
      .where(
        allOf(
          isNotNull(LOCATION.country), isNotNull(LOCATION.city), isNotNull(LOCATION.streetAddress)
        )
      )
      .leftJoin(LOGICAL_METER.location, LOCATION)
      .leftJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .leftJoin(PHYSICAL_METER.statusLogs, STATUS_LOG)
      .distinct()
      .fetch()
      .size();

    return new MeterSummary(meters, cities, addresses);
  }

  private static Predicate toPredicate(RequestParameters parameters) {
    return new LogicalMeterQueryFilters().toExpression(parameters);
  }
}
