package base;

import core.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Strategy-pattern implementation of TestNG's {@link IRetryAnalyzer}.
 *
 * <p>Decides whether a failed test should be retried based on two criteria:</p>
 * <ol>
 *   <li>The failure is caused by a <em>technical</em> exception (not an {@link AssertionError}
 *       or any of its subclasses).</li>
 *   <li>The number of retries for this test instance has not yet reached the configured
 *       maximum ({@code retry.count} in {@code config.properties}, default {@code 1}).</li>
 * </ol>
 *
 * <p>Usage — annotate a test method or class:</p>
 * <pre>{@code
 * @Test(retryAnalyzer = RetryAnalyzer.class)
 * public void myTest() { ... }
 * }</pre>
 *
 * <p>Satisfies Requirements: 8.1, 8.2, 8.5</p>
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);

    /** Number of retry attempts already made for the current test instance. */
    private int retryCount = 0;

    /**
     * Maximum number of retries allowed, read once from {@link ConfigManager}.
     * Defaults to {@code 1} if the key is absent.
     */
    private final int maxRetry = ConfigManager.getInstance().getInt("retry.count", 1);

    // -------------------------------------------------------------------------
    // IRetryAnalyzer implementation
    // -------------------------------------------------------------------------

    /**
     * Called by TestNG after each test failure to determine whether the test
     * should be re-run.
     *
     * <p>Returns {@code true} (retry) only when <em>both</em> conditions hold:</p>
     * <ul>
     *   <li>The failure throwable is not an {@link AssertionError} (or subclass).</li>
     *   <li>The current retry count is strictly less than {@code maxRetry}.</li>
     * </ul>
     *
     * @param result the {@link ITestResult} of the failed test
     * @return {@code true} if the test should be retried, {@code false} otherwise
     */
    @Override
    public boolean retry(ITestResult result) {
        Throwable throwable = result.getThrowable();

        // Never retry assertion failures — they indicate a genuine test failure,
        // not a transient technical issue (Requirement 8.5).
        if (!isRetryableException(throwable)) {
            return false;
        }

        // Retry only if we have not yet exhausted the configured retry budget
        // (Requirement 8.1).
        if (retryCount < maxRetry) {
            retryCount++;
            long threadId = Thread.currentThread().getId();
            String testName = result.getMethod().getMethodName();
            log.warn("[THREAD-{}] [WARN] [RetryAnalyzer] - Retrying test: {} (attempt {}/{})",
                    threadId, testName, retryCount, maxRetry);
            return true;
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // Helper — exception classification
    // -------------------------------------------------------------------------

    /**
     * Determines whether the given {@link Throwable} represents a retryable
     * technical failure.
     *
     * <p>An {@link AssertionError} (or any subclass) is <strong>not</strong>
     * retryable because it signals a deliberate test assertion that failed,
     * not a transient infrastructure problem (Requirement 8.5).</p>
     *
     * <p>A {@code null} throwable (no exception recorded) is treated as
     * non-retryable to avoid retrying tests that failed for unknown reasons.</p>
     *
     * @param throwable the exception that caused the test to fail, may be {@code null}
     * @return {@code false} if {@code throwable} is {@code null} or an
     *         {@link AssertionError} / subclass; {@code true} otherwise
     */
    private boolean isRetryableException(Throwable throwable) {
        if (throwable == null) {
            return false;
        }
        // AssertionError and all its subclasses are NOT retryable
        if (throwable instanceof AssertionError) {
            return false;
        }
        return true;
    }
}
