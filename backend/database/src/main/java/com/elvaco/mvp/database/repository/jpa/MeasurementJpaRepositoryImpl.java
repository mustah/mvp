package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.core.filter.RequestParametersMapper.toFilters;
import static com.elvaco.mvp.database.entity.jooq.tables.Measurement.MEASUREMENT;
import static com.elvaco.mvp.database.repository.queryfilters.SortUtil.resolveSortFields;

@Repository
public class MeasurementJpaRepositoryImpl
  extends BaseJooqRepository<MeasurementEntity, Long>
  implements MeasurementJpaRepositoryCustom {

  private static final Map<String, Field<?>> SORT_FIELDS_MAP = Map.of(
    "created", MEASUREMENT.CREATED,
    "quantity", MEASUREMENT.QUANTITY
  );

  private final DSLContext dsl;

  @Autowired
  MeasurementJpaRepositoryImpl(EntityManager entityManager, DSLContext dsl) {
    super(entityManager, MeasurementEntity.class);
    this.dsl = dsl;
  }

  @Override
  public List<MeasurementEntity> findAll(RequestParameters parameters) {
    var query = dsl.select().from(MEASUREMENT);

    FilterVisitors.measurement().accept(toFilters(parameters)).andJoinsOn(query);

    query.orderBy(resolveSortFields(parameters, SORT_FIELDS_MAP));

    return nativeQuery(query);
  }
}
