package com.elvaco.mvp.entity.user;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "user")
public class UserEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String name;
  public String email;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "organisationId")
  public OrganisationEntity company;

  public UserEntity() {}

  public UserEntity(String name, String email, OrganisationEntity company) {
    this.name = name;
    this.email = email;
    this.company = company;
  }
}
