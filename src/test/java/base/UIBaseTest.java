package base;

import core.config.ConfigManager;
import core.driver.WebDriverManager;
import core.report.ScreenshotCapturer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;

/**
 * Abstract base class for all UI (Selenium) test cases.
 *
 * <p>Extends {@link BaseTest} and adds WebDriver lifecycle management:
 * initialises a browser instance before each test and quits it after.</p>
 *
 * <p>Subclasses override {@link #beforeTest()} to initialise Page Objects
 * and navigate to the starting URL via the Page Object's {@code open()} method.
 * They should NOT call {@code driver.get()} directly.</p>
 *
 * <p>Usage:</p>
 * <pre>
 *   public class LoginTest extends UIBaseTest {
 *       private LoginPage loginPage;
 *
 *       {@literal @}Override
 *       protected void beforeTest() {
 *           loginPage = new LoginPage(driver);
 *           loginPage.open(); // navigates to page.url.login from config
 *       }
 *   }
 * </pre>
 */
public abstract class UIBaseTest extends BaseTest {

    private static final Logger log = LogManager.getLogger(UIBaseTest.class);

    /** WebDriver instance for the current test thread. */
    protected WebDriver driver;

    // -------------------------------------------------------------------------
    // TestNG lifecycle
    // -------------------------------------------------------------------------

    @BeforeMethod(alwaysRun = true)
    public void setUpDriver(Method method) {
        ConfigManager config = ConfigManager.getInstance();

        String env = config.getString("env", "dev");
        config.loadConfig(env);

        String browser = config.getString("browser", "chrome");
        long threadId = Thread.currentThread().getId();

        log.info("[THREAD-{}] [INFO] [UIBaseTest] - Starting test: {} [{}]",
                threadId, method.getName(), browser);

        WebDriverManager.initDriver(browser);
        this.driver = WebDriverManager.getDriver();

        beforeTest();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDownDriver(ITestResult result) {
        afterTest();

        if (result.getStatus() == ITestResult.FAILURE) {
            handleTestFailure(result);
        }

        WebDriverManager.quitDriver();
        this.driver = null;
    }

    // -------------------------------------------------------------------------
    // Failure handling
    // -------------------------------------------------------------------------

    private void handleTestFailure(ITestResult result) {
        long threadId = Thread.currentThread().getId();
        String testName = result.getMethod().getMethodName();
        String errorMessage = result.getThrowable() != null
                ? result.getThrowable().getMessage() : "No exception message";

        log.error("[THREAD-{}] [ERROR] [UIBaseTest] - Test FAILED: {} - {}",
                threadId, testName, errorMessage);

        boolean screenshotOnFailure = ConfigManager.getInstance()
                .getBoolean("screenshot.on.failure", true);

        if (screenshotOnFailure && driver != null) {
            try {
                ScreenshotCapturer.attachToAllure(driver);
                log.info("[THREAD-{}] [INFO] [UIBaseTest] - Screenshot attached for: {}",
                        threadId, testName);
            } catch (Exception e) {
                log.warn("[THREAD-{}] [WARN] [UIBaseTest] - Screenshot failed for {}: {}",
                        threadId, testName, e.getMessage());
            }
        }
    }
}
