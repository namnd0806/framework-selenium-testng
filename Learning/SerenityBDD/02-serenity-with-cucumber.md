# Serenity + Cucumber — Tích hợp đầy đủ

---

## 1. Project Structure

```
src/
├── test/
│   ├── java/
│   │   └── com/example/
│   │       ├── runners/
│   │       │   └── CucumberTestRunner.java
│   │       ├── stepdefinitions/
│   │       │   ├── LoginStepDefs.java
│   │       │   └── CheckoutStepDefs.java
│   │       ├── steps/           ← Serenity @Steps library
│   │       │   ├── LoginSteps.java
│   │       │   └── NavigationSteps.java
│   │       └── pages/           ← Page Objects
│   │           ├── LoginPage.java
│   │           └── DashboardPage.java
│   └── resources/
│       ├── features/
│       │   ├── login/
│       │   │   └── login.feature
│       │   └── checkout/
│       │       └── checkout.feature
│       └── serenity.conf
```

---

## 2. Cucumber Runner với Serenity

```java
// CucumberTestRunner.java
import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)  // Dùng Serenity runner, không phải Cucumber runner
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "com.example.stepdefinitions",
    tags = "@smoke",
    plugin = {
        "pretty",
        "json:target/cucumber-reports/cucumber.json"
        // Không cần Allure plugin — Serenity tự generate report
    }
)
public class CucumberTestRunner {
    // Empty — Serenity xử lý tất cả
}
```

---

## 3. Feature File với Requirements Hierarchy

```gherkin
# src/test/resources/features/authentication/login.feature

@authentication
Feature: User Authentication
  In order to access the application
  As a registered user
  I want to be able to login securely

  Background:
    Given the application is available

  @smoke @login
  Scenario: Successful login with valid credentials
    Given I am on the login page
    When I login with email "user@test.com" and password "Pass@123"
    Then I should be on the dashboard
    And I should see welcome message "Hello, Test User"

  @regression @login
  Scenario Outline: Login with invalid credentials
    Given I am on the login page
    When I login with email "<email>" and password "<password>"
    Then I should see error "<error_message>"

    Examples:
      | email              | password  | error_message             |
      | wrong@test.com     | Pass@123  | Invalid email or password |
      | user@test.com      | wrongpass | Invalid email or password |
      |                    | Pass@123  | Email is required         |
```

---

## 4. Step Definitions — Kết nối Gherkin với Steps

```java
// LoginStepDefs.java — Cucumber step definitions
import io.cucumber.java.en.*;
import net.thucydides.core.annotations.Steps;

public class LoginStepDefs {

    // Inject Serenity @Steps library
    @Steps
    LoginSteps loginSteps;

    @Steps
    NavigationSteps navigationSteps;

    @Given("the application is available")
    public void theApplicationIsAvailable() {
        navigationSteps.openHomePage();
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        navigationSteps.openLoginPage();
    }

    @When("I login with email {string} and password {string}")
    public void iLoginWith(String email, String password) {
        loginSteps.enterEmail(email);
        loginSteps.enterPassword(password);
        loginSteps.clickLoginButton();
    }

    @Then("I should be on the dashboard")
    public void iShouldBeOnDashboard() {
        loginSteps.verifyOnDashboard();
    }

    @Then("I should see welcome message {string}")
    public void iShouldSeeWelcomeMessage(String message) {
        loginSteps.verifyWelcomeMessage(message);
    }

    @Then("I should see error {string}")
    public void iShouldSeeError(String errorMessage) {
        loginSteps.verifyErrorMessage(errorMessage);
    }
}
```

---

## 5. Serenity Steps Library

```java
// LoginSteps.java — Serenity @Steps (không phải Cucumber step defs)
import net.serenitybdd.annotations.Step;
import net.thucydides.core.annotations.Steps;
import static net.serenitybdd.rest.SerenityRest.*;
import static org.assertj.core.api.Assertions.*;

public class LoginSteps {

    LoginPage loginPage;  // Serenity inject PageObject tự động

    @Step("Open login page")
    public void openLoginPage() {
        loginPage.open();  // @DefaultUrl trong LoginPage
    }

    @Step("Enter email: {0}")
    public void enterEmail(String email) {
        loginPage.enterEmail(email);
    }

    @Step("Enter password")
    public void enterPassword(String password) {
        loginPage.enterPassword(password);
    }

    @Step("Click login button")
    public void clickLoginButton() {
        loginPage.clickLogin();
    }

    @Step("Verify user is on dashboard")
    public void verifyOnDashboard() {
        assertThat(loginPage.getCurrentUrl()).contains("/dashboard");
    }

    @Step("Verify welcome message: {0}")
    public void verifyWelcomeMessage(String expectedMessage) {
        assertThat(loginPage.getWelcomeMessage()).isEqualTo(expectedMessage);
    }

    @Step("Verify error message: {0}")
    public void verifyErrorMessage(String expectedError) {
        assertThat(loginPage.getErrorMessage()).isEqualTo(expectedError);
    }
}
```

---

## 6. Hooks trong Serenity + Cucumber

```java
// Hooks.java
import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import net.serenitybdd.core.Serenity;

public class Hooks {

    @Before
    public void beforeScenario(Scenario scenario) {
        System.out.println("Starting: " + scenario.getName());
        // Serenity tự quản lý WebDriver — không cần khởi tạo ở đây
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (scenario.isFailed()) {
            // Serenity tự chụp screenshot khi fail
            // Nhưng có thể thêm custom attachment
            Serenity.recordReportData()
                .withTitle("Failure Details")
                .andContents("Scenario failed: " + scenario.getName());
        }
    }

    @Before("@api")
    public void setupApiTest() {
        // Setup chỉ cho scenarios có tag @api
        SerenityRest.setDefaultBasePath("/api/v1");
    }
}
```

---

## 7. Serenity + REST Assured

```java
// ApiSteps.java
import net.serenitybdd.annotations.Step;
import static net.serenitybdd.rest.SerenityRest.*;

public class UserApiSteps {

    @Step("Create user via API: {0}")
    public int createUser(String email, String password) {
        return given()
            .contentType("application/json")
            .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
        .when()
            .post("/users")
        .then()
            .statusCode(201)
            .extract().path("id");
        // SerenityRest tự log request/response vào report
    }

    @Step("Delete user via API: {0}")
    public void deleteUser(int userId) {
        given()
            .pathParam("id", userId)
        .when()
            .delete("/users/{id}")
        .then()
            .statusCode(204);
    }
}

// Dùng trong test — setup data qua API, test UI
public class LoginStepDefs {

    @Steps UserApiSteps userApiSteps;
    @Steps LoginSteps loginSteps;

    private int createdUserId;

    @Before("@createUser")
    public void createTestUser() {
        createdUserId = userApiSteps.createUser("test@example.com", "Pass@123");
    }

    @After("@createUser")
    public void cleanupTestUser() {
        userApiSteps.deleteUser(createdUserId);
    }
}
```

---

## 8. Chạy test

```bash
# Chạy tất cả
mvn clean verify

# Chạy theo tag
mvn clean verify -Dcucumber.filter.tags="@smoke"

# Chạy theo environment
mvn clean verify -Denvironment=staging

# Chạy headless
mvn clean verify -Dwebdriver.driver=chrome -Dchrome.switches="--headless=new"

# Report ở: target/site/serenity/index.html
open target/site/serenity/index.html
```

---

## Câu hỏi phỏng vấn

**Q1: Sự khác nhau giữa Serenity @Steps và Cucumber Step Definitions?**
```
Cucumber Step Definitions:
- Map Gherkin text → Java code
- @Given, @When, @Then
- Không hiển thị trong report như bước riêng

Serenity @Steps:
- Reusable action library
- @Step annotation → hiển thị trong report
- Inject bằng @Steps annotation
- Có thể nested (step trong step)

Gợi nhớ: Step Defs = cầu nối Gherkin-Java, @Steps = thư viện hành động tái sử dụng
```

**Q2: Tại sao dùng CucumberWithSerenity runner thay vì Cucumber runner thông thường?**
```
CucumberWithSerenity runner:
- Tích hợp Serenity lifecycle (setup/teardown WebDriver)
- Tự generate Serenity report
- Hỗ trợ @Steps injection
- Hỗ trợ @Managed WebDriver

Cucumber runner thông thường:
- Chỉ chạy Cucumber, không có Serenity features
- Phải tự setup WebDriver, report

Gợi nhớ: CucumberWithSerenity = Cucumber runner + Serenity magic
```

**Q3: Serenity report có gì đặc biệt so với Allure?**
```
Serenity report:
- Requirements hierarchy (Epic → Feature → Story)
- Living Documentation — feature files được render
- Coverage % theo requirements
- Tự động từ feature files, không cần annotation thêm

Allure report:
- Đẹp hơn về UI
- Cần @Feature, @Story annotation thủ công
- Không có requirements hierarchy tự động
- Phổ biến hơn với TestNG thuần

Gợi nhớ: Serenity report = tự động từ feature files, Allure = cần annotation thủ công
```

---

**Tiếp theo:** [03-screenplay-pattern.md](./03-screenplay-pattern.md) | [Quay lại README](./README.md)
