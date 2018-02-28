package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;

public interface Gateways {

  List<Gateway> findAll();

  List<Gateway> findAllByOrganisationId(Long organisationId);

  Gateway save(Gateway gateway);
}
