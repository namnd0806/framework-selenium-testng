package tests;

import base.BaseTest;
import base.RetryAnalyzer;
import core.report.AllureListener;
import core.utils.AssertionUtils;
import dataproviders.CsvDataProvider;
import io.qameta.allure.*;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.LoginPage;

/**
 * Sample test class demonstrating the framework's capabilities:
 * <ul>
 *   <li>Page Object Model via {@link LoginPage}</li>
 *   <li>Data-Driven Testing via {@link CsvDataProvider}</li>
 *   <li>Allure reporting annotations</li>
 *   <li>Retry logic via {@link RetryAnalyzer}</li>
 *   <li>Custom assertions via {@link AssertionUtils}</li>
 * </ul>
 *
 * <p>Satisfies Requirements: 1.2, 1.3, 7.2, 8.1</p>
 */
@Listeners(AllureListener.class)
@Epic("Authentication")
@Feature("Login")
public class LoginTest extends BaseTest {

    // -------------------------------------------------------------------------
    // Data Providers
    // -------------------------------------------------------------------------

    /**
     * Provides invalid credential sets for data-driven login failure tests.
     * Reads from {@code src/test/resources/testdata/invalid-credentials.csv}.
     *
     * <p>CSV format (header row skipped):
     * <pre>username,password,expectedError</pre>
     *
     * <p>Falls back to inline data when the CSV file is not present, so the
     * test can compile and run in environments without the test data file.
     */
    @DataProvider(name = "invalidCredentials")
    public Object[][] invalidCredentialsProvider() {
        String csvPath = "src/test/resources/testdata/invalid-credentials.csv";
        java.io.File csvFile = new java.io.File(csvPath);
        if (csvFile.exists()) {
            return CsvDataProvider.getData(csvPath);
        }
        // Inline fallback data when CSV file is not present
        return new Object[][] {
            { "invalidUser", "wrongPassword", "Invalid username or password" },
            { "",            "anyPassword",   "Username is required"         },
            { "validUser",   "",              "Password is required"         }
        };
    }

    // -------------------------------------------------------------------------
    // Test methods
    // -------------------------------------------------------------------------

    /**
     * Verifies that a user with valid credentials can log in successfully.
     *
     * <p>This test navigates to the base URL, performs a login with known-good
     * credentials, and asserts that the resulting page URL does not contain
     * "login" (indicating a successful redirect to the authenticated area).</p>
     *
     * <p>NOTE: This is a sample/template test. Update the base URL, credentials,
     * and assertions to match the actual application under test.</p>
     */
    @Test(description = "Login with valid credentials should redirect to dashboard",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("Valid Login")
    @Severity(SeverityLevel.CRITICAL)
    public void loginWithValidCredentials() {
        // Navigate to the application base URL
        String baseUrl = core.config.ConfigManager.getInstance().getString("base.url", "http://localhost:8080");
        driver.get(baseUrl + "/login");

        // Perform login using the LoginPage Page Object
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login("admin", "admin123");

        // Assert that the URL changed away from the login page
        String currentUrl = driver.getCurrentUrl();
        AssertionUtils.assertFalse(
                currentUrl.contains("login"),
                "loginWithValidCredentials — URL after login should not contain 'login'"
        );
    }

    /**
     * Verifies that login with invalid credentials displays an appropriate error message.
     *
     * <p>Runs once per row in the {@code invalidCredentials} data provider.
     * Each row provides a username, password, and the expected error message substring.</p>
     *
     * <p>NOTE: This is a sample/template test. Update the base URL and assertions
     * to match the actual application under test.</p>
     *
     * @param username      the username to attempt login with
     * @param password      the password to attempt login with
     * @param expectedError the expected error message substring
     */
    @Test(description = "Login with invalid credentials should show error message",
          dataProvider = "invalidCredentials",
          retryAnalyzer = RetryAnalyzer.class)
    @Story("Invalid Login")
    @Severity(SeverityLevel.NORMAL)
    public void loginWithInvalidCredentials(String username, String password, String expectedError) {
        // Navigate to the application base URL
        String baseUrl = core.config.ConfigManager.getInstance().getString("base.url", "http://localhost:8080");
        driver.get(baseUrl + "/login");

        // Perform login using the LoginPage Page Object
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(username, password);

        // Assert that an error message is displayed
        String errorMessage = loginPage.getErrorMessage();
        AssertionUtils.assertContains(
                errorMessage,
                expectedError,
                "loginWithInvalidCredentials — error message for user='" + username + "'"
        );
    }
}
