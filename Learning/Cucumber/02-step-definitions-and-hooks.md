# Step Definitions và Hooks

## 1. Step Definitions - Map Gherkin → Java

```java
import io.cucumber.java.en.*;

public class LoginSteps {

    // Map với Gherkin step
    @Given("I am on the login page")
    public void iAmOnLoginPage() {
        driver.get(baseUrl + "/login");
    }

    // Cucumber Expressions - {string}, {int}, {double}, {word}
    @When("I enter email {string} and password {string}")
    public void iEnterCredentials(String email, String password) {
        loginPage.enterEmail(email);
        loginPage.enterPassword(password);
    }

    @When("I wait {int} seconds")
    public void iWaitSeconds(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
    }

    @Then("I should see error message {string}")
    public void iShouldSeeErrorMessage(String expectedMessage) {
        String actualMessage = loginPage.getErrorMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

    // Regular Expression (linh hoạt hơn)
    @When("^I click the (.+) button$")
    public void iClickButton(String buttonName) {
        page.clickButton(buttonName);
    }
}
```

---

## 2. Parameter Types

```java
// Built-in parameter types
{string}  → String  (trong dấu nháy đôi hoặc đơn: "text" hoặc 'text')
{int}     → int     (số nguyên: 42)
{double}  → double  (số thực: 3.14)
{word}    → String  (1 từ không có space: hello)
{bigdecimal} → BigDecimal
{}        → String  (bất kỳ text nào, không cần nháy)

// Custom Parameter Type
@ParameterType("admin|user|manager")
public UserRole role(String roleName) {
    return UserRole.valueOf(roleName.toUpperCase());
}

// Dùng trong step
@Given("I am logged in as {role}")
public void iAmLoggedInAs(UserRole role) {
    authHelper.loginAs(role);
}

// Gherkin: Given I am logged in as admin
```

---

## 3. Hooks - @Before, @After, @BeforeStep, @AfterStep

```java
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Scenario;

public class Hooks {

    private final DriverManager driverManager;

    // PicoContainer inject dependency
    public Hooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    // Chạy trước mỗi Scenario
    @Before(order = 1)
    public void setupDriver() {
        WebDriver driver = DriverFactory.createDriver("chrome", false);
        driverManager.setDriver(driver);
    }

    // Chỉ chạy cho scenario có tag @login
    @Before(value = "@login", order = 2)
    public void loginBeforeTest() {
        driverManager.getDriver().get(baseUrl + "/login");
        new LoginPage(driverManager.getDriver()).login("admin@test.com", "Admin@123");
    }

    // Chạy sau mỗi Scenario
    @After(order = 1)
    public void teardown(Scenario scenario) {
        // Chụp screenshot nếu fail
        if (scenario.isFailed()) {
            WebDriver driver = driverManager.getDriver();
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Screenshot on failure");
        }
        driverManager.quitDriver();
    }

    // Chạy trước/sau mỗi Step
    @BeforeStep
    public void beforeEachStep(Scenario scenario) {
        System.out.println("  → Step: " + scenario.getName());
    }

    @AfterStep
    public void afterEachStep(Scenario scenario) {
        // Chụp screenshot sau mỗi step (debug mode)
        if (System.getProperty("debug.screenshots") != null) {
            byte[] screenshot = ((TakesScreenshot) driverManager.getDriver())
                .getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", "Step screenshot");
        }
    }
}
```

---

## 4. Sharing State với PicoContainer (Cách đúng nhất)

```java
// Vấn đề: Step definitions ở nhiều class, cần share WebDriver và state

// ❌ Sai: dùng static field
public class LoginSteps {
    public static WebDriver driver; // Thread-unsafe, khó test
}

// ✅ Đúng: PicoContainer Dependency Injection
// pom.xml dependency:
// <artifactId>cucumber-picocontainer</artifactId>

// 1. Tạo class chứa shared state
public class TestContext {
    private WebDriver driver;
    private String authToken;
    private Map<String, Object> testData = new HashMap<>();

    public WebDriver getDriver() { return driver; }
    public void setDriver(WebDriver driver) { this.driver = driver; }
    public String getAuthToken() { return authToken; }
    public void setAuthToken(String token) { this.authToken = token; }
}

// 2. Inject vào Step Definition classes
public class LoginSteps {
    private final TestContext context;
    private LoginPage loginPage;

    // PicoContainer tự inject TestContext
    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Given("I am on the login page")
    public void iAmOnLoginPage() {
        loginPage = new LoginPage(context.getDriver());
        context.getDriver().get(baseUrl + "/login");
    }
}

public class DashboardSteps {
    private final TestContext context;

    public DashboardSteps(TestContext context) {
        this.context = context; // Cùng instance với LoginSteps
    }

    @Then("I should be on the dashboard")
    public void iShouldBeOnDashboard() {
        DashboardPage dashboard = new DashboardPage(context.getDriver());
        assertThat(dashboard.isLoaded()).isTrue();
    }
}
```

---

## 5. Cucumber Runner

```java
// JUnit 4 Runner
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.example.steps", "com.example.hooks"},
    tags = "@smoke",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/report.html",
        "json:target/cucumber-reports/report.json",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
    },
    monochrome = true,    // Output không có màu (đọc dễ hơn trong CI)
    dryRun = false        // true = chỉ check step definitions, không chạy test
)
public class TestRunner {}

// TestNG Runner
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.example",
    plugin = {"pretty", "html:target/cucumber-reports"}
)
public class TestNGRunner extends AbstractTestNGCucumberTests {

    // Override để chạy parallel
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
```

---

## 6. Câu hỏi phỏng vấn

**Q1: Tại sao dùng PicoContainer thay vì static fields để share state?**
> **Trả lời:** Static fields gây vấn đề khi chạy parallel (thread-unsafe), khó reset giữa các scenarios, và tạo coupling giữa classes. PicoContainer inject dependency, mỗi scenario có instance riêng, thread-safe.
>
> **Gợi nhớ:** PicoContainer = mỗi scenario có hộp riêng, static = tất cả dùng chung 1 hộp

**Q2: Sự khác nhau giữa @Before Cucumber và @BeforeMethod TestNG?**
> **Trả lời:** @Before Cucumber chạy trước mỗi Scenario, có thể filter theo tag. @BeforeMethod TestNG chạy trước mỗi @Test method. Trong Cucumber project, dùng @Before Cucumber, không dùng @BeforeMethod.
>
> **Gợi nhớ:** Cucumber dùng @Before của Cucumber, không mix với TestNG annotations

**Q3: dryRun = true trong CucumberOptions dùng để làm gì?**
> **Trả lời:** Chỉ kiểm tra xem tất cả Gherkin steps có step definition tương ứng không, không thực sự chạy test. Hữu ích khi viết feature file mới để biết cần implement step nào.
>
> **Gợi nhớ:** dryRun = diễn tập, kiểm tra kịch bản mà không diễn thật

**Q4: Cucumber Expressions khác Regular Expressions thế nào?**
> **Trả lời:** Cucumber Expressions đơn giản hơn: {string}, {int}, {word} dễ đọc và viết. Regular Expressions linh hoạt hơn nhưng phức tạp. Nên dùng Cucumber Expressions trừ khi cần pattern phức tạp.
>
> **Gợi nhớ:** Cucumber Expressions = tiếng Việt, Regex = tiếng mã hóa

---

[Tiếp theo: 03-cucumber-with-selenium.md](./03-cucumber-with-selenium.md) | [Quay lại README](./README.md)
