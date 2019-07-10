package com.elvaco.mvp.database.entity.gateway;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateways_meters")
public class GatewayMeterEntity implements Serializable {

  @EmbeddedId
  public GatewayMeterPk pk;

  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
  @JoinColumns(
    value = {@JoinColumn(name = "gateway_id", referencedColumnName = "id"),
             @JoinColumn(name = "organisation_id", referencedColumnName = "organisationId")})
  @MapsId("gateway_id")
  public GatewayEntity gateway;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumns(
    value = {@JoinColumn(name = "logical_meter_id", referencedColumnName = "id"),
             @JoinColumn(name = "organisation_id", referencedColumnName = "organisationId")})
  @MapsId("logical_meter_id")
  public LogicalMeterEntity logicalMeter;

  @Column
  public ZonedDateTime created;

  @Column
  public ZonedDateTime lastSeen;

  public GatewayMeterEntity(
    GatewayEntity gatewayEntity,
    LogicalMeterEntity logicalMeterEntity,
    ZonedDateTime created,
    ZonedDateTime lastSeen
  ) {
    this.gateway = gatewayEntity;
    this.logicalMeter = logicalMeterEntity;
    this.created = created;
    this.lastSeen = lastSeen;
    this.pk = new GatewayMeterPk(gateway.pk.organisationId, gateway.pk.id, logicalMeter.pk.id);
  }
}
