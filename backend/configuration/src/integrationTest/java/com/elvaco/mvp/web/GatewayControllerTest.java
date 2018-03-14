package com.elvaco.mvp.web;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.GatewayDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.elvaco.mvp.testing.fixture.UserTestData.DAILY_PLANET;
import static com.elvaco.mvp.testing.fixture.UserTestData.dailyPlanetUser;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
public class GatewayControllerTest extends IntegrationTest {

  @Autowired
  private GatewayJpaRepository jpaRepository;

  @Autowired
  private Gateways gateways;

  @Autowired
  private Organisations organisations;

  private Organisation dailyPlanet;

  @Before
  public void setUp() {
    dailyPlanet = organisations.save(DAILY_PLANET);
  }

  @After
  public void tearDown() {
    jpaRepository.deleteAll();
    organisations.deleteById(dailyPlanet.id);
  }

  @Test
  public void fetchAllGatewaysShouldBeEmptyWhenNoGatewaysExists() {
    ResponseEntity<List> response = asSuperAdmin().get("/gateways", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEmpty();
  }

  @Test
  public void superAdminsCanListAllGateways() {
    gateways.save(new Gateway(randomUUID(), dailyPlanet.id, "1111", "serial-1"));
    gateways.save(new Gateway(randomUUID(), dailyPlanet.id, "2222", "serial-2"));
    gateways.save(new Gateway(randomUUID(), context().organisation().id, "3333", "serial-3"));

    ResponseEntity<List> response = asSuperAdmin().get("/gateways", List.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(3);
  }

  @Test
  public void createNewGateway() {
    GatewayDto requestModel = new GatewayDto(null, "123", "2100");

    ResponseEntity<GatewayDto> response = asSuperAdmin()
      .post("/gateways", requestModel, GatewayDto.class);

    GatewayDto created = response.getBody();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(created.id).isNotNull();
    assertThat(created.serial).isEqualTo("123");
    assertThat(created.productModel).isEqualTo("2100");
  }

  @Test
  public void otherUsersCannotFetchGatewaysFromOtherOrganisations() {
    gateways.save(new Gateway(randomUUID(), context().organisation().id, "1111", "serial-1"));

    ResponseEntity<List> gatewaysResponse = restAsUser(dailyPlanetUser(dailyPlanet))
      .get("/gateways", List.class);

    assertThat(gatewaysResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(gatewaysResponse.getBody()).isEmpty();
  }

  @Test
  public void userCanOnlyListGatewaysWithinSameOrganisation() {
    Gateway g1 = gateways.save(new Gateway(randomUUID(), dailyPlanet.id, "1111", "serial-1"));
    Gateway g2 = gateways.save(new Gateway(randomUUID(), dailyPlanet.id, "2222", "serial-2"));
    gateways.save(new Gateway(randomUUID(), context().organisation().id, "3333", "serial-3"));

    List<String> gatewayIds = restAsUser(dailyPlanetUser(dailyPlanet))
      .getList("/gateways", GatewayDto.class)
      .getBody()
      .stream()
      .map(g -> g.id)
      .collect(toList());

    assertThat(gatewayIds).containsOnly(g1.id.toString(), g2.id.toString());
  }
}
