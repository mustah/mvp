package com.elvaco.mvp.database.entity.meter;

import java.io.Serializable;
import java.util.Date;
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

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter_status_log")
public class PhysicalMeterStatusLogEntity implements Serializable {
  private static final long serialVersionUID = -365050103321687201L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long physicalMeterId;

  @Temporal(value = TemporalType.TIMESTAMP)
  public Date start;
  @Temporal(value = TemporalType.TIMESTAMP)
  public Date stop;

  @ManyToOne
  public MeterStatusEntity status;
}
