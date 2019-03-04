package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSelectionJpaRepository extends JpaRepository<UserSelectionEntity, UUID> {
  Optional<UserSelectionEntity> findByIdAndOwnerUserIdAndOrganisationId(
    UUID id,
    UUID ownerUserId,
    UUID organisationId
  );

  List<UserSelectionEntity> findByOwnerUserIdAndOrganisationIdOrderByNameAsc(
    UUID ownerUserId,
    UUID organisationId
  );
}
