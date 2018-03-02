package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class Gateway implements Identifiable<UUID> {

  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  public final List<LogicalMeter> meters;

  public Gateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel
  ) {
    this(id, organisationId, serial, productModel, emptyList());
  }

  public Gateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel,
    List<LogicalMeter> meters
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.meters = unmodifiableList(meters);
  }

  @Override
  public UUID getId() {
    return id;
  }
}
