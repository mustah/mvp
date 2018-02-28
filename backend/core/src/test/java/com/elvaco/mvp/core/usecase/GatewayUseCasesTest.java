package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Test;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.CLARK_KENT;
import static com.elvaco.mvp.testing.fixture.UserTestData.DAILY_PLANET;
import static com.elvaco.mvp.testing.fixture.UserTestData.MARVEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GatewayUseCasesTest {

  @Test
  public void saveGateway() {
    GatewayUseCases useCases = useCasesWithCurrentUser(CLARK_KENT);

    assertThat(useCases.save(new Gateway(null, DAILY_PLANET.id, "1", "t")).id).isNotNegative();
  }

  @Test
  public void canOnlySaveGatewaysForSameOrganisationWhenNotSuperAdmin() {
    GatewayUseCases useCases = useCasesWithCurrentUser(CLARK_KENT);

    assertThatThrownBy(() -> useCases.save(new Gateway(null, MARVEL.id, "1", "t")))
      .isInstanceOf(Unauthorized.class)
      .hasMessage("User is not authorized to save this entity");
  }

  @Test
  public void superAdminCanSaveAllGateways() {
    GatewayUseCases useCases = useCasesWithCurrentUser(ELVACO_SUPER_ADMIN_USER);

    assertThat(useCases.save(new Gateway(null, DAILY_PLANET.id, "1", "t")).id).isNotNegative();
    assertThat(useCases.save(new Gateway(null, DAILY_PLANET.id, "1", "t")).id).isNotNegative();
  }

  private GatewayUseCases useCasesWithCurrentUser(User currentUser) {
    return new GatewayUseCases(
      new MockGateways(),
      new MockAuthenticatedUser(currentUser, "token123")
    );
  }
}
