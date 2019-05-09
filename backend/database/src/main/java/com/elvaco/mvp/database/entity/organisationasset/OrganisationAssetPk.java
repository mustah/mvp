package com.elvaco.mvp.database.entity.organisationasset;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinTable;

import com.elvaco.mvp.core.domainmodels.AssetType;

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
public class OrganisationAssetPk implements Serializable {

  private static final long serialVersionUID = -2066177018943218418L;

  @Column(nullable = false, updatable = false)
  @JoinTable
  public UUID organisationId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, updatable = false)
  public AssetType assetType;
}
