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
 * Abstract base class for all TestNG test cases in the framework.
 *
 * <p>Applies the <strong>Template Method + Hook</strong> pattern to provide a
 * consistent, thread-safe setup/teardown lifecycle for every test:</p>
 * <ol>
 *   <li>{@link #setUp(Method)} — initialises the WebDriver and calls the
 *       overridable {@link #beforeTest()} hook.</li>
 *   <li>Test method executes.</li>
 *   <li>{@link #tearDown(ITestResult)} — calls the overridable {@link #afterTest()}
 *       hook, handles failure (screenshot + log), then quits the driver.</li>
 * </ol>
 *
 * <p>Subclasses may override {@link #beforeTest()} and/or {@link #afterTest()} to
 * inject custom pre/post-test logic without touching the lifecycle plumbing.</p>
 *
 * <p>Satisfies Requirements: 1.3, 2.4, 6.3, 6.6, 8.4</p>
 */
public abstract class BaseTest {

    private static final Logger log = LogManager.getLogger(BaseTest.class);

    /**
     * The WebDriver instance for the current test thread.
     * Populated by {@link #setUp(Method)} and nulled after {@link #tearDown(ITestResult)}.
     */
    protected WebDriver driver;

    // -------------------------------------------------------------------------
    // TestNG lifecycle — @BeforeMethod
    // -------------------------------------------------------------------------

    /**
     * Initialises the WebDriver for the current thread and invokes the
     * {@link #beforeTest()} hook.
     *
     * <p>Called automatically by TestNG before every test method.
     * {@code alwaysRun = true} ensures it runs even for tests in groups that
     * are not explicitly included in the suite run.</p>
     *
     * @param method the test {@link Method} about to be executed (injected by TestNG)
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        ConfigManager config = ConfigManager.getInstance();

        // Load environment-specific config if an env profile is specified
        String env = config.getString("env", "dev");
        config.loadConfig(env);

        String browser = config.getString("browser", "chrome");
        String testName = method.getName();
        long threadId = Thread.currentThread().getId();

        // Log test start in the required format (Requirement 6.6)
        log.info("[THREAD-{}] [INFO] [BaseTest] - Starting test: {} [{}]",
                threadId, testName, browser);

        // Initialise the WebDriver for this thread (Requirement 2.4, 2.7)
        WebDriverManager.initDriver(browser);
        this.driver = WebDriverManager.getDriver();

        // Invoke the overridable pre-test hook (Template Method pattern)
        beforeTest();
    }

    // -------------------------------------------------------------------------
    // TestNG lifecycle — @AfterMethod
    // -------------------------------------------------------------------------

    /**
     * Invokes the {@link #afterTest()} hook, handles test failure (screenshot +
     * error log), and quits the WebDriver for the current thread.
     *
     * <p>Called automatically by TestNG after every test method.
     * {@code alwaysRun = true} guarantees teardown even when the test throws.</p>
     *
     * @param result the {@link ITestResult} of the completed test (injected by TestNG)
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        // Invoke the overridable post-test hook first
        afterTest();

        // Handle failure: screenshot + error log (Requirements 6.3, 6.6, 8.4)
        if (result.getStatus() == ITestResult.FAILURE) {
            handleTestFailure(result);
        }

        // Always quit the driver to release resources (Requirement 2.4)
        WebDriverManager.quitDriver();
        this.driver = null;
    }

    // -------------------------------------------------------------------------
    // Failure handling
    // -------------------------------------------------------------------------

    /**
     * Captures a screenshot and attaches it to the Allure report when a test fails,
     * provided {@code screenshot.on.failure} is configured as {@code true}.
     *
     * <p>Also logs an ERROR entry containing the test name and thread ID
     * (Requirement 6.6).</p>
     *
     * @param result the {@link ITestResult} of the failed test
     */
    private void handleTestFailure(ITestResult result) {
        long threadId = Thread.currentThread().getId();
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        String errorMessage = (throwable != null) ? throwable.getMessage() : "No exception message";

        // Log ERROR with test name and thread ID (Requirements 6.6, 3.7)
        log.error("[THREAD-{}] [ERROR] [BaseTest] - Test FAILED: {} - {}",
                threadId, testName, errorMessage);

        // Capture and attach screenshot if configured (Requirement 6.3)
        boolean screenshotOnFailure = ConfigManager.getInstance()
                .getBoolean("screenshot.on.failure", true);

        if (screenshotOnFailure && driver != null) {
            try {
                ScreenshotCapturer.attachToAllure(driver);
                log.info("[THREAD-{}] [INFO] [BaseTest] - Screenshot attached to Allure for test: {}",
                        threadId, testName);
            } catch (Exception e) {
                log.warn("[THREAD-{}] [WARN] [BaseTest] - Failed to capture screenshot for test {}: {}",
                        threadId, testName, e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Hook methods — Template Method pattern (overridable by subclasses)
    // -------------------------------------------------------------------------

    /**
     * Hook called immediately after the WebDriver is initialised, before the
     * test method runs.
     *
     * <p>Default implementation is empty. Subclasses may override this method
     * to perform custom pre-test setup (e.g., navigate to a base URL, log in,
     * seed test data).</p>
     */
    protected void beforeTest() {
        // Empty by default — subclasses override as needed
    }

    /**
     * Hook called immediately after the test method completes, before the
     * WebDriver is quit.
     *
     * <p>Default implementation is empty. Subclasses may override this method
     * to perform custom post-test cleanup (e.g., log out, clean up test data,
     * record additional metrics).</p>
     */
    protected void afterTest() {
        // Empty by default — subclasses override as needed
    }
}
