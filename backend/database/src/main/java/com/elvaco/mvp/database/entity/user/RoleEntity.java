package com.elvaco.mvp.database.entity.user;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;

import static com.elvaco.mvp.core.domainmodels.Role.MVP_ADMIN;
import static com.elvaco.mvp.core.domainmodels.Role.MVP_USER;
import static com.elvaco.mvp.core.domainmodels.Role.SUPER_ADMIN;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Audited
@Entity
@Access(AccessType.FIELD)
@Table(name = "role")
public class RoleEntity extends IdentifiableType<String> {

  private static final long serialVersionUID = -4988854057995751873L;

  @Id
  public String role;

  public static RoleEntity mvpUser() {
    return new RoleEntity(MVP_USER.role);
  }

  public static RoleEntity mvpAdmin() {
    return new RoleEntity(MVP_ADMIN.role);
  }

  public static RoleEntity superAdmin() {
    return new RoleEntity(SUPER_ADMIN.role);
  }

  @Override
  public String getId() {
    return role;
  }
}
