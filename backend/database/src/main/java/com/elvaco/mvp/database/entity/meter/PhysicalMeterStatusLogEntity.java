package com.elvaco.mvp.database.entity.meter;

import java.util.Date;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.elvaco.mvp.database.entity.EntityType;
import lombok.ToString;

@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter_status_log")
public class PhysicalMeterStatusLogEntity extends EntityType<Long> {

  private static final long serialVersionUID = -365050103321687201L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public UUID physicalMeterId;

  @Temporal(value = TemporalType.TIMESTAMP)
  public Date start;

  @Temporal(value = TemporalType.TIMESTAMP)
  public Date stop;

  @ManyToOne
  public MeterStatusEntity status;

  @Override
  public Long getId() {
    return id;
  }
}
