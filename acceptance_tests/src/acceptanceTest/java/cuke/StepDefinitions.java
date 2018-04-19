package cuke;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

  private WebDriver driver;
  private String mvpServerFinal;
  private String mvpApiServer;

  @Before
  public void setUp() throws MalformedURLException, UnknownHostException {
    String mvpServer = Optional.ofNullable(System.getenv("MVP_SERVER"))
      .orElse(getHostName());
    String mvpPort = Optional.ofNullable(System.getenv("MVP_WEB_PORT"))
      .orElse("4444");
    String mvpApiPort = Optional.ofNullable(System.getenv("MVP_API_PORT"))
      .orElse("8080");

    mvpServerFinal = mvpServer + ":" + mvpPort;
    mvpApiServer = mvpServer + ":" + mvpApiPort;

    System.out.println("MVP_WEB_SERVER: " + mvpServerFinal);
    System.out.println("MVP_API_SERVER: " + mvpApiServer);

    String localBrowser = Optional.ofNullable(System.getenv("LOCAL_BROWSER"))
      .orElse(null);

    ChromeOptions options = new ChromeOptions();
    if (localBrowser != null) {
      driver = new ChromeDriver(options);
    } else {
      String seleniumChromeStandaloneUrl = Optional.ofNullable(System.getenv("CHROME_URL"))
        .orElse("http://localhost:5555/wd/hub");

      driver = new RemoteWebDriver(new URL(seleniumChromeStandaloneUrl), options);
    }

    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  @After
  public void tearDown(Scenario scenario) {
    if (scenario.isFailed()) {
      final byte[] screenshot = ((TakesScreenshot) driver)
        .getScreenshotAs(OutputType.BYTES);
      scenario.write("URL at failure: " + driver.getCurrentUrl());
      scenario.embed(screenshot, "image/png");
    }
    driver.quit();
  }

  @Given("I am on the login page")
  public void givenIAmOnTheLoginPage() {
    driver.get(mvpServerFinal);
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
