package cuke;

import java.net.MalformedURLException;
import java.net.URL;
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
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

  private WebDriver driver;
  private String mvpServer;

  @Before
  public void setUp() throws MalformedURLException {
    mvpServer = Optional.ofNullable(System.getenv("MVP_SERVER"))
      .orElse("http://localhost:4444");
    System.out.println("MVP_SERVER: " + mvpServer);

    String seleniumChromeStandaloneUrl = Optional.ofNullable(System.getenv("CHROME_URL"))
      .orElse("http://localhost:5555/wd/hub");

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    driver = new RemoteWebDriver(new URL(seleniumChromeStandaloneUrl), options);
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  @After
  public void tearDown(Scenario scenario) {
    if (scenario.isFailed()) {
      final byte[] screenshot = ((TakesScreenshot) driver)
        .getScreenshotAs(OutputType.BYTES);
      scenario.write("URL at failure: " + driver.getCurrentUrl());
      scenario.embed(screenshot, "image/png"); //stick it in the report
    }
    driver.quit();
  }

  @Given("I am on the login page")
  public void givenIAmOnTheLoginPage() throws Throwable {
    driver.get(mvpServer);
    assertThat(driver.getTitle()).isEqualTo("Elvaco");
  }

  @When("I login as user '(.*)' and password '(.*)'")
  public void whenILoginAsUserWithPassword(String username, String password) throws Throwable {
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
  public void thenIShouldBeLoggedInAs(String username) throws Throwable {
    assertElementHasText("Profile", username);
  }

  @Then("I should see error message '(.*)'")
  public void thenIShouldSeeErrorMessage(String text) throws Throwable {
    assertElementHasText("Error-message", text);
  }

  private void assertElementHasText(String className, String text) {
    WebElement contextElement = driver.findElement(By.className(className));
    assertThat(contextElement.getText()).isEqualTo(text);
  }
}
