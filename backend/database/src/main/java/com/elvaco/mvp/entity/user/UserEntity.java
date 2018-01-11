package com.elvaco.mvp.entity.user;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Email;

@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "user")
public class UserEntity implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public String name;

  @Email
  public String email;

  public String password;

  @ManyToOne(optional = false, fetch = FetchType.EAGER)
  @JoinColumn(name = "organisationId")
  public OrganisationEntity organisation;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "users_roles",
    joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role")
  )
  public Collection<RoleEntity> roles;

  public UserEntity() {}

  public UserEntity(
    String name,
    String email,
    String password,
    OrganisationEntity organisation,
    Collection<RoleEntity> roles
  ) {
    this(null, name, email, password, organisation, roles);
  }

  public UserEntity(
    Long id,
    String name,
    String email,
    String password,
    OrganisationEntity organisation,
    Collection<RoleEntity> roles
  ) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.password = password;
    this.organisation = organisation;
    this.roles = roles;
  }
}
