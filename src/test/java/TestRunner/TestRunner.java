package TestRunner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(features = "src/test/java/features", // Path to feature files
		glue = { "StepDefinitions", "Hooks" }, // Path to step definitions and hooks
		plugin = {
				"pretty", // Cucumber pretty plugin
				"html:target/cucumber-reports.html", // Cucumber HTML report
				"json:target/cucumber-reports/cucumber.json", // Cucumber JSON report
		})
public class TestRunner extends AbstractTestNGCucumberTests {
	
}
