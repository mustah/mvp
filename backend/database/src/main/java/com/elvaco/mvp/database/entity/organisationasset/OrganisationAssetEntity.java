package com.elvaco.mvp.database.entity.organisationasset;

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
@Table(name = "organisation_asset")
public class OrganisationAssetEntity implements Serializable {

  private static final long serialVersionUID = -4395950267764561717L;

  @EmbeddedId
  public OrganisationAssetPk organisationAssetPk;

  @Column(nullable = false)
  public String contentType;

  @Column(nullable = false)
  public byte[] content;
}
