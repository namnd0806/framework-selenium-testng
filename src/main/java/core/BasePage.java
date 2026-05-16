package core;

import core.config.ConfigManager;
import core.exceptions.ElementNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Abstract base class for all Page Objects in the framework.
 *
 * <p>Applies the <strong>Template Method</strong> pattern: concrete page classes extend
 * this class and inherit all WebDriver interaction methods with built-in Explicit Wait
 * and StaleElement retry logic.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Wrap every element interaction with {@link WebDriverWait} (timeout from config)</li>
 *   <li>Retry up to 3 times on {@link StaleElementReferenceException} (Requirement 8.3)</li>
 *   <li>Throw {@link ElementNotFoundException} with locator + timeout info when element
 *       is not found (Requirement 4.4)</li>
 *   <li>Provide JavaScript utilities: executeScript, scrollToElement, highlightElement</li>
 *   <li>Provide page-state helpers: waitForPageLoad, getPageTitle, getCurrentUrl</li>
 * </ul>
 *
 * <p>Satisfies Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7</p>
 */
public abstract class BasePage {

    private static final Logger log = LogManager.getLogger(BasePage.class);

    /** Maximum number of retries when a StaleElementReferenceException is encountered. */
    private static final int MAX_STALE_RETRY = 3;

    /** Highlight style applied by {@link #highlightElement(By)}. */
    private static final String HIGHLIGHT_STYLE =
            "arguments[0].style.border='3px solid red'; arguments[0].style.backgroundColor='yellow';";

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    /** Explicit wait timeout in seconds — read once from ConfigManager. */
    private final int explicitWaitTimeout;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@code BasePage} and initialises the {@link WebDriverWait}
     * using the {@code explicit.wait.timeout} value from {@link ConfigManager}.
     *
     * <p>Also calls {@link PageFactory#initElements(WebDriver, Object)} so that
     * subclasses can declare {@code @FindBy}-annotated fields and have them
     * lazily resolved by Selenium's PageFactory mechanism (Requirement 4.6).</p>
     *
     * @param driver the WebDriver instance for this page (must not be {@code null})
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.explicitWaitTimeout = ConfigManager.getInstance()
                .getInt("explicit.wait.timeout", 10);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitTimeout));
        // Initialise @FindBy / @FindBys / @FindAll annotated fields in subclasses
        PageFactory.initElements(driver, this);
    }

    // -------------------------------------------------------------------------
    // Core element interaction — all use explicit wait internally (Req 4.1, 4.2)
    // -------------------------------------------------------------------------

    /**
     * Finds a single element identified by {@code locator}, waiting up to
     * {@code explicit.wait.timeout} seconds for it to become visible.
     *
     * <p>Retries up to {@value #MAX_STALE_RETRY} times on
     * {@link StaleElementReferenceException} before giving up (Requirement 8.3).</p>
     *
     * @param locator the {@link By} locator strategy
     * @return the found {@link WebElement}
     * @throws ElementNotFoundException if the element is not found within the timeout
     */
    protected WebElement findElement(By locator) {
        return findElementWithRetry(locator);
    }

    /**
     * Finds all elements matching {@code locator}, waiting up to
     * {@code explicit.wait.timeout} seconds for at least one to be present.
     *
     * @param locator the {@link By} locator strategy
     * @return a list of matching {@link WebElement}s (never {@code null})
     * @throws ElementNotFoundException if no elements are found within the timeout
     */
    protected List<WebElement> findElements(By locator) {
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (TimeoutException e) {
            throw new ElementNotFoundException(
                    buildNotFoundMessage(locator, explicitWaitTimeout), e);
        }
    }

    /**
     * Clicks the element identified by {@code locator}.
     *
     * @param locator the {@link By} locator strategy
     * @throws ElementNotFoundException if the element is not found within the timeout
     */
    protected void click(By locator) {
        findElement(locator).click();
    }

    /**
     * Clears the element identified by {@code locator} and types {@code text} into it.
     *
     * @param locator the {@link By} locator strategy
     * @param text    the text to type
     * @throws ElementNotFoundException if the element is not found within the timeout
     */
    protected void sendKeys(By locator, String text) {
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Returns the visible text of the element identified by {@code locator}.
     *
     * @param locator the {@link By} locator strategy
     * @return the element's visible text
     * @throws ElementNotFoundException if the element is not found within the timeout
     */
    protected String getText(By locator) {
        return findElement(locator).getText();
    }

    /**
     * Returns {@code true} if the element identified by {@code locator} is displayed.
     *
     * @param locator the {@link By} locator strategy
     * @return {@code true} if the element is displayed; {@code false} otherwise
     */
    protected boolean isDisplayed(By locator) {
        try {
            return findElement(locator).isDisplayed();
        } catch (ElementNotFoundException e) {
            return false;
        }
    }

    /**
     * Selects an option from a {@code <select>} dropdown by its visible text.
     *
     * @param locator     the {@link By} locator strategy for the {@code <select>} element
     * @param visibleText the visible text of the option to select
     * @throws ElementNotFoundException if the element is not found within the timeout
     */
    protected void selectFromDropdown(By locator, String visibleText) {
        WebElement selectElement = findElement(locator);
        new Select(selectElement).selectByVisibleText(visibleText);
    }

    // -------------------------------------------------------------------------
    // Page state (Requirement 4.3)
    // -------------------------------------------------------------------------

    /**
     * Waits until {@code document.readyState} equals {@code "complete"}.
     * Uses JavaScript execution to poll the browser's document state.
     */
    protected void waitForPageLoad() {
        wait.until(driver -> {
            String readyState = (String) ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState");
            return "complete".equals(readyState);
        });
    }

    /**
     * Returns the current page title.
     *
     * @return the page title string
     */
    protected String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Returns the current URL of the browser.
     *
     * @return the current URL string
     */
    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    // -------------------------------------------------------------------------
    // JavaScript utilities (Requirement 4.7)
    // -------------------------------------------------------------------------

    /**
     * Executes the given JavaScript {@code script} in the context of the current page.
     *
     * @param script the JavaScript to execute
     * @param args   optional arguments passed to the script as {@code arguments[0]}, etc.
     * @return the value returned by the script, or {@code null}
     */
    protected Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    /**
     * Scrolls the browser viewport so that the element identified by {@code locator}
     * is visible.
     *
     * @param locator the {@link By} locator strategy
     * @throws ElementNotFoundException if the element is not found within the timeout
     */
    protected void scrollToElement(By locator) {
        WebElement element = findElement(locator);
        executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                element);
    }

    /**
     * Highlights the element identified by {@code locator} with a red border and
     * yellow background using JavaScript.
     *
     * @param locator the {@link By} locator strategy
     * @throws ElementNotFoundException if the element is not found within the timeout
     */
    protected void highlightElement(By locator) {
        WebElement element = findElement(locator);
        executeScript(HIGHLIGHT_STYLE, element);
    }

    // -------------------------------------------------------------------------
    // StaleElement retry logic (Requirement 8.3)
    // -------------------------------------------------------------------------

    /**
     * Attempts to find and return the element identified by {@code locator},
     * retrying up to {@value #MAX_STALE_RETRY} times when a
     * {@link StaleElementReferenceException} is encountered.
     *
     * <p>A WARN log entry is written for each retry attempt.</p>
     *
     * @param locator the {@link By} locator strategy
     * @return the found {@link WebElement}
     * @throws ElementNotFoundException if the element cannot be found after all retries
     */
    private WebElement findElementWithRetry(By locator) {
        int attempt = 0;
        while (true) {
            try {
                return waitForElement(locator);
            } catch (StaleElementReferenceException e) {
                attempt++;
                if (attempt >= MAX_STALE_RETRY) {
                    log.warn("[THREAD-{}] [WARN] [BasePage] - StaleElementReferenceException on locator '{}'"
                            + " — max retries ({}) reached, giving up.",
                            Thread.currentThread().getId(), locator, MAX_STALE_RETRY);
                    throw new ElementNotFoundException(
                            buildNotFoundMessage(locator, explicitWaitTimeout), e);
                }
                log.warn("[THREAD-{}] [WARN] [BasePage] - StaleElementReferenceException on locator '{}'"
                        + " — retry attempt {}/{}.",
                        Thread.currentThread().getId(), locator, attempt, MAX_STALE_RETRY);
            }
        }
    }

    /**
     * Waits for the element identified by {@code locator} to become visible,
     * using the configured explicit wait timeout.
     *
     * @param locator the {@link By} locator strategy
     * @return the visible {@link WebElement}
     * @throws ElementNotFoundException if the element is not visible within the timeout
     */
    private WebElement waitForElement(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            throw new ElementNotFoundException(
                    buildNotFoundMessage(locator, explicitWaitTimeout), e);
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Builds the {@link ElementNotFoundException} message containing both the
     * locator's string representation and the timeout value (Requirement 4.4).
     *
     * @param locator        the locator that was not found
     * @param timeoutSeconds the wait timeout in seconds
     * @return the formatted exception message
     */
    private static String buildNotFoundMessage(By locator, int timeoutSeconds) {
        return "Element not found: " + locator.toString()
                + " — timeout: " + timeoutSeconds + "s";
    }
}
