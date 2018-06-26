package com.elvaco.mvp.database.entity.property;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Embeddable
public class PropertyPk implements Serializable {

  private static final long serialVersionUID = 5500927183937651569L;

  @Column(nullable = false)
  public UUID entityId;

  @Column(nullable = false)
  public UUID organisationId;

  @Column(name = "property_key", nullable = false)
  public String key;
}
