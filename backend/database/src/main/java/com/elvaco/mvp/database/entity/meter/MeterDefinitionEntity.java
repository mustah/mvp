package com.elvaco.mvp.database.entity.meter;

import java.util.HashSet;
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

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Entity
@Access(AccessType.FIELD)
@Table(name = "meter_definition")
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class MeterDefinitionEntity extends IdentifiableType<MeterDefinitionType> {

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
  public Set<QuantityEntity> quantities = new HashSet<>();

  public String medium;

  public boolean systemOwned;

  @Override
  public MeterDefinitionType getId() {
    return type;
  }
}
