package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.database.entity.property.PropertyEntity;
import com.elvaco.mvp.database.entity.property.PropertyPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertiesJpaRepository extends JpaRepository<PropertyEntity, PropertyPk> {

  List<PropertyEntity> findAllById_Key(String key);

  @Override
  Optional<PropertyEntity> findById(PropertyPk id);
}
