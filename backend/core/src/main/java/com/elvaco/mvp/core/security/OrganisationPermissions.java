package com.elvaco.mvp.core.security;

import com.elvaco.mvp.core.domainmodels.Organisation;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrganisationPermissions {

  public boolean isAllowed(
    AuthenticatedUser authenticatedUser,
    Organisation target,
    Permission permission
  ) {
    if (authenticatedUser.isSuperAdmin()) {
      return true;
    }

    if (authenticatedUser.isMvpAdmin() && authenticatedUser.isWithinOrganisation(target.id)) {
      return permission.equals(Permission.READ);
    }

    return authenticatedUser.isMvpAdmin()
      && target.parent != null
      && authenticatedUser.isWithinOrganisation(target.parent.id);
  }

  public boolean isAllowedToRead(AuthenticatedUser authenticatedUser, Organisation target) {
    return isAllowed(authenticatedUser, target, Permission.READ);
  }

  public boolean isAllowedToDelete(AuthenticatedUser authenticatedUser, Organisation target) {
    return isAllowed(authenticatedUser, target, Permission.DELETE);
  }
}
