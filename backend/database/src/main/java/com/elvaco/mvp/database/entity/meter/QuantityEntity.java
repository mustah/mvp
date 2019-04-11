package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.IdentifiableType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NaturalId;
import org.hibernate.envers.Audited;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Access(AccessType.FIELD)
@Table(name = "quantity")
@Audited
public class QuantityEntity extends IdentifiableType<Integer> {

  private static final long serialVersionUID = -8628799320716504900L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "INT")
  public Integer id;

  @NaturalId
  public String name;

  public String storageUnit;

  public DisplayMode storageMode;

  @Override
  public Integer getId() {
    return id;
  }
}
