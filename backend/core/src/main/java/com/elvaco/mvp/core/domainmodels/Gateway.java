package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.util.StatusLogEntryHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

import static java.util.UUID.randomUUID;

@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Gateway implements Identifiable<UUID> {

  @Default
  public UUID id = randomUUID();
  public final UUID organisationId;
  public final String serial;
  public final String productModel;
  @Singular
  public List<LogicalMeter> meters;
  @Singular
  public List<StatusLogEntry<UUID>> statusLogs;

  @Override
  public UUID getId() {
    return id;
  }

  public StatusLogEntry<UUID> currentStatus() {
    return statusLogs.stream()
      .findFirst()
      .orElseGet(() -> StatusLogEntry.unknownFor(this));
  }

  public Gateway replaceActiveStatus(StatusType status) {
    List<StatusLogEntry<UUID>> newStatuses = StatusLogEntryHelper.replaceActiveStatus(
      statusLogs,
      StatusLogEntry.<UUID>builder().entityId(id).status(status).start(ZonedDateTime.now()).build()
    );
    this.statusLogs = new ArrayList<>();
    return toBuilder().statusLogs(newStatuses).build();
  }
}
