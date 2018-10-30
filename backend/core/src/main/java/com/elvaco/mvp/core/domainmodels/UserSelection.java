package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public class UserSelection {

  public final UUID id;
  public final UUID ownerUserId;
  public final UUID organisationId;
  public final String name;
  public final JsonNode selectionParameters;
}
