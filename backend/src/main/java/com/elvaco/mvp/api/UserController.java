package com.elvaco.mvp.api;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elvaco.mvp.entities.user.UserEntity;
import com.elvaco.mvp.repositories.UserRepository;

@RestApi
public class UserController {

  private final UserRepository userRepository;

  @Autowired
  UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Get user object from user identifier.
   *
   * @param id user identifier
   *
   * @return the user object if user exists with this user id
   */
  @RequestMapping("/users/{id}")
  public UserEntity user(@PathVariable Long id) {
    return userRepository.findOne(id);
  }

  /**
   * Get a list of all users in system.
   *
   * @return a list of all defined users.
   */
  @RequestMapping("/users")
  public Collection<UserEntity> users() {
    return userRepository.findAll();
  }
}
