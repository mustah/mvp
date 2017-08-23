package com.elvaco.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.meteringpoint.MeteringPoint;

public interface MeteringPointRepository extends JpaRepository<MeteringPoint, Long> {

  MeteringPoint findByMoid(String moid);
}
