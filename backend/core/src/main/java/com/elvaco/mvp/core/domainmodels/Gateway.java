package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.util.StatusLogEntryHelper;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.UUID.randomUUID;

@Builder
@ToString
@EqualsAndHashCode
public class Gateway implements Identifiable<UUID>, Serializable {

  private static final long serialVersionUID = 7972281400092648431L;

  @Default
  public UUID id = randomUUID();
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  @Singular
  public List<LogicalMeter> meters;
  @Singular
  public List<StatusLogEntry<UUID>> statusLogs;

  public Gateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel
  ) {
    this(
      id,
      organisationId,
      serial,
      productModel,
      emptyList(),
      emptyList()
    );
  }

  private Gateway(
    UUID id,
    UUID organisationId,
    String serial,
    String productModel,
    List<LogicalMeter> meters,
    List<StatusLogEntry<UUID>> statusLogs
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

  public StatusLogEntry<UUID> currentStatus() {
    return statusLogs.stream()
      .findFirst()
      .orElseGet(() -> StatusLogEntry.unknownFor(this));
  }

  public Gateway replaceActiveStatus(StatusType status) {
    return replaceActiveStatus(status, ZonedDateTime.now());
  }

  private Gateway replaceActiveStatus(StatusType status, ZonedDateTime when) {
    List<StatusLogEntry<UUID>> newStatuses = StatusLogEntryHelper.replaceActiveStatus(
      statusLogs,
      new StatusLogEntry<>(id, status, when)
    );

    return new Gateway(
      id,
      organisationId,
      serial,
      productModel,
      meters,
      newStatuses
    );
  }
}
