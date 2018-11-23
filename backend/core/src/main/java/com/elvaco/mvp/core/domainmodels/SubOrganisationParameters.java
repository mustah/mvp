package com.elvaco.mvp.core.domainmodels;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.UserSelection.SelectionParametersDto;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.exception.InvalidUserSelection.misconfiguredParentOrganisationSelection;

@RequiredArgsConstructor
public class SubOrganisationParameters implements Serializable {

  private final UUID organisationId;

  @Nullable
  private final UUID parentOrganisationId;

  @Nullable
  private final SelectionParametersDto selectionParameters;

  public Optional<SelectionParametersDto> selectionParameters() {
    if (selectionParameters == null && parentOrganisationId != null) {
      throw misconfiguredParentOrganisationSelection(parentOrganisationId, organisationId);
    }
    return Optional.ofNullable(selectionParameters);
  }

  public UUID getEffectiveOrganisationId() {
    return parentOrganisationId != null ? parentOrganisationId : organisationId;
  }

  @Nullable
  public UUID getParentOrganisationId() {
    return parentOrganisationId;
  }

  public UUID getOrganisationId() {
    return organisationId;
  }
}
