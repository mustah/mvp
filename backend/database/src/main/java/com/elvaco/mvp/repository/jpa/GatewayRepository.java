package com.elvaco.mvp.repository.jpa;

import com.elvaco.mvp.entity.gateway.GatewayEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayRepository extends JpaRepository<GatewayEntity, Long> {
}
