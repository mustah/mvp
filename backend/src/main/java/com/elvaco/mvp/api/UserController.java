package com.elvaco.mvp.api;

import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.UserRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi
public class UserController {

  private final UserRepository userRepository;

  @Autowired
  UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @RequestMapping("/users/{id}")
  public UserEntity userById(@PathVariable Long id) {
    return userRepository.findOne(id);
  }

  @RequestMapping("/users")
  public Collection<UserEntity> allUsers() {
    return userRepository.findAll();
  }
}
