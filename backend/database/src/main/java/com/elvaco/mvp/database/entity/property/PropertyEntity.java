package com.elvaco.mvp.database.entity.property;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Access(AccessType.FIELD)
@Table(name = "property")
public class PropertyEntity extends IdentifiableType<PropertyPk> {

  private static final long serialVersionUID = -756658347835426979L;

  @EmbeddedId
  public PropertyPk id;

  @Column(name = "property_value", nullable = false)
  public String value;

  public PropertyEntity(UUID entityId, UUID organisationId, String key, String value) {
    this.id = new PropertyPk(entityId, organisationId, key);
    this.value = value;
  }

  @Override
  public PropertyPk getId() {
    return id;
  }
}
