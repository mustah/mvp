package com.elvaco.mvp.entity.meter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class LogicalMeterEntity {
  @Id
  UUID moid;
  Long meterDefinition;
}
