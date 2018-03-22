package com.elvaco.mvp.database.repository.access;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Status;
import com.elvaco.mvp.core.spi.repository.Statuses;
import com.elvaco.mvp.database.repository.jpa.StatusJpaRepository;
import com.elvaco.mvp.database.repository.mappers.StatusMapper;

import static java.util.stream.Collectors.toList;

public class StatusRepository implements Statuses {

  private final StatusJpaRepository statusJpaRepository;
  private final StatusMapper statusMapper;

  public StatusRepository(
    StatusJpaRepository statusJpaRepository,
    StatusMapper statusMapper
  ) {
    this.statusJpaRepository = statusJpaRepository;
    this.statusMapper = statusMapper;
  }

  @Override
  public List<Status> findAll() {
    return statusJpaRepository.findAll()
      .stream()
      .map(statusMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public void save(List<Status> statuses) {
    statusJpaRepository.save(
      statuses.stream().map(statusMapper::toEntity).collect(toList())
    );
  }
}
