package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Password;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;

public interface Users {

  List<User> findAll();

  Optional<User> findByEmail(String email);

  Optional<User> findById(Long id);

  Optional<Password> findPasswordByUserId(Long userId);

  User create(User user);

  User update(User user);

  void deleteById(Long id);

  List<User> findByRole(Role role);
}
