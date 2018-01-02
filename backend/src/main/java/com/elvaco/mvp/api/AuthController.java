package com.elvaco.mvp.api;

import com.elvaco.mvp.dto.UserDto;
import com.elvaco.mvp.entity.user.UserEntity;
import com.elvaco.mvp.exception.UserNotFound;
import com.elvaco.mvp.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/api/authenticate")
public class AuthController {

  private final UserRepository userRepository;
  private final ModelMapper modelMapper;

  @Autowired
  public AuthController(UserRepository userRepository, ModelMapper modelMapper) {
    this.userRepository = userRepository;
    this.modelMapper = modelMapper;
  }

  @RequestMapping
  public UserDto authenticate() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userRepository.findByEmail(email)
        .map(this::toDto)
        .orElseThrow(() -> new UserNotFound(email));
  }

  private UserDto toDto(UserEntity userEntity) {
    return modelMapper.map(userEntity, UserDto.class);
  }
}
