package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.dto.GatewaySummaryDto;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.gateway.GatewayEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GatewayJpaRepository {

  <S extends GatewayEntity> S save(S entity);

  void deleteAll();

  List<GatewayEntity> findAll();

  Page<GatewaySummaryDto> findAll(RequestParameters parameters, Pageable pageable);

  Page<String> findSerials(RequestParameters parameters, Pageable pageable);

  List<GatewayEntity> findAllByOrganisationId(UUID organisationId);

  Optional<GatewayEntity> findByOrganisationIdAndProductModelAndSerial(
    UUID organisationId,
    String productModel,
    String serial
  );

  Optional<GatewayEntity> findByOrganisationIdAndSerial(UUID organisationId, String serial);

  List<GatewayEntity> findBySerial(String serial);

  Optional<GatewayEntity> findById(UUID id);

  Optional<GatewayEntity> findByOrganisationIdAndId(UUID organisationId, UUID id);
}
