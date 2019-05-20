package com.elvaco.mvp.database.repository.access;

import java.util.stream.Collectors;
import javax.transaction.Transactional;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.core.spi.repository.OrganisationThemes;
import com.elvaco.mvp.database.entity.organisationtheme.OrganisationThemeEntity;
import com.elvaco.mvp.database.entity.organisationtheme.OrganisationThemePk;
import com.elvaco.mvp.database.repository.jpa.OrganisationThemeJpaRepository;

import lombok.RequiredArgsConstructor;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class OrganisationThemeRepository implements OrganisationThemes {

  private final OrganisationThemeJpaRepository organisationThemeJpaRepository;

  @Override
  @Transactional
  public Theme save(Theme theme) {
    organisationThemeJpaRepository.deleteByOrganisationThemePkOrganisationId(theme.organisationId);

    var pkBuilder = OrganisationThemePk.builder().organisationId(theme.organisationId);

    var savedEntities =
      organisationThemeJpaRepository.saveAll(
        theme.properties.entrySet().stream()
          .map(entry ->
            OrganisationThemeEntity.builder()
              .organisationThemePk(pkBuilder.property(entry.getKey()).build())
              .value(entry.getValue())
              .build())
          .collect(toList()));

    return Theme.builder()
      .properties(savedEntities.stream()
        .collect(Collectors.toMap(e -> e.organisationThemePk.property, e -> e.value)))
      .build();
  }

  @Override
  public Theme findByOrganisation(Organisation organisation) {
    var themeBuilder = Theme.builder().organisationId(organisation.id);

    if (organisation.parent != null) {
      organisationThemeJpaRepository.findByOrganisationThemePkOrganisationId(organisation.parent.id)
        .stream()
        .forEach(entity -> themeBuilder.property(
          entity.organisationThemePk.property,
          entity.value
        ));
    }

    organisationThemeJpaRepository.findByOrganisationThemePkOrganisationId(organisation.id)
      .stream()
      .forEach(entity -> themeBuilder.property(entity.organisationThemePk.property, entity.value));

    return themeBuilder.build();
  }

  @Override
  @Transactional
  public void deleteThemeForOrganisation(Organisation organisation) {
    organisationThemeJpaRepository.deleteByOrganisationThemePkOrganisationId(organisation.id);
  }
}
