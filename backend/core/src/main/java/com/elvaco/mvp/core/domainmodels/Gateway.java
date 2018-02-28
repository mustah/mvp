package com.elvaco.mvp.core.domainmodels;

import java.util.List;
import javax.annotation.Nullable;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public class Gateway implements Identifiable<Long> {

  public final Long id;
  public final Long organisationId;
  public final String serial;
  public final String productModel;
  public final List<LogicalMeter> meters;

  public Gateway(
    @Nullable Long id,
    Long organisationId,
    String serial,
    String productModel
  ) {
    this(id, organisationId, serial, productModel, emptyList());
  }

  public Gateway(
    @Nullable Long id,
    Long organisationId,
    String serial,
    String productModel,
    List<LogicalMeter> meters
  ) {
    this.id = id;
    this.organisationId = organisationId;
    this.serial = serial;
    this.productModel = productModel;
    this.meters = unmodifiableList(meters);
  }

  @Override
  public Long getId() {
    return id;
  }
}
