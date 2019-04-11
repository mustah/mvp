package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.util.StatusLogEntryHelper;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import static com.elvaco.mvp.core.util.Json.toJsonNode;
import static java.util.UUID.randomUUID;

@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Gateway implements Identifiable<UUID>, PrimaryKeyed {

  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  public final String ip;
  public final String phoneNumber;
  @Default
  public final JsonNode extraInfo = toJsonNode("{}");
  @Default
  public UUID id = randomUUID();
  @Singular
  public List<LogicalMeter> meters;
  @Singular
  public List<StatusLogEntry> statusLogs;

  @Override
  public UUID getId() {
    return id;
  }

  public StatusLogEntry currentStatus() {
    return statusLogs.stream()
      .findFirst()
      .orElseGet(() -> StatusLogEntry.unknownFor(this));
  }

  public Gateway replaceActiveStatus(StatusType status) {
    List<StatusLogEntry> newStatuses = StatusLogEntryHelper.replaceActiveStatus(
      List.copyOf(statusLogs),
      StatusLogEntry.builder()
        .primaryKey(primaryKey())
        .status(status)
        .start(ZonedDateTime.now())
        .build()
    );
    this.statusLogs = new ArrayList<>();
    return toBuilder().statusLogs(newStatuses).build();
  }

  @Override
  public PrimaryKey primaryKey() {
    return new Pk(id, organisationId);
  }
}
