package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.core.filter.Filters;

public interface SummaryJpaRepository {

  long meterCount(Filters parameters);

  long cityCount(Filters parameters);

  long addressCount(Filters parameters);
}
