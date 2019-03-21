package com.elvaco.mvp.core.spi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.UserSelection;

public interface UserSelections {

  Optional<UserSelection> findByIdAndOwnerUserIdAndOrganisationId(
    UUID id,
    UUID ownerUserId,
    UUID organisationId
  );

  List<UserSelection> findByOwnerUserIdAndOrganisationId(UUID ownerUserId, UUID organisationId);

  UserSelection save(UserSelection userSelection);

  void delete(UserSelection userSelection);
}
