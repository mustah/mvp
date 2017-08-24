package com.elvaco.mvp.repositories;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.user.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  Collection<UserEntity> findByFirstName(String firstName);

  Collection<UserEntity> findByLastName(String lastName);
}
