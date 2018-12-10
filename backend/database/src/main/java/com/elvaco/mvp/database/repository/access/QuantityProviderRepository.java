package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.database.repository.jpa.QuantityProviderJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class QuantityProviderRepository {

  private final QuantityProviderJpaRepository quantityProviderJpaRepository;

  public List<QuantityEntity> findAllEntities() {
    return quantityProviderJpaRepository.findAll();
  }

  public Optional<QuantityEntity> findByName(String quantity) {
    return quantityProviderJpaRepository.findByName(quantity);
  }

  public QuantityEntity save(QuantityEntity quantity) {
    return quantityProviderJpaRepository.save(quantity);
  }
}
