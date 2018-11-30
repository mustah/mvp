package com.elvaco.mvp.database.entity.meter;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Entity
@Access(AccessType.FIELD)
@Table(name = "location")
public class LocationEntity extends IdentifiableType<EntityPk> {

  private static final long serialVersionUID = -6244183552379157552L;

  @EmbeddedId
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "logical_meter_id")),
    @AttributeOverride(name = "organisationId", column = @Column(name = "organisation_id"))
  })
  public EntityPk pk;

  public String country;
  public String city;
  public String streetAddress;
  public Double latitude;
  public Double longitude;
  public Double confidence;

  @Override
  public EntityPk getId() {
    return pk;
  }

  public boolean hasCoordinates() {
    return latitude != null && longitude != null && confidence != null;
  }
}
