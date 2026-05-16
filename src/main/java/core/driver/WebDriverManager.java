package core.driver;

import core.exceptions.DriverInitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Custom ThreadLocal wrapper for managing {@link WebDriver} lifecycle per thread.
 *
 * <p>This class is a <strong>custom implementation</strong> — it is NOT the
 * {@code io.github.bonigarcia.wdm.WebDriverManager} library. The bonigarcia library
 * is used internally by {@link DriverFactory} to manage driver binaries.</p>
 *
 * <p>Applies the <strong>ThreadLocal</strong> pattern to ensure each thread
 * (i.e., each parallel test execution) has its own isolated {@link WebDriver}
 * instance, preventing shared state and race conditions.</p>
 *
 * <p>Typical lifecycle per test thread:</p>
 * <ol>
 *   <li>{@link #initDriver(String)} — called in {@code @BeforeMethod}</li>
 *   <li>{@link #getDriver()} — called throughout the test</li>
 *   <li>{@link #quitDriver()} — called in {@code @AfterMethod}</li>
 * </ol>
 *
 * <p>Satisfies Requirements: 2.3, 2.4, 2.7, 3.3</p>
 */
public class WebDriverManager {

    private static final Logger log = LogManager.getLogger(WebDriverManager.class);

    /**
     * ThreadLocal storage for WebDriver instances.
     * Each thread gets its own independent WebDriver — guarantees thread-safety
     * during parallel test execution (Requirement 2.7, 3.3).
     */
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // Utility class — no instantiation
    private WebDriverManager() {
    }

    // -------------------------------------------------------------------------
    // Driver lifecycle methods
    // -------------------------------------------------------------------------

    /**
     * Initialises a new {@link WebDriver} instance for the current thread.
     *
     * <p>Delegates browser creation to {@link DriverFactory#createDriver(String)}.
     * The resulting driver is stored in {@link ThreadLocal} so it is accessible
     * only to the calling thread.</p>
     *
     * @param browser the browser name: {@code "chrome"}, {@code "firefox"},
     *                {@code "edge"}, or {@code "safari"}
     * @throws DriverInitializationException if the driver cannot be created for
     *                                       any reason (wraps the underlying cause)
     */
    public static void initDriver(String browser) {
        long threadId = Thread.currentThread().getId();
        log.info("[THREAD-{}] [INFO] [WebDriverManager] - Initialising {} driver for thread.",
                threadId, browser);

        try {
            WebDriver driver = DriverFactory.createDriver(browser);
            driverThreadLocal.set(driver);
            log.info("[THREAD-{}] [INFO] [WebDriverManager] - Driver initialised successfully: {}",
                    threadId, driver.getClass().getSimpleName());
        } catch (Exception e) {
            log.error("[THREAD-{}] [ERROR] [WebDriverManager] - Failed to initialise driver for browser '{}': {}",
                    threadId, browser, e.getMessage());
            throw new DriverInitializationException(
                    "Failed to initialise WebDriver for browser '" + browser + "': " + e.getMessage(), e);
        }
    }

    /**
     * Returns the {@link WebDriver} instance associated with the current thread.
     *
     * @return the current thread's {@link WebDriver}
     * @throws DriverInitializationException if no driver has been initialised for
     *                                       the current thread (i.e., {@link #initDriver(String)}
     *                                       was not called beforehand)
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            long threadId = Thread.currentThread().getId();
            log.error("[THREAD-{}] [ERROR] [WebDriverManager] - No WebDriver found for current thread. "
                    + "Ensure initDriver() was called before getDriver().", threadId);
            throw new DriverInitializationException(
                    "No WebDriver initialised for thread " + threadId
                            + ". Call WebDriverManager.initDriver(browser) before using getDriver().");
        }
        return driver;
    }

    /**
     * Quits the {@link WebDriver} instance for the current thread and removes it
     * from the {@link ThreadLocal} storage.
     *
     * <p>This method is safe to call even if no driver is initialised for the
     * current thread — it will simply log a warning and return without error.</p>
     *
     * <p>Satisfies Requirement 2.4: driver is closed and released after each test.</p>
     */
    public static void quitDriver() {
        long threadId = Thread.currentThread().getId();
        WebDriver driver = driverThreadLocal.get();

        if (driver != null) {
            try {
                driver.quit();
                log.info("[THREAD-{}] [INFO] [WebDriverManager] - Driver quit successfully.", threadId);
            } catch (Exception e) {
                log.warn("[THREAD-{}] [WARN] [WebDriverManager] - Exception while quitting driver: {}",
                        threadId, e.getMessage());
            } finally {
                // Always remove from ThreadLocal to prevent memory leaks,
                // even if quit() threw an exception (Requirement 2.4)
                driverThreadLocal.remove();
                log.debug("[THREAD-{}] [DEBUG] [WebDriverManager] - Driver removed from ThreadLocal.", threadId);
            }
        } else {
            log.warn("[THREAD-{}] [WARN] [WebDriverManager] - quitDriver() called but no driver was initialised "
                    + "for this thread.", threadId);
        }
    }

    /**
     * Checks whether a {@link WebDriver} instance has been initialised for the
     * current thread.
     *
     * @return {@code true} if a driver is present in the ThreadLocal for this
     *         thread; {@code false} otherwise
     */
    public static boolean isDriverInitialized() {
        return driverThreadLocal.get() != null;
    }
}
