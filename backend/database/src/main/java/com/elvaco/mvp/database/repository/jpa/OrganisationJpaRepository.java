package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.user.OrganisationEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationJpaRepository extends JpaRepository<OrganisationEntity, UUID> {
  Optional<OrganisationEntity> findBySlug(String slug);

  Optional<OrganisationEntity> findByExternalId(String externalId);

  List<OrganisationEntity> findAllByOrderByNameAsc();
}
