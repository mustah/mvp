package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface SummaryJpaRepository {

  MeterSummary summary(RequestParameters parameters);
}
