package com.elvaco.mvp.security;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.dto.UserDto;
import com.elvaco.mvp.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@Slf4j
public class OrganisationPermissionEvaluator implements PermissionEvaluator {
  private final Users users;
  private final UserMapper userMapper;

  public OrganisationPermissionEvaluator(Users users, UserMapper userMapper) {
    this.users = users;
    this.userMapper = userMapper;
  }

  private static boolean isAdmin(User user) {
    return user.roles.contains(Role.admin());
  }

  private static boolean isUser(User user) {
    return user.roles.contains(Role.user());
  }

  private static boolean isSuperAdmin(User user) {
    return user.roles.contains(Role.superAdmin());
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object
    permissionObj) {

    if (authentication == null || targetDomainObject == null || permissionObj == null
      || !(permissionObj instanceof String)) {
      log.warn("Missing or invalid parameters to hasPermission() - defaulting to DENY!");
      return false;
    }

    UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    Optional<User> optionalUser = users.findByEmail(userDetails.getUsername());
    if (!optionalUser.isPresent()) {
      log.warn("Principal user does not exist - DENY!");
      return false;
    }

    Permission permission = Permission.fromString((String) permissionObj);
    User principalUser = optionalUser.get();

    if (targetDomainObject instanceof UserDto) {
      return evaluateUserDtoPermissions(principalUser, (UserDto) targetDomainObject, permission);
    }
    log.warn("Unknown domain object target type '%s' - DENY!", targetDomainObject.getClass()
      .toString());
    return false;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId, String
    targetType, Object permission) {
    try {
      Class targetClass = Class.forName(targetType);
      if (UserDto.class.isAssignableFrom(targetClass) && targetId instanceof Long) {
        Optional<UserDto> userDto = users.findById((Long) targetId).map(u -> userMapper.toDto(u));
        // If the target object exists, check permissions; otherwise allow access to non-existent
        // object
        return userDto.map(userDto1 -> hasPermission(authentication, userDto1, permission))
          .orElse(true);
      }
      log.warn("Unhandled domain object target class '%s' - DENY!", targetClass.toString());
      return false;
    } catch (ClassNotFoundException e) {
      log.warn("Unknown domain object target class '%s' - DENY!", targetType);
      return false;
    }
  }

  boolean evaluateUserDtoPermissions(User principal, UserDto targetDomainObject, Permission
    permission) {
    if (isSuperAdmin(principal)) {
      // Disallow deleting last superAdmin
      if (permission.equals(Permission.DELETE) && users.findByRole(Role.superAdmin()).size() == 1) {
        return false;
      }
      return true;
    }

    if (!principal.organisation.id.equals(targetDomainObject.organisation.id)) {
      return false;
    }

    if (isAdmin(principal)) {
      // admins can do anything on users of the same organisation
      return true;
    }

    if (isUser(principal)) {
      switch (permission) {
        case READ:
          return true;
        case UPDATE:
          return principal.email.equalsIgnoreCase(targetDomainObject.email);
        case DELETE:
        case CREATE:
          return false;
        default:
          return false;
      }
    }
    return false;
  }

  public enum Permission {
    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DELETE("delete");
    private final String name;

    Permission(String name) {
      this.name = name;
    }

    public static Permission fromString(String s) {
      for (Permission p : values()) {
        if (p.name.equalsIgnoreCase(s)) {
          return p;
        }
      }
      throw new NoSuchElementException(s);
    }
  }
}
