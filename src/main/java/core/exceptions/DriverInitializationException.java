package core.exceptions;

/**
 * Thrown by WebDriverManager when a WebDriver instance fails to initialize.
 *
 * Validates: Requirement 2.3 (driver lifecycle management)
 */
public class DriverInitializationException extends FrameworkException {

    public DriverInitializationException(String message) {
        super(message);
    }

    public DriverInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
