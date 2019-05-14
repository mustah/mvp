package com.elvaco.mvp.database.entity.organisationtheme;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Builder
@Embeddable
public class OrganisationThemePk implements Serializable {

  private static final long serialVersionUID = 29314497195502576L;

  @Column(nullable = false, updatable = false)
  @JoinTable
  public UUID organisationId;

  @Column(nullable = false, updatable = false)
  public String property;
}
