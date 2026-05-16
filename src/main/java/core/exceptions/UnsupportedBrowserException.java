package core.exceptions;

/**
 * Thrown by DriverFactory when an unsupported browser name is specified.
 * The exception message must contain the invalid browser name.
 *
 * Validates: Requirement 2.6
 */
public class UnsupportedBrowserException extends FrameworkException {

    public UnsupportedBrowserException(String message) {
        super(message);
    }

    public UnsupportedBrowserException(String message, Throwable cause) {
        super(message, cause);
    }
}
