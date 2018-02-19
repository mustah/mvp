package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "meter_definition")
public class MeterDefinitionEntity implements Serializable {

  private static final long serialVersionUID = -8819531921424251045L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
    name = "meter_definition_quantities",
    joinColumns = @JoinColumn(name = "meter_definition_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "quantity_id", referencedColumnName = "id")
  )
  public Set<QuantityEntity> quantities;

  public String medium;

  public boolean systemOwned;

  public MeterDefinitionEntity() {}

  public MeterDefinitionEntity(
    Long id,
    Set<QuantityEntity> quantities,
    String medium,
    boolean systemOwned
  ) {
    this.id = id;
    this.quantities = quantities;
    this.medium = medium;
    this.systemOwned = systemOwned;
  }
}
