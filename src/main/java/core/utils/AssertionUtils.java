package core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;

import java.util.List;

/**
 * Utility class that wraps TestNG {@link Assert} methods with descriptive failure messages
 * and ERROR-level logging before the assertion exception is propagated.
 *
 * <p>All methods are {@code static}. Every failure message contains the {@code context}
 * string verbatim so that test reports clearly identify which assertion failed and why.</p>
 *
 * <p>Satisfies Requirements: 10.4, 10.6</p>
 */
public final class AssertionUtils {

    private static final Logger log = LogManager.getLogger(AssertionUtils.class);

    /** Utility class — no instantiation. */
    private AssertionUtils() {
        throw new UnsupportedOperationException("AssertionUtils is a utility class and cannot be instantiated.");
    }

    // -------------------------------------------------------------------------
    // assertEquals
    // -------------------------------------------------------------------------

    /**
     * Asserts that {@code actual} equals {@code expected}.
     *
     * <p>Failure message format:
     * {@code [<context>] Expected: <expected>, but was: <actual>}</p>
     *
     * @param actual   the actual value
     * @param expected the expected value
     * @param context  a descriptive label for this assertion (included verbatim in the failure message)
     * @throws AssertionError if {@code actual} does not equal {@code expected}
     */
    public static void assertEquals(Object actual, Object expected, String context) {
        String message = buildMessage(context,
                "Expected: [" + expected + "], but was: [" + actual + "]");
        try {
            Assert.assertEquals(actual, expected, message);
        } catch (AssertionError e) {
            log.error("[AssertionUtils] Assertion FAILED — {}", message);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // assertTrue
    // -------------------------------------------------------------------------

    /**
     * Asserts that {@code condition} is {@code true}.
     *
     * <p>Failure message format:
     * {@code [<context>] Expected condition to be TRUE, but was FALSE}</p>
     *
     * @param condition the boolean condition to evaluate
     * @param context   a descriptive label for this assertion (included verbatim in the failure message)
     * @throws AssertionError if {@code condition} is {@code false}
     */
    public static void assertTrue(boolean condition, String context) {
        String message = buildMessage(context, "Expected condition to be TRUE, but was FALSE");
        try {
            Assert.assertTrue(condition, message);
        } catch (AssertionError e) {
            log.error("[AssertionUtils] Assertion FAILED — {}", message);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // assertFalse
    // -------------------------------------------------------------------------

    /**
     * Asserts that {@code condition} is {@code false}.
     *
     * <p>Failure message format:
     * {@code [<context>] Expected condition to be FALSE, but was TRUE}</p>
     *
     * @param condition the boolean condition to evaluate
     * @param context   a descriptive label for this assertion (included verbatim in the failure message)
     * @throws AssertionError if {@code condition} is {@code true}
     */
    public static void assertFalse(boolean condition, String context) {
        String message = buildMessage(context, "Expected condition to be FALSE, but was TRUE");
        try {
            Assert.assertFalse(condition, message);
        } catch (AssertionError e) {
            log.error("[AssertionUtils] Assertion FAILED — {}", message);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // assertNotNull
    // -------------------------------------------------------------------------

    /**
     * Asserts that {@code object} is not {@code null}.
     *
     * <p>Failure message format:
     * {@code [<context>] Expected object to be NOT NULL, but was NULL}</p>
     *
     * @param object  the object to check
     * @param context a descriptive label for this assertion (included verbatim in the failure message)
     * @throws AssertionError if {@code object} is {@code null}
     */
    public static void assertNotNull(Object object, String context) {
        String message = buildMessage(context, "Expected object to be NOT NULL, but was NULL");
        try {
            Assert.assertNotNull(object, message);
        } catch (AssertionError e) {
            log.error("[AssertionUtils] Assertion FAILED — {}", message);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // assertNull
    // -------------------------------------------------------------------------

    /**
     * Asserts that {@code object} is {@code null}.
     *
     * <p>Failure message format:
     * {@code [<context>] Expected object to be NULL, but was: <object>}</p>
     *
     * @param object  the object to check
     * @param context a descriptive label for this assertion (included verbatim in the failure message)
     * @throws AssertionError if {@code object} is not {@code null}
     */
    public static void assertNull(Object object, String context) {
        String message = buildMessage(context,
                "Expected object to be NULL, but was: [" + object + "]");
        try {
            Assert.assertNull(object, message);
        } catch (AssertionError e) {
            log.error("[AssertionUtils] Assertion FAILED — {}", message);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // assertContains
    // -------------------------------------------------------------------------

    /**
     * Asserts that the {@code actual} string contains the {@code expected} substring.
     *
     * <p>Failure message format:
     * {@code [<context>] Expected string to contain: [<expected>], but actual was: [<actual>]}</p>
     *
     * @param actual   the string to search within
     * @param expected the substring that must be present in {@code actual}
     * @param context  a descriptive label for this assertion (included verbatim in the failure message)
     * @throws AssertionError if {@code actual} does not contain {@code expected}
     */
    public static void assertContains(String actual, String expected, String context) {
        String message = buildMessage(context,
                "Expected string to contain: [" + expected + "], but actual was: [" + actual + "]");
        try {
            Assert.assertTrue(
                    actual != null && actual.contains(expected),
                    message
            );
        } catch (AssertionError e) {
            log.error("[AssertionUtils] Assertion FAILED — {}", message);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // assertListEquals
    // -------------------------------------------------------------------------

    /**
     * Asserts that {@code actual} list equals {@code expected} list (same size, same elements
     * in the same order).
     *
     * <p>Failure message format:
     * {@code [<context>] Expected list: <expected>, but was: <actual>}</p>
     *
     * @param actual   the actual list
     * @param expected the expected list
     * @param context  a descriptive label for this assertion (included verbatim in the failure message)
     * @throws AssertionError if the lists are not equal
     */
    public static void assertListEquals(List<?> actual, List<?> expected, String context) {
        String message = buildMessage(context,
                "Expected list: " + expected + ", but was: " + actual);
        try {
            Assert.assertEquals(actual, expected, message);
        } catch (AssertionError e) {
            log.error("[AssertionUtils] Assertion FAILED — {}", message);
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a failure message that always contains the {@code context} string verbatim
     * as a prefix, followed by the assertion-specific detail.
     *
     * @param context the caller-supplied context label
     * @param detail  the assertion-specific detail message
     * @return the combined failure message
     */
    private static String buildMessage(String context, String detail) {
        return "[" + context + "] " + detail;
    }
}
