package com.elvaco.mvp.database.entity.gateway;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Access(AccessType.FIELD)
@Embeddable
public class GatewayMeterPk implements Serializable {

  private static final long serialVersionUID = 230553728890847907L;

  @Column(name = "organisation_id")
  public UUID organisationId;

  @Column(name = "gateway_id")
  public UUID gatewayId;

  @Column(name = "logical_meter_id")
  public UUID logicalMeterId;
}
