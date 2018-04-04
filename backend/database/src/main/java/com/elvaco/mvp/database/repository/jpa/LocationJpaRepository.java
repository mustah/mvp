package com.elvaco.mvp.database.repository.jpa;

import java.util.UUID;

import com.elvaco.mvp.database.entity.meter.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationJpaRepository extends JpaRepository<LocationEntity, UUID> {

}
