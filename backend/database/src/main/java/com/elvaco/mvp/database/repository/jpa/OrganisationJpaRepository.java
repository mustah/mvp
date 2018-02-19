package com.elvaco.mvp.database.repository.jpa;

import java.util.Optional;

import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationJpaRepository extends JpaRepository<OrganisationEntity, Long> {
  Optional<OrganisationEntity> findByCode(String code);
}
