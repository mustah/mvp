package com.elvaco.mvp.repository;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;

public abstract class MeteringPointBaseRepository extends SimpleJpaRepository<MeteringPointEntity, Long> implements MeteringPointRepository {

  protected final EntityManager entityManager;

  protected MeteringPointBaseRepository(EntityManager entityManager) {
    super(MeteringPointEntity.class, entityManager);
    this.entityManager = entityManager;
  }

  @Override
  public MeteringPointEntity findByMoid(String moid) {
    return entityManager
      .createQuery("FROM MeteringPointEntity WHERE moid = :moid", getDomainClass())
      .setParameter("moid", moid)
      .getSingleResult();
  }
}
