# Implementation Plan: Selenium Test Framework

## Overview

Triển khai framework tự động hóa kiểm thử Selenium theo kiến trúc phân lớp 4 tầng: Infrastructure → Core → Test Layer → CI/CD. Mỗi task xây dựng tăng dần trên task trước, đảm bảo không có code "treo" chưa được tích hợp. Framework sử dụng Java + Maven + TestNG + Allure Report, hỗ trợ 4 browsers và parallel execution thread-safe.

---

## Tasks

- [x] 1. Khởi tạo cấu trúc dự án Maven và cấu hình dependencies
  - Tạo `pom.xml` với đầy đủ dependencies: Selenium 4.x, TestNG, WebDriverManager (bonigarcia), Allure TestNG, Log4j2/SLF4J, Java Faker, jqwik (JUnit 5), Mockito, Apache POI (Excel), OpenCSV, Jackson (JSON)
  - Tạo cấu trúc thư mục chuẩn Maven: `src/main/java`, `src/test/java`, `src/main/resources`, `src/test/resources`
  - Tạo package structure: `core/`, `core/config/`, `core/driver/`, `core/utils/`, `core/report/`, `base/`, `utils/`, `pages/`, `tests/`, `dataproviders/`
  - Tạo `src/main/resources/config.properties` với các tham số: `base.url`, `browser`, `headless`, `implicit.wait.timeout`, `explicit.wait.timeout`, `thread.count`, `screenshot.on.failure`, `retry.count`, `env`, `ci.mode`
  - Tạo `src/main/resources/config-staging.properties` và `config-production.properties` cho multi-profile support
  - Tạo `src/main/resources/log4j2.xml` với pattern `[THREAD-%t] [%p] [%c{1}] - %m%n`
  - Tạo `src/test/resources/testdata/` directory placeholder
  - _Requirements: 1.1, 1.6, 5.1, 5.2, 6.5_

- [x] 2. Triển khai Infrastructure Layer — ConfigManager
  - [x] 2.1 Implement `ConfigManager` class (Singleton, double-checked locking)
    - Tạo file `src/main/java/core/config/ConfigManager.java`
    - Implement `getInstance()` với double-checked locking và `volatile` keyword
    - Implement `loadConfig(String env)` để load profile-specific properties file
    - Implement `resolveValue(String key)` với priority chain: system property → env variable → properties file
    - Implement type-safe getters: `getString`, `getInt`, `getBoolean` (có và không có default value)
    - Implement `MissingConfigException` trong package `core/config/exceptions/` — message phải chứa tên key bị thiếu
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7_

  - [ ]* 2.2 Viết property test cho ConfigManager (Property 1, 2, 6)
    - **Property 1: ConfigManager Priority Override** — system property / env var override file value
    - **Property 2: ConfigManager Singleton Identity** — tất cả concurrent calls trả về cùng object reference
    - **Property 6: Missing Required Config Exception Contains Key Name** — exception message chứa tên key
    - **Validates: Requirements 5.3, 5.7, 5.5**
    - Tạo file `src/test/java/core/config/ConfigManagerPropertyTest.java` dùng jqwik `@Property`, tối thiểu 100 iterations
    - Tag: `@Tag("Feature: selenium-test-framework, Property 1: ConfigManager Priority Override")`, v.v.

- [x] 3. Triển khai Infrastructure Layer — DriverFactory và WebDriverManager
  - [x] 3.1 Implement `FrameworkException` hierarchy
    - Tạo `src/main/java/core/exceptions/FrameworkException.java` (base exception)
    - Tạo các subclass: `UnsupportedBrowserException`, `DriverInitializationException`, `ElementNotFoundException`, `DataSourceException`
    - Mỗi exception phải nhận message string trong constructor và expose qua `getMessage()`
    - _Requirements: 2.6, 4.4, 7.4_

  - [x] 3.2 Implement `DriverFactory` class (Factory Method pattern)
    - Tạo file `src/main/java/core/driver/DriverFactory.java`
    - Implement `createDriver(String browser)` — dispatch đến browser-specific creators
    - Implement `createChromeDriver(boolean headless)`, `createFirefoxDriver(boolean headless)`, `createEdgeDriver(boolean headless)`, `createSafariDriver()`
    - Sử dụng `io.github.bonigarcia.wdm.WebDriverManager` để quản lý driver binary
    - Throw `UnsupportedBrowserException` với message chứa tên browser không hợp lệ khi browser không thuộc `{chrome, firefox, edge, safari}`
    - Đọc `headless` và window size từ `ConfigManager`
    - _Requirements: 2.1, 2.2, 2.3, 2.5, 2.6_

  - [ ]* 3.3 Viết property test cho DriverFactory (Property 5)
    - **Property 5: Unsupported Browser Exception Contains Browser Name** — exception message chứa tên browser không hợp lệ
    - **Validates: Requirements 2.6**
    - Tạo file `src/test/java/core/driver/DriverFactoryPropertyTest.java` dùng jqwik
    - Generate arbitrary strings không thuộc `{chrome, firefox, edge, safari}`, verify exception message

  - [x] 3.4 Implement custom `WebDriverManager` class (ThreadLocal wrapper)
    - Tạo file `src/main/java/core/driver/WebDriverManager.java`
    - Implement `ThreadLocal<WebDriver> driverThreadLocal`
    - Implement `initDriver(String browser)` — gọi `DriverFactory.createDriver()` và set vào ThreadLocal
    - Implement `getDriver()` — trả về driver của thread hiện tại
    - Implement `quitDriver()` — quit driver và gọi `threadLocal.remove()`
    - Implement `isDriverInitialized()` — kiểm tra ThreadLocal có giá trị không
    - Throw `DriverInitializationException` nếu driver không khởi tạo được
    - _Requirements: 2.3, 2.4, 2.7, 3.3_

  - [ ]* 3.5 Viết property test cho WebDriverManager (Property 3, 4)
    - **Property 3: ThreadLocal Driver Isolation** — hai thread khác nhau nhận WebDriver instance khác nhau
    - **Property 4: Driver Cleanup After Test** — sau `quitDriver()`, `isDriverInitialized()` trả về `false`
    - **Validates: Requirements 2.7, 3.3, 2.4**
    - Tạo file `src/test/java/core/driver/WebDriverManagerPropertyTest.java` dùng jqwik + mock driver

- [x] 4. Checkpoint — Verify Infrastructure Layer
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Triển khai Core Framework — BasePage
  - [x] 5.1 Implement `BasePage` abstract class (Template Method pattern)
    - Tạo file `src/main/java/core/BasePage.java`
    - Constructor nhận `WebDriver driver`, khởi tạo `WebDriverWait` với timeout từ `ConfigManager`
    - Implement `findElement(By locator)` với explicit wait và StaleElement retry (tối đa 3 lần)
    - Implement `findElements(By locator)`, `click(By locator)`, `sendKeys(By locator, String text)`, `getText(By locator)`, `isDisplayed(By locator)`, `selectFromDropdown(By locator, String visibleText)`
    - Implement `waitForPageLoad()` — chờ `document.readyState == "complete"` qua JavaScript
    - Implement `getPageTitle()`, `getCurrentUrl()`
    - Implement `executeScript(String script, Object... args)`, `scrollToElement(By locator)`, `highlightElement(By locator)`
    - Throw `ElementNotFoundException` với message chứa locator string và timeout value khi element không tìm thấy
    - Log WARN cho mỗi StaleElement retry attempt
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7_

  - [ ]* 5.2 Viết property test cho BasePage (Property 11)
    - **Property 11: Element Not Found Exception Contains Locator Info** — exception message chứa locator string và timeout value
    - **Validates: Requirements 4.4**
    - Tạo file `src/test/java/core/BasePagePropertyTest.java` dùng jqwik + MockWebDriver
    - Generate arbitrary `By` locators, verify exception message format

- [x] 6. Triển khai Core Framework — Utility Classes
  - [x] 6.1 Implement `WaitUtils` class
    - Tạo file `src/main/java/core/utils/WaitUtils.java`
    - Constructor nhận `WebDriver driver` và optional `int timeoutSeconds`
    - Implement: `waitForElementVisible(By locator)`, `waitForElementClickable(By locator)`, `waitForElementInvisible(By locator)`, `waitForTextPresent(By locator, String text)`, `waitForUrlContains(String urlFragment)`, `waitForTitleContains(String title)`
    - _Requirements: 10.1_

  - [x] 6.2 Implement `JavaScriptUtils` class
    - Tạo file `src/main/java/core/utils/JavaScriptUtils.java`
    - Constructor nhận `WebDriver driver`, cast sang `JavascriptExecutor`
    - Implement: `scrollToElement(WebElement)`, `scrollToTop()`, `scrollToBottom()`, `highlightElement(WebElement)`, `removeHighlight(WebElement)`, `getAttribute(WebElement, String)`, `clickByJS(WebElement)`, `setValueByJS(WebElement, String)`
    - _Requirements: 10.2_

  - [x] 6.3 Implement `BrowserUtils` class
    - Tạo file `src/main/java/core/utils/BrowserUtils.java`
    - Constructor nhận `WebDriver driver`
    - Implement: `switchToNewTab()`, `switchToTab(int index)`, `closeCurrentTab()`, `acceptAlert()`, `dismissAlert()`, `getAlertText()`, `switchToIframe(By locator)`, `switchToDefaultContent()`, `captureScreenshotAsBytes()`, `captureScreenshotToFile(String filePath)`
    - _Requirements: 10.5_

  - [x] 6.4 Implement `AssertionUtils` class
    - Tạo file `src/main/java/core/utils/AssertionUtils.java`
    - Tất cả methods là `static`, wrap TestNG `Assert` với descriptive messages
    - Implement: `assertEquals`, `assertTrue`, `assertFalse`, `assertNotNull`, `assertNull`, `assertContains(String actual, String expected, String context)`, `assertListEquals`
    - Mỗi assertion failure message phải chứa `context` string verbatim
    - Log ERROR trước khi exception được propagate
    - _Requirements: 10.4, 10.6_

  - [ ]* 6.5 Viết property test cho AssertionUtils (Property 10)
    - **Property 10: AssertionUtils Error Message Contains Context** — AssertionError message chứa context string verbatim
    - **Validates: Requirements 10.4**
    - Tạo file `src/test/java/core/utils/AssertionUtilsPropertyTest.java` dùng jqwik
    - Generate arbitrary non-null, non-empty context strings, verify AssertionError message

  - [x] 6.6 Implement `FileUtils` class
    - Tạo file `src/main/java/core/utils/FileUtils.java`
    - Implement: đọc file text, ghi file text, kiểm tra sự tồn tại của file trong `target/`
    - _Requirements: 10.3_

  - [x] 6.7 Implement `DataGenerator` class
    - Tạo file `src/main/java/utils/DataGenerator.java`
    - Khởi tạo `Faker` với `Locale("vi")`
    - Implement: `generateFullName()`, `generateEmail()`, `generatePhoneNumber()`, `generatePassword(int length)`, `generateUsername()`, `generateRandomInt(int min, int max)`, `generateAddress()`
    - _Requirements: 10.1 (implicit — utility support)_

- [x] 7. Triển khai Infrastructure Layer — Reporting và Retry
  - [x] 7.1 Implement `ScreenshotCapturer` class
    - Tạo file `src/main/java/core/report/ScreenshotCapturer.java`
    - Implement `captureScreenshot(WebDriver driver)` — trả về `byte[]` non-empty
    - Implement `captureAndSave(WebDriver driver, String testName)` — lưu vào `target/screenshots/{testName}_{timestamp}.png`
    - Implement `attachToAllure(WebDriver driver)` với `@Attachment(value = "Screenshot on Failure", type = "image/png")`
    - _Requirements: 6.3_

  - [ ]* 7.2 Viết property test cho ScreenshotCapturer (Property 13)
    - **Property 13: Screenshot Captured on Failure When Configured** — `captureScreenshot()` trả về non-empty byte array
    - **Validates: Requirements 6.3**
    - Tạo file `src/test/java/core/report/ScreenshotCapturerPropertyTest.java` dùng jqwik + mock WebDriver

  - [x] 7.3 Implement `AllureListener` class (Observer pattern — ITestListener)
    - Tạo file `src/main/java/core/report/AllureListener.java`
    - Implement `ITestListener`: `onTestStart`, `onTestSuccess`, `onTestFailure`, `onTestSkipped`
    - Implement `attachTestMetadata(ITestResult result)` — đính kèm browser info, thread ID, duration vào Allure
    - Log ERROR khi test fail với format: `[THREAD-{threadId}] [ERROR] [{className}] - Test FAILED: {testName} - {errorMessage}`
    - _Requirements: 6.1, 6.2, 6.4, 6.6_

  - [ ]* 7.4 Viết property test cho Logging (Property 12)
    - **Property 12: Failure Log Contains Test Name and Thread ID** — ERROR log entry chứa test method name và thread ID
    - **Validates: Requirements 3.7, 6.6**
    - Tạo file `src/test/java/core/report/LoggingPropertyTest.java` dùng jqwik + Log4j2 test appender
    - Generate arbitrary test names và thread IDs, verify log output format

  - [x] 7.5 Implement `RetryAnalyzer` class (Strategy pattern)
    - Tạo file `src/main/java/base/RetryAnalyzer.java`
    - Implement `IRetryAnalyzer.retry(ITestResult result)`
    - Implement `isRetryableException(Throwable throwable)` — trả về `false` cho `AssertionError` và subclasses
    - Đọc `retry.count` từ `ConfigManager` (default 1)
    - Log WARN khi retry: `[THREAD-{threadId}] [WARN] [RetryAnalyzer] - Retrying test: {testName} (attempt {n}/{max})`
    - _Requirements: 8.1, 8.2, 8.5_

  - [ ]* 7.6 Viết property test cho RetryAnalyzer (Property 7, 8)
    - **Property 7: RetryAnalyzer Does Not Retry Assertion Failures** — `retry()` trả về `false` cho mọi `AssertionError`
    - **Property 8: RetryAnalyzer Respects Max Retry Count** — `retry()` trả về `true` đúng N lần, `false` lần thứ N+1
    - **Validates: Requirements 8.5, 8.1**
    - Tạo file `src/test/java/base/RetryAnalyzerPropertyTest.java` dùng jqwik
    - Generate arbitrary retry.count values (N ≥ 0) và exception types

- [x] 8. Checkpoint — Verify Core và Infrastructure Layer
  - Ensure all tests pass, ask the user if questions arise.

- [x] 9. Triển khai Test Layer — BaseTest và Data Providers
  - [x] 9.1 Implement `BaseTest` abstract class (Template Method + Hook pattern)
    - Tạo file `src/test/java/base/BaseTest.java`
    - Declare `protected WebDriver driver`
    - Implement `@BeforeMethod(alwaysRun = true) setUp(Method method)` — gọi `ConfigManager`, `WebDriverManager.initDriver()`, set `this.driver`, gọi `beforeTest()`
    - Implement `@AfterMethod(alwaysRun = true) tearDown(ITestResult result)` — gọi `afterTest()`, handle failure (screenshot + log), gọi `WebDriverManager.quitDriver()`
    - Implement `handleTestFailure(ITestResult result)` — chụp screenshot và attach vào Allure nếu `screenshot.on.failure=true`
    - Implement hook methods `beforeTest()` và `afterTest()` (empty, overridable)
    - Log INFO khi test start: `[THREAD-{threadId}] [INFO] [BaseTest] - Starting test: {testName} [{browser}]`
    - _Requirements: 1.3, 2.4, 6.3, 6.6, 8.4_

  - [x] 9.2 Implement Data Provider classes
    - Tạo file `src/test/java/dataproviders/ExcelDataProvider.java` — đọc `.xlsx` dùng Apache POI
    - Tạo file `src/test/java/dataproviders/CsvDataProvider.java` — đọc `.csv` dùng OpenCSV
    - Tạo file `src/test/java/dataproviders/JsonDataProvider.java` — đọc `.json` dùng Jackson
    - Mỗi provider trả về `Object[][]` cho TestNG `@DataProvider`
    - Throw `DataSourceException` với message chứa file path khi file không tồn tại
    - Hỗ trợ kiểu dữ liệu: String, Integer, Boolean, Double
    - Hỗ trợ filter theo sheet name (Excel) hoặc file name (CSV/JSON)
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6_

  - [x] 9.3 Viết property test cho DataProvider (Property 9)
    - **Property 9: DataProvider Exception Contains File Path** — exception message chứa file path không tồn tại
    - **Validates: Requirements 7.4**
    - Tạo file `src/test/java/dataproviders/DataProviderPropertyTest.java` dùng jqwik
    - Generate arbitrary non-existent file paths, verify `DataSourceException` message cho cả 3 providers

- [x] 10. Tạo TestNG Suite XML và cấu hình Parallel Execution
  - Tạo `src/test/resources/testng-suite.xml` — full suite với `parallel="methods"`, `thread-count` từ config
  - Tạo `src/test/resources/testng-unit.xml` — unit/property tests, `parallel="methods"`, `thread-count="4"`
  - Tạo `src/test/resources/testng-integration.xml` — integration tests với browser headless
  - Đăng ký `AllureListener` trong suite XML: `<listener class-name="core.report.AllureListener"/>`
  - Cấu hình `maven-surefire-plugin` trong `pom.xml` để nhận `-Dtest.suite` parameter
  - Cấu hình Allure Maven plugin để generate report vào `target/reports/allure-report`
  - Cấu hình JUnit XML output vào `target/surefire-reports` cho CI compatibility
  - _Requirements: 3.1, 3.2, 3.4, 3.5, 3.6, 6.7, 9.1_

- [x] 11. Tạo Sample Page Objects và Test Cases minh họa
  - [x] 11.1 Tạo sample `LoginPage` Page Object
    - Tạo file `src/test/java/pages/LoginPage.java` kế thừa `BasePage`
    - Định nghĩa locators (By.id, By.cssSelector) cho username, password, submit button, error message
    - Implement methods: `enterUsername(String)`, `enterPassword(String)`, `clickLogin()`, `getErrorMessage()`, `login(String username, String password)`
    - Không chứa assertion logic — chỉ chứa locators và interactions
    - _Requirements: 1.2, 1.4, 1.5, 4.5_

  - [x] 11.2 Tạo sample `LoginTest` Test Case
    - Tạo file `src/test/java/tests/LoginTest.java` kế thừa `BaseTest`
    - Implement `@Test loginWithValidCredentials()` — dùng `LoginPage` và `AssertionUtils`
    - Implement `@Test loginWithInvalidCredentials()` với `@DataProvider` từ `CsvDataProvider`
    - Annotate với `@Listeners(AllureListener.class)` và `retryAnalyzer = RetryAnalyzer.class`
    - Thêm Allure annotations: `@Epic`, `@Feature`, `@Story`, `@Severity`
    - _Requirements: 1.2, 1.3, 7.2, 8.1_

- [x] 12. Tạo CI/CD Configuration Files
  - Tạo `.ci/github-actions.yml` — workflow với jobs: test (headless Chrome), publish Allure report artifact
    - Trigger: `push` và `pull_request` trên `main`
    - Steps: checkout, setup Java 17, `mvn test -Dbrowser=chrome -Dheadless=true -Dci.mode=true`, upload `target/reports` artifact
  - Tạo `.ci/Jenkinsfile` — declarative pipeline với stages: Checkout, Test, Publish Report
    - Sử dụng `withEnv` để set `BROWSER=chrome`, `HEADLESS=true`, `CI_MODE=true`
  - Tạo `.ci/gitlab-ci.yml` — GitLab CI pipeline với stage `test` và artifact `target/reports`
  - Đảm bảo Maven exit code khác 0 khi có test failure (mặc định của Surefire)
  - _Requirements: 9.2, 9.3, 9.4, 9.5, 9.6_

- [x] 13. Tạo README và tài liệu dự án
  - Tạo `README.md` tại root với các section: Project Structure, Prerequisites, Installation, Running Tests, Configuration, CI/CD Integration, Reporting
  - Mô tả cách chạy: `mvn test`, `mvn test -Dbrowser=firefox`, `mvn test -Denv=staging -Dheadless=true -Dthread.count=4`
  - Mô tả cách xem Allure report: `mvn allure:serve`
  - _Requirements: 1.7_

- [ ] 14. Final Checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

---

## Notes

- Tasks đánh dấu `*` là optional — có thể bỏ qua để MVP nhanh hơn, nhưng nên implement để đảm bảo correctness của framework
- Property tests dùng **jqwik** (JUnit 5 compatible), chạy tối thiểu 100 iterations mỗi property
- Unit tests dùng **JUnit 5 + Mockito** để mock WebDriver và các dependencies
- Mỗi property test task tham chiếu đến property number trong design document để traceability
- `WebDriverManager` trong package `core.driver` là custom class — không nhầm với thư viện `io.github.bonigarcia`
- Safari chỉ hỗ trợ trên macOS — CI pipeline nên dùng Chrome/Firefox headless
- Allure report được generate vào `target/reports/allure-report`, CI artifact upload từ thư mục này

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["2.1", "3.1"] },
    { "id": 1, "tasks": ["2.2", "3.2"] },
    { "id": 2, "tasks": ["3.3", "3.4"] },
    { "id": 3, "tasks": ["3.5", "5.1"] },
    { "id": 4, "tasks": ["5.2", "6.1", "6.2", "6.3", "6.4", "6.6", "6.7"] },
    { "id": 5, "tasks": ["6.5", "7.1", "7.3", "7.5"] },
    { "id": 6, "tasks": ["7.2", "7.4", "7.6", "9.1"] },
    { "id": 7, "tasks": ["9.2"] },
    { "id": 8, "tasks": ["9.3"] },
    { "id": 9, "tasks": ["11.1"] },
    { "id": 10, "tasks": ["11.2"] }
  ]
}
```
