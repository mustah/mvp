package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.Quantities;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import com.elvaco.mvp.database.repository.jpa.QuantityJpaRepository;
import com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class QuantityRepository implements Quantities {

  private final QuantityJpaRepository quantityJpaRepository;
  private final QuantityEntityMapper quantityEntityMapper;

  @Override
  public List<Quantity> findAll() {
    return quantityJpaRepository.findAll().stream()
      .map(quantityEntityMapper::toDomainModel)
      .collect(toList());
  }

  @Override
  public Optional<Quantity> findByName(String quantity) {
    return quantityJpaRepository.findByName(quantity)
      .map(quantityEntityMapper::toDomainModel);
  }

  @Override
  public Quantity save(Quantity quantity) {
    QuantityEntity saved = quantityJpaRepository.save(quantityEntityMapper.toEntity(quantity));
    return quantityEntityMapper.toDomainModel(saved);
  }
}
