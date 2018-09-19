package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.AbstractJPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
public class MeasurementJpaRepositoryImpl
  extends BaseQueryDslRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

  @Autowired
  MeasurementJpaRepositoryImpl(EntityManager entityManager) {
    super(entityManager, MeasurementEntity.class);
  }

  @Override
  public Optional<MeasurementEntity> findBy(
    UUID physicalMeterId,
    String quantity,
    ZonedDateTime created
  ) {
    Predicate predicate = MEASUREMENT.id.physicalMeter.id.eq(physicalMeterId)
      .and(MEASUREMENT.id.quantity.name.eq(quantity))
      .and(MEASUREMENT.id.created.eq(created));

    return Optional.ofNullable(
      createQuery(predicate)
        .select(path)
        .join(MEASUREMENT.id.physicalMeter, PHYSICAL_METER)
        .fetchJoin()
        .fetchOne()
    );
  }

  @Override
  public Page<MeasurementEntity> findAll(Predicate predicate, Pageable pageable) {
    JPQLQuery<MeasurementEntity> countQuery = getFindAllQuery(
      createCountQuery(querydsl.createQuery(LOGICAL_METER).where(predicate)));

    JPQLQuery<MeasurementEntity> query =
      getFindAllQuery(querydsl.createQuery(LOGICAL_METER).where(predicate));

    List<MeasurementEntity> all = querydsl.applyPagination(pageable, query).fetch();

    return getPage(all, pageable, countQuery::fetchCount);
  }

  private JPQLQuery<MeasurementEntity> getFindAllQuery(AbstractJPAQuery<?, ?> query) {
    return query.select(Projections.constructor(
      MeasurementEntity.class,
      MEASUREMENT.id.created,
      MEASUREMENT.id.quantity,
      MEASUREMENT.value,
      MEASUREMENT.id.physicalMeter
      )
    )
      .innerJoin(LOGICAL_METER.physicalMeters, PHYSICAL_METER)
      .innerJoin(PHYSICAL_METER.measurements, MEASUREMENT);
  }
}

