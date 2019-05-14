package com.elvaco.mvp.database.entity.organisationtheme;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.envers.Audited;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Builder
@Audited
@Entity
@Table(name = "organisation_theme")
public class OrganisationThemeEntity implements Serializable {

  private static final long serialVersionUID = -2023484719236771818L;

  @EmbeddedId
  public OrganisationThemePk organisationThemePk;

  @Column(nullable = false)
  public String value;
}
