package com.elvaco.mvp.repository.jpa;

import com.elvaco.mvp.entity.user.RoleEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
}
