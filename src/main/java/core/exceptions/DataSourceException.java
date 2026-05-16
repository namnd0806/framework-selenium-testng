package core.exceptions;

/**
 * Thrown by DataProvider classes when a data source file cannot be found
 * or parsed. The exception message must contain the file path that was
 * not found or could not be read.
 *
 * Validates: Requirement 7.4
 */
public class DataSourceException extends FrameworkException {

    public DataSourceException(String message) {
        super(message);
    }

    public DataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
