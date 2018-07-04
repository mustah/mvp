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
import javax.persistence.UniqueConstraint;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.StatusType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "physical_meter_status_log",
  uniqueConstraints = @UniqueConstraint(columnNames = {"physicalMeterId", "start", "status"})
)
public class PhysicalMeterStatusLogEntity extends IdentifiableType<Long> {

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

  @Nullable
  public ZonedDateTime stop;

  @Override
  public Long getId() {
    return id;
  }
}
