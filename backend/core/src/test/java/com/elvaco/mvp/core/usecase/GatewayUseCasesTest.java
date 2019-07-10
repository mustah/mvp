package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockGatewaysMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.DAILY_PLANET;
import static com.elvaco.mvp.testing.fixture.OrganisationTestData.MARVEL;
import static com.elvaco.mvp.testing.fixture.UserTestData.CLARK_KENT;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_SUPER_ADMIN_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GatewayUseCasesTest {

  public static final String SERIAL = "1A2b";
  public static final String PRODUCT_MODEL = "t";

  @Test
  public void saveGateway() {
    GatewayUseCases useCases = useCasesWithCurrentUser(CLARK_KENT);

    assertThat(useCases.save(gatewayBuilder().build()).id).isNotNull();
  }

  @Test
  public void canOnlySaveGatewaysForSameOrganisationWhenNotSuperAdmin() {
    GatewayUseCases useCases = useCasesWithCurrentUser(CLARK_KENT);

    assertThatThrownBy(() -> useCases.save(gatewayBuilder().organisationId(MARVEL.id).build()))
      .isInstanceOf(Unauthorized.class)
      .hasMessage("User is not authorized to save this entity");
  }

  @Test
  public void superAdminCanSaveAllGateways() {
    GatewayUseCases useCases = useCasesWithCurrentUser(ELVACO_SUPER_ADMIN_USER);

    assertThat(useCases.save(gatewayBuilder().organisationId(MARVEL.id).build()).id).isNotNull();
    assertThat(useCases.save(gatewayBuilder().build()).id).isNotNull();
  }

  @Test
  public void doesNotFindGatewayByOrganisationSerialAndProductModel() {
    GatewayUseCases useCases = useCasesWithCurrentUser(ELVACO_SUPER_ADMIN_USER);

    assertThat(useCases.findBy(DAILY_PLANET.id, "test", "123")).isNotPresent();
  }

  @Test
  public void findGatewayByOrganisationSerialAndProductModel() {
    GatewayUseCases useCases = useCasesWithCurrentUser(ELVACO_SUPER_ADMIN_USER);

    useCases.save(gatewayBuilder().build());

    assertThat(useCases.findBy(DAILY_PLANET.id, PRODUCT_MODEL, SERIAL)).isPresent();
  }

  private GatewayUseCases useCasesWithCurrentUser(User currentUser) {
    return new GatewayUseCases(
      new MockGateways(),
      new MockGatewaysMeters(),
      new MockAuthenticatedUser(currentUser, "token123")
    );
  }

  private static Gateway.GatewayBuilder gatewayBuilder() {
    return Gateway.builder()
      .organisationId(DAILY_PLANET.id)
      .serial(SERIAL)
      .productModel(PRODUCT_MODEL);
  }
}
