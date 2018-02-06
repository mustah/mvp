package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public interface LogicalMeters {

  LogicalMeter findById(Long id);

  List<LogicalMeter> findAll();

  Page<LogicalMeter> findAll(Map<String, List<String>> filterParams, Pageable pageable);

  void save(LogicalMeter logicalMeter);

  void deleteAll();
}
