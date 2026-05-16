# Selenium Test Framework

A professional SDET-grade Selenium automation framework built with Java, Maven, TestNG, and Allure Report. Supports multi-browser execution, parallel testing, data-driven testing, and seamless CI/CD integration.

---

## Project Structure

```
framework-selenium-testng/
├── .ci/                              # CI/CD configuration files
│   ├── github-actions.yml            # GitHub Actions workflow
│   ├── Jenkinsfile                   # Jenkins declarative pipeline
│   └── gitlab-ci.yml                 # GitLab CI pipeline
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── base/
│   │   │   │   └── RetryAnalyzer.java        # Flaky test retry logic
│   │   │   ├── core/
│   │   │   │   ├── BasePage.java             # Abstract base for all Page Objects
│   │   │   │   ├── config/
│   │   │   │   │   └── ConfigManager.java    # Singleton config manager
│   │   │   │   ├── driver/
│   │   │   │   │   ├── DriverFactory.java    # WebDriver factory (multi-browser)
│   │   │   │   │   └── WebDriverManager.java # ThreadLocal WebDriver manager
│   │   │   │   ├── exceptions/               # Custom exception hierarchy
│   │   │   │   ├── report/
│   │   │   │   │   ├── AllureListener.java   # TestNG → Allure integration
│   │   │   │   │   └── ScreenshotCapturer.java
│   │   │   │   └── utils/
│   │   │   │       ├── AssertionUtils.java   # Descriptive assertion wrappers
│   │   │   │       ├── BrowserUtils.java     # Tab, alert, iframe helpers
│   │   │   │       ├── FileUtils.java        # File read/write utilities
│   │   │   │       ├── JavaScriptUtils.java  # JS execution helpers
│   │   │   │       └── WaitUtils.java        # Explicit wait helpers
│   │   │   └── utils/
│   │   │       └── DataGenerator.java        # Faker-based test data generator
│   │   └── resources/
│   │       ├── config.properties             # Default configuration
│   │       ├── config-staging.properties     # Staging environment config
│   │       ├── config-production.properties  # Production environment config
│   │       └── log4j2.xml                    # Log4j2 configuration
│   └── test/
│       ├── java/
│       │   ├── base/
│       │   │   └── BaseTest.java             # Abstract base for all test classes
│       │   ├── dataproviders/
│       │   │   ├── CsvDataProvider.java      # CSV data provider
│       │   │   ├── ExcelDataProvider.java    # Excel (.xlsx) data provider
│       │   │   └── JsonDataProvider.java     # JSON data provider
│       │   ├── pages/
│       │   │   └── LoginPage.java            # Sample Login Page Object
│       │   └── tests/
│       │       └── LoginTest.java            # Sample Login test cases
│       └── resources/
│           ├── testng-suite.xml              # Full test suite
│           ├── testng-unit.xml               # Unit & property tests
│           ├── testng-integration.xml        # Integration tests (headless)
│           └── testdata/                     # Test data files (CSV, JSON, Excel)
├── pom.xml
└── README.md
```

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **Google Chrome** (for default browser tests)
- **Firefox** (optional, for cross-browser tests)
- **Microsoft Edge** (optional)

WebDriver binaries are managed automatically by [WebDriverManager (bonigarcia)](https://github.com/bonigarcia/webdrivermanager) — no manual driver downloads required.

---

## Installation

```bash
# Clone the repository
git clone <repository-url>
cd framework-selenium-testng

# Download all dependencies
mvn dependency:resolve
```

---

## Running Tests

### Run all tests (default suite)

```bash
mvn test
```

### Run with a specific browser

```bash
mvn test -Dbrowser=firefox
mvn test -Dbrowser=edge
mvn test -Dbrowser=chrome
```

### Run in headless mode

```bash
mvn test -Dheadless=true
```

### Run against a specific environment

```bash
mvn test -Denv=staging -Dheadless=true
mvn test -Denv=production -Dbrowser=chrome
```

### Run with parallel execution

```bash
mvn test -Dthread.count=4
```

### Run a specific TestNG suite

```bash
# Full suite (default)
mvn test -Dtest.suite=testng-suite.xml

# Unit and property tests only
mvn test -Dtest.suite=testng-unit.xml

# Integration tests (headless)
mvn test -Dtest.suite=testng-integration.xml -Dheadless=true
```

### Run in CI mode (auto-enables headless)

```bash
mvn test -Dbrowser=chrome -Dheadless=true -Dci.mode=true
```

### Combined example

```bash
mvn test -Denv=staging -Dheadless=true -Dthread.count=4 -Dbrowser=chrome
```

---

## Configuration

All configuration is managed through `src/main/resources/config.properties`. Values can be overridden at runtime via system properties (`-D` flags) or environment variables.

| Parameter               | Default       | Description                                      |
|-------------------------|---------------|--------------------------------------------------|
| `base.url`              | (required)    | Base URL of the application under test           |
| `browser`               | `chrome`      | Browser to use: `chrome`, `firefox`, `edge`, `safari` |
| `headless`              | `false`       | Run browser in headless mode                     |
| `env`                   | `dev`         | Environment profile: `dev`, `staging`, `production` |
| `implicit.wait.timeout` | `10`          | Implicit wait timeout in seconds                 |
| `explicit.wait.timeout` | `10`          | Explicit wait timeout in seconds                 |
| `thread.count`          | `1`           | Number of parallel test threads                  |
| `screenshot.on.failure` | `true`        | Capture screenshot on test failure               |
| `retry.count`           | `1`           | Number of retries for flaky tests                |
| `ci.mode`               | `false`       | Enable CI mode (forces headless)                 |

### Environment Profiles

Create environment-specific config files in `src/main/resources/`:

- `config.properties` — default (dev) configuration
- `config-staging.properties` — staging overrides
- `config-production.properties` — production overrides

Activate a profile with `-Denv=staging`.

---

## CI/CD Integration

Pre-configured pipeline files are in the `.ci/` directory.

### GitHub Actions

The workflow in `.ci/github-actions.yml` triggers on `push` and `pull_request` to `main`:

```yaml
# Runs headless Chrome tests and uploads reports as artifacts
mvn test -Dbrowser=chrome -Dheadless=true -Dci.mode=true
```

### Jenkins

The declarative pipeline in `.ci/Jenkinsfile` includes stages: Checkout → Test → Publish Report.

Requires the [Allure Jenkins Plugin](https://plugins.jenkins.io/allure-jenkins-plugin/) for report publishing.

### GitLab CI

The pipeline in `.ci/gitlab-ci.yml` runs the `test` stage and publishes `target/reports/` as an artifact with JUnit XML integration.

---

## Reporting

### View Allure Report (interactive)

```bash
# Generate and open the Allure report in a browser
mvn allure:serve
```

### Generate static Allure Report

```bash
mvn allure:report
# Report is generated at: target/reports/allure-report/index.html
```

### JUnit XML Reports

JUnit-compatible XML reports are written to `target/surefire-reports/` for CI integration.

### Screenshots

Screenshots captured on test failure are:
- Attached to the Allure report entry for the failed test
- Saved to `target/screenshots/` (when `screenshot.on.failure=true`)

---

## Writing Tests

### Create a Page Object

```java
package pages;

import core.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MyPage extends BasePage {

    private static final By SOME_ELEMENT = By.id("element-id");

    public MyPage(WebDriver driver) {
        super(driver);
    }

    public String getSomeText() {
        return getText(SOME_ELEMENT);
    }
}
```

### Create a Test Class

```java
package tests;

import base.BaseTest;
import base.RetryAnalyzer;
import core.report.AllureListener;
import core.utils.AssertionUtils;
import io.qameta.allure.*;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pages.MyPage;

@Listeners(AllureListener.class)
@Epic("My Feature")
@Feature("My Page")
public class MyTest extends BaseTest {

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Story("Verify element text")
    @Severity(SeverityLevel.NORMAL)
    public void verifyElementText() {
        driver.get(core.config.ConfigManager.getInstance().getString("base.url", "") + "/my-page");
        MyPage page = new MyPage(driver);
        AssertionUtils.assertEquals(page.getSomeText(), "Expected Text", "verifyElementText");
    }
}
```

### Data-Driven Testing

```java
@DataProvider(name = "loginData")
public Object[][] loginData() {
    return CsvDataProvider.getData("src/test/resources/testdata/login.csv");
}

@Test(dataProvider = "loginData")
public void testLogin(String username, String password, String expected) {
    // test logic
}
```
