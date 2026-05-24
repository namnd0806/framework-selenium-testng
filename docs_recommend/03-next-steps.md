# Next Steps — Hành động cụ thể

Dựa trên phân tích framework và industry standards, đây là các bước cụ thể để nâng cấp framework theo thứ tự ưu tiên.

---

## Tuần 1-2: Quick Wins

### ✅ Task 1: Soft Assertions
Thêm `SoftAssert` vào `AssertionUtils` để collect nhiều lỗi trong 1 test.

**File cần sửa:** `src/main/java/core/utils/AssertionUtils.java`

```java
public static void softAssertAll(java.util.function.Consumer<org.testng.asserts.SoftAssert> assertions) {
    org.testng.asserts.SoftAssert soft = new org.testng.asserts.SoftAssert();
    assertions.accept(soft);
    soft.assertAll();
}
```

---

### ✅ Task 2: Move credentials to config

**File cần sửa:** `src/main/resources/config-dev.properties`

```properties
# Test credentials — KHÔNG commit production credentials
test.user.standard=standard_user
test.user.password=secret_sauce
test.user.locked=locked_out_user
test.user.problem=problem_user
```

**File cần sửa:** `SaucePurchaseTest.java`

```java
// Thay thế hardcoded values
private final String USERNAME = ConfigManager.getInstance().getString("test.user.standard");
private final String PASSWORD = ConfigManager.getInstance().getString("test.user.password");
```

---

### ✅ Task 3: Fluent Interface cho Page Objects

**File cần sửa:** Tất cả Page Objects trong `pages/saucedemo/`

```java
// Thêm return this vào các method
@Step("Nhập username: {username}")
public SauceLoginPage enterUsername(String username) {
    sendKeys(USERNAME_INPUT, username);
    return this;
}

@Step("Nhập password")
public SauceLoginPage enterPassword(String password) {
    sendKeys(PASSWORD_INPUT, password);
    return this;
}
```

---

## Tháng 1: API Testing Layer

### ✅ Task 4: Thêm RestAssured dependency

**File cần sửa:** `pom.xml`

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

### ✅ Task 5: Tạo APIBaseTest

**File cần tạo:** `src/test/java/base/APIBaseTest.java`

```java
public abstract class APIBaseTest extends BaseTest {
    protected io.restassured.specification.RequestSpecification requestSpec;

    @BeforeMethod
    public void setUpApi() {
        String apiUrl = ConfigManager.getInstance().getString("api.base.url", "");
        requestSpec = io.restassured.RestAssured.given()
            .baseUri(apiUrl)
            .contentType(io.restassured.http.ContentType.JSON)
            .log().ifValidationFails();
    }
}
```

---

## Tháng 2: BDD với Cucumber

### ✅ Task 6: Thêm Cucumber dependencies

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.18.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-testng</artifactId>
    <version>7.18.0</version>
    <scope>test</scope>
</dependency>
```

### ✅ Task 7: Tạo feature file

**File cần tạo:** `src/test/resources/features/purchase.feature`

```gherkin
Feature: Purchase Flow
  As a customer
  I want to purchase products
  So that I can receive them at home

  Background:
    Given user is on the login page

  Scenario: Successful purchase with 2 products
    When user logs in with username "standard_user" and password "secret_sauce"
    Then user should be on the inventory page
    When user adds 2 products to cart
    And user proceeds to checkout
    And user fills in "Nguyen" "Van A" "70000"
    And user confirms the order
    Then order confirmation should be displayed with "Thank you"
```

---

## Tháng 3: Docker + Selenium Grid

### ✅ Task 8: Tạo docker-compose.yml

**File cần tạo:** `docker/docker-compose.yml`

```yaml
version: "3.8"
services:
  selenium-hub:
    image: selenium/hub:4.21.0
    ports:
      - "4444:4444"

  chrome:
    image: selenium/node-chrome:4.21.0
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_NODE_MAX_SESSIONS=3
    volumes:
      - /dev/shm:/dev/shm

  firefox:
    image: selenium/node-firefox:4.21.0
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_NODE_MAX_SESSIONS=3
```

### ✅ Task 9: Remote WebDriver support trong DriverFactory

```java
// Thêm vào DriverFactory
private static WebDriver createRemoteDriver(String browser, boolean headless) {
    String gridUrl = ConfigManager.getInstance().getString("selenium.grid.url", "");
    if (gridUrl.isEmpty()) return null;

    ChromeOptions options = new ChromeOptions();
    if (headless) options.addArguments("--headless=new");

    try {
        return new RemoteWebDriver(new URL(gridUrl), options);
    } catch (Exception e) {
        log.warn("Grid not available, falling back to local driver");
        return null;
    }
}
```

---

## Tài nguyên học thêm

### Books
- **"Selenium Design Patterns and Best Practices"** — Dima Kovalenko (Packt)
- **"Software Engineering at Google"** — Titus Winters et al. (O'Reilly)
- **"Growing Object-Oriented Software, Guided by Tests"** — Freeman & Pryce

### Online Courses
- [LambdaTest Learning Hub](https://www.lambdatest.com/learning-hub/)
- [BrowserStack University](https://www.browserstack.com/guide)
- [Test Automation University](https://testautomationu.applitools.com/) — Free

### Communities
- [Ministry of Testing](https://www.ministryoftesting.com/)
- [Software Testing Help](https://www.softwaretestinghelp.com/)
- [r/QualityAssurance](https://www.reddit.com/r/QualityAssurance/)

### GitHub Repos đáng star
- [SeleniumHQ/selenium](https://github.com/SeleniumHQ/selenium)
- [AlfredStenwin/Advanced-Selenium-Automation-Framework](https://github.com/AlfredStenwin/Advanced-Selenium-Automation-Framework)
- [osandadeshan/selenium-testng-page-factory-extended](https://github.com/osandadeshan/selenium-testng-page-factory-extended)
