package core.report;

import io.qameta.allure.Attachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for capturing screenshots and attaching them to Allure reports.
 * <p>
 * Supports three modes:
 * <ul>
 *   <li>{@link #captureScreenshot(WebDriver)} — returns raw bytes for programmatic use</li>
 *   <li>{@link #captureAndSave(WebDriver, String)} — persists PNG to {@code target/screenshots/}</li>
 *   <li>{@link #attachToAllure(WebDriver)} — attaches screenshot directly to the Allure report</li>
 * </ul>
 *
 * Requirements: 6.3
 */
public class ScreenshotCapturer {

    private static final Logger log = LogManager.getLogger(ScreenshotCapturer.class);

    private static final String SCREENSHOTS_DIR = "target/screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    // Private constructor — all methods are static
    private ScreenshotCapturer() {}

    /**
     * Captures a screenshot from the given WebDriver and returns it as a byte array.
     *
     * @param driver the WebDriver instance (must implement {@link TakesScreenshot})
     * @return non-empty byte array containing the PNG screenshot data
     * @throws IllegalArgumentException if driver is null or does not support screenshots
     */
    public static byte[] captureScreenshot(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver must not be null");
        }
        if (!(driver instanceof TakesScreenshot)) {
            throw new IllegalArgumentException(
                    "WebDriver does not support screenshot capture: " + driver.getClass().getName());
        }

        try {
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            log.debug("[THREAD-{}] Screenshot captured — {} bytes",
                    Thread.currentThread().getId(), screenshotBytes.length);
            return screenshotBytes;
        } catch (Exception e) {
            log.error("[THREAD-{}] Failed to capture screenshot: {}",
                    Thread.currentThread().getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Captures a screenshot and saves it to {@code target/screenshots/{testName}_{timestamp}.png}.
     *
     * @param driver   the WebDriver instance
     * @param testName the name of the test (used in the filename)
     * @return the absolute path of the saved screenshot file
     */
    public static String captureAndSave(WebDriver driver, String testName) {
        byte[] screenshotBytes = captureScreenshot(driver);

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        // Sanitize testName to be filesystem-safe
        String safeName = testName == null ? "unknown" : testName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        String fileName = safeName + "_" + timestamp + ".png";

        Path screenshotsDir = Paths.get(SCREENSHOTS_DIR);
        Path filePath = screenshotsDir.resolve(fileName);

        try {
            Files.createDirectories(screenshotsDir);
            Files.write(filePath, screenshotBytes);
            String absolutePath = filePath.toAbsolutePath().toString();
            log.info("[THREAD-{}] Screenshot saved: {}",
                    Thread.currentThread().getId(), absolutePath);
            return absolutePath;
        } catch (IOException e) {
            log.error("[THREAD-{}] Failed to save screenshot to {}: {}",
                    Thread.currentThread().getId(), filePath, e.getMessage(), e);
            throw new RuntimeException("Failed to save screenshot: " + filePath, e);
        }
    }

    /**
     * Captures a screenshot and attaches it to the current Allure report step.
     * <p>
     * The {@code @Attachment} annotation instructs Allure to embed the returned
     * byte array as an image attachment named "Screenshot on Failure".
     *
     * @param driver the WebDriver instance
     * @return the screenshot bytes (also attached to Allure via the annotation)
     */
    @Attachment(value = "Screenshot on Failure", type = "image/png")
    public static byte[] attachToAllure(WebDriver driver) {
        log.debug("[THREAD-{}] Attaching screenshot to Allure report",
                Thread.currentThread().getId());
        return captureScreenshot(driver);
    }
}
