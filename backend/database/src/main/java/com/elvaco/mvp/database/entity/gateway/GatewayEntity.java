package com.elvaco.mvp.database.entity.gateway;

import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Collections.emptyList;

@EqualsAndHashCode
@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateway")
public class GatewayEntity implements Serializable {

  private static final long serialVersionUID = -2132372383987246715L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public String serial;

  @Column(nullable = false)
  public String productModel;

  @Column(nullable = false)
  public Long organisationId;

  @ManyToMany(mappedBy = "gateways")
  public List<LogicalMeterEntity> meters;

  GatewayEntity() {}

  public GatewayEntity(
    @Nullable Long id,
    Long organisationId,
    String serial,
    String productModel
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.meters = emptyList();
  }
}
