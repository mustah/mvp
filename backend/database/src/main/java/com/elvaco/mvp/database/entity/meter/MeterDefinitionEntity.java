package com.elvaco.mvp.database.entity.meter;

import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.database.entity.EntityType;
import lombok.NoArgsConstructor;

@Entity
@Access(AccessType.FIELD)
@Table(name = "meter_definition")
@NoArgsConstructor
public class MeterDefinitionEntity extends EntityType<MeterDefinitionType> {

  private static final long serialVersionUID = -8819531921424251045L;

  @Id
  @Enumerated(EnumType.ORDINAL)
  public MeterDefinitionType type;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
    name = "meter_definition_quantities",
    joinColumns = @JoinColumn(name = "meter_definition_type", referencedColumnName = "type"),
    inverseJoinColumns = @JoinColumn(name = "quantity_id", referencedColumnName = "id")
  )
  public Set<QuantityEntity> quantities;

  public String medium;

  public boolean systemOwned;

  public MeterDefinitionEntity(
    MeterDefinitionType type,
    Set<QuantityEntity> quantities,
    String medium,
    boolean systemOwned
  ) {
    this.type = type;
    this.quantities = quantities;
    this.medium = medium;
    this.systemOwned = systemOwned;
  }

  @Override
  public MeterDefinitionType getId() {
    return type;
  }
}
