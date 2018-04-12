package cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class StepDefinitions {

  @Given("I am on the login page")
  public void givenIAmOnTheLoginPage() {
    // PENDING
  }

  @When("I login as user '(.*)' and password '(.*)'")
  public void whenILoginAsUserWithPassword(String user, String password){
    // PENDING
  }

  @Then("I should see '(.*)'")
  public void thenIShouldSee(String context) {
    // PENDING
  }

  @Then("I should not see '(.*)'")
  public void thenIShouldNotSee(String context) {
    // PENDING
  }
}
