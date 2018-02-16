package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterStatus;

public interface MeterStatuses {
  List<MeterStatus> findAll();

  void save(List<MeterStatus> meterStatus);
}
