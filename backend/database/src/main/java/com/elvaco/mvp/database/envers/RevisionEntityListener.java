package com.elvaco.mvp.database.envers;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.database.entity.envers.RevisionEntity;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class RevisionEntityListener implements RevisionListener {

  @Override
  public void newRevision(Object revisionEntity) {
    RevisionEntity customRevisionEntity = (RevisionEntity) revisionEntity;
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    // TODO: handle other types of details...
    if (auth != null && auth.getDetails() instanceof AuthenticatedUser) {
      customRevisionEntity.userEntityId = ((AuthenticatedUser) auth.getDetails()).getUserId();
    }
  }
}
