package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.jooq.tables.PhysicalMeter.PHYSICAL_METER;

@Repository
class PhysicalMeterJooqJpaRepository
  extends BaseJooqRepository<PhysicalMeterEntity, UUID>
  implements PhysicalMeterJpaRepository {

  private final DSLContext dsl;

  @Autowired
  PhysicalMeterJooqJpaRepository(EntityManager entityManager, DSLContext dsl) {
    super(entityManager, PhysicalMeterEntity.class);
    this.dsl = dsl;
  }

  @Override
  public Optional<PhysicalMeterEntity> findById(UUID id) {
    return fetchOne(PHYSICAL_METER.ID.equal(id));
  }

  @Override
  public List<PhysicalMeterEntity> findByMedium(String medium) {
    return nativeQuery(dsl.select().from(PHYSICAL_METER)
      .where(PHYSICAL_METER.MEDIUM.equal(medium)));
  }

  @Override
  public List<PhysicalMeterEntity> findByOrganisationIdAndExternalId(
    UUID organisationId,
    String externalId
  ) {
    return nativeQuery(dsl.select().from(PHYSICAL_METER)
      .where(PHYSICAL_METER.ORGANISATION_ID.equal(organisationId))
      .and(PHYSICAL_METER.EXTERNAL_ID.equal(externalId)));
  }

  @Override
  public Optional<PhysicalMeterEntity> findByOrganisationIdAndExternalIdAndAddress(
    UUID organisationId,
    String externalId,
    String address
  ) {
    return fetchOne(PHYSICAL_METER.ORGANISATION_ID.equal(organisationId)
      .and(PHYSICAL_METER.EXTERNAL_ID.equal(externalId)
        .and(PHYSICAL_METER.ADDRESS.equal(address))));
  }

  private Optional<PhysicalMeterEntity> fetchOne(Condition... conditions) {
    return nativeQuery(dsl.select().from(PHYSICAL_METER).where(conditions).limit(1)).stream()
      .findAny();
  }
}
