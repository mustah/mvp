package com.elvaco.mvp.database.entity.measurement;

import java.time.ZonedDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Access(AccessType.FIELD)
@Entity
@Table(name = "measurement")
public class MeasurementEntity extends IdentifiableType<MeasurementPk> {

  private static final long serialVersionUID = -3650501037709018061L;

  @EmbeddedId
  public MeasurementPk id;

  @Type(type = "measurement-unit")
  @Column(nullable = false)
  public MeasurementUnit value;

  public MeasurementEntity(
    ZonedDateTime created,
    QuantityEntity quantity,
    MeasurementUnit value,
    PhysicalMeterEntity physicalMeter
  ) {
    this.id = new MeasurementPk(created, quantity, physicalMeter);
    this.value = value;
  }

  @Override
  public MeasurementPk getId() {
    return id;
  }
}
