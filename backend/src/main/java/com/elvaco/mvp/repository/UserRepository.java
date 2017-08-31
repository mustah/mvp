package com.elvaco.mvp.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.user.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Collection<UserEntity> findByFirstName(String firstName);

  Collection<UserEntity> findByLastName(String lastName);
}
