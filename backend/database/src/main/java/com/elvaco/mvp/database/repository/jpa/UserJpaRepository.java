package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByEmail(String email);

  Optional<Password> findPasswordById(Long id);

  List<UserEntity> findByOrganisation(OrganisationEntity organisation);

  List<UserEntity> findByRoles_Role(String role);
}
