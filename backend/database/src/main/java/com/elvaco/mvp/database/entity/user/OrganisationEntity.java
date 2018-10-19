package com.elvaco.mvp.database.entity.user;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;

import static java.util.Collections.emptySet;

@NoArgsConstructor
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

  @OneToOne
  @JoinColumn(name = "parent_id")
  @Audited(modifiedColumnName = "parent_id_mod")
  public OrganisationEntity parent;

  @OneToOne
  @JoinTable(
    name = "organisation_user_selection",
    joinColumns = @JoinColumn(name = "organisation_id", referencedColumnName = "id")
  )
  public UserSelectionEntity selection;

  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "organisation")
  public Set<UserEntity> users = new HashSet<>();

  public OrganisationEntity(UUID id, String name, String slug, String externalId) {
    this.id = id;
    this.name = name;
    this.slug = slug;
    this.externalId = externalId;
    this.users = emptySet();
  }

  public OrganisationEntity(
    UUID id, String name, String slug, String externalId, OrganisationEntity parent
  ) {
    this(id, name, slug, externalId);
    this.parent = parent;
  }

  @Override
  public UUID getId() {
    return id;
  }
}
