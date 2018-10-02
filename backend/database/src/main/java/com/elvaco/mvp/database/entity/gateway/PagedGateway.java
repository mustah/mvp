package com.elvaco.mvp.database.entity.gateway;

import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import static java.util.Collections.emptySet;

@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PagedGateway implements Identifiable<UUID> {

  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  public final Set<LogicalMeterEntity> meters;

  public PagedGateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel
  ) {
    this(id, organisationId, serial, productModel, emptySet());
  }

  @Override
  public UUID getId() {
    return id;
  }
}
