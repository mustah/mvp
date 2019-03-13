package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Dashboard;
import com.elvaco.mvp.core.spi.repository.Dashboards;
import com.elvaco.mvp.database.repository.jpa.DashboardJpaRepository;
import com.elvaco.mvp.database.repository.mappers.DashboardEntityMapper;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.database.repository.mappers.DashboardEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.DashboardEntityMapper.toEntity;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class DashboardRepository implements Dashboards {

  private final DashboardJpaRepository dashboardJpaRepository;

  @Override
  public List<Dashboard> findAll() {
    return dashboardJpaRepository.findAll().stream()
      .map(DashboardEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<Dashboard> findByIdAndOwnerUserIdAndOrganisationId(
    UUID dashboardId, UUID ownerUserId, UUID organisationId
  ) {
    return dashboardJpaRepository.findByIdAndOwnerUserIdAndOrganisationId(
      dashboardId,
      ownerUserId,
      organisationId
    ).map(DashboardEntityMapper::toDomainModel);
  }

  @Override
  public List<Dashboard> findByOwnerUserIdAndOrganisationId(
    UUID ownerUserId, UUID organisationId
  ) {
    return dashboardJpaRepository.findByOwnerUserIdAndOrganisationId(
      ownerUserId,
      organisationId
    ).stream()
      .map(DashboardEntityMapper::toDomainModel).collect(toList());
  }

  @Override
  public Dashboard save(Dashboard dashboard) {
    return toDomainModel(dashboardJpaRepository.save(toEntity(dashboard)));
  }

  @Override
  public void deleteById(UUID id) {
    dashboardJpaRepository.deleteById(id);
  }
}
