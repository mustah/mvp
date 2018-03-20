package com.elvaco.mvp.database.repository.jpa;

import java.util.UUID;

import com.elvaco.mvp.database.entity.gateway.GatewayStatusLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GatewayStatusLogJpaRepository extends JpaRepository<GatewayStatusLogEntity, UUID>,
                                                       GatewayStatusLogJpaRepositoryCustom {
}
