package dataproviders;

import core.exceptions.DataSourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data provider that reads test data from Excel (.xlsx) files using Apache POI.
 *
 * <p>Returns {@code Object[][]} compatible with TestNG {@code @DataProvider}.
 * Supports String, Integer, Boolean, and Double cell types.
 * Supports filtering by sheet name.
 *
 * <p>Throws {@link DataSourceException} with the file path in the message
 * when the file does not exist or cannot be read.
 *
 * <p>Validates: Requirements 7.1, 7.3, 7.4, 7.5, 7.6
 */
public class ExcelDataProvider {

    private static final Logger log = LogManager.getLogger(ExcelDataProvider.class);

    private ExcelDataProvider() {
        // Utility class — no instantiation
    }

    /**
     * Reads all rows from the first sheet of the given Excel file.
     * The first row is treated as a header and is skipped.
     *
     * @param filePath absolute or relative path to the .xlsx file
     * @return {@code Object[][]} where each inner array is one data row
     * @throws DataSourceException if the file does not exist or cannot be read
     */
    public static Object[][] getData(String filePath) {
        return getData(filePath, null);
    }

    /**
     * Reads all rows from the specified sheet of the given Excel file.
     * The first row is treated as a header and is skipped.
     *
     * @param filePath  absolute or relative path to the .xlsx file
     * @param sheetName name of the sheet to read; if {@code null} or empty,
     *                  the first sheet is used
     * @return {@code Object[][]} where each inner array is one data row
     * @throws DataSourceException if the file does not exist, the sheet is not
     *                             found, or the file cannot be read
     */
    public static Object[][] getData(String filePath, String sheetName) {
        File file = resolveFile(filePath);

        log.info("[THREAD-{}] [INFO] [ExcelDataProvider] - Reading Excel file: {} (sheet: {})",
                Thread.currentThread().getId(), filePath,
                sheetName != null && !sheetName.isEmpty() ? sheetName : "<first sheet>");

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = resolveSheet(workbook, sheetName, filePath);
            return extractData(sheet);

        } catch (IOException e) {
            String msg = "Failed to read Excel file: " + filePath;
            log.error("[THREAD-{}] [ERROR] [ExcelDataProvider] - {}",
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
            throw new DataSourceException("Excel file path must not be null or blank: " + filePath);
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            String msg = "Excel data file not found: " + filePath;
            log.error("[THREAD-{}] [ERROR] [ExcelDataProvider] - {}",
                    Thread.currentThread().getId(), msg);
            throw new DataSourceException(msg);
        }
        return file;
    }

    /**
     * Returns the sheet by name, or the first sheet when {@code sheetName} is
     * {@code null} / empty.
     */
    private static Sheet resolveSheet(Workbook workbook, String sheetName, String filePath) {
        if (sheetName != null && !sheetName.isEmpty()) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                String msg = "Sheet '" + sheetName + "' not found in Excel file: " + filePath;
                log.error("[THREAD-{}] [ERROR] [ExcelDataProvider] - {}",
                        Thread.currentThread().getId(), msg);
                throw new DataSourceException(msg);
            }
            return sheet;
        }
        return workbook.getSheetAt(0);
    }

    /**
     * Extracts data rows from the sheet, skipping the header row (index 0).
     * Empty rows are skipped.
     */
    private static Object[][] extractData(Sheet sheet) {
        int lastRowNum = sheet.getLastRowNum();
        List<Object[]> rows = new ArrayList<>();

        // Row 0 is the header — start from row 1
        for (int rowIdx = 1; rowIdx <= lastRowNum; rowIdx++) {
            Row row = sheet.getRow(rowIdx);
            if (row == null) {
                continue; // skip empty rows
            }

            int lastCellNum = row.getLastCellNum();
            if (lastCellNum <= 0) {
                continue;
            }

            Object[] rowData = new Object[lastCellNum];
            for (int colIdx = 0; colIdx < lastCellNum; colIdx++) {
                Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                rowData[colIdx] = extractCellValue(cell);
            }
            rows.add(rowData);
        }

        return rows.toArray(new Object[0][]);
    }

    /**
     * Converts a POI {@link Cell} to a Java value.
     * Supported types: String, Integer (when numeric value is whole), Double,
     * Boolean. Blank/null cells return an empty String.
     */
    private static Object extractCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                // Return Integer when the value has no fractional part
                if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                    return (int) numericValue;
                }
                return numericValue;

            case STRING:
                return cell.getStringCellValue();

            case BOOLEAN:
                return cell.getBooleanCellValue();

            case FORMULA:
                // Evaluate cached formula result
                return evaluateFormula(cell);

            case BLANK:
            default:
                return "";
        }
    }

    /**
     * Evaluates a formula cell and returns its cached result value.
     * Uses {@link CellType} (the same enum used for regular cells) to inspect
     * the cached formula result type, which is the correct API in POI 5.x.
     */
    private static Object evaluateFormula(Cell cell) {
        CellType cachedType = cell.getCachedFormulaResultType();
        switch (cachedType) {
            case NUMERIC:
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                    return (int) numericValue;
                }
                return numericValue;
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            default:
                return "";
        }
    }
}
