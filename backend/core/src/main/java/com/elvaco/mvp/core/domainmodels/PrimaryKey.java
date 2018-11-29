package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

public interface PrimaryKey {

  UUID getId();

  UUID getOrganisationId();
}
