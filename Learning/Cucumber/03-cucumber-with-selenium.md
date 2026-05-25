# Cucumber với Selenium

## 1. Project Structure chuẩn

```
src/
├── main/java/com/example/
│   ├── pages/
│   │   ├── BasePage.java
│   │   ├── LoginPage.java
│   │   └── DashboardPage.java
│   └── utils/
│       ├── DriverFactory.java
│       └── ConfigReader.java
└── test/
    ├── java/com/example/
    │   ├── context/
    │   │   └── TestContext.java       ← Shared state
    │   ├── hooks/
    │   │   └── Hooks.java             ← @Before/@After
    │   ├── steps/
    │   │   ├── LoginSteps.java
    │   │   └── DashboardSteps.java
    │   └── runners/
    │       └── TestRunner.java
    └── resources/
        ├── features/
        │   ├── login.feature
        │   └── checkout.feature
        └── cucumber.properties
```

---

## 2. WebDriver Management với PicoContainer

```java
// TestContext.java - Shared state container
public class TestContext {
    private WebDriver driver;

    public WebDriver getDriver() {
        if (driver == null) {
            driver = DriverFactory.createDriver(
                ConfigReader.get("browser"),
                Boolean.parseBoolean(ConfigReader.get("headless"))
            );
        }
        return driver;
    }

    public void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}

// Hooks.java
public class Hooks {
    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @Before(order = 1)
    public void openBrowser() {
        context.getDriver().manage().window().maximize();
        context.getDriver().get(ConfigReader.get("base.url"));
    }

    @After(order = 1)
    public void closeBrowser(Scenario scenario) {
        if (scenario.isFailed()) {
            attachScreenshot(scenario);
        }
        context.quitDriver();
    }

    private void attachScreenshot(Scenario scenario) {
        try {
            byte[] screenshot = ((TakesScreenshot) context.getDriver())
                .getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png",
                "Failure Screenshot - " + scenario.getName());
        } catch (Exception e) {
            System.err.println("Could not take screenshot: " + e.getMessage());
        }
    }
}
```

---

## 3. Page Object Model với Cucumber

```java
// LoginSteps.java
public class LoginSteps {
    private final TestContext context;
    private LoginPage loginPage;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Given("I am on the login page")
    public void iAmOnLoginPage() {
        loginPage = new LoginPage(context.getDriver());
        assertThat(context.getDriver().getCurrentUrl()).contains("/login");
    }

    @When("I login with email {string} and password {string}")
    public void iLoginWith(String email, String password) {
        loginPage = new LoginPage(context.getDriver());
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
        loginPage.clickLoginButton();
    }

    @Then("I should be redirected to dashboard")
    public void iShouldBeOnDashboard() {
        WebDriverWait wait = new WebDriverWait(context.getDriver(), Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertThat(context.getDriver().getCurrentUrl()).contains("/dashboard");
    }

    @Then("I should see error {string}")
    public void iShouldSeeError(String expectedError) {
        assertThat(loginPage.getErrorMessage()).isEqualTo(expectedError);
    }
}
```

---

## 4. Ví dụ đầy đủ - Feature → Steps → Runner

```gherkin
# src/test/resources/features/login.feature
@smoke
Feature: User Login

  Background:
    Given I am on the login page

  @positive
  Scenario: Login with valid credentials
    When I login with email "user@test.com" and password "Pass@123"
    Then I should be redirected to dashboard
    And I should see welcome message "Hello, John"

  @negative
  Scenario Outline: Login with invalid credentials
    When I login with email "<email>" and password "<password>"
    Then I should see error "<error>"

    Examples:
      | email           | password  | error                     |
      | bad@test.com    | Pass@123  | Invalid email or password |
      | user@test.com   | wrongpass | Invalid email or password |
```

```java
// TestRunner.java
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.example.steps", "com.example.hooks"},
    plugin = {
        "pretty",
        "html:target/cucumber-reports/index.html",
        "json:target/cucumber-reports/cucumber.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    tags = "@smoke and not @wip",
    monochrome = true
)
public class TestRunner {}
```

---

## 5. Cucumber Reports

```xml
<!-- pom.xml - Allure Cucumber plugin -->
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-cucumber7-jvm</artifactId>
    <version>2.25.0</version>
    <scope>test</scope>
</dependency>
```

```java
// Thêm Allure annotations vào Step Definitions
@Given("I am on the login page")
@Step("Navigate to login page")
public void iAmOnLoginPage() {
    Allure.step("Opening login page: " + baseUrl + "/login");
    context.getDriver().get(baseUrl + "/login");
}
```

---

## 6. Parallel Cucumber với TestNG

```java
// TestNGRunner.java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.example",
    plugin = {"pretty", "html:target/cucumber-reports"}
)
public class TestNGRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

```xml
<!-- testng.xml -->
<suite name="Cucumber Suite" verbose="1">
    <test name="Cucumber Tests" thread-count="4">
        <classes>
            <class name="com.example.runners.TestNGRunner"/>
        </classes>
    </test>
</suite>
```

---

## 7. Chạy theo Tags từ command line

```bash
# Chạy smoke tests
mvn test -Dcucumber.filter.tags="@smoke"

# Chạy regression nhưng bỏ wip
mvn test -Dcucumber.filter.tags="@regression and not @wip"

# Chạy smoke hoặc sanity
mvn test -Dcucumber.filter.tags="@smoke or @sanity"

# Chạy với browser cụ thể
mvn test -Dbrowser=firefox -Dcucumber.filter.tags="@smoke"

# Chạy headless
mvn test -Dheadless=true -Dcucumber.filter.tags="@regression"
```

---

## 8. Câu hỏi phỏng vấn

**Q1: Làm thế nào để share WebDriver giữa các Step Definition classes trong Cucumber?**
> **Trả lời:** Dùng PicoContainer dependency injection. Tạo TestContext class chứa WebDriver, inject vào tất cả Step Definition classes qua constructor. PicoContainer đảm bảo cùng instance được inject.
>
> **Gợi nhớ:** PicoContainer = bưu điện, TestContext = bưu kiện, tất cả nhận cùng 1 bưu kiện

**Q2: Cucumber Scenario fail thì screenshot được chụp ở đâu?**
> **Trả lời:** Trong @After hook, kiểm tra scenario.isFailed(), nếu true thì chụp screenshot và attach vào scenario bằng scenario.attach(bytes, "image/png", "name"). Screenshot sẽ hiển thị trong Cucumber HTML report.
>
> **Gợi nhớ:** @After hook + scenario.isFailed() + scenario.attach() = screenshot tự động

**Q3: Sự khác nhau giữa JUnit Runner và TestNG Runner trong Cucumber?**
> **Trả lời:** JUnit Runner dùng @RunWith(Cucumber.class). TestNG Runner extends AbstractTestNGCucumberTests, có thể override scenarios() với @DataProvider(parallel=true) để chạy parallel dễ hơn.
>
> **Gợi nhớ:** TestNG Runner = JUnit Runner + parallel support built-in

**Q4: Tại sao không nên dùng static WebDriver trong Cucumber parallel?**
> **Trả lời:** Khi chạy parallel, nhiều scenarios chạy đồng thời trên nhiều threads. Static WebDriver bị share giữa threads → race condition, test fail ngẫu nhiên. Dùng PicoContainer để mỗi scenario có instance riêng.
>
> **Gợi nhớ:** Static + parallel = thảm họa, PicoContainer + parallel = an toàn

---

[Tiếp theo: ../Framework-Infrastructure/README.md](../Framework-Infrastructure/README.md) | [Quay lại README](./README.md)
