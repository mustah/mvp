package com.elvaco.mvp.database.repository.jpa;

import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.PhysicalMeterStatusLogEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PhysicalMeterStatusLogJpaRepository
  extends QueryDslJpaRepository<PhysicalMeterStatusLogEntity, Long> {

  private final EntityManager entityManager;

  @Autowired
  PhysicalMeterStatusLogJpaRepository(EntityManager entityManager) {
    this(
      new JpaMetamodelEntityInformation<>(
        PhysicalMeterStatusLogEntity.class,
        entityManager.getMetamodel()
      ),
      entityManager
    );
  }

  private PhysicalMeterStatusLogJpaRepository(
    JpaEntityInformation<PhysicalMeterStatusLogEntity, Long> entityInformation,
    EntityManager entityManager
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
  }
}
