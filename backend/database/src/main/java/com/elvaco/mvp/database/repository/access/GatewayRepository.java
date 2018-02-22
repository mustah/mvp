package com.elvaco.mvp.database.repository.access;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;

import static java.util.stream.Collectors.toList;

public class GatewayRepository implements Gateways {

  private final GatewayJpaRepository repository;

  public GatewayRepository(GatewayJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<Gateway> findAll() {
    return repository.findAll()
      .stream()
      .map(e -> new Gateway(e.id, e.serial, e.productModel))
      .collect(toList());
  }
}
