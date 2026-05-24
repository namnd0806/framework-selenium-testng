# Framework Overview

## Giới thiệu

`framework-selenium-testng` là một Selenium automation framework được xây dựng theo mindset SDET (Software Development Engineer in Test) chuyên nghiệp. Framework áp dụng các nguyên tắc kỹ thuật phần mềm hiện đại để đảm bảo khả năng bảo trì, mở rộng và tái sử dụng cao.

---

## Tech Stack

| Thành phần | Công nghệ | Phiên bản |
|---|---|---|
| Ngôn ngữ | Java | 17 |
| Build tool | Maven | 3.8+ |
| Test runner | TestNG | 7.10.x |
| Browser automation | Selenium WebDriver | 4.21.x |
| Driver management | WebDriverManager (bonigarcia) | 5.8.x |
| Reporting | Allure Report | 2.27.x |
| Logging | Log4j2 / SLF4J | 2.23.x |
| Data generation | Java Faker | 1.0.x |
| Property-based testing | jqwik | 1.8.x |
| CI/CD | GitHub Actions, Jenkins, GitLab CI | — |

---

## Kiến trúc phân lớp

```
┌─────────────────────────────────────────────┐
│           TEST LAYER (src/test)              │
│  UIBaseTest → Tests → Pages → DataProviders  │
└──────────────────┬──────────────────────────┘
                   │ extends / uses
┌──────────────────▼──────────────────────────┐
│         CORE FRAMEWORK (src/main)            │
│  BasePage · WaitUtils · BrowserUtils         │
│  JavaScriptUtils · AssertionUtils            │
└──────────────────┬──────────────────────────┘
                   │ uses
┌──────────────────▼──────────────────────────┐
│       INFRASTRUCTURE LAYER (src/main)        │
│  DriverFactory · WebDriverManager            │
│  ConfigManager · ScreenshotCapturer          │
│  AllureListener · RetryAnalyzer              │
└──────────────────┬──────────────────────────┘
                   │ integrates
┌──────────────────▼──────────────────────────┐
│            EXTERNAL SYSTEMS                  │
│  Chrome/Firefox/Edge/Safari · Allure         │
│  CI Pipelines · Excel/CSV/JSON data          │
└─────────────────────────────────────────────┘
```

---

## Cấu trúc thư mục

```
framework-selenium-testng/
├── src/
│   ├── main/java/
│   │   ├── core/
│   │   │   ├── BasePage.java              # Abstract base cho Page Objects
│   │   │   ├── config/ConfigManager.java  # Singleton config reader
│   │   │   ├── driver/DriverFactory.java  # Factory tạo WebDriver
│   │   │   ├── driver/WebDriverManager.java # ThreadLocal driver wrapper
│   │   │   ├── report/AllureListener.java # TestNG event listener
│   │   │   ├── report/ScreenshotCapturer.java
│   │   │   └── utils/                    # WaitUtils, BrowserUtils, etc.
│   │   └── utils/DataGenerator.java      # Java Faker wrapper
│   ├── main/resources/
│   │   ├── config.properties             # Default config
│   │   ├── config-dev.properties         # Dev environment
│   │   ├── config-staging.properties     # Staging environment
│   │   ├── config-production.properties  # Production environment
│   │   └── log4j2.xml                    # Logging config
│   ├── test/java/
│   │   ├── base/
│   │   │   ├── BaseTest.java             # Root base (hooks only)
│   │   │   ├── UIBaseTest.java           # WebDriver lifecycle
│   │   │   └── RetryAnalyzer.java        # Flaky test retry
│   │   ├── pages/                        # Page Objects
│   │   ├── tests/                        # Test Cases
│   │   ├── dataproviders/                # Excel/CSV/JSON readers
│   │   └── utils/DataGenerator.java      # Test data generation
│   └── test/resources/
│       ├── testdata/                     # Test data files
│       └── testng-*.xml                  # TestNG suite configs
├── .ci/
│   ├── github-actions.yml
│   ├── Jenkinsfile
│   └── gitlab-ci.yml
├── docs/                                 # Framework documentation
├── docs_recommend/                       # Industry best practices
└── pom.xml
```

---

## Design Patterns áp dụng

| Pattern | Class | Mục đích |
|---|---|---|
| **Singleton** | `ConfigManager` | Một instance duy nhất, tránh đọc file nhiều lần |
| **Factory Method** | `DriverFactory` | Tạo WebDriver theo browser, dễ mở rộng |
| **ThreadLocal** | `WebDriverManager` | Thread-safe cho parallel execution |
| **Template Method** | `BasePage`, `BaseTest` | Skeleton cố định, subclass override từng bước |
| **Page Object Model** | Tất cả Page classes | Tách UI locators khỏi test logic |
| **Observer** | `AllureListener` | Lắng nghe TestNG events, không coupling |
| **Strategy** | `RetryAnalyzer` | Chiến lược retry có thể thay đổi |
| **Fluent Interface** | Page Object methods | Method chaining, readable test code |

---

## Luồng thực thi

```
mvn test -Dtest.suite=testng-saucedemo.xml -Denv=dev
    │
    ├── ConfigManager.loadConfig("dev")
    │     └── đọc config-dev.properties
    │
    ├── UIBaseTest.setUpDriver()
    │     ├── DriverFactory.createDriver("chrome")
    │     └── WebDriverManager.initDriver() → ThreadLocal
    │
    ├── beforeTest() hook
    │     └── new LoginPage(driver).open()
    │           └── BasePage.openByKey("login")
    │                 └── ConfigManager.getString("page.url.login")
    │
    ├── @Test method executes
    │     └── Page Object methods với @Step annotations
    │
    ├── afterTest() hook
    │
    ├── UIBaseTest.tearDownDriver()
    │     ├── handleTestFailure() → ScreenshotCapturer nếu fail
    │     └── WebDriverManager.quitDriver()
    │
    └── AllureListener → ghi kết quả vào target/allure-results/
```
