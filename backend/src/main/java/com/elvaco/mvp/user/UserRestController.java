package com.elvaco.mvp.user;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author petjan
 */
@RestController
public class UserRestController {

  private final UserRepository userRepository;

  @Autowired
  UserRestController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * Get user object from user identifier.
   *
   * @param uid user identifier
   *
   * @return the user object if user exists with this user id
   */
  @RequestMapping("/user")
  public User user(@RequestParam(value = "uid", defaultValue = "0") Long uid) {
    return userRepository.findById(uid);
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
