package com.elvaco.mvp.database.entity.user;

import java.util.Set;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.EntityType;
import lombok.ToString;

import static java.util.Collections.emptySet;

@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "organisation")
public class OrganisationEntity extends EntityType<Long> {

  private static final long serialVersionUID = -2369738291498443749L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;
  public String name;
  public String code;

  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "organisation")
  public Set<UserEntity> users;

  OrganisationEntity() {}

  public OrganisationEntity(String name, String code) {
    this(null, name, code);
  }

  public OrganisationEntity(Long id, String name, String code) {
    this.id = id;
    this.name = name;
    this.code = code;
    this.users = emptySet();
  }

  @Override
  public Long getId() {
    return id;
  }
}
