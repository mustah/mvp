package com.elvaco.mvp.database.repository.jpa;

import javax.persistence.EntityManager;

import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class LogicalMeterJpaRepository extends QueryDslJpaRepository<LogicalMeterEntity, Long> {

  private final EntityManager entityManager;

  @Autowired
  LogicalMeterJpaRepository(EntityManager entityManager) {
    this(
      new JpaMetamodelEntityInformation<>(
        LogicalMeterEntity.class,
        entityManager.getMetamodel()
      ),
      entityManager
    );
  }

  private LogicalMeterJpaRepository(
    JpaEntityInformation<LogicalMeterEntity, Long> entityInformation,
    EntityManager entityManager
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
  }
}
