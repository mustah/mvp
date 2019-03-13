package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Dashboard implements Identifiable<UUID> {

  public final UUID id;
  public final UUID ownerUserId;
  public final UUID organisationId;
  public final String name;
  public final JsonNode layout;

  @Singular
  public final List<Widget> widgets;

  @Override
  public UUID getId() {
    return id;
  }
}
