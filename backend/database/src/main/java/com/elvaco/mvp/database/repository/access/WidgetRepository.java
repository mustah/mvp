package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Widget;
import com.elvaco.mvp.core.spi.repository.Widgets;
import com.elvaco.mvp.database.repository.jpa.WidgetJpaRepository;
import com.elvaco.mvp.database.repository.mappers.WidgetEntityMapper;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.database.repository.mappers.WidgetEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.WidgetEntityMapper.toEntity;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class WidgetRepository implements Widgets {

  private final WidgetJpaRepository widgetJpaRepository;

  @Override
  public List<Widget> findAll() {
    return widgetJpaRepository.findAll().stream()
      .map(WidgetEntityMapper::toDomainModel)
      .toList();
  }

  @Override
  public List<Widget> findByDashboardIdAndOwnerUserIdAndOrganisationId(
    UUID dashboardId, UUID ownerUserId, UUID organisationId
  ) {
    return widgetJpaRepository.findByDashboardIdAndOwnerUserIdAndOrganisationId(
      dashboardId,
      ownerUserId,
      organisationId
    ).stream().map(WidgetEntityMapper::toDomainModel).toList();
  }

  @Override
  public Optional<Widget> findByIdAndOwnerUserIdAndOrganisationId(
    UUID widgetId, UUID ownerUserId, UUID organisationId
  ) {
    return widgetJpaRepository.findByIdAndOwnerUserIdAndOrganisationId(
      widgetId,
      ownerUserId,
      organisationId
    ).map(WidgetEntityMapper::toDomainModel);
  }

  @Override
  public Widget save(Widget widget) {
    return toDomainModel(widgetJpaRepository.save(toEntity(widget)));
  }

  @Override
  public void deleteById(UUID id) {
    widgetJpaRepository.deleteById(id);
  }
}
