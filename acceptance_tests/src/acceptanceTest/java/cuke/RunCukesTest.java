package cuke;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = {"."},
    tags = {"not @wip"},
    plugin = {"pretty", "html:target/cucumber"})
public class RunCukesTest {

}
