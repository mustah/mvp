package com.elvaco.mvp.entity.gateway;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.ToString;

@ToString
@Entity
@Access(AccessType.FIELD)
@Table(name = "gateway")
public class GatewayEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false)
  public String serial;

  @Column(nullable = false)
  public String productModel;

  public GatewayEntity() {}

  public GatewayEntity(String serial, String productModel) {
    this.serial = serial;
    this.productModel = productModel;
  }
}
