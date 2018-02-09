package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.gateway.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayRepository extends JpaRepository<GatewayEntity, Long> {
}
