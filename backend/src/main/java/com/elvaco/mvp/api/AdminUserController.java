package com.elvaco.mvp.api;

import java.util.List;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.dto.UserDto;
import com.elvaco.mvp.core.usecase.AdminUserUseCases;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestApi("/api/admin/users")
public class AdminUserController {

  private final AdminUserUseCases adminUserUseCases;

  @Autowired
  AdminUserController(AdminUserUseCases adminUserUseCases) {
    this.adminUserUseCases = adminUserUseCases;
  }

  @RequestMapping
  public List<UserDto> allUsers() {
    return adminUserUseCases.findAll();
  }

  @Nullable
  @RequestMapping("{id}")
  public UserDto userById(@PathVariable Long id) {
    return adminUserUseCases.findById(id).orElse(null);
  }

  @RequestMapping(method = RequestMethod.POST)
  public UserDto createUser(@RequestBody UserDto user) {
    return adminUserUseCases.save(user);
  }

  @RequestMapping(method = RequestMethod.PUT)
  public UserDto updateUser(@RequestBody UserDto user) {
    return adminUserUseCases.save(user);
  }

  @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
  public void deleteUser(@PathVariable Long id) {
    adminUserUseCases.deleteById(id);
  }
}
