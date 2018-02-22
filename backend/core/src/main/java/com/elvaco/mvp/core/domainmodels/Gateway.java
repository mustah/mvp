package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

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

  public Gateway(
    @Nullable Long id,
    String serial,
    String productModel,
    @Nullable String phoneNumber,
    @Nullable String port,
    @Nullable String ip
  ) {
    this.id = id;
    this.serial = serial;
    this.productModel = productModel;
    this.phoneNumber = phoneNumber;
    this.port = port;
    this.ip = ip;
  }
}
