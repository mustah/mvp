package com.elvaco.mvp.repository.jpa;

import javax.persistence.EntityManager;

import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public abstract class MeteringPointBaseRepository
  extends SimpleJpaRepository<MeteringPointEntity, Long>
  implements MeteringPointRepository {

  protected final EntityManager entityManager;

  protected MeteringPointBaseRepository(EntityManager entityManager) {
    super(MeteringPointEntity.class, entityManager);
    this.entityManager = entityManager;
  }
}

