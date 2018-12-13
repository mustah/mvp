package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.meter.QPhysicalMeterEntity.physicalMeterEntity;

@Repository
class PhysicalMeterQueryDslJpaRepository
  extends BaseQueryDslRepository<PhysicalMeterEntity, UUID>
  implements PhysicalMeterJpaRepository {

  @Autowired
  PhysicalMeterQueryDslJpaRepository(EntityManager entityManager) {
    super(entityManager, PhysicalMeterEntity.class);
  }

  @Override
  public Optional<PhysicalMeterEntity> findById(UUID id) {
    return Optional.ofNullable(fetchOne(physicalMeterEntity.id.eq(id)));
  }

  @Override
  public List<PhysicalMeterEntity> findByMedium(String medium) {
    Predicate predicate = physicalMeterEntity.medium.eq(medium);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public Optional<PhysicalMeterEntity> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  ) {
    Predicate predicate = physicalMeterEntity.logicalMeterPk.organisationId.eq(organisationId)
      .and(physicalMeterEntity.externalId.eq(externalId))
      .and(physicalMeterEntity.address.eq(address));
    return Optional.ofNullable(fetchOne(predicate));
  }

  private PhysicalMeterEntity fetchOne(Predicate predicate) {
    return createQuery(predicate).select(path).fetchOne();
  }
}
