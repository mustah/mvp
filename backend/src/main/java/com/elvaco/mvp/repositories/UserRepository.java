package com.elvaco.mvp.repositories;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.user.User;

public interface UserRepository extends JpaRepository<User, Long> {

  Collection<User> findByFirstName(String firstName);

  Collection<User> findByLastName(String lastName);
}
