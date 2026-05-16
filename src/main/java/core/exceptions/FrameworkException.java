package core.exceptions;

/**
 * Base exception for all framework-level errors.
 * All custom exceptions in the Selenium Test Framework extend this class.
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
