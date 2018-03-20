package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Status;

public interface Statuses {
  List<Status> findAll();

  void save(List<Status> statuses);
}
