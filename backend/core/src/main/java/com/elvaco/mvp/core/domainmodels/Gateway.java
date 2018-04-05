package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@ToString
@EqualsAndHashCode
public class Gateway implements Identifiable<UUID> {

  public final UUID id;
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  public final List<GatewayStatusLog> statusLogs;
  public final List<LogicalMeter> meters;

  public Gateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel
  ) {
    this(id, organisationId, serial, productModel, emptyList(), emptyList());
  }

  public Gateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel,
    List<LogicalMeter> meters,
    List<GatewayStatusLog> statusLogs
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.meters = unmodifiableList(meters);
    this.statusLogs = unmodifiableList(statusLogs);
  }

  @Override
  public UUID getId() {
    return id;
  }

  public Gateway withProductModel(String productModel) {
    return new Gateway(
      id,
      organisationId,
      serial,
      productModel,
      meters,
      statusLogs
    );
  }
}
