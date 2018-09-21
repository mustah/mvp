package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
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
      createQuery(predicate).select(path)
        .join(MEASUREMENT.id.physicalMeter, PHYSICAL_METER)
        .fetchJoin()
        .fetchOne()
    );
  }

  @Override
  public Page<MeasurementEntity> findAllBy(
    UUID physicalMeterId,
    RequestParameters parameters,
    Pageable pageable
  ) {
    Predicate predicate = MEASUREMENT.id.physicalMeter.id.eq(physicalMeterId);

    JPQLQuery<?> countQuery = createCountQuery(predicate).select(path);
    JPQLQuery<MeasurementEntity> query = createQuery(predicate).select(path);
    List<MeasurementEntity> all = querydsl.applyPagination(pageable, query).fetch();

    return getPage(all, pageable, countQuery::fetchCount);
  }
}

