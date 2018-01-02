package com.elvaco.mvp.repository;

import java.util.Optional;

import com.elvaco.mvp.entity.user.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByEmail(String email);
}
