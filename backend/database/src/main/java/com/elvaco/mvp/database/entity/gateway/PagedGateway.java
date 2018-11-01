package com.elvaco.mvp.database.entity.gateway;

import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder(toBuilder = true)
@RequiredArgsConstructor
public class PagedGateway implements Identifiable<UUID> {

  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  public final Set<LogicalMeterEntity> meters;
  public final Set<GatewayStatusLogEntity> statusLogs;

  @Override
  public UUID getId() {
    return id;
  }
}
