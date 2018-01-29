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

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.USER;

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

  public static RoleEntity user() {
    return new RoleEntity(USER.role);
  }

  public static RoleEntity admin() {
    return new RoleEntity(ADMIN.role);
  }

  public static RoleEntity superAdmin() {
    return new RoleEntity(SUPER_ADMIN.role);
  }

  @Override
  public String toString() {
    return "RoleEntity{"
           + "role='"
           + role
           + '\''
           + '}';
  }
}
