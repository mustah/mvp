package com.elvaco.mvp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.user.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Optional<UserEntity> findByEmail(String email);

}
