package com.elvaco.mvp.database.repository.jpa;

import java.util.Map;
import java.util.UUID;

import com.elvaco.mvp.database.entity.measurement.MeasurementEntity;
import com.querydsl.core.types.Predicate;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface MeasurementJpaRepositoryCustom extends
                                                QueryDslPredicateExecutor<MeasurementEntity> {

  Map<UUID, Long> countGroupedByPhysicalMeterId(Predicate predicate);
}
