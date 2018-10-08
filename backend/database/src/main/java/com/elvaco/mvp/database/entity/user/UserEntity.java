package com.elvaco.mvp.database.entity.user;

import java.util.Collection;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.Language;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "mvp_user")
@Audited
public class UserEntity extends IdentifiableType<UUID> {

  private static final long serialVersionUID = -3697251067617203364L;

  @Id
  public UUID id;

  @Column(nullable = false)
  public String name;

  @Email
  @Column(unique = true, nullable = false)
  public String email;

  @NotAudited
  @Column(nullable = false)
  public String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public Language language;

  @ManyToOne(optional = false)
  @JoinColumn(name = "organisation_id", nullable = false)
  public OrganisationEntity organisation;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
    name = "users_roles",
    joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role")
  )
  public Collection<RoleEntity> roles;

  @Override
  public UUID getId() {
    return id;
  }
}
