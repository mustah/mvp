package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Role;

public interface Roles {

  List<Role> save(List<Role> role);

  List<Role> findAll();
}
