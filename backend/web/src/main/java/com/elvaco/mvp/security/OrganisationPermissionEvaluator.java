package com.elvaco.mvp.security;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.dto.UserDto;
import com.elvaco.mvp.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

@Slf4j
public class OrganisationPermissionEvaluator implements PermissionEvaluator {
  private final Users users;
  private final UserMapper userMapper;

  public OrganisationPermissionEvaluator(Users users, UserMapper userMapper) {
    this.users = users;
    this.userMapper = userMapper;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object
    permissionObj) {

    if (authentication == null || targetDomainObject == null || permissionObj == null
      || !(permissionObj instanceof String)) {
      log.warn("Missing or invalid parameters to hasPermission() - defaulting to DENY!");
      return false;
    }

    MvpUserDetails principalUserDetails = (MvpUserDetails) authentication.getPrincipal();
    Permission permission = Permission.fromString((String) permissionObj);

    if (targetDomainObject instanceof UserDto) {
      return evaluateUserDtoPermissions(principalUserDetails, (UserDto) targetDomainObject,
        permission);
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
        Optional<UserDto> userDto = users.findById((Long) targetId).map(userMapper::toDto);
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

  boolean evaluateUserDtoPermissions(MvpUserDetails principal, UserDto targetDomainObject,
                                     Permission
    permission) {
    if (principal.isSuperAdmin()) {
      // Disallow deleting last superAdmin
      return !permission.equals(Permission.DELETE)
        || users.findByRole(Role.superAdmin()).size() != 1;
    }

    if (!principal.isWithinOrganisation(userMapper.organisationOf(targetDomainObject
      .organisation))) {
      return false;
    }

    if (principal.isAdmin()) {
      // admins can do anything on users of the same organisation
      return true;
    }

    switch (permission) {
      case READ:
        return true;
      case UPDATE:
        return principal.getUsername().equalsIgnoreCase(targetDomainObject.email);
      case DELETE:
      case CREATE:
      default:
        return false;
    }
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
