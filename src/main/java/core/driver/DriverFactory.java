package core.driver;

import core.config.ConfigManager;
import core.exceptions.UnsupportedBrowserException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;

/**
 * Factory class responsible for creating WebDriver instances for supported browsers.
 *
 * <p>Applies the <strong>Factory Method</strong> pattern: {@link #createDriver(String)}
 * dispatches to browser-specific creator methods based on the browser name.
 * Driver binaries are managed automatically via
 * {@code io.github.bonigarcia.wdm.WebDriverManager}.</p>
 *
 * <p>Supported browsers: {@code chrome}, {@code firefox}, {@code edge}, {@code safari}</p>
 *
 * <p>Configuration is read from {@link ConfigManager}:</p>
 * <ul>
 *   <li>{@code headless} — run browser without a GUI (default: {@code false})</li>
 *   <li>{@code window.width} — browser window width in pixels (default: {@code 1920})</li>
 *   <li>{@code window.height} — browser window height in pixels (default: {@code 1080})</li>
 * </ul>
 *
 * <p>Satisfies Requirements: 2.1, 2.2, 2.3, 2.5, 2.6</p>
 */
public class DriverFactory {

    private static final Logger log = LogManager.getLogger(DriverFactory.class);

    /** Default window width when not specified in config. */
    private static final int DEFAULT_WINDOW_WIDTH = 1920;

    /** Default window height when not specified in config. */
    private static final int DEFAULT_WINDOW_HEIGHT = 1080;

    // Utility class — no instantiation
    private DriverFactory() {
    }

    // -------------------------------------------------------------------------
    // Main factory method (Requirement 2.2)
    // -------------------------------------------------------------------------

    /**
     * Creates and returns a {@link WebDriver} instance for the specified browser.
     *
     * <p>The {@code browser} parameter is case-insensitive. Headless mode and
     * window size are read from {@link ConfigManager}.</p>
     *
     * @param browser the browser name: {@code "chrome"}, {@code "firefox"},
     *                {@code "edge"}, or {@code "safari"}
     * @return a fully configured, ready-to-use {@link WebDriver} instance
     * @throws UnsupportedBrowserException if {@code browser} is not one of the
     *                                     supported values
     */
    public static WebDriver createDriver(String browser) {
        if (browser == null) {
            throw new UnsupportedBrowserException(
                    "Unsupported browser: null. Supported browsers are: chrome, firefox, edge, safari.");
        }

        ConfigManager config = ConfigManager.getInstance();
        boolean headless = config.getBoolean("headless", false);

        // Also honour ci.mode — force headless when running in CI pipeline
        boolean ciMode = config.getBoolean("ci.mode", false);
        if (ciMode) {
            headless = true;
        }

        String normalizedBrowser = browser.trim().toLowerCase();
        log.info("[THREAD-{}] [INFO] [DriverFactory] - Creating {} driver (headless={})",
                Thread.currentThread().getId(), normalizedBrowser, headless);

        WebDriver driver;
        switch (normalizedBrowser) {
            case "chrome":
                driver = createChromeDriver(headless);
                break;
            case "firefox":
                driver = createFirefoxDriver(headless);
                break;
            case "edge":
                driver = createEdgeDriver(headless);
                break;
            case "safari":
                driver = createSafariDriver();
                break;
            default:
                log.error("[THREAD-{}] [ERROR] [DriverFactory] - Unsupported browser: {}",
                        Thread.currentThread().getId(), browser);
                throw new UnsupportedBrowserException(
                        "Unsupported browser: " + browser
                                + ". Supported browsers are: chrome, firefox, edge, safari.");
        }

        // Apply window size from config
        int width = config.getInt("window.width", DEFAULT_WINDOW_WIDTH);
        int height = config.getInt("window.height", DEFAULT_WINDOW_HEIGHT);
        if (!headless) {
            // In headless mode the window size is set via browser options;
            // for non-headless we set it explicitly after launch.
            driver.manage().window().setSize(new Dimension(width, height));
        }

        log.info("[THREAD-{}] [INFO] [DriverFactory] - {} driver created successfully.",
                Thread.currentThread().getId(), normalizedBrowser);
        return driver;
    }

    // -------------------------------------------------------------------------
    // Browser-specific creators (Requirement 2.1, 2.5)
    // -------------------------------------------------------------------------

    /**
     * Creates a Chrome {@link WebDriver} instance.
     *
     * <p>Uses {@link WebDriverManager} to automatically download and configure
     * the ChromeDriver binary matching the installed Chrome version.</p>
     *
     * @param headless {@code true} to run Chrome in headless mode
     * @return a configured {@link ChromeDriver}
     */
    private static WebDriver createChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            ConfigManager config = ConfigManager.getInstance();
            int width = config.getInt("window.width", DEFAULT_WINDOW_WIDTH);
            int height = config.getInt("window.height", DEFAULT_WINDOW_HEIGHT);
            options.addArguments("--window-size=" + width + "," + height);
        }

        // Common stability options
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--remote-allow-origins=*");

        // Disable Chrome's native password manager / credential save popup
        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--password-store=basic");

        return new ChromeDriver(options);
    }

    /**
     * Creates a Firefox {@link WebDriver} instance.
     *
     * <p>Uses {@link WebDriverManager} to automatically download and configure
     * the GeckoDriver binary matching the installed Firefox version.</p>
     *
     * @param headless {@code true} to run Firefox in headless mode
     * @return a configured {@link FirefoxDriver}
     */
    private static WebDriver createFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("--headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }

        return new FirefoxDriver(options);
    }

    /**
     * Creates a Microsoft Edge {@link WebDriver} instance.
     *
     * <p>Uses {@link WebDriverManager} to automatically download and configure
     * the EdgeDriver binary matching the installed Edge version.</p>
     *
     * @param headless {@code true} to run Edge in headless mode
     * @return a configured {@link EdgeDriver}
     */
    private static WebDriver createEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();

        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");

            // Set window size for headless mode via options
            ConfigManager config = ConfigManager.getInstance();
            int width = config.getInt("window.width", DEFAULT_WINDOW_WIDTH);
            int height = config.getInt("window.height", DEFAULT_WINDOW_HEIGHT);
            options.addArguments("--window-size=" + width + "," + height);
        }

        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");

        return new EdgeDriver(options);
    }

    /**
     * Creates a Safari {@link WebDriver} instance.
     *
     * <p>Safari does not require a separate driver binary — it uses the built-in
     * SafariDriver on macOS. Headless mode is <strong>not supported</strong> by Safari;
     * this method always launches Safari with a visible window.</p>
     *
     * <p><strong>Note:</strong> Safari is only available on macOS. Running this on
     * other operating systems will result in a runtime error from Selenium.</p>
     *
     * @return a configured {@link SafariDriver}
     */
    private static WebDriver createSafariDriver() {
        // Safari uses the built-in driver on macOS — no WebDriverManager setup needed.
        // SafariDriver requires "Allow Remote Automation" to be enabled in Safari's
        // Develop menu before use.
        return new SafariDriver();
    }
}
