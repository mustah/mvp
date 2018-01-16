package com.elvaco.mvp.repository.jpa;

import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.entity.user.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByEmail(String email);

  Optional<Password> findPasswordById(Long id);
}
