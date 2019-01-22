package com.elvaco.mvp.testdata;

import java.time.ZonedDateTime;

import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import com.elvaco.mvp.core.domainmodels.StatusType;

import lombok.RequiredArgsConstructor;

/**
 * ContextDSL requires specific types to overload given() when specifying fixtures.
 * StatusLogEntryBuilder is contained by both meters and gateways, but the implementation must
 * save logs for either a meter or a gateway, hence this decorating type.
 */
@RequiredArgsConstructor
public class GatewayStatusLogEntryBuilderDelegate {
  private final StatusLogEntry.StatusLogEntryBuilder statusLogEntryBuilder;

  public GatewayStatusLogEntryBuilderDelegate status(StatusType statusType) {
    statusLogEntryBuilder.status(statusType);
    return this;
  }

  public GatewayStatusLogEntryBuilderDelegate primaryKey(PrimaryKey primaryKey) {
    statusLogEntryBuilder.primaryKey(primaryKey);
    return this;
  }

  public GatewayStatusLogEntryBuilderDelegate start(ZonedDateTime start) {
    statusLogEntryBuilder.start(start);
    return this;
  }

  public GatewayStatusLogEntryBuilderDelegate stop(ZonedDateTime stop) {
    statusLogEntryBuilder.stop(stop);
    return this;
  }

  public GatewayStatusLogEntryBuilderDelegate id(Long id) {
    statusLogEntryBuilder.id(id);
    return this;
  }

  public StatusLogEntry build() {
    return statusLogEntryBuilder.build();
  }
}
