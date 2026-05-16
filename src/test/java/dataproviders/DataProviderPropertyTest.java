package dataproviders;

import core.exceptions.DataSourceException;
import net.jqwik.api.*;

import java.util.UUID;

/**
 * Property-based tests for DataProvider classes.
 *
 * <p><strong>Property 9: DataProvider Exception Contains File Path</strong><br>
 * For any non-existent file path, each DataProvider (ExcelDataProvider,
 * CsvDataProvider, JsonDataProvider) must throw a {@link DataSourceException}
 * whose message contains the supplied file path.
 *
 * <p><strong>Validates: Requirements 7.4</strong>
 *
 * <p>Tag: Feature: selenium-test-framework, Property 9: DataProvider Exception Contains File Path
 */
class DataProviderPropertyTest {

    // -------------------------------------------------------------------------
    // Arbitrary generators
    // -------------------------------------------------------------------------

    /**
     * Generates arbitrary non-existent file paths.
     * Paths are constructed as absolute-looking paths with random UUID segments
     * to guarantee they do not exist on the filesystem.
     */
    @Provide
    Arbitrary<String> nonExistentFilePaths() {
        // Generate paths that are guaranteed not to exist:
        // e.g. /nonexistent_<uuid>/<uuid>.xlsx
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(4)
                .ofMaxLength(12)
                .map(prefix -> System.getProperty("java.io.tmpdir")
                        + java.io.File.separator
                        + "nonexistent_" + prefix + "_" + UUID.randomUUID()
                        + java.io.File.separator
                        + UUID.randomUUID() + ".dat");
    }

    // -------------------------------------------------------------------------
    // Property 9a — ExcelDataProvider
    // -------------------------------------------------------------------------

    /**
     * Property 9a: ExcelDataProvider throws DataSourceException whose message
     * contains the non-existent file path.
     *
     * <p><strong>Validates: Requirements 7.4</strong>
     */
    @Property(tries = 100)
    void excelDataProviderExceptionContainsFilePath(
            @ForAll("nonExistentFilePaths") String filePath) {

        try {
            ExcelDataProvider.getData(filePath);
            // If no exception is thrown, the property fails
            throw new AssertionError(
                    "Expected DataSourceException for path: " + filePath + " but none was thrown");
        } catch (DataSourceException e) {
            String message = e.getMessage();
            if (message == null || !message.contains(filePath)) {
                throw new AssertionError(
                        "DataSourceException message [" + message
                                + "] does not contain file path [" + filePath + "]");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Property 9b — CsvDataProvider
    // -------------------------------------------------------------------------

    /**
     * Property 9b: CsvDataProvider throws DataSourceException whose message
     * contains the non-existent file path.
     *
     * <p><strong>Validates: Requirements 7.4</strong>
     */
    @Property(tries = 100)
    void csvDataProviderExceptionContainsFilePath(
            @ForAll("nonExistentFilePaths") String filePath) {

        try {
            CsvDataProvider.getData(filePath);
            throw new AssertionError(
                    "Expected DataSourceException for path: " + filePath + " but none was thrown");
        } catch (DataSourceException e) {
            String message = e.getMessage();
            if (message == null || !message.contains(filePath)) {
                throw new AssertionError(
                        "DataSourceException message [" + message
                                + "] does not contain file path [" + filePath + "]");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Property 9c — JsonDataProvider
    // -------------------------------------------------------------------------

    /**
     * Property 9c: JsonDataProvider throws DataSourceException whose message
     * contains the non-existent file path.
     *
     * <p><strong>Validates: Requirements 7.4</strong>
     */
    @Property(tries = 100)
    void jsonDataProviderExceptionContainsFilePath(
            @ForAll("nonExistentFilePaths") String filePath) {

        try {
            JsonDataProvider.getData(filePath);
            throw new AssertionError(
                    "Expected DataSourceException for path: " + filePath + " but none was thrown");
        } catch (DataSourceException e) {
            String message = e.getMessage();
            if (message == null || !message.contains(filePath)) {
                throw new AssertionError(
                        "DataSourceException message [" + message
                                + "] does not contain file path [" + filePath + "]");
            }
        }
    }
}
