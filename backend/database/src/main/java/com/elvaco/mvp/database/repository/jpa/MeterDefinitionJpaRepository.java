package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.MediumEntity;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeterDefinitionJpaRepository
  extends JpaRepository<MeterDefinitionEntity, Long> {

  Optional<MeterDefinitionEntity> findByMediumAndOrganisationIsNull(MediumEntity mediumEntity);

  List<MeterDefinitionEntity> findByOrganisationIdOrOrganisationIsNull(UUID organisationId);
}
