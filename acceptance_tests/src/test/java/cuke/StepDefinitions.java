package cuke;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {

  WebDriver driver;
  WebDriverWait wait;
  String mvp_server = new String();

  @Before
  public void setUp() {
    if (System.getenv("MVP_SERVER") != null) {
      mvp_server = System.getenv("MVP_SERVER");
    } else {
      mvp_server = "http://localhost:4444";
    }
    System.out.println("MVP_SERVER: " + mvp_server);

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    driver = new ChromeDriver(options);
    wait = new WebDriverWait(driver, 10);
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
    driver.get(mvp_server);
    assertThat(driver.getTitle()).isEqualTo("Elvaco");
  }

  @When("I login as user '(.*)' and password '(.*)'")
  public void whenILoginAsUserWithPassword(String user, String passwd) throws Throwable {

    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    WebElement email = driver.findElement(By.id("email"));
    email.clear();
    email.sendKeys(user);

    WebElement password = driver.findElement(By.id("password"));
    password.clear();
    password.sendKeys(passwd);

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
