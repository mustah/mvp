package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.database.entity.meter.MeterDefinitionEntity;
import com.elvaco.mvp.database.repository.jpa.DisplayQuantityJpaRepository;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.MediumEntityMapper;
import com.elvaco.mvp.database.repository.mappers.MeterDefinitionEntityMapper;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class MeterDefinitionRepository implements MeterDefinitions {

  private final MeterDefinitionJpaRepository meterDefinitionJpaRepository;
  private final DisplayQuantityJpaRepository displayQuantityJpaRepository;
  private final MeterDefinitionEntityMapper meterDefinitionEntityMapper;
  private final MediumEntityMapper mediumEntityMapper;

  @Override
  @Transactional
  public MeterDefinition save(MeterDefinition meterDefinition) {
    MeterDefinitionEntity meterDefinitionEntity = meterDefinitionJpaRepository.save(
      meterDefinitionEntityMapper.toEntity(
        meterDefinition));

    displayQuantityJpaRepository.deleteAllByPkMeterDefinitionId(meterDefinitionEntity.id);

    meterDefinitionEntity.quantities = meterDefinitionEntityMapper.toDisplayQuantityEntities(
      meterDefinition).stream()
      .peek(displayQuantity -> displayQuantity.pk.meterDefinitionId = meterDefinitionEntity.id)
      .map(displayQuantityJpaRepository::save)
      .collect(Collectors.toSet());

    return meterDefinitionEntityMapper.toDomainModel(meterDefinitionEntity);
  }

  @Override
  public Optional<MeterDefinition> findById(Long id) {
    return meterDefinitionJpaRepository.findById(id)
      .map(meterDefinitionEntityMapper::toDomainModel);
  }

  @Override
  public Optional<MeterDefinition> findSystemMeterDefinition(Medium medium) {
    return meterDefinitionJpaRepository.findByMediumAndOrganisationIsNull(
      mediumEntityMapper.toMediumEntity(medium)
    ).map(meterDefinitionEntityMapper::toDomainModel);
  }

  @Override
  public List<MeterDefinition> findAll(UUID organisationId) {
    return meterDefinitionJpaRepository.findByOrganisationIdOrOrganisationIsNull(organisationId)
      .stream()
      .map(meterDefinitionEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public List<MeterDefinition> findAll() {
    return meterDefinitionJpaRepository.findAll()
      .stream()
      .map(meterDefinitionEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public void deleteById(Long id) {
    meterDefinitionJpaRepository.deleteById(id);
  }
}
