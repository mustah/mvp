package com.elvaco.mvp.database.repository.access;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.UserSelection;
import com.elvaco.mvp.core.spi.repository.UserSelections;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;
import com.elvaco.mvp.database.repository.mappers.UserSelectionEntityMapper;
import lombok.AllArgsConstructor;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
public class UserSelectionRepository implements UserSelections {

  private final UserSelectionJpaRepository userSelectionJpaRepository;
  private final UserSelectionEntityMapper mapper;

  @Override
  public Optional<UserSelection> findByIdAndOwnerUserIdAndOrganisationId(
    UUID id,
    UUID ownerUserId,
    UUID organisationId
  ) {
    return userSelectionJpaRepository.findByIdAndOwnerUserIdAndOrganisationId(
      id,
      ownerUserId,
      organisationId
    )
      .map(mapper::toDomainModel);
  }

  @Override
  public List<UserSelection> findByOwnerUserIdAndOrganisationId(
    UUID ownerUserId,
    UUID organisationId
  ) {
    return userSelectionJpaRepository.findByOwnerUserIdAndOrganisationId(
      ownerUserId,
      organisationId
    )
      .stream()
      .map(mapper::toDomainModel)
      .collect(toList());
  }

  public UserSelection save(UserSelection userSelection) {
    return mapper.toDomainModel(userSelectionJpaRepository.save(mapper.toEntity(userSelection)));
  }
}
