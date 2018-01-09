package com.elvaco.mvp.entity.user;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "role")
public class RoleEntity implements Serializable {

  @Id
  public String role;

  @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
  public Collection<UserEntity> users;

  public RoleEntity() {}

  public RoleEntity(String role) {
    this.role = role;
  }
}
