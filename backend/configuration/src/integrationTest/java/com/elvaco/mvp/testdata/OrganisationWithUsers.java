package com.elvaco.mvp.testdata;

import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrganisationWithUsers {
  public final Organisation organisation;
  public final List<User> users;

  public UUID getId() {
    return organisation.getId();
  }

  public User getUser() {
    return users.get(0);
  }
}
