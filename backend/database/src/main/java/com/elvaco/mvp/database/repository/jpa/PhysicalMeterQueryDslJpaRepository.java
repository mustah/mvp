package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;

import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    return Optional.ofNullable(fetchOne(PHYSICAL_METER.id.eq(id)));
  }

  @Override
  public List<PhysicalMeterEntity> findByMedium(String medium) {
    Predicate predicate = PHYSICAL_METER.medium.eq(medium);
    return createQuery(predicate).select(path).fetch();
  }

  @Override
  public Optional<PhysicalMeterEntity> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  ) {
    Predicate predicate = PHYSICAL_METER.logicalMeterPk.organisationId.eq(organisationId)
      .and(PHYSICAL_METER.externalId.eq(externalId))
      .and(PHYSICAL_METER.address.eq(address));
    return Optional.ofNullable(fetchOne(predicate));
  }

  private PhysicalMeterEntity fetchOne(Predicate predicate) {
    return createQuery(predicate).select(path).fetchOne();
  }
}
