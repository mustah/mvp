package com.elvaco.mvp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;

public interface MeteringPointRepository extends JpaRepository<MeteringPointEntity, Long> {

  MeteringPointEntity findByMoid(String moid);
}
