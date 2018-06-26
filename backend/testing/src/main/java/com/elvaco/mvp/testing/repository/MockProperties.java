package com.elvaco.mvp.testing.repository;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.Property;
import com.elvaco.mvp.core.domainmodels.Property.Id;
import com.elvaco.mvp.core.exception.PropertyNotFound;
import com.elvaco.mvp.core.spi.repository.Properties;

public class MockProperties extends MockRepository<Property.Id, Property> implements Properties {

  @Override
  public Property save(Property property) {
    return saveMock(property);
  }

  @Override
  public Optional<Property> findById(Property.Id id) {
    return filter(property -> property.getId().equals(
      Property.idOf(
        id.entityId,
        id.organisationId,
        id.key
      )))
      .findFirst();
  }

  @Override
  public void deleteById(Property.Id id) {
    findById(id)
      .map(property -> deleteMockById(id))
      .orElseThrow(() -> new PropertyNotFound(FeatureType.UPDATE_GEOLOCATION, id.entityId));
  }

  @Override
  protected Property copyWithId(Property.Id id, Property entity) {
    return new Property(id.entityId, id.organisationId, id.key, entity.value);
  }

  @Override
  protected Id generateId() {
    return null;
  }
}
