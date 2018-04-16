package cuke;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

  private WebDriver driver;
  private WebDriverWait waitDriver;
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
    waitDriver = new WebDriverWait(driver, 10);
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
    waitDriver.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
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

  @Then("I should see '(.*)'")
  public void thenIShouldSee(String context) throws Throwable {
    assertThat(driver.getPageSource().contains(context)).isTrue();
  }

  @Then("I should not see '(.*)'")
  public void thenIShouldNotSee(String context) throws Throwable {
    assertThat(driver.getPageSource().contains(context)).isFalse();
  }

  @Then("I sleep for '(.*)' seconds")
  public void thenISleepForSeconds(Integer seconds) throws Throwable {
    Thread.sleep(seconds * 1000);
  }
}
