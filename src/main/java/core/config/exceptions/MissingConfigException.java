package core.config.exceptions;

import core.exceptions.FrameworkException;

/**
 * Exception thrown when a required configuration key is absent from all
 * configuration sources (system properties, environment variables, and properties file).
 *
 * <p>The exception message always contains the exact key name that was missing,
 * satisfying Requirement 5.5.</p>
 */
public class MissingConfigException extends FrameworkException {

    private final String missingKey;

    /**
     * Constructs a new MissingConfigException for the given key.
     *
     * @param key the configuration key that was not found in any source
     */
    public MissingConfigException(String key) {
        super("Required configuration key is missing: '" + key + "'");
        this.missingKey = key;
    }

    /**
     * Constructs a new MissingConfigException with a custom message that still
     * contains the key name.
     *
     * @param key     the configuration key that was not found
     * @param message additional context message
     */
    public MissingConfigException(String key, String message) {
        super("Required configuration key is missing: '" + key + "'. " + message);
        this.missingKey = key;
    }

    /**
     * Returns the key name that triggered this exception.
     *
     * @return the missing configuration key
     */
    public String getMissingKey() {
        return missingKey;
    }
}
