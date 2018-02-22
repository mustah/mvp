package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;

public interface Gateways {

  List<Gateway> findAll();

  Gateway save(Gateway gateway);
}
