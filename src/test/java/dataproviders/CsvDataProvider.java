package dataproviders;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import core.exceptions.DataSourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data provider that reads test data from CSV files using OpenCSV.
 *
 * <p>Returns {@code Object[][]} compatible with TestNG {@code @DataProvider}.
 * Supports String, Integer, Boolean, and Double value types — values are
 * parsed from the raw CSV strings automatically.
 *
 * <p>The first row of the CSV is treated as a header and is skipped.
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
public class CsvDataProvider {

    private static final Logger log = LogManager.getLogger(CsvDataProvider.class);

    private CsvDataProvider() {
        // Utility class — no instantiation
    }

    /**
     * Reads all data rows from the given CSV file.
     * The first row (header) is skipped.
     *
     * @param filePath absolute or relative path to the .csv file
     * @return {@code Object[][]} where each inner array is one data row
     * @throws DataSourceException if the file does not exist or cannot be read
     */
    public static Object[][] getData(String filePath) {
        return getData(filePath, null);
    }

    /**
     * Reads all data rows from the given CSV file, optionally filtered by
     * file name.
     *
     * <p>When {@code fileNameFilter} is non-null and non-empty, the actual
     * file name (last path segment) of {@code filePath} must match the filter
     * (case-insensitive). If it does not match, an empty {@code Object[][]}
     * is returned without reading the file.
     *
     * @param filePath       absolute or relative path to the .csv file
     * @param fileNameFilter optional file name filter (e.g. {@code "login.csv"});
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
                log.info("[THREAD-{}] [INFO] [CsvDataProvider] - Skipping file '{}': does not match filter '{}'",
                        Thread.currentThread().getId(), actualFileName, fileNameFilter);
                return new Object[0][0];
            }
        }

        File file = resolveFile(filePath);

        log.info("[THREAD-{}] [INFO] [CsvDataProvider] - Reading CSV file: {}",
                Thread.currentThread().getId(), filePath);

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            List<String[]> allRows = reader.readAll();
            return extractData(allRows);
        } catch (IOException | CsvException e) {
            String msg = "Failed to read CSV file: " + filePath;
            log.error("[THREAD-{}] [ERROR] [CsvDataProvider] - {}",
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
            throw new DataSourceException("CSV file path must not be null or blank: " + filePath);
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            String msg = "CSV data file not found: " + filePath;
            log.error("[THREAD-{}] [ERROR] [CsvDataProvider] - {}",
                    Thread.currentThread().getId(), msg);
            throw new DataSourceException(msg);
        }
        return file;
    }

    /**
     * Converts raw CSV rows to {@code Object[][]}, skipping the header row
     * (index 0) and empty rows.
     */
    private static Object[][] extractData(List<String[]> allRows) {
        if (allRows == null || allRows.isEmpty()) {
            return new Object[0][0];
        }

        List<Object[]> result = new ArrayList<>();

        // Row 0 is the header — start from index 1
        for (int i = 1; i < allRows.size(); i++) {
            String[] rawRow = allRows.get(i);
            if (rawRow == null || rawRow.length == 0) {
                continue; // skip empty rows
            }

            Object[] row = new Object[rawRow.length];
            for (int j = 0; j < rawRow.length; j++) {
                row[j] = parseValue(rawRow[j]);
            }
            result.add(row);
        }

        return result.toArray(new Object[0][]);
    }

    /**
     * Parses a raw CSV string value into the most appropriate Java type.
     *
     * <p>Type resolution order:
     * <ol>
     *   <li>Boolean — {@code "true"} or {@code "false"} (case-insensitive)</li>
     *   <li>Integer — whole number without decimal point</li>
     *   <li>Double  — number with decimal point</li>
     *   <li>String  — fallback for everything else</li>
     * </ol>
     */
    static Object parseValue(String raw) {
        if (raw == null) {
            return "";
        }

        String trimmed = raw.trim();

        // Boolean check
        if ("true".equalsIgnoreCase(trimmed)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(trimmed)) {
            return Boolean.FALSE;
        }

        // Integer check (no decimal point)
        if (!trimmed.isEmpty() && !trimmed.contains(".")) {
            try {
                return Integer.parseInt(trimmed);
            } catch (NumberFormatException ignored) {
                // not an integer — fall through
            }
        }

        // Double check
        if (!trimmed.isEmpty()) {
            try {
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException ignored) {
                // not a double — fall through
            }
        }

        return trimmed;
    }
}
