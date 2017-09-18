package com.elvaco.mvp.api;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elvaco.mvp.dto.UserDTO;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.exception.UserNotFound;
import com.elvaco.mvp.repository.UserRepository;

@RestApi
public class AuthController {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;

  @Autowired
  public AuthController(UserRepository userRepository, ModelMapper modelMapper) {
    this.userRepository = userRepository;
    this.modelMapper = modelMapper;
  }

  @RequestMapping("/authenticate")
  public UserDTO authenticate() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userRepository.findByEmail(email)
      .map(this::toDTO)
      .orElseThrow(() -> new UserNotFound(email));
  }

  private UserDTO toDTO(UserEntity userEntity) {
    return modelMapper.map(userEntity, UserDTO.class);
  }
}
