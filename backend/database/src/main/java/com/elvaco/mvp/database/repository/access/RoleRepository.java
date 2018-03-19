package com.elvaco.mvp.database.repository.access;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.database.entity.user.RoleEntity;
import com.elvaco.mvp.database.repository.jpa.RoleJpaRepository;

import static java.util.stream.Collectors.toList;

public class RoleRepository implements Roles {

  private final RoleJpaRepository roleJpaRepository;

  public RoleRepository(RoleJpaRepository roleJpaRepository) {

    this.roleJpaRepository = roleJpaRepository;
  }

  @Override
  public List<Role> save(List<Role> roles) {
    return this.roleJpaRepository
      .save(roles.stream().map(this::toEntity).collect(toList()))
      .stream().map(this::toDomainModel).collect(toList());
  }

  private Role toDomainModel(RoleEntity roleEntity) {
    return new Role(roleEntity.role);
  }

  private RoleEntity toEntity(Role role) {
    return new RoleEntity(role.role);
  }
}