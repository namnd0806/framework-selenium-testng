package core.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Utility class providing file read, write, and existence-check operations
 * scoped to the Maven {@code target/} output directory.
 *
 * <p>All methods log at ERROR level before propagating any unexpected exception,
 * satisfying Requirement 10.6.</p>
 *
 * <p>Satisfies Requirements: 10.3</p>
 */
public class FileUtils {

    private static final Logger log = LogManager.getLogger(FileUtils.class);

    /**
     * Root directory for all file operations — resolves to {@code target/} relative
     * to the current working directory (the Maven project root during test execution).
     */
    private static final Path TARGET_DIR = Paths.get("target");

    // Utility class — no instantiation needed
    private FileUtils() {
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Reads the entire content of a text file located inside {@code target/} and
     * returns it as a single {@code String} (UTF-8 encoded).
     *
     * @param relativePath path relative to {@code target/} (e.g. {@code "reports/result.txt"})
     * @return the file content as a {@code String}
     * @throws IOException if the file does not exist or cannot be read
     */
    public static String readFile(String relativePath) throws IOException {
        Path filePath = resolveTargetPath(relativePath);
        log.info("[FileUtils] Reading file: {}", filePath.toAbsolutePath());
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("[FileUtils] Failed to read file: {} — {}", filePath.toAbsolutePath(), e.getMessage());
            throw e;
        }
    }

    /**
     * Reads all lines of a text file located inside {@code target/} and returns
     * them as a {@code List<String>} (UTF-8 encoded).
     *
     * @param relativePath path relative to {@code target/} (e.g. {@code "reports/result.txt"})
     * @return an immutable list of lines; empty list if the file is empty
     * @throws IOException if the file does not exist or cannot be read
     */
    public static List<String> readLines(String relativePath) throws IOException {
        Path filePath = resolveTargetPath(relativePath);
        log.info("[FileUtils] Reading lines from file: {}", filePath.toAbsolutePath());
        try {
            return Files.readAllLines(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("[FileUtils] Failed to read lines from file: {} — {}", filePath.toAbsolutePath(), e.getMessage());
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Writes (or overwrites) a text file at the given path inside {@code target/}.
     * Parent directories are created automatically if they do not exist.
     *
     * @param relativePath path relative to {@code target/} (e.g. {@code "screenshots/log.txt"})
     * @param content      the text content to write (UTF-8 encoded)
     * @throws IOException if the file cannot be created or written
     */
    public static void writeFile(String relativePath, String content) throws IOException {
        Path filePath = resolveTargetPath(relativePath);
        log.info("[FileUtils] Writing file: {}", filePath.toAbsolutePath());
        try {
            ensureParentDirectoriesExist(filePath);
            Files.writeString(filePath, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("[FileUtils] Failed to write file: {} — {}", filePath.toAbsolutePath(), e.getMessage());
            throw e;
        }
    }

    /**
     * Appends text to an existing file inside {@code target/}, or creates the file
     * if it does not yet exist. Parent directories are created automatically.
     *
     * @param relativePath path relative to {@code target/} (e.g. {@code "logs/run.log"})
     * @param content      the text content to append (UTF-8 encoded)
     * @throws IOException if the file cannot be written
     */
    public static void appendToFile(String relativePath, String content) throws IOException {
        Path filePath = resolveTargetPath(relativePath);
        log.info("[FileUtils] Appending to file: {}", filePath.toAbsolutePath());
        try {
            ensureParentDirectoriesExist(filePath);
            Files.writeString(filePath, content, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("[FileUtils] Failed to append to file: {} — {}", filePath.toAbsolutePath(), e.getMessage());
            throw e;
        }
    }

    // -------------------------------------------------------------------------
    // Existence check
    // -------------------------------------------------------------------------

    /**
     * Checks whether a file exists at the given path inside {@code target/}.
     *
     * @param relativePath path relative to {@code target/} (e.g. {@code "screenshots/test.png"})
     * @return {@code true} if the file exists and is a regular file; {@code false} otherwise
     */
    public static boolean fileExists(String relativePath) {
        Path filePath = resolveTargetPath(relativePath);
        boolean exists = Files.exists(filePath) && Files.isRegularFile(filePath);
        log.debug("[FileUtils] fileExists('{}') → {}", filePath.toAbsolutePath(), exists);
        return exists;
    }

    /**
     * Checks whether a directory exists at the given path inside {@code target/}.
     *
     * @param relativePath path relative to {@code target/} (e.g. {@code "reports"})
     * @return {@code true} if the path exists and is a directory; {@code false} otherwise
     */
    public static boolean directoryExists(String relativePath) {
        Path dirPath = resolveTargetPath(relativePath);
        boolean exists = Files.exists(dirPath) && Files.isDirectory(dirPath);
        log.debug("[FileUtils] directoryExists('{}') → {}", dirPath.toAbsolutePath(), exists);
        return exists;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Resolves a path relative to {@code target/}.
     *
     * @param relativePath the path segment(s) to append to {@code target/}
     * @return the resolved {@link Path}
     */
    static Path resolveTargetPath(String relativePath) {
        return TARGET_DIR.resolve(relativePath).normalize();
    }

    /**
     * Creates all parent directories for the given file path if they do not exist.
     *
     * @param filePath the file whose parent directories should be created
     * @throws IOException if directory creation fails
     */
    private static void ensureParentDirectoriesExist(Path filePath) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
