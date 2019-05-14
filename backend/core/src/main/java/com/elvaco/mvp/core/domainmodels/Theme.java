package com.elvaco.mvp.core.domainmodels;

import java.util.Map;
import java.util.UUID;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Theme {

  public final UUID organisationId;

  @Singular
  public Map<String, String> properties;
}
