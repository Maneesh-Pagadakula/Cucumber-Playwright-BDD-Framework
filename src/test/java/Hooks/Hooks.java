package Hooks;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.nio.file.Paths;

import com.baseLibrary.Configurations;
import com.baseLibrary.ExcelDataOperations;

public class Hooks extends Configurations {

    public static ExcelDataOperations excelDataOperations;


    // Runs before each scenario
    @Before
    public void beforeScenario(Scenario scenario) throws Exception {
        try {
            // Extract feature name
            String featureUri = scenario.getUri().toString();
            String fileName = Paths.get(featureUri.substring(featureUri.lastIndexOf("/") + 1)).getFileName().toString();
            String featureName = fileName.contains(".")
                    ? fileName.substring(0, fileName.lastIndexOf("."))
                    : fileName;

            // Initialize configuration, browser, and reports
            initializeConfiguration();
            initializeReportsOnce();

            // Create and start ExtentReport for this scenario
            createFeatureParentTest(featureName);
            startExtentParentTest(featureName, scenario.getName()); // If its only one scenario without any examples it'll directly use this

        } catch (Exception e) {
            System.err.println("Error in beforeScenario: " + e.getMessage());
            throw e; // Re-throw the exception to stop execution
        }
    }

    // Runs after each scenario
    @After
    public void afterScenario(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                extentParentTest.fail("Scenario failed: " + scenario.getName());
            } else {
                extentParentTest.pass("Scenario passed: " + scenario.getName());
            }
        } catch (Exception e) {
            System.err.println("Error in afterScenario (reporting): " + e.getMessage());
        } finally {
            // Ensure the Excel file is closed properly after each scenario
            if (excelDataOperations != null) {
                try {
                    excelDataOperations.close();
                } catch (Exception e) {
                    System.err.println("Error closing ExcelDataOperations: " + e.getMessage());
                }
            }
            // Ensure any teardown steps (browser, etc.) are completed
            tearDown(); // Close browser and Playwright
        }
    }

    // Runs after all scenarios to flush the report
    @AfterAll
    public static void afterAll() {
        if (reports != null) {
            reports.flush();
        }
    }
}
