package StepDefinitions;

import com.baseLibrary.Configurations;
import com.baseLibrary.SkipScenarioException;

import Locators.Login;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class LoginSteps extends Configurations {

	Login loginElements;

	// Initialize login elements
	public LoginSteps() {
		loginElements = new Login();
	}

	@Given("I navigate to the OrangeHRM login page")
	public void iNavigateToLoginPage() throws Exception {
		try {
			Thread.sleep(3000);
			boolean isLoginPageVisible = getPage().locator(loginElements.username).isVisible();
			if (isLoginPageVisible) {
				LOGPASS("Should navigate", "Successfully navigated", "OrangeHRM Login Page");
			} else {
				throw new AssertionError("Login page not visible!");
			}
		} catch (Exception e) {
			LOGFAIL("Should navigate", "Failed to navigate", "OrangeHRM Login Page", e);
			throw e;
		}
	}

	@When("I enter credentials for {string} and {string} with flag {string}")
	public void iEnterCredentials(String username, String password, String flag) {
		// Validate the flag
		if (!"X".equalsIgnoreCase(flag)) {
			System.out.println("Scenario skipped due to flag not set to X");
			SkipScenarioException ex = new SkipScenarioException("Scenario skipped as the flag is not set to X");
			LOGFAIL("Should enter credentials", "Failed to enter expected credentials", username + " and " + password, ex);
			throw ex;
		}
		try {
			getPage().locator(loginElements.username).fill(username);
			getPage().locator(loginElements.password).fill(password);

			boolean isUsernameFilled = getPage().locator(loginElements.username).inputValue().equals(username);
			boolean isPasswordFilled = !getPage().locator(loginElements.password).inputValue().isEmpty();

			if (isUsernameFilled && isPasswordFilled) {
				LOGPASS("Should enter credentials", "Successfully entered expected credentials", username + " and " + password);
			} 
		} catch (Exception e) {
			LOGFAIL("Should enter credentials", "Failed to enter expected credentials", username + " and " + password, e);
			throw e;
		}
	}

	@When("I click on the login button")
	public void iClickOnTheLoginButton() {
		try {
			getPage().locator(loginElements.btn_Submit).click(); // Click the login button
			boolean isButtonClicked = getPage().locator(loginElements.btn_Submit).isVisible();
			if (isButtonClicked) {
				LOGPASS("Should click", "Successfully clicked", "Login Button");
			}
		} catch (Exception e) {
			LOGFAIL("Should click", "Failed to click", "Login Button", e);
			throw e;
		}
	}

	@Then("I should see the {string} message")
	public void iShouldSeeTheMessage(String expectedResult) {
		try {
			String actualResult;

			// Depending on the expected result, check the outcome
			if (expectedResult.equalsIgnoreCase("Dashboard")) {
				actualResult = getPage().locator(loginElements.heading_Dashboard).textContent();
			} else {
				actualResult = getPage().locator(loginElements.alert_Invalid).textContent();
			}

			if (actualResult.contains(expectedResult)) {
				LOGPASS("Should display", "Successfully displayed", "Message: " + expectedResult);
			} else {
				throw new AssertionError(
						"Expected message not found! Expected: " + expectedResult + " Actual: " + actualResult);
			}
		} catch (Exception e) {
			LOGFAIL("Should display", "Failed to display", "Message: " + expectedResult, e);
			throw e;
		}
	}

}
