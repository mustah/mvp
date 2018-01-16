package com.elvaco.mvp.mapper;

import com.elvaco.mvp.core.domainmodels.User;
import org.springframework.security.core.userdetails.UserDetails;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public final class UserDetailsMapper {

  private UserDetailsMapper() {}

  public static UserDetails toUserDetails(User user) {
    return org.springframework.security.core.userdetails.User.withUsername(user.email)
      .password(requireNonNull(user.password))
      .roles(user.roles.stream()
               .map(r -> r.role)
               .collect(toList())
               .toArray(new String[user.roles.size()]))
      .build();
  }
}
