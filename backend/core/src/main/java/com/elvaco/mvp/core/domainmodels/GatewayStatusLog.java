package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

import static java.util.UUID.randomUUID;

@EqualsAndHashCode
public class GatewayStatusLog {

  public static final GatewayStatusLog NULL_OBJECT =
    new GatewayStatusLog(
      null,
      randomUUID(),
      StatusType.UNKNOWN,
      ZonedDateTime.now(),
      null
    );

  @Nullable
  public final Long id;
  public final UUID gatewayId;
  public final StatusType status;
  public final ZonedDateTime start;
  @Nullable
  public final ZonedDateTime stop;

  public GatewayStatusLog(
    @Nullable Long id,
    UUID gatewayId,
    StatusType status,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    this.id = id;
    this.gatewayId = gatewayId;
    this.status = status;
    this.start = start;
    this.stop = stop;
  }
}
