package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import javax.annotation.Nullable;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class Gateway {

  @Nullable
  public final Long id;
  public final String serial;
  public final String productModel;
  @Nullable
  public final String phoneNumber;
  @Nullable
  public final String port;
  @Nullable
  public final String ip;
  public final List<LogicalMeter> meters;

  public Gateway(
    @Nullable Long id,
    String serial,
    String productModel,
    @Nullable String phoneNumber,
    @Nullable String port,
    @Nullable String ip
  ) {
    this(id, serial, productModel, phoneNumber, port, ip, emptyList());
  }

  public Gateway(
    @Nullable Long id,
    String serial,
    String productModel,
    @Nullable String phoneNumber,
    @Nullable String port,
    @Nullable String ip,
    List<LogicalMeter> meters
  ) {
    this.id = id;
    this.serial = serial;
    this.productModel = productModel;
    this.phoneNumber = phoneNumber;
    this.port = port;
    this.ip = ip;
    this.meters = unmodifiableList(meters);
  }
}
