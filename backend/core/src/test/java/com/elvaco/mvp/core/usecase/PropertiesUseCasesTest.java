package com.elvaco.mvp.core.usecase;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.Property;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.repository.Properties;
import com.elvaco.mvp.testing.repository.MockProperties;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.UserTestData.SUPER_ADMIN;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesUseCasesTest {

  private PropertiesUseCases useCases;
  private Properties properties;
  private AuthenticatedUser currentUser;

  @Before
  public void setUp() {
    properties = new MockProperties();
    currentUser = new MockAuthenticatedUser(SUPER_ADMIN, "token123");
    useCases = new PropertiesUseCases(currentUser, properties);
  }

  @Test
  public void createEntry() {
    Property property = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    );

    Property saved = useCases.create(property);

    assertThat(saved).isEqualTo(property);
  }

  @Test
  public void canHaveMultipleEntriesForSameKey_DifferentEntityIds() {
    Property property1 = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    );
    Property property2 = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    );

    useCases.create(property1);
    useCases.create(property2);

    assertThat(properties.findById(property1.getId()).get()).isEqualTo(property1);
    assertThat(properties.findById(property2.getId()).get()).isEqualTo(property2);
  }

  @Test
  public void canFindById() {
    Property property1 = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    );
    Property property2 = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    );

    useCases.create(property1);
    useCases.create(property2);

    assertThat(useCases.findById(property1.getId()).get()).isEqualTo(property1);
    assertThat(useCases.findBy(
      FeatureType.UPDATE_GEOLOCATION,
      property1.entityId,
      property1.organisationId
    ).get()).isEqualTo(property1);
  }

  @Test
  public void getValueAsBoolean_True() {
    Property property = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    );

    useCases.create(property);

    assertThat(useCases.isEnabled(property.getId())).isEqualTo(true);
  }

  @Test
  public void getValueAsBoolean_False() {
    Property property = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "false"
    );
    useCases.create(property);

    assertThat(useCases.isEnabled(property.getId())).isEqualTo(false);
  }

  @Test
  public void getValueAsBoolean_FallbacksToFalseWhenNotFound() {
    assertThat(useCases.isEnabled(Property.idOf(
      randomUUID(),
      currentUser.getOrganisationId(),
      "test"
    ))).isEqualTo(false);
  }

  @Test
  public void featureShouldBeEnabled() {
    UUID entityId = randomUUID();
    Property property = new Property(
      entityId,
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "true"
    );
    useCases.create(property);

    assertThat(useCases.isEnabled(FeatureType.UPDATE_GEOLOCATION, entityId)).isEqualTo(true);
  }

  @Test
  public void deleteProperty() {
    Property property = new Property(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key,
      "false"
    );
    useCases.create(property);

    useCases.deleteById(property.getId());

    assertThat(useCases.findById(property.getId()).isPresent()).isEqualTo(false);
  }

  @Test
  public void ignoreSilentlyWhenRemovingNonExistingProperty() {
    useCases.deleteById(Property.idOf(
      randomUUID(),
      currentUser.getOrganisationId(),
      FeatureType.UPDATE_GEOLOCATION.key
    ));
  }
}
