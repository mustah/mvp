package com.elvaco.mvp.core.usecase;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.Property;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PropertiesUseCases {

  private final AuthenticatedUser currentUser;
  private final Properties properties;

  public Property create(Property property) {
    return properties.save(property);
  }

  public void forceUpdateGeolocation(UUID entityId, UUID organisationId) {
    create(new Property(
      entityId,
      organisationId,
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    ));
  }

  public boolean shouldUpdateGeolocation(UUID entityId, UUID organisationId) {
    return findBy(FeatureType.UPDATE_GEOLOCATION, entityId, organisationId).isPresent();
  }

  public Optional<Property> findBy(FeatureType feature, UUID entityId, UUID organisationId) {
    return findById(Property.idOf(entityId, organisationId, feature.key));
  }

  public Optional<Property> findById(Property.Id id) {
    return properties.findById(id);
  }

  boolean isEnabled(FeatureType feature, UUID entityId) {
    return isEnabled(Property.idOf(entityId, currentUser.getOrganisationId(), feature.key));
  }

  boolean isEnabled(Property.Id id) {
    return findById(id)
      .map(p -> Boolean.parseBoolean(p.value))
      .orElse(false);
  }

  public void deleteBy(FeatureType feature, UUID entityIds, UUID organisationId) {
    properties.deleteById(Property.idOf(entityIds, organisationId, feature.key));
  }

  public void deleteById(Property.Id id) {
    try {
      properties.deleteById(id);
    } catch (Exception e) {
      log.warn("Unable to find property with id: {}", id, e);
    }
  }
}
