package com.elvaco.mvp.user;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserRestController {

  private final UserRepository userRepository;

  @Autowired
  UserRestController(UserRepository userRepository) {
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
  public User user(@PathVariable Long id) {
    return userRepository.findById(id);
  }

  /**
   * Get a list of all users in system.
   *
   * @return a list of all defined users.
   */
  @RequestMapping("/users")
  public Collection<User> users() {
    return userRepository.findAll();
  }
}
