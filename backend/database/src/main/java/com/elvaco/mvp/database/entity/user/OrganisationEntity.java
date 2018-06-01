package com.elvaco.mvp.database.entity.user;

import java.util.Set;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import lombok.ToString;
import org.hibernate.envers.Audited;

import static java.util.Collections.emptySet;

@ToString(exclude = "users")
@Entity
@Access(AccessType.FIELD)
@Table(name = "organisation")
@Audited
public class OrganisationEntity extends IdentifiableType<UUID> {

  private static final long serialVersionUID = -2369738291498443749L;

  @Id
  public UUID id;
  public String name;
  public String slug;
  public String externalId;

  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "organisation")
  public Set<UserEntity> users;

  OrganisationEntity() {}

  public OrganisationEntity(UUID id, String name, String slug, String externalId) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.externalId = externalId;
    this.users = emptySet();
  }

  @Override
  public UUID getId() {
    return id;
  }
}
