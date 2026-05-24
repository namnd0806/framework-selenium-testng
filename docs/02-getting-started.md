# Getting Started

## Yêu cầu hệ thống

| Công cụ | Phiên bản tối thiểu | Kiểm tra |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Chrome | Latest | — |
| Firefox | Latest | — |
| Git | 2.x+ | `git --version` |

---

## Cài đặt

```bash
git clone https://github.com/namnd0806/framework-selenium-testng.git
cd framework-selenium-testng
mvn clean install -DskipTests
```

---

## Chạy test

### Cơ bản
```bash
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=dev"
```

### Chọn browser
```bash
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=dev" "-Dbrowser=firefox"
```

### Headless (không mở browser)
```bash
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=dev" "-Dheadless=true"
```

### Chạy song song
```bash
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=dev" "-Dthread.count=4"
```

### Chạy theo môi trường
```bash
# Dev
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=dev"

# Staging
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=staging"

# Production
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=production"
```

---

## Xem Allure Report

```bash
# Chạy test (tạo allure-results)
mvn test "-Dtest.suite=testng-saucedemo.xml" "-Denv=dev" "-Dallure.results.directory=target/allure-results"

# Mở report trên browser
mvn allure:serve "-Dallure.results.directory=target/allure-results"
```

---

## Cấu hình môi trường

Mỗi môi trường có file properties riêng trong `src/main/resources/`:

```properties
# config-dev.properties
base.url=https://www.saucedemo.com
browser=chrome
headless=false
explicit.wait.timeout=10
page.url.login=https://www.saucedemo.com
```

Override bất kỳ giá trị nào qua command line:
```bash
mvn test "-Dbase.url=https://custom-url.com" "-Dbrowser=edge"
```

Priority: **System property > Environment variable > config-{env}.properties > config.properties**

---

## Viết test mới

### 1. Tạo Page Object

```java
package pages.myapp;

import core.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DashboardPage extends BasePage {

    private static final By WELCOME_MSG = By.cssSelector(".welcome-message");
    private static final By LOGOUT_BTN  = By.id("logout");

    public DashboardPage(WebDriver driver) {
        super(driver);
    }

    @Step("Mở trang Dashboard")
    public DashboardPage open() {
        openByKey("dashboard"); // đọc page.url.dashboard từ config
        return this;
    }

    public String getWelcomeMessage() {
        return getText(WELCOME_MSG);
    }

    @Step("Đăng xuất")
    public void logout() {
        click(LOGOUT_BTN);
    }
}
```

### 2. Tạo Test Class

```java
package tests.myapp;

import base.UIBaseTest;
import core.utils.AssertionUtils;
import io.qameta.allure.*;
import org.testng.annotations.Test;
import pages.myapp.DashboardPage;

@Epic("My Application")
@Feature("Dashboard")
public class DashboardTest extends UIBaseTest {

    private DashboardPage dashboardPage;

    @Override
    protected void beforeTest() {
        dashboardPage = new DashboardPage(driver);
        dashboardPage.open();
    }

    @Test(description = "Verify welcome message is displayed")
    @Story("Welcome Message")
    @Severity(SeverityLevel.NORMAL)
    public void testWelcomeMessage() {
        String msg = dashboardPage.getWelcomeMessage();
        AssertionUtils.assertTrue(!msg.isEmpty(), "Welcome message should not be empty");
    }
}
```

### 3. Thêm URL vào config

```properties
# config-dev.properties
page.url.dashboard=https://myapp.dev/dashboard
```

### 4. Chạy test
```bash
mvn test "-Dtest.suite=testng-suite.xml" "-Denv=dev"
```
