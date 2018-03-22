package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class GatewayStatusLog {

  @Nullable
  public final Long id;
  public final UUID gatewayId;
  public final UUID organisationId;
  public final long statusId;
  public final String name;

  public final ZonedDateTime start;
  @Nullable
  public final ZonedDateTime stop;

  public GatewayStatusLog(
    @Nullable Long id,
    UUID gatewayId,
    UUID organisationId,
    long statusId,
    String name,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    this.id = id;
    this.gatewayId = gatewayId;
    this.organisationId = organisationId;
    this.statusId = statusId;
    this.name = name;
    this.start = ZonedDateTime.ofInstant(start.toInstant(), start.getZone());
    this.stop = stop != null ? ZonedDateTime.ofInstant(stop.toInstant(), stop.getZone()) : null;
  }
}
