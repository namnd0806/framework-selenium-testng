package core.config;

import core.config.exceptions.MissingConfigException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Singleton configuration manager for the Selenium test framework.
 *
 * <p>Reads configuration from profile-specific {@code config-{env}.properties} files
 * located on the classpath, with the following priority chain (highest → lowest):</p>
 * <ol>
 *   <li>System properties ({@code -Dkey=value} on the command line)</li>
 *   <li>Environment variables ({@code KEY=value} in the OS environment)</li>
 *   <li>Profile-specific properties file ({@code config-{env}.properties})</li>
 *   <li>Default properties file ({@code config.properties})</li>
 * </ol>
 *
 * <p>Thread-safe via double-checked locking with a {@code volatile} instance field.</p>
 *
 * <p>Satisfies Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7</p>
 */
public final class ConfigManager {

    // volatile ensures visibility of the fully-constructed instance across threads
    private static volatile ConfigManager instance;

    /** Base (default) properties loaded from config.properties */
    private final Properties defaultProperties;

    /** Profile-specific properties that override the defaults */
    private Properties profileProperties;

    // -------------------------------------------------------------------------
    // Constructor — private to enforce Singleton
    // -------------------------------------------------------------------------

    private ConfigManager() {
        defaultProperties = new Properties();
        profileProperties = new Properties();
        loadDefaultConfig();
    }

    // -------------------------------------------------------------------------
    // Singleton accessor — double-checked locking (Requirement 5.7)
    // -------------------------------------------------------------------------

    /**
     * Returns the single {@code ConfigManager} instance for this JVM process.
     * Uses double-checked locking to guarantee thread-safety without the overhead
     * of synchronizing every call.
     *
     * @return the singleton {@code ConfigManager} instance
     */
    public static ConfigManager getInstance() {
        if (instance == null) {                          // first check (no lock)
            synchronized (ConfigManager.class) {
                if (instance == null) {                  // second check (with lock)
                    instance = new ConfigManager();
                }
            }
        }
        return instance;
    }

    // -------------------------------------------------------------------------
    // Config loading (Requirement 5.4)
    // -------------------------------------------------------------------------

    /**
     * Loads the profile-specific properties file for the given environment name.
     * The file is expected to be at {@code config-{env}.properties} on the classpath.
     *
     * <p>If the profile file is not found, a warning is printed and only the default
     * {@code config.properties} values are used.</p>
     *
     * @param env the environment name (e.g. {@code "dev"}, {@code "staging"}, {@code "production"})
     */
    public void loadConfig(String env) {
        if (env == null || env.trim().isEmpty()) {
            return;
        }
        String profileFile = "config-" + env.trim() + ".properties";
        Properties loaded = loadPropertiesFromClasspath(profileFile);
        if (loaded != null) {
            profileProperties = loaded;
        } else {
            System.out.println("[ConfigManager] WARN: Profile config file not found on classpath: "
                    + profileFile + ". Falling back to default config.");
        }
    }

    // -------------------------------------------------------------------------
    // Type-safe getters (Requirement 5.6)
    // -------------------------------------------------------------------------

    /**
     * Returns the configuration value for {@code key} as a {@code String}.
     * Throws {@link MissingConfigException} if the key is absent from all sources.
     *
     * @param key the configuration key
     * @return the resolved value
     * @throws MissingConfigException if the key is not found in any source
     */
    public String getString(String key) {
        String value = resolveValue(key);
        if (value == null) {
            throw new MissingConfigException(key);
        }
        return value;
    }

    /**
     * Returns the configuration value for {@code key} as a {@code String},
     * or {@code defaultValue} if the key is absent from all sources.
     *
     * @param key          the configuration key
     * @param defaultValue the fallback value
     * @return the resolved value, or {@code defaultValue}
     */
    public String getString(String key, String defaultValue) {
        String value = resolveValue(key);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Returns the configuration value for {@code key} as an {@code int}.
     * Throws {@link MissingConfigException} if the key is absent from all sources.
     *
     * @param key the configuration key
     * @return the resolved integer value
     * @throws MissingConfigException if the key is not found in any source
     * @throws NumberFormatException  if the value cannot be parsed as an integer
     */
    public int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    /**
     * Returns the configuration value for {@code key} as an {@code int},
     * or {@code defaultValue} if the key is absent from all sources.
     *
     * @param key          the configuration key
     * @param defaultValue the fallback integer value
     * @return the resolved integer value, or {@code defaultValue}
     */
    public int getInt(String key, int defaultValue) {
        String value = resolveValue(key);
        if (value == null) {
            return defaultValue;
        }
        return Integer.parseInt(value);
    }

    /**
     * Returns the configuration value for {@code key} as a {@code boolean}.
     * Throws {@link MissingConfigException} if the key is absent from all sources.
     *
     * @param key the configuration key
     * @return {@code true} if the value equals {@code "true"} (case-insensitive)
     * @throws MissingConfigException if the key is not found in any source
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

    /**
     * Returns the configuration value for {@code key} as a {@code boolean},
     * or {@code defaultValue} if the key is absent from all sources.
     *
     * @param key          the configuration key
     * @param defaultValue the fallback boolean value
     * @return the resolved boolean value, or {@code defaultValue}
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = resolveValue(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    // -------------------------------------------------------------------------
    // Priority resolution (Requirement 5.3, 9.6)
    // -------------------------------------------------------------------------

    /**
     * Resolves the value for {@code key} using the priority chain:
     * <ol>
     *   <li>System property</li>
     *   <li>Environment variable (key as-is, then uppercased with dots replaced by underscores)</li>
     *   <li>Profile-specific properties file</li>
     *   <li>Default properties file</li>
     * </ol>
     *
     * @param key the configuration key
     * @return the resolved value, or {@code null} if not found in any source
     */
    private String resolveValue(String key) {
        // 1. System property (highest priority)
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isEmpty()) {
            return sysProp;
        }

        // 2. Environment variable — try exact key first, then UPPER_SNAKE_CASE
        String envValue = System.getenv(key);
        if (envValue == null || envValue.isEmpty()) {
            // Convert "base.url" → "BASE_URL"
            String envKey = key.toUpperCase().replace('.', '_').replace('-', '_');
            envValue = System.getenv(envKey);
        }
        if (envValue != null && !envValue.isEmpty()) {
            return envValue;
        }

        // 3. Profile-specific properties file
        String profileValue = profileProperties.getProperty(key);
        if (profileValue != null && !profileValue.isEmpty()) {
            return profileValue;
        }

        // 4. Default properties file (lowest priority)
        return defaultProperties.getProperty(key);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Loads {@code config.properties} from the classpath into {@link #defaultProperties}.
     * Silently ignores missing file (properties will simply be empty).
     */
    private void loadDefaultConfig() {
        Properties loaded = loadPropertiesFromClasspath("config.properties");
        if (loaded != null) {
            defaultProperties.putAll(loaded);
        }
    }

    /**
     * Loads a {@link Properties} object from the given classpath resource name.
     *
     * @param resourceName the classpath resource name (e.g. {@code "config.properties"})
     * @return the loaded {@link Properties}, or {@code null} if the resource was not found
     */
    private Properties loadPropertiesFromClasspath(String resourceName) {
        Properties props = new Properties();
        // Try thread context class loader first, then fall back to this class's loader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ConfigManager.class.getClassLoader();
        }
        try (InputStream is = cl.getResourceAsStream(resourceName)) {
            if (is == null) {
                return null;
            }
            props.load(is);
            return props;
        } catch (IOException e) {
            System.err.println("[ConfigManager] ERROR: Failed to load config file: "
                    + resourceName + " — " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Package-private helper for testing — allows resetting the singleton
    // -------------------------------------------------------------------------

    /**
     * Resets the singleton instance. <strong>For testing purposes only.</strong>
     * This method is intentionally package-private so that only test code in the
     * same package (or via reflection) can call it.
     */
    static void resetInstance() {
        synchronized (ConfigManager.class) {
            instance = null;
        }
    }
}
