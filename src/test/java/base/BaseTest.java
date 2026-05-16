package base;

/**
 * Root abstract base class for all test cases in the framework.
 *
 * <p>Provides only the hook methods shared by all test types (UI, API, etc.).
 * Does NOT manage WebDriver or any browser-specific lifecycle — that belongs
 * in {@link UIBaseTest}.</p>
 *
 * <p>Hierarchy:</p>
 * <pre>
 *   BaseTest
 *     └── UIBaseTest   (Selenium WebDriver lifecycle)
 *           └── YourPageTest
 * </pre>
 */
public abstract class BaseTest {

    /**
     * Hook called before each test method runs.
     * Override to initialise Page Objects, navigate to starting URL, etc.
     * Default implementation is empty.
     */
    protected void beforeTest() {
        // Empty by default — subclasses override as needed
    }

    /**
     * Hook called after each test method completes (before driver quit).
     * Override to perform cleanup, logout, reset state, etc.
     * Default implementation is empty.
     */
    protected void afterTest() {
        // Empty by default — subclasses override as needed
    }
}
