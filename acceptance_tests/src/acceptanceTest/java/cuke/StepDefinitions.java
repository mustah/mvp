package cuke;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import cucumber.api.DataTable;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import support.ApiRequestHelper;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

  private WebDriver driver;
  private String mvpServer;
  private String mvpApiServer;
  private String mvpAdminUsername;
  private String mvpAdminPassword;
  private String useLocalBrowser;
  private List<JsonNode> organisations;
  private ApiRequestHelper api;

  private void getEnvironmentOptions() throws UnknownHostException {
    String server = Optional.ofNullable(System.getenv("MVP_SERVER"))
      .orElse("http://" + getHostName());
    String webPort = Optional.ofNullable(System.getenv("MVP_WEB_PORT"))
      .orElse("4444");
    String apiPort = Optional.ofNullable(System.getenv("MVP_API_PORT"))
      .orElse("8080");
    useLocalBrowser = Optional.ofNullable(System.getenv("LOCAL_BROWSER"))
      .orElse(null);
    mvpAdminUsername = Optional.ofNullable(System.getenv("MVP_ADMIN_USERNAME"))
      .orElse("mvpadmin@elvaco.se");
    mvpAdminPassword = Optional.ofNullable(System.getenv("MVP_ADMIN_PASSWORD"))
      .orElse("changeme");

    mvpServer = server + ":" + webPort;
    mvpApiServer = server + ":" + apiPort;
  }

  @Before
  public void beforeScenario() throws MalformedURLException, UnknownHostException {
    getEnvironmentOptions();

    ChromeOptions options = new ChromeOptions();
    if (useLocalBrowser != null) {
      driver = new ChromeDriver(options);
    } else {
      String seleniumChromeStandaloneUrl = Optional.ofNullable(System.getenv("CHROME_URL"))
        .orElse("http://localhost:5555/wd/hub");

      driver = new RemoteWebDriver(new URL(seleniumChromeStandaloneUrl), options);
    }

    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    System.out.println("MVP_WEB_SERVER: " + mvpServer);
    System.out.println("MVP_API_SERVER: " + mvpApiServer);
    api = new ApiRequestHelper(mvpApiServer, mvpAdminUsername, mvpAdminPassword);
    organisations = new ArrayList<>();
  }

  @After
  public void afterScenario(Scenario scenario) {
    api.deleteOrganisations(organisations);
    if (scenario.isFailed()) {
      final byte[] screenshot = ((TakesScreenshot) driver)
        .getScreenshotAs(OutputType.BYTES);
      scenario.write("URL at failure: " + driver.getCurrentUrl());
      scenario.embed(screenshot, "image/png");
    }
    driver.quit();
  }

  @Given("the following companies exist")
  public void givenTheFollowingCompaniesExist(DataTable table) throws UnirestException {
    List<Map<String, String>> data = table.asMaps(String.class, String.class);
    for (Map<String, String> row : data) {
      JsonNode organisation = api.createOrganisation(row.get("company"), row.get("slug"));
      organisations.add(organisation);
    }
  }

  @Given("the following users exist")
  public void givenTheFollowingUsersExist(DataTable table) throws UnirestException {
    List<Map<String,String>> data = table.asMaps(String.class, String.class);
    for (Map<String, String> row: data) {
      api.createUser(row.get("username"),
        row.get("email"),
        row.get("password"),
        api.findOrganisationByName(row.get("organisation"), organisations));
    }
  }

  @Given("I am on the login page")
  public void givenIAmOnTheLoginPage() {
    driver.get(mvpServer);
    assertThat(driver.getTitle()).isEqualTo("Elvaco");
  }

  @When("I login as user '(.*)' and password '(.*)'")
  public void whenILoginAsUserWithPassword(String username, String password) {
    WebElement emailElement = driver.findElement(By.id("email"));
    emailElement.clear();
    emailElement.sendKeys(username);

    WebElement passwordElement = driver.findElement(By.id("password"));
    passwordElement.clear();
    passwordElement.sendKeys(password);

    driver.findElement(By.tagName("form"))
      .findElement(By.tagName("button"))
      .click();
  }

  @Then("I should be logged in as '(.*)'")
  public void thenIShouldBeLoggedInAs(String username) {
    assertClassElementHasText("Profile", username);
  }

  @Then("I should see error message '(.*)'")
  public void thenIShouldSeeErrorMessage(String text) {
    assertClassElementHasText("Error-message", text);
  }

  private void assertClassElementHasText(String className, String text) {
    WebElement contextElement = driver.findElement(By.className(className));
    assertThat(contextElement.getText()).isEqualTo(text);
  }

  private String getHostName() throws UnknownHostException {
    InetAddress hostName = InetAddress.getLocalHost();
    return hostName.getCanonicalHostName();
  }
}
