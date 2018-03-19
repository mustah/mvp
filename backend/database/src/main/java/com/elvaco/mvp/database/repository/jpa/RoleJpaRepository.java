package com.elvaco.mvp.database.repository.jpa;

import com.elvaco.mvp.database.entity.user.RoleEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
}
