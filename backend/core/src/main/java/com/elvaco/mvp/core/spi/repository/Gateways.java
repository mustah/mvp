package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;

public interface Gateways {

  List<Gateway> findAll();

  List<Gateway> findAllByOrganisationId(UUID organisationId);

  Gateway save(Gateway gateway);
}
