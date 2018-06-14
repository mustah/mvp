package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.querydsl.core.types.Predicate;

public interface SummaryJpaRepository {

  MeterSummary summary(RequestParameters parameters, Predicate predicate);
}
