package com.elvaco.mvp.database.repository.jpa;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.gateway.GatewayMeterEntity;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.jooq.Tables.GATEWAYS_METERS;

@Repository
public class GatewaysMetersJooqJpaRepository
  extends BaseJooqRepository<GatewayMeterEntity, UUID>
  implements GatewaysMetersJpaRepository {

  private final DSLContext dsl;

  @Autowired
  GatewaysMetersJooqJpaRepository(EntityManager entityManager, DSLContext dsl) {
    super(entityManager, GatewayMeterEntity.class);
    this.dsl = dsl;
  }

  @Override
  public List<GatewayMeterEntity> findByLogicalMeterIdAndOrganisationId(
    UUID logicalMeterId,
    UUID organisationId
  ) {
    return nativeQuery(dsl.select().from(GATEWAYS_METERS)
      .where(GATEWAYS_METERS.LOGICAL_METER_ID.equal(logicalMeterId))
      .and(GATEWAYS_METERS.ORGANISATION_ID.equal(organisationId)));
  }

  @Override
  public void saveOrUpdate(
    UUID organisationId,
    UUID gatewayId,
    UUID logicalMeterId,
    ZonedDateTime lastSeen
  ) {
    int found = executeUpdate(
      dsl.update(GATEWAYS_METERS)
        .set(GATEWAYS_METERS.LAST_SEEN, lastSeen.toOffsetDateTime())
        .where(GATEWAYS_METERS.ORGANISATION_ID.eq(organisationId))
        .and(GATEWAYS_METERS.GATEWAY_ID.eq(gatewayId))
        .and(GATEWAYS_METERS.LOGICAL_METER_ID.eq(logicalMeterId))
        .and(GATEWAYS_METERS.LAST_SEEN.isNull()
          .or(GATEWAYS_METERS.LAST_SEEN.lessThan(lastSeen.toOffsetDateTime()))
        )
    );

    if (found < 1) {
      executeUpdate(
        dsl.insertInto(GATEWAYS_METERS)
          .set(GATEWAYS_METERS.ORGANISATION_ID, organisationId)
          .set(GATEWAYS_METERS.GATEWAY_ID, gatewayId)
          .set(GATEWAYS_METERS.LOGICAL_METER_ID, logicalMeterId)
          .set(GATEWAYS_METERS.LAST_SEEN, lastSeen.toOffsetDateTime())
          .set(GATEWAYS_METERS.CREATED, lastSeen.toOffsetDateTime())
          .onConflictDoNothing() // Historical values
      );
    }
  }
}
