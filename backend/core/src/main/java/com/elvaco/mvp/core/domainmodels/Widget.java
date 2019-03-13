package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Widget implements Identifiable<UUID> {

  public final UUID id;
  public final UUID dashboardId;
  public final UUID ownerUserId;
  public final UUID organisationId;
  public final WidgetType type;
  public final String title;
  public final JsonNode settings;

  @Override
  public UUID getId() {
    return id;
  }
}
