package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Map;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;

public interface MeterStatusLogs {
  Page<MeterStatusLog> findAll(Map<String, List<String>> filterParams, Pageable pageable);

  void save(MeterStatusLog meterStatusLog);

  void save(List<MeterStatusLog> meterStatusLogs);
}
