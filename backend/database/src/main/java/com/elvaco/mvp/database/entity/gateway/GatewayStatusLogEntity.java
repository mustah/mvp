package com.elvaco.mvp.database.entity.gateway;

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

import com.elvaco.mvp.core.domainmodels.IdentifiableType;
import com.elvaco.mvp.core.domainmodels.PrimaryKey;
import com.elvaco.mvp.core.domainmodels.PrimaryKeyed;
import com.elvaco.mvp.core.domainmodels.StatusType;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateway_status_log")
public class GatewayStatusLogEntity extends IdentifiableType<Long> implements PrimaryKeyed {

  private static final long serialVersionUID = -365050443321687201L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public GatewayPk gatewayId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  public StatusType status;

  @Column(nullable = false)
  public ZonedDateTime start;

  public ZonedDateTime stop;

  public GatewayStatusLogEntity(
    @Nullable Long id,
    GatewayPk gatewayId,
    StatusType status,
    ZonedDateTime start,
    ZonedDateTime stop
  ) {
    this.id = id;
    this.gatewayId = gatewayId;
    this.status = status;
    this.start = start;
    this.stop = stop;
  }

  @Override
  public Long getId() {
    return id;
  }

  public UUID getGatewayId() {
    return gatewayId.getId();
  }

  public UUID getOrganisationId() {
    return gatewayId.getOrganisationId();
  }

  @Override
  public PrimaryKey primaryKey() {
    return gatewayId;
  }
}
