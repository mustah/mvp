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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;

@ToString(exclude = "users")
@Entity
@Access(AccessType.FIELD)
@Table(name = "organisation")
@Audited
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationEntity extends IdentifiableType<UUID> {

  private static final long serialVersionUID = -2369738291498443749L;

  @Id
  public UUID id;
  public String name;
  public String slug;
  public String externalId;
  public String shortPrefix;

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
  @Builder.Default
  public Set<UserEntity> users = new HashSet<>();

  @Override
  public UUID getId() {
    return id;
  }
}
