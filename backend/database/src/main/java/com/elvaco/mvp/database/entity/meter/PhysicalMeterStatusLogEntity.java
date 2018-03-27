package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.StatusType;
import com.elvaco.mvp.database.entity.EntityType;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter_status_log")
public class PhysicalMeterStatusLogEntity extends EntityType<Long> {

  private static final long serialVersionUID = -365050103321687201L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public UUID physicalMeterId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public StatusType status;

  @Column(nullable = false)
  public ZonedDateTime start;

  public ZonedDateTime stop;

  public PhysicalMeterStatusLogEntity(
    @Nullable Long id,
    UUID physicalMeterId,
    StatusType status,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop
  ) {
    this.id = id;
    this.physicalMeterId = physicalMeterId;
    this.status = status;
    this.start = start;
    this.stop = stop;
  }

  public PhysicalMeterStatusLogEntity(
    Long id,
    UUID physicalMeterId,
    ZonedDateTime start,
    @Nullable ZonedDateTime stop,
    StatusType status
  ) {
    this.id = id;
    this.physicalMeterId = physicalMeterId;
    this.start = start;
    this.stop = stop;
    this.status = status;
  }

  @Override
  public Long getId() {
    return id;
  }
}
