package com.elvaco.mvp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;

public interface MeteringPointRepository extends JpaRepository<MeteringPointEntity, Long> {

  MeteringPointEntity findByMoid(String moid);

  @Query(value = "SELECT * FROM mps WHERE jsonb_contains(property_collection, to_jsonb(:json))", nativeQuery = true)
  List<MeteringPointEntity> findByExternalId(@Param("json") String json);
}
