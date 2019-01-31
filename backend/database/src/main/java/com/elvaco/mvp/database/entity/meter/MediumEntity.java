package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
@Table(name = "medium")
@Audited
public class MediumEntity extends IdentifiableType<Long> {

  private static final long serialVersionUID = 2942332009835061326L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @NaturalId
  public String name;

  @Override
  public Long getId() {
    return id;
  }
}
