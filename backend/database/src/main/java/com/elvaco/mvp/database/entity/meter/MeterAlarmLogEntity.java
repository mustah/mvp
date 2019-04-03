package com.elvaco.mvp.database.entity.meter;

import java.time.ZonedDateTime;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.domainmodels.PrimaryKeyed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "meter_alarm_log",
  uniqueConstraints = @UniqueConstraint(columnNames = {"physicalMeterId", "mask"})
)
public class MeterAlarmLogEntity extends IdentifiableType<Long> implements PrimaryKeyed {

  private static final long serialVersionUID = 4500720595207523427L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public PhysicalMeterPk pk;

  @Column(nullable = false)
  public Integer mask;

  @Column(nullable = false)
  public ZonedDateTime start;

  @Column(nullable = false)
  public ZonedDateTime lastSeen;

  @Nullable
  public ZonedDateTime stop;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public PrimaryKey primaryKey() {
    return pk;
  }
}
