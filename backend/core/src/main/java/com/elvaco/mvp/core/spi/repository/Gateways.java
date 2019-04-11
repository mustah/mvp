package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.dto.GatewaySummaryDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface Gateways {

  List<Gateway> findAll();

  Page<GatewaySummaryDto> findAll(RequestParameters requestParameters, Pageable pageable);

  Gateway save(Gateway gateway);

  Optional<Gateway> findBy(
    UUID organisationId,
    String productModel,
    String serial
  );

  Optional<Gateway> findBy(UUID organisationId, String serial);

  List<Gateway> findBy(String serial);

  Optional<Gateway> findById(UUID id);

  Optional<Gateway> findByOrganisationIdAndId(UUID organisationId, UUID id);

  Page<String> findSerials(RequestParameters parameters, Pageable pageable);
}
