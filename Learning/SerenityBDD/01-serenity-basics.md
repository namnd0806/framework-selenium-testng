# Serenity BDD Basics — Setup, Steps, Reports

---

## 1. Serenity là gì?

Serenity BDD = framework kết hợp:
- **BDD** — viết test bằng Gherkin (Cucumber)
- **Automation** — tích hợp Selenium, REST Assured
- **Living Documentation** — report tự động từ test code

```
Không có Serenity:
Cucumber + Selenium + REST Assured + Allure = 4 thứ cần cấu hình riêng

Có Serenity:
Serenity = tất cả trong 1, report đẹp hơn, ít boilerplate hơn
```

---

## 2. Setup Maven

```xml
<!-- pom.xml -->
<properties>
    <serenity.version>4.1.4</serenity.version>
    <serenity.cucumber.version>4.1.4</serenity.cucumber.version>
</properties>

<dependencies>
    <!-- Serenity core -->
    <dependency>
        <groupId>net.serenity-bdd</groupId>
        <artifactId>serenity-core</artifactId>
        <version>${serenity.version}</version>
        <scope>test</scope>
    </dependency>

    <!-- Serenity + Cucumber -->
    <dependency>
        <groupId>net.serenity-bdd</groupId>
        <artifactId>serenity-cucumber</artifactId>
        <version>${serenity.cucumber.version}</version>
        <scope>test</scope>
    </dependency>

    <!-- Serenity + REST Assured -->
    <dependency>
        <groupId>net.serenity-bdd</groupId>
        <artifactId>serenity-rest-assured</artifactId>
        <version>${serenity.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Maven Failsafe cho integration test -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-failsafe-plugin</artifactId>
            <version>3.2.5</version>
            <configuration>
                <includes>
                    <include>**/*Runner.java</include>
                </includes>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>integration-test</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- Serenity Maven Plugin — generate report -->
        <plugin>
            <groupId>net.serenity-bdd.maven.plugins</groupId>
            <artifactId>serenity-maven-plugin</artifactId>
            <version>${serenity.version}</version>
            <executions>
                <execution>
                    <id>serenity-reports</id>
                    <phase>post-integration-test</phase>
                    <goals>
                        <goal>aggregate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

---

## 3. serenity.conf — Cấu hình

```hocon
# src/test/resources/serenity.conf
serenity {
    project.name = "E-commerce Automation"
    take.screenshots = FOR_FAILURES  # BEFORE_AND_AFTER_EACH_STEP, FOR_FAILURES, AFTER_EACH_STEP

    # WebDriver config
    driver = chrome
    webdriver {
        driver = chrome
        autodownload = true  # Tự download ChromeDriver
    }
}

environments {
    default {
        webdriver.base.url = "https://staging.example.com"
    }
    staging {
        webdriver.base.url = "https://staging.example.com"
    }
    prod {
        webdriver.base.url = "https://www.example.com"
    }
}

# Chạy với environment: mvn verify -Denvironment=staging
```

---

## 4. @Steps — Reusable Step Library

`@Steps` là tính năng đặc trưng của Serenity — tạo thư viện bước tái sử dụng, hiển thị trong report.

```java
// Step library — không phải Cucumber step definitions
import net.serenitybdd.annotations.Step;

public class LoginSteps {

    // @Step — hiển thị trong Serenity report như 1 bước
    @Step("Open the login page")
    public void openLoginPage() {
        driver.get(Serenity.environmentVariables().getProperty("webdriver.base.url") + "/login");
    }

    @Step("Enter email {0}")
    public void enterEmail(String email) {
        $(By.id("email")).sendKeys(email);
    }

    @Step("Enter password")
    public void enterPassword(String password) {
        $(By.id("password")).sendKeys(password);
    }

    @Step("Click login button")
    public void clickLogin() {
        $(By.cssSelector(".btn-login")).click();
    }

    // Composite step — gộp nhiều bước
    @Step("Login as {0}")
    public void loginAs(String email, String password) {
        openLoginPage();
        enterEmail(email);
        enterPassword(password);
        clickLogin();
    }

    @Step("Verify dashboard is displayed")
    public void verifyDashboardDisplayed() {
        assertThat($(By.id("dashboard")).isDisplayed()).isTrue();
    }
}
```

---

## 5. @Managed WebDriver — Serenity quản lý driver

```java
import net.serenitybdd.annotations.Managed;
import net.thucydides.core.annotations.Steps;

// Serenity tự khởi tạo và đóng WebDriver
public class LoginTest {

    @Managed(driver = "chrome")
    WebDriver driver;  // Serenity inject driver tự động

    @Steps
    LoginSteps loginSteps;  // Serenity inject steps tự động

    @Test
    public void shouldLoginSuccessfully() {
        loginSteps.loginAs("user@test.com", "Pass@123");
        loginSteps.verifyDashboardDisplayed();
    }
}
```

---

## 6. Page Object với Serenity

```java
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.annotations.DefaultUrl;

@DefaultUrl("https://staging.example.com/login")
public class LoginPage extends PageObject {

    // Serenity dùng $ để tìm element (tự wait)
    private static final By EMAIL_INPUT    = By.id("email");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By LOGIN_BUTTON   = By.cssSelector(".btn-login");
    private static final By ERROR_MESSAGE  = By.cssSelector(".error-msg");

    public void enterEmail(String email) {
        $(EMAIL_INPUT).sendKeys(email);
    }

    public void enterPassword(String password) {
        $(PASSWORD_INPUT).sendKeys(password);
    }

    public void clickLogin() {
        $(LOGIN_BUTTON).click();
    }

    public String getErrorMessage() {
        return $(ERROR_MESSAGE).getText();
    }

    // Serenity $ tự chờ element visible — không cần explicit wait
    public boolean isErrorDisplayed() {
        return $(ERROR_MESSAGE).isVisible();
    }
}
```

---

## 7. Serenity Report

```bash
# Chạy test và generate report
mvn clean verify

# Report ở: target/site/serenity/index.html
```

**Report bao gồm:**
- **Requirements** — hierarchy: Epic → Feature → Story → Scenario
- **Test Results** — pass/fail/pending với screenshots
- **Steps** — từng @Step với thời gian chạy
- **Living Documentation** — feature files được render đẹp
- **Coverage** — % requirements được test

---

## Câu hỏi phỏng vấn

**Q1: Serenity BDD khác Cucumber thuần thế nào?**
```
Serenity = Cucumber + nhiều thứ hơn:
- @Steps: step library tái sử dụng, hiển thị trong report
- @Managed: Serenity quản lý WebDriver lifecycle
- Living Documentation: report đẹp với requirements hierarchy
- PageObject: $ operator tự wait, không cần explicit wait
- REST Assured tích hợp sẵn

Gợi nhớ: Cucumber = khung xương, Serenity = khung xương + thịt + quần áo đẹp
```

**Q2: @Steps trong Serenity dùng thế nào?**
```
@Steps = annotation để inject step library vào test class
Serenity tự tạo instance và proxy để ghi lại steps vào report

@Steps
LoginSteps loginSteps; // Serenity inject

loginSteps.loginAs("user@test.com", "pass"); // Hiển thị trong report

Gợi nhớ: @Steps = @Autowired của Serenity, tự inject và tự log
```

**Q3: Living Documentation là gì?**
```
Living Documentation = tài liệu luôn up-to-date vì được generate từ test code

Khi test pass → tài liệu xanh (feature hoạt động)
Khi test fail → tài liệu đỏ (feature bị broken)

Không như Word/Confluence có thể outdated,
Living Documentation luôn phản ánh trạng thái thực của hệ thống.

Gợi nhớ: Living = sống, tự cập nhật theo kết quả test
```

---

**Tiếp theo:** [02-serenity-with-cucumber.md](./02-serenity-with-cucumber.md) | [Quay lại README](./README.md)
