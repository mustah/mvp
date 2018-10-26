package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserSelectionUseCases {

  private final AuthenticatedUser currentUser;
  private final UserSelections userSelections;

  public Optional<UserSelection> findByIdForCurrentUser(UUID id) {
    return userSelections.findByIdAndOwnerUserIdAndOrganisationId(
      id,
      currentUser.getUserId(),
      currentUser.getOrganisationId()
    );
  }

  public List<UserSelection> findAllForCurrentUser() {
    return userSelections.findByOwnerUserIdAndOrganisationId(
      currentUser.getUserId(),
      currentUser.getOrganisationId()
    );
  }

  public UserSelection save(UserSelection userSelection) {
    return userSelections.save(userSelection.toBuilder()
      .ownerUserId(currentUser.getUserId())
      .organisationId(currentUser.getOrganisationId())
      .build()
    );
  }

  public void delete(UserSelection userSelection) {
    if (currentUser.getUserId().equals(userSelection.ownerUserId)) {
      userSelections.delete(userSelection);
    }
  }
}
