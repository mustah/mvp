package cuke;

import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class StepDefinitions {

  @Given("I am on the login page")
  public void givenIAmOnTheLoginPage() throws Throwable {
    throw new PendingException();
  }

  @When("I login as user '(.*)' and password '(.*)'")
  public void whenILoginAsUserWithPassword(String user, String password) throws Throwable {
    throw new PendingException();
  }

  @Then("I should see '(.*)'")
  public void thenIShouldSee(String context) throws Throwable {
    throw new PendingException();
  }

  @Then("I should not see '(.*)'")
  public void thenIShouldNotSee(String context) throws Throwable {
    throw new PendingException();
  }
}
