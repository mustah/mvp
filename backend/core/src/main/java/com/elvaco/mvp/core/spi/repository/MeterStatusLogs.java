package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterStatusLog;

public interface MeterStatusLogs {
  void save(MeterStatusLog meterStatusLog);

  void save(List<MeterStatusLog> meterStatusLogs);
}
