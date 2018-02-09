package com.elvaco.mvp.database.entity.gateway;

import java.io.Serializable;
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
public class GatewayEntity implements Serializable {

  private static final long serialVersionUID = -2132372383987246715L;

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
