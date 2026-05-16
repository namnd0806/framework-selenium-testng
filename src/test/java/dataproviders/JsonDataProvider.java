package dataproviders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.exceptions.DataSourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Data provider that reads test data from JSON files using Jackson.
 *
 * <p>Returns {@code Object[][]} compatible with TestNG {@code @DataProvider}.
 * Supports String, Integer, Boolean, and Double value types as they appear
 * in the JSON document.
 *
 * <p>Expected JSON format — an array of objects where each object represents
 * one test data row:
 * <pre>{@code
 * [
 *   { "username": "admin", "password": "secret", "expected": true },
 *   { "username": "guest", "password": "wrong",  "expected": false }
 * ]
 * }</pre>
 * Each object's values are collected in insertion order and placed into a
 * single {@code Object[]} row.
 *
 * <p>Supports filtering by file name: when a {@code fileNameFilter} is
 * provided, only the file whose name (without path) matches the filter is
 * processed; otherwise the file at {@code filePath} is read directly.
 *
 * <p>Throws {@link DataSourceException} with the file path in the message
 * when the file does not exist or cannot be read.
 *
 * <p>Validates: Requirements 7.1, 7.3, 7.4, 7.5, 7.6
 */
public class JsonDataProvider {

    private static final Logger log = LogManager.getLogger(JsonDataProvider.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonDataProvider() {
        // Utility class — no instantiation
    }

    /**
     * Reads all data rows from the given JSON file.
     *
     * @param filePath absolute or relative path to the .json file
     * @return {@code Object[][]} where each inner array is one data row
     * @throws DataSourceException if the file does not exist or cannot be read
     */
    public static Object[][] getData(String filePath) {
        return getData(filePath, null);
    }

    /**
     * Reads all data rows from the given JSON file, optionally filtered by
     * file name.
     *
     * <p>When {@code fileNameFilter} is non-null and non-empty, the actual
     * file name (last path segment) of {@code filePath} must match the filter
     * (case-insensitive). If it does not match, an empty {@code Object[][]}
     * is returned without reading the file.
     *
     * @param filePath       absolute or relative path to the .json file
     * @param fileNameFilter optional file name filter (e.g. {@code "login.json"});
     *                       pass {@code null} or empty to skip filtering
     * @return {@code Object[][]} where each inner array is one data row,
     *         or an empty array when the filter does not match
     * @throws DataSourceException if the file does not exist or cannot be read
     */
    public static Object[][] getData(String filePath, String fileNameFilter) {
        // Apply file name filter before touching the filesystem
        if (fileNameFilter != null && !fileNameFilter.isEmpty()) {
            String actualFileName = new File(filePath).getName();
            if (!actualFileName.equalsIgnoreCase(fileNameFilter)) {
                log.info("[THREAD-{}] [INFO] [JsonDataProvider] - Skipping file '{}': does not match filter '{}'",
                        Thread.currentThread().getId(), actualFileName, fileNameFilter);
                return new Object[0][0];
            }
        }

        File file = resolveFile(filePath);

        log.info("[THREAD-{}] [INFO] [JsonDataProvider] - Reading JSON file: {}",
                Thread.currentThread().getId(), filePath);

        try {
            List<Map<String, Object>> records = MAPPER.readValue(
                    file, new TypeReference<List<Map<String, Object>>>() {});
            return extractData(records);
        } catch (IOException e) {
            String msg = "Failed to read JSON file: " + filePath;
            log.error("[THREAD-{}] [ERROR] [JsonDataProvider] - {}",
                    Thread.currentThread().getId(), msg, e);
            throw new DataSourceException(msg, e);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves and validates that the file exists and is readable.
     */
    private static File resolveFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new DataSourceException("JSON file path must not be null or blank: " + filePath);
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            String msg = "JSON data file not found: " + filePath;
            log.error("[THREAD-{}] [ERROR] [JsonDataProvider] - {}",
                    Thread.currentThread().getId(), msg);
            throw new DataSourceException(msg);
        }
        return file;
    }

    /**
     * Converts a list of JSON record maps to {@code Object[][]}.
     * Each map's values are collected in insertion order into a row array.
     * Numeric values are normalised: Jackson deserialises JSON integers as
     * {@link Integer} and JSON decimals as {@link Double}, which matches the
     * required type support.
     */
    private static Object[][] extractData(List<Map<String, Object>> records) {
        if (records == null || records.isEmpty()) {
            return new Object[0][0];
        }

        List<Object[]> result = new ArrayList<>();
        for (Map<String, Object> record : records) {
            if (record == null || record.isEmpty()) {
                continue;
            }
            Object[] row = record.values().stream()
                    .map(JsonDataProvider::normaliseValue)
                    .toArray();
            result.add(row);
        }

        return result.toArray(new Object[0][]);
    }

    /**
     * Normalises a Jackson-deserialised value to one of the supported types:
     * String, Integer, Boolean, Double.
     *
     * <p>Jackson already maps JSON booleans → {@link Boolean}, JSON integers
     * → {@link Integer}, JSON decimals → {@link Double}, and JSON strings →
     * {@link String}. Long values (JSON integers that overflow int) are
     * downcast to Integer for consistency; if they overflow, they are kept as
     * Long and callers should handle accordingly.
     */
    static Object normaliseValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Boolean || value instanceof Integer
                || value instanceof Double || value instanceof String) {
            return value;
        }
        if (value instanceof Long) {
            long l = (Long) value;
            if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
            return value; // keep as Long if it overflows int
        }
        if (value instanceof Float) {
            return ((Float) value).doubleValue();
        }
        // Fallback: convert to String
        return value.toString();
    }
}
