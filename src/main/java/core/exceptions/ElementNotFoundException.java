package core.exceptions;

/**
 * Thrown by BasePage when an element cannot be found within the configured
 * explicit wait timeout. The exception message must contain the locator's
 * string representation and the timeout value.
 *
 * Validates: Requirement 4.4
 */
public class ElementNotFoundException extends FrameworkException {

    public ElementNotFoundException(String message) {
        super(message);
    }

    public ElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
