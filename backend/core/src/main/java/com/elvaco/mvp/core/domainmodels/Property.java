package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class Property extends IdentifiableType<Property.Id> {

  public final UUID entityId;
  public final UUID organisationId;
  public final String key;
  public final String value;

  public static Property.Id idOf(UUID entityId, UUID organisationId, String key) {
    return new Property.Id(entityId, organisationId, key);
  }

  @Override
  public Id getId() {
    return idOf(entityId, organisationId, key);
  }

  @ToString
  @EqualsAndHashCode
  public static class Id {

    public final UUID entityId;
    public final UUID organisationId;
    public final String key;

    private Id(UUID entityId, UUID organisationId, String key) {
      this.entityId = entityId;
      this.organisationId = organisationId;
      this.key = key;
    }
  }
}
