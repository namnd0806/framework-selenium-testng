package core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Utility class that wraps common JavaScript operations performed via
 * {@link JavascriptExecutor}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Scroll the viewport to a specific element, to the top, or to the bottom</li>
 *   <li>Highlight / remove highlight on an element for visual debugging</li>
 *   <li>Retrieve element attributes via JavaScript (bypasses Selenium attribute caching)</li>
 *   <li>Click and set values on elements that are not reachable through standard Selenium
 *       interactions (e.g., hidden or overlapping elements)</li>
 * </ul>
 *
 * <p>Satisfies Requirement: 10.2</p>
 */
public class JavaScriptUtils {

    private static final Logger log = LogManager.getLogger(JavaScriptUtils.class);

    /** CSS style applied when an element is highlighted. */
    private static final String HIGHLIGHT_STYLE =
            "arguments[0].setAttribute('style', 'border: 3px solid red; background-color: yellow;');";

    /** CSS style used to remove the highlight from an element. */
    private static final String REMOVE_HIGHLIGHT_STYLE =
            "arguments[0].setAttribute('style', '');";

    private final JavascriptExecutor js;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@code JavaScriptUtils} instance by casting the supplied
     * {@link WebDriver} to a {@link JavascriptExecutor}.
     *
     * @param driver the WebDriver instance (must implement {@link JavascriptExecutor})
     * @throws ClassCastException if the driver does not implement {@link JavascriptExecutor}
     */
    public JavaScriptUtils(WebDriver driver) {
        this.js = (JavascriptExecutor) driver;
    }

    // -------------------------------------------------------------------------
    // Scroll operations
    // -------------------------------------------------------------------------

    /**
     * Scrolls the browser viewport so that {@code element} is centred in view.
     *
     * @param element the target {@link WebElement}
     */
    public void scrollToElement(WebElement element) {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Scrolling to element: {}",
                Thread.currentThread().getId(), element);
        js.executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});",
                element);
    }

    /**
     * Scrolls the browser viewport to the very top of the page.
     */
    public void scrollToTop() {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Scrolling to top of page.",
                Thread.currentThread().getId());
        js.executeScript("window.scrollTo({top: 0, behavior: 'smooth'});");
    }

    /**
     * Scrolls the browser viewport to the very bottom of the page.
     */
    public void scrollToBottom() {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Scrolling to bottom of page.",
                Thread.currentThread().getId());
        js.executeScript("window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});");
    }

    // -------------------------------------------------------------------------
    // Highlight operations
    // -------------------------------------------------------------------------

    /**
     * Highlights {@code element} with a red border and yellow background using
     * JavaScript — useful for visual debugging during test development.
     *
     * @param element the {@link WebElement} to highlight
     */
    public void highlightElement(WebElement element) {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Highlighting element: {}",
                Thread.currentThread().getId(), element);
        js.executeScript(HIGHLIGHT_STYLE, element);
    }

    /**
     * Removes any inline style previously applied to {@code element} by
     * {@link #highlightElement(WebElement)}.
     *
     * @param element the {@link WebElement} whose highlight should be removed
     */
    public void removeHighlight(WebElement element) {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Removing highlight from element: {}",
                Thread.currentThread().getId(), element);
        js.executeScript(REMOVE_HIGHLIGHT_STYLE, element);
    }

    // -------------------------------------------------------------------------
    // Attribute retrieval
    // -------------------------------------------------------------------------

    /**
     * Retrieves the value of the specified {@code attribute} from {@code element}
     * using JavaScript.
     *
     * <p>This bypasses Selenium's own attribute-retrieval mechanism and is useful
     * when the attribute value is set dynamically by client-side scripts.</p>
     *
     * @param element   the target {@link WebElement}
     * @param attribute the name of the attribute to retrieve
     * @return the attribute value as an {@link Object}, or {@code null} if absent
     */
    public Object getAttribute(WebElement element, String attribute) {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Getting attribute '{}' from element: {}",
                Thread.currentThread().getId(), attribute, element);
        return js.executeScript(
                "return arguments[0].getAttribute(arguments[1]);",
                element, attribute);
    }

    // -------------------------------------------------------------------------
    // Interaction operations
    // -------------------------------------------------------------------------

    /**
     * Clicks {@code element} via JavaScript.
     *
     * <p>Use this when the standard {@link WebElement#click()} is blocked by an
     * overlapping element or when the element is not in the visible viewport.</p>
     *
     * @param element the {@link WebElement} to click
     */
    public void clickByJS(WebElement element) {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Clicking element via JS: {}",
                Thread.currentThread().getId(), element);
        js.executeScript("arguments[0].click();", element);
    }

    /**
     * Sets the {@code value} property of {@code element} via JavaScript.
     *
     * <p>Useful for input fields that reject keyboard events (e.g., date pickers,
     * custom components) or when {@link WebElement#sendKeys(CharSequence...)} is
     * not applicable.</p>
     *
     * @param element the target {@link WebElement}
     * @param value   the value to set
     */
    public void setValueByJS(WebElement element, String value) {
        log.debug("[THREAD-{}] [DEBUG] [JavaScriptUtils] - Setting value '{}' on element via JS: {}",
                Thread.currentThread().getId(), value, element);
        js.executeScript("arguments[0].value = arguments[1];", element, value);
    }
}
