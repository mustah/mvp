package com.elvaco.mvp.user;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Collection<User> findByFirstName(String firstName);

  Collection<User> findByLastName(String lastName);
}
