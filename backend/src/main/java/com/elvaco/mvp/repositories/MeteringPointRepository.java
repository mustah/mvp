package com.elvaco.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.meteringpoint.MeteringPointEntity;

public interface MeteringPointRepository extends JpaRepository<MeteringPointEntity, Long> {

  MeteringPointEntity findByMoid(String moid);
}
