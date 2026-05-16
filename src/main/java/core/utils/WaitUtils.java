package core.utils;

import core.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Utility class providing explicit wait helpers for common Selenium wait conditions.
 *
 * <p>Wraps {@link WebDriverWait} with convenience methods for the most frequently
 * needed wait conditions: element visibility, clickability, invisibility, text
 * presence, URL fragment, and page title.</p>
 *
 * <p>Two constructors are provided:</p>
 * <ul>
 *   <li>{@link #WaitUtils(WebDriver)} — uses {@code explicit.wait.timeout} from
 *       {@link ConfigManager} (default 10 seconds)</li>
 *   <li>{@link #WaitUtils(WebDriver, int)} — uses the caller-supplied timeout</li>
 * </ul>
 *
 * <p>Satisfies Requirements: 10.1</p>
 */
public class WaitUtils {

    private static final Logger log = LogManager.getLogger(WaitUtils.class);

    private final WebDriverWait wait;
    private final int timeoutSeconds;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Constructs a {@code WaitUtils} instance using the {@code explicit.wait.timeout}
     * value from {@link ConfigManager} (defaults to 10 seconds if not configured).
     *
     * @param driver the WebDriver instance (must not be {@code null})
     */
    public WaitUtils(WebDriver driver) {
        this(driver, ConfigManager.getInstance().getInt("explicit.wait.timeout", 10));
    }

    /**
     * Constructs a {@code WaitUtils} instance with an explicit timeout.
     *
     * @param driver         the WebDriver instance (must not be {@code null})
     * @param timeoutSeconds the maximum number of seconds to wait for each condition
     */
    public WaitUtils(WebDriver driver, int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    // -------------------------------------------------------------------------
    // Element visibility / clickability / invisibility
    // -------------------------------------------------------------------------

    /**
     * Waits until the element identified by {@code locator} is visible in the DOM
     * and has non-zero dimensions.
     *
     * @param locator the {@link By} locator strategy
     * @return the visible {@link WebElement}
     * @throws org.openqa.selenium.TimeoutException if the element is not visible
     *         within the configured timeout
     */
    public WebElement waitForElementVisible(By locator) {
        log.debug("[THREAD-{}] [DEBUG] [WaitUtils] - Waiting up to {}s for element to be visible: {}",
                Thread.currentThread().getId(), timeoutSeconds, locator);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Waits until the element identified by {@code locator} is visible and enabled
     * so that it can be clicked.
     *
     * @param locator the {@link By} locator strategy
     * @return the clickable {@link WebElement}
     * @throws org.openqa.selenium.TimeoutException if the element is not clickable
     *         within the configured timeout
     */
    public WebElement waitForElementClickable(By locator) {
        log.debug("[THREAD-{}] [DEBUG] [WaitUtils] - Waiting up to {}s for element to be clickable: {}",
                Thread.currentThread().getId(), timeoutSeconds, locator);
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Waits until the element identified by {@code locator} is either invisible or
     * absent from the DOM.
     *
     * @param locator the {@link By} locator strategy
     * @return {@code true} if the element is invisible or not present; {@code false}
     *         if the timeout expires before the condition is met
     */
    public boolean waitForElementInvisible(By locator) {
        log.debug("[THREAD-{}] [DEBUG] [WaitUtils] - Waiting up to {}s for element to be invisible: {}",
                Thread.currentThread().getId(), timeoutSeconds, locator);
        return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    // -------------------------------------------------------------------------
    // Text / URL / Title conditions
    // -------------------------------------------------------------------------

    /**
     * Waits until the element identified by {@code locator} contains the given
     * {@code text} as part of its visible text.
     *
     * @param locator the {@link By} locator strategy
     * @param text    the text substring to wait for
     * @return {@code true} when the text is present; {@code false} if the timeout
     *         expires before the condition is met
     */
    public boolean waitForTextPresent(By locator, String text) {
        log.debug("[THREAD-{}] [DEBUG] [WaitUtils] - Waiting up to {}s for text '{}' in element: {}",
                Thread.currentThread().getId(), timeoutSeconds, text, locator);
        return wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    /**
     * Waits until the current URL contains the given {@code urlFragment}.
     *
     * @param urlFragment the substring expected to appear in the current URL
     * @return {@code true} when the URL contains the fragment; {@code false} if the
     *         timeout expires before the condition is met
     */
    public boolean waitForUrlContains(String urlFragment) {
        log.debug("[THREAD-{}] [DEBUG] [WaitUtils] - Waiting up to {}s for URL to contain: {}",
                Thread.currentThread().getId(), timeoutSeconds, urlFragment);
        return wait.until(ExpectedConditions.urlContains(urlFragment));
    }

    /**
     * Waits until the current page title contains the given {@code title} substring.
     *
     * @param title the substring expected to appear in the page title
     * @return {@code true} when the title contains the substring; {@code false} if
     *         the timeout expires before the condition is met
     */
    public boolean waitForTitleContains(String title) {
        log.debug("[THREAD-{}] [DEBUG] [WaitUtils] - Waiting up to {}s for title to contain: {}",
                Thread.currentThread().getId(), timeoutSeconds, title);
        return wait.until(ExpectedConditions.titleContains(title));
    }
}
