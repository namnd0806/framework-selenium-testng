package core.report;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;

/**
 * TestNG listener that integrates with Allure Report to capture test metadata
 * and lifecycle events.
 *
 * <p>Applies the <strong>Observer</strong> pattern: this class listens to TestNG
 * lifecycle events ({@code onTestStart}, {@code onTestSuccess}, {@code onTestFailure},
 * {@code onTestSkipped}) and reacts by writing structured data into the Allure report
 * without any coupling to the test code itself.</p>
 *
 * <p>Register this listener in the TestNG suite XML:</p>
 * <pre>{@code
 * <listeners>
 *     <listener class-name="core.report.AllureListener"/>
 * </listeners>
 * }</pre>
 *
 * <p>Or annotate individual test classes:</p>
 * <pre>{@code
 * @Listeners(AllureListener.class)
 * public class LoginTest extends BaseTest { ... }
 * }</pre>
 *
 * <p>Satisfies Requirements: 6.1, 6.2, 6.4, 6.6</p>
 */
public class AllureListener implements ITestListener {

    private static final Logger log = LogManager.getLogger(AllureListener.class);

    // -------------------------------------------------------------------------
    // ITestListener — lifecycle callbacks
    // -------------------------------------------------------------------------

    /**
     * Called by TestNG immediately before a test method is invoked.
     *
     * <p>Adds a descriptive label to the Allure report indicating which thread
     * is executing the test, satisfying the parallel-execution traceability
     * requirement (Requirement 6.4).</p>
     *
     * @param result the {@link ITestResult} for the test that is about to start
     */
    @Override
    public void onTestStart(ITestResult result) {
        long threadId = Thread.currentThread().getId();
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();

        log.info("[THREAD-{}] [INFO] [AllureListener] - Test STARTED: {}.{}",
                threadId, className, testName);

        // Attach thread label so Allure report shows which thread ran this test
        Allure.label("thread", "THREAD-" + threadId);
    }

    /**
     * Called by TestNG after a test method completes successfully.
     *
     * <p>Attaches metadata (browser, thread ID, duration) to the Allure report
     * entry for this test (Requirement 6.2, 6.4).</p>
     *
     * @param result the {@link ITestResult} for the test that passed
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        long threadId = Thread.currentThread().getId();
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();

        log.info("[THREAD-{}] [INFO] [AllureListener] - Test PASSED: {}.{}",
                threadId, className, testName);

        attachTestMetadata(result);
    }

    /**
     * Called by TestNG after a test method fails (throws an exception or assertion error).
     *
     * <p>Logs an ERROR-level entry with the format required by the logging convention
     * (Requirement 6.6) and attaches full metadata to the Allure report.</p>
     *
     * <p>Log format: {@code [THREAD-{threadId}] [ERROR] [{className}] - Test FAILED: {testName} - {errorMessage}}</p>
     *
     * @param result the {@link ITestResult} for the test that failed
     */
    @Override
    public void onTestFailure(ITestResult result) {
        long threadId = Thread.currentThread().getId();
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();

        // Derive the error message — use the throwable message if available
        Throwable throwable = result.getThrowable();
        String errorMessage = (throwable != null && throwable.getMessage() != null)
                ? throwable.getMessage()
                : "No error message available";

        // Required log format (Requirement 6.6, Property 12)
        log.error("[THREAD-{}] [ERROR] [{}] - Test FAILED: {} - {}",
                threadId, className, testName, errorMessage);

        // Attach stack trace to Allure for easier debugging (Requirement 6.4)
        if (throwable != null) {
            String stackTrace = buildStackTraceString(throwable);
            Allure.addAttachment("Stack Trace", "text/plain",
                    new ByteArrayInputStream(stackTrace.getBytes()), ".txt");
        }

        attachTestMetadata(result);
    }

    /**
     * Called by TestNG when a test method is skipped (e.g., a dependency failed
     * or the test was explicitly skipped via {@code throw new SkipException(...)}).
     *
     * <p>Logs an INFO-level entry and attaches metadata to the Allure report
     * (Requirement 6.1).</p>
     *
     * @param result the {@link ITestResult} for the test that was skipped
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        long threadId = Thread.currentThread().getId();
        String testName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();

        log.warn("[THREAD-{}] [WARN] [AllureListener] - Test SKIPPED: {}.{}",
                threadId, className, testName);

        attachTestMetadata(result);
    }

    // -------------------------------------------------------------------------
    // Metadata attachment
    // -------------------------------------------------------------------------

    /**
     * Attaches structured test metadata to the current Allure report entry.
     *
     * <p>Metadata attached:</p>
     * <ul>
     *   <li><strong>Browser</strong> — read from the {@code browser} system property
     *       (set by Maven Surefire via {@code -Dbrowser=chrome}); falls back to
     *       {@code "unknown"} if not set.</li>
     *   <li><strong>Thread ID</strong> — the OS-level thread identifier of the
     *       executing thread, enabling correlation of parallel test output.</li>
     *   <li><strong>Duration (ms)</strong> — wall-clock duration of the test in
     *       milliseconds, computed from {@link ITestResult#getStartMillis()} and
     *       {@link ITestResult#getEndMillis()}.</li>
     *   <li><strong>Test Status</strong> — the final TestNG status string
     *       (PASS / FAIL / SKIP).</li>
     * </ul>
     *
     * <p>Satisfies Requirements: 6.2, 6.4</p>
     *
     * @param result the {@link ITestResult} whose metadata should be attached
     */
    private void attachTestMetadata(ITestResult result) {
        long threadId = Thread.currentThread().getId();

        // Browser — provided by Maven Surefire as a system property
        String browser = System.getProperty("browser", "unknown");

        // Duration — TestNG populates endMillis after the test completes;
        // for onTestStart it will be 0, which is acceptable.
        long startMillis = result.getStartMillis();
        long endMillis = result.getEndMillis();
        long durationMs = (endMillis > 0) ? (endMillis - startMillis) : 0L;

        // Map TestNG status integer to a human-readable string
        String statusLabel = resolveStatusLabel(result.getStatus());

        // Build a compact metadata block and attach it as plain text
        String metadata = String.format(
                "Test Name  : %s%n"
                        + "Class      : %s%n"
                        + "Browser    : %s%n"
                        + "Thread ID  : %d%n"
                        + "Duration   : %d ms%n"
                        + "Status     : %s%n",
                result.getMethod().getMethodName(),
                result.getTestClass().getRealClass().getSimpleName(),
                browser,
                threadId,
                durationMs,
                statusLabel
        );

        Allure.addAttachment("Test Metadata", "text/plain",
                new ByteArrayInputStream(metadata.getBytes()), ".txt");

        // Also expose individual values as Allure parameters so they appear
        // in the Parameters tab of the Allure report (Requirement 6.4)
        Allure.parameter("browser", browser);
        Allure.parameter("threadId", String.valueOf(threadId));
        Allure.parameter("durationMs", String.valueOf(durationMs));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a TestNG {@link ITestResult} status integer to a human-readable label.
     *
     * @param status one of {@link ITestResult#SUCCESS}, {@link ITestResult#FAILURE},
     *               {@link ITestResult#SKIP}, etc.
     * @return a descriptive string such as {@code "PASS"}, {@code "FAIL"}, or {@code "SKIP"}
     */
    private String resolveStatusLabel(int status) {
        return switch (status) {
            case ITestResult.SUCCESS -> "PASS";
            case ITestResult.FAILURE -> "FAIL";
            case ITestResult.SKIP -> "SKIP";
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE -> "PARTIAL_FAIL";
            case ITestResult.STARTED -> "STARTED";
            default -> "UNKNOWN";
        };
    }

    /**
     * Builds a formatted stack trace string from a {@link Throwable}.
     *
     * @param throwable the exception whose stack trace should be formatted
     * @return a multi-line string containing the exception class, message, and stack frames
     */
    private String buildStackTraceString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName());
        if (throwable.getMessage() != null) {
            sb.append(": ").append(throwable.getMessage());
        }
        sb.append(System.lineSeparator());

        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append(System.lineSeparator());
        }

        // Include cause chain
        Throwable cause = throwable.getCause();
        if (cause != null) {
            sb.append("Caused by: ").append(buildStackTraceString(cause));
        }

        return sb.toString();
    }
}
