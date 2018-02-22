package com.elvaco.mvp.core.domainmodels;

import javax.annotation.Nullable;

public class Gateway {

  @Nullable
  public final Long id;
  public final String serial;
  public final String productModel;

  public Gateway(@Nullable Long id, String serial, String productModel) {
    this.id = id;
    this.serial = serial;
    this.productModel = productModel;
  }
}
