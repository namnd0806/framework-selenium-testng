package core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class providing browser-level operations beyond basic element interaction.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Tab / window management: switch to new tab, switch by index, close current tab</li>
 *   <li>Alert handling: accept, dismiss, get text</li>
 *   <li>Iframe switching: switch into an iframe by locator, return to default content</li>
 *   <li>Screenshot capture: return as {@code byte[]}, or save to a file path</li>
 * </ul>
 *
 * <p>Satisfies Requirement: 10.5</p>
 */
public class BrowserUtils {

    private static final Logger log = LogManager.getLogger(BrowserUtils.class);

    private final WebDriver driver;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Constructs a new {@code BrowserUtils} bound to the given {@link WebDriver}.
     *
     * @param driver the WebDriver instance to operate on (must not be {@code null})
     */
    public BrowserUtils(WebDriver driver) {
        this.driver = driver;
    }

    // -------------------------------------------------------------------------
    // Tab / window management
    // -------------------------------------------------------------------------

    /**
     * Switches focus to the most recently opened browser tab/window.
     *
     * <p>Iterates over all current window handles and switches to the last one
     * in the ordered list, which is typically the newest tab.</p>
     */
    public void switchToNewTab() {
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        if (handles.size() < 2) {
            log.warn("[THREAD-{}] [WARN] [BrowserUtils] - switchToNewTab() called but only {} window handle(s) found.",
                    Thread.currentThread().getId(), handles.size());
            return;
        }
        String newHandle = handles.get(handles.size() - 1);
        driver.switchTo().window(newHandle);
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Switched to new tab: {}",
                Thread.currentThread().getId(), newHandle);
    }

    /**
     * Switches focus to the browser tab/window at the given zero-based {@code index}.
     *
     * @param index zero-based index of the target tab in the ordered list of window handles
     * @throws IndexOutOfBoundsException if {@code index} is out of range for the current
     *                                   number of open windows
     */
    public void switchToTab(int index) {
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        if (index < 0 || index >= handles.size()) {
            throw new IndexOutOfBoundsException(
                    "Tab index " + index + " is out of range — only " + handles.size()
                            + " window handle(s) are open.");
        }
        String targetHandle = handles.get(index);
        driver.switchTo().window(targetHandle);
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Switched to tab index {}: {}",
                Thread.currentThread().getId(), index, targetHandle);
    }

    /**
     * Closes the currently focused browser tab/window.
     *
     * <p>If other tabs remain open, focus is automatically moved to the last tab
     * in the handle list after closing.</p>
     */
    public void closeCurrentTab() {
        String closedHandle = driver.getWindowHandle();
        driver.close();
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Closed tab: {}",
                Thread.currentThread().getId(), closedHandle);

        // Switch focus to the last remaining tab, if any
        List<String> remaining = new ArrayList<>(driver.getWindowHandles());
        if (!remaining.isEmpty()) {
            driver.switchTo().window(remaining.get(remaining.size() - 1));
            log.info("[THREAD-{}] [INFO] [BrowserUtils] - Switched focus to remaining tab: {}",
                    Thread.currentThread().getId(), remaining.get(remaining.size() - 1));
        }
    }

    // -------------------------------------------------------------------------
    // Alert handling
    // -------------------------------------------------------------------------

    /**
     * Accepts (clicks "OK" on) the currently active browser alert.
     *
     * @throws NoAlertPresentException if no alert is currently present
     */
    public void acceptAlert() {
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        alert.accept();
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Accepted alert with text: \"{}\"",
                Thread.currentThread().getId(), alertText);
    }

    /**
     * Dismisses (clicks "Cancel" on) the currently active browser alert.
     *
     * @throws NoAlertPresentException if no alert is currently present
     */
    public void dismissAlert() {
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        alert.dismiss();
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Dismissed alert with text: \"{}\"",
                Thread.currentThread().getId(), alertText);
    }

    /**
     * Returns the text displayed in the currently active browser alert.
     *
     * @return the alert text (never {@code null})
     * @throws NoAlertPresentException if no alert is currently present
     */
    public String getAlertText() {
        String text = driver.switchTo().alert().getText();
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Alert text: \"{}\"",
                Thread.currentThread().getId(), text);
        return text;
    }

    // -------------------------------------------------------------------------
    // Iframe switching
    // -------------------------------------------------------------------------

    /**
     * Switches the WebDriver context into the iframe identified by {@code locator}.
     *
     * @param locator the {@link By} locator strategy for the target {@code <iframe>} element
     * @throws NoSuchElementException if the iframe element cannot be found
     */
    public void switchToIframe(By locator) {
        WebElement iframeElement = driver.findElement(locator);
        driver.switchTo().frame(iframeElement);
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Switched to iframe: {}",
                Thread.currentThread().getId(), locator);
    }

    /**
     * Switches the WebDriver context back to the top-level (default) document,
     * exiting any currently active iframe or nested frame.
     */
    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Switched back to default content.",
                Thread.currentThread().getId());
    }

    // -------------------------------------------------------------------------
    // Screenshot capture
    // -------------------------------------------------------------------------

    /**
     * Captures a screenshot of the current browser viewport and returns it as a
     * raw {@code byte[]} suitable for attaching to Allure or other reporters.
     *
     * @return a non-empty {@code byte[]} containing the PNG screenshot data
     * @throws ClassCastException if the underlying {@link WebDriver} does not
     *                            implement {@link TakesScreenshot}
     */
    public byte[] captureScreenshotAsBytes() {
        byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        log.info("[THREAD-{}] [INFO] [BrowserUtils] - Screenshot captured ({} bytes).",
                Thread.currentThread().getId(), screenshotBytes.length);
        return screenshotBytes;
    }

    /**
     * Captures a screenshot of the current browser viewport and saves it to the
     * specified {@code filePath}.
     *
     * <p>Parent directories are created automatically if they do not exist.</p>
     *
     * @param filePath the absolute or relative path (including filename) where the
     *                 PNG screenshot should be saved
     * @throws RuntimeException wrapping any {@link IOException} that occurs while
     *                          writing the file
     */
    public void captureScreenshotToFile(String filePath) {
        byte[] screenshotBytes = captureScreenshotAsBytes();
        try {
            File targetFile = new File(filePath);
            // Ensure parent directories exist
            File parentDir = targetFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            Files.write(Paths.get(filePath), screenshotBytes);
            log.info("[THREAD-{}] [INFO] [BrowserUtils] - Screenshot saved to: {}",
                    Thread.currentThread().getId(), filePath);
        } catch (IOException e) {
            log.error("[THREAD-{}] [ERROR] [BrowserUtils] - Failed to save screenshot to: {}",
                    Thread.currentThread().getId(), filePath, e);
            throw new RuntimeException("Failed to save screenshot to file: " + filePath, e);
        }
    }
}
