package com.elvaco.mvp.database.repository.access;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.repository.jpa.RoleJpaRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleRepository implements Roles {

  private final RoleJpaRepository roleJpaRepository;

  @Override
  public List<Role> save(List<Role> roles) {
    return roles.stream()
      .map(this::toEntity)
      .map(roleJpaRepository::save)
      .map(this::toDomainModel)
      .toList();
  }

  @Override
  public List<Role> findAll() {
    return roleJpaRepository.findAll().stream()
      .map(this::toDomainModel)
      .toList();
  }

  private Role toDomainModel(RoleEntity roleEntity) {
    return new Role(roleEntity.role);
  }

  private RoleEntity toEntity(Role role) {
    return new RoleEntity(role.role);
  }
}
