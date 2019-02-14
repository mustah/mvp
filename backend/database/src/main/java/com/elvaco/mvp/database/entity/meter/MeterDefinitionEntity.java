package com.elvaco.mvp.database.entity.meter;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

@Entity
@Access(AccessType.FIELD)
@Table(name = "meter_definition")
@NoArgsConstructor
@AllArgsConstructor
@Audited
public class MeterDefinitionEntity extends IdentifiableType<Long> {

  private static final long serialVersionUID = -8819531921424251045L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  @Audited(modifiedColumnName = "organisation_id_mod")
  public OrganisationEntity organisation;

  @OneToMany(
    mappedBy = "pk.meterDefinitionId",
    fetch = FetchType.EAGER
  )
  @Cascade(CascadeType.MERGE)
  public Set<DisplayQuantityEntity> quantities = new HashSet<>();

  public String name;

  @ManyToOne(optional = false)
  @JoinColumn(name = "medium_id", nullable = false)
  @Audited(modifiedColumnName = "medium_id_mod")
  public MediumEntity medium;

  public boolean autoApply;

  @Override
  public Long getId() {
    return id;
  }
}
