package com.elvaco.mvp.api;

import java.util.Collection;

import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/api/users")
public class UserController {

  private final UserRepository userRepository;

  @Autowired
  UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @RequestMapping("{id}")
  public UserEntity userById(@PathVariable Long id) {
    return userRepository.findOne(id);
  }

  @RequestMapping
  public Collection<UserEntity> allUsers() {
    return userRepository.findAll();
  }
}
