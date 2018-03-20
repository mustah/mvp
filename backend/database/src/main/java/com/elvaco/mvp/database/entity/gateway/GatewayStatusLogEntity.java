package com.elvaco.mvp.database.entity.gateway;

import java.time.ZonedDateTime;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.EntityType;
import com.elvaco.mvp.database.entity.meter.StatusEntity;
import lombok.ToString;

@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateway_status_log")
public class GatewayStatusLogEntity extends EntityType<Long> {

  private static final long serialVersionUID = -365050443321687201L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public UUID gatewayId;

  public ZonedDateTime start;

  public ZonedDateTime stop;

  @ManyToOne
  public StatusEntity status;

  @Override
  public Long getId() {
    return id;
  }
}
