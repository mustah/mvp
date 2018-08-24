package com.elvaco.mvp.testing.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.spi.repository.Roles;

public class MockRoles extends MockRepository<String, Role> implements Roles {

  @Override
  public List<Role> save(List<Role> role) {
    return null;
  }

  @Override
  protected Role copyWithId(String id, Role entity) {
    return null;
  }

  @Override
  protected String generateId(Role entity) {
    return null;
  }
}
