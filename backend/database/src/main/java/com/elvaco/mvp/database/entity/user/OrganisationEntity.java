package com.elvaco.mvp.database.entity.user;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "organisation")
public class OrganisationEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String name;
  public String code;

  public OrganisationEntity() {}

  public OrganisationEntity(String name, String code) {
    this.name = name;
    this.code = code;
  }

  public OrganisationEntity(Long id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
  }
}
