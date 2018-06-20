package com.elvaco.mvp.database.entity.gateway;

import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Identifiable;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import static java.util.Collections.emptySet;

public class PagedGateway implements Identifiable<UUID> {

  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  public final Set<LogicalMeterEntity> meters;

  private PagedGateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel,
    Set<LogicalMeterEntity> meters
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.meters = meters;
  }

  public PagedGateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel
  ) {
    this(id, organisationId, serial, productModel, emptySet());
  }

  public PagedGateway withMeters(Set<LogicalMeterEntity> meters) {
    return new PagedGateway(id, organisationId, serial, productModel, meters);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
