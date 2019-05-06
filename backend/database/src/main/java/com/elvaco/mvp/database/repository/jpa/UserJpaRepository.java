package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.database.entity.user.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

  Optional<UserEntity> findByEmail(String email);

  Optional<Password> findPasswordById(UUID id);

  List<UserEntity> findByRoles_Role(String role);

  List<UserEntity> findByOrganisationId(UUID organisationId);

  List<UserEntity> findByOrganisationIdOrOrganisation_ParentId(UUID organisationId, UUID parentId);

  List<UserEntity> findAllByOrderByOrganisationNameAscNameAsc();
}
