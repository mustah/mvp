package com.elvaco.mvp.database.repository.access;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Property;
import com.elvaco.mvp.core.spi.repository.Properties;
import com.elvaco.mvp.database.entity.property.PropertyEntity;
import com.elvaco.mvp.database.entity.property.PropertyPk;
import com.elvaco.mvp.database.repository.jpa.PropertiesJpaRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PropertiesRepository implements Properties {

  private final PropertiesJpaRepository propertiesJpaRepository;

  @Override
  public Property save(Property property) {
    PropertyEntity entity = propertiesJpaRepository.save(toEntity(property));
    return toDomainModel(entity);
  }

  @Override
  public Optional<Property> findById(Property.Id id) {
    return propertiesJpaRepository.findById(toPk(id))
      .map(PropertiesRepository::toDomainModel);
  }

  @Override
  public void deleteById(Property.Id id) {
    propertiesJpaRepository.deleteById(toPk(id));
  }

  private static PropertyPk toPk(Property.Id id) {
    return new PropertyPk(id.entityId, id.organisationId, id.key);
  }

  private static Property toDomainModel(PropertyEntity entity) {
    return new Property(entity.id.entityId, entity.id.organisationId, entity.id.key, entity.value);
  }

  private static PropertyEntity toEntity(Property property) {
    return new PropertyEntity(
      property.entityId,
      property.organisationId,
      property.key,
      property.value
    );
  }
}
