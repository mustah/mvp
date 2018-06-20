package com.elvaco.mvp.database.entity.meter;

import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@NoArgsConstructor
@Entity
@Table(name = "location")
@Access(AccessType.FIELD)
@Audited
public class LocationEntity extends IdentifiableType<UUID> {

  private static final long serialVersionUID = -6244183552379157552L;

  @Id
  @Column(name = "logical_meter_id")
  public UUID logicalMeterId;

  @NotAudited
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "logical_meter_id", insertable = false, updatable = false)
  public LogicalMeterEntity logicalMeter;

  public String country;
  public String city;
  public String streetAddress;
  public Double latitude;
  public Double longitude;
  public Double confidence;

  public LocationEntity(
    UUID logicalMeterId,
    Double latitude,
    Double longitude,
    Double confidence
  ) {
    this.logicalMeterId = logicalMeterId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.confidence = confidence;
  }

  LocationEntity(UUID logicalMeterId) {
    this.logicalMeterId = logicalMeterId;
  }

  public LocationEntity(
    UUID logicalMeterId,
    String country,
    String city,
    String address
  ) {
    this.logicalMeterId = logicalMeterId;
    this.country = country;
    this.city = city;
    this.streetAddress = address;
  }

  public boolean hasCoordinates() {
    return latitude != null && longitude != null && confidence != null;
  }

  @Override
  public UUID getId() {
    return logicalMeterId;
  }
}
