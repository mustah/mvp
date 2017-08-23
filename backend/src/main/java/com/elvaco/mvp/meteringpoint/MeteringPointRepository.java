package com.elvaco.mvp.meteringpoint;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeteringPointRepository extends JpaRepository<MeteringPoint, Long> {

  MeteringPoint findByMoid(String moid);
}
