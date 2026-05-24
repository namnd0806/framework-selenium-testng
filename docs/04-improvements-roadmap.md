# Improvements Roadmap

Dựa trên phân tích framework hiện tại và so sánh với các tiêu chuẩn enterprise, đây là các cải tiến được đề xuất theo thứ tự ưu tiên.

---

## Priority 1 — Cải tiến ngay (High Impact, Low Effort)

### 1.1 Soft Assertions
**Vấn đề hiện tại:** Hard assertion dừng test ngay khi fail đầu tiên — mất thông tin về các lỗi khác.

**Giải pháp:** Thêm `SoftAssert` wrapper để collect tất cả lỗi rồi report một lần.

```java
// Thêm vào AssertionUtils
public static void softAssertAll(Consumer<SoftAssert> assertions) {
    SoftAssert soft = new SoftAssert();
    assertions.accept(soft);
    soft.assertAll(); // throw nếu có bất kỳ lỗi nào
}

// Dùng trong test
AssertionUtils.softAssertAll(soft -> {
    soft.assertEquals(page.getTitle(), "Expected Title", "Title check");
    soft.assertTrue(page.isButtonVisible(), "Button visibility");
    soft.assertNotNull(page.getUserName(), "Username not null");
});
```

---

### 1.2 Fluent Page Object Interface
**Vấn đề hiện tại:** Các method không trả về `this` → không thể chain.

**Giải pháp:** Áp dụng Fluent Interface pattern.

```java
// Trước
loginPage.enterUsername("user");
loginPage.enterPassword("pass");
loginPage.clickLogin();

// Sau (Fluent)
loginPage
    .enterUsername("user")
    .enterPassword("pass")
    .clickLogin();
```

---

### 1.3 Environment-aware Test Data
**Vấn đề hiện tại:** Credentials hardcoded trong test class.

**Giải pháp:** Đọc test credentials từ config hoặc environment variables.

```properties
# config-dev.properties
test.user.standard=standard_user
test.user.password=secret_sauce
```

```java
String username = ConfigManager.getInstance().getString("test.user.standard");
```

---

### 1.4 Screenshot on Every Step (Optional)
**Giải pháp:** Thêm option chụp ảnh sau mỗi `@Step` quan trọng.

```java
@Step("Click nút Checkout")
public void clickCheckout() {
    executeScript("document.getElementById('checkout').click();");
    if (ConfigManager.getInstance().getBoolean("screenshot.on.step", false)) {
        ScreenshotCapturer.attachToAllure(driver);
    }
}
```

---

## Priority 2 — Cải tiến trung hạn (Medium Effort)

### 2.1 ExtentReports Integration
**Lý do:** Allure cần server để xem — ExtentReports tạo 1 file HTML duy nhất, gửi email được.

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.aventstack</groupId>
    <artifactId>extentreports</artifactId>
    <version>5.1.1</version>
</dependency>
```

Tạo `ExtentReportListener` song song với `AllureListener`.

---

### 2.2 BDD với Cucumber
**Lý do:** Business stakeholders đọc được test scenarios, không cần biết code.

```gherkin
Feature: Purchase Flow
  Scenario: Successful purchase
    Given user is on the login page
    When user logs in with valid credentials
    And user adds 2 products to cart
    And user completes checkout with valid info
    Then order confirmation should be displayed
```

Stack: Selenium + TestNG + Cucumber + Allure

---

### 2.3 API Testing Layer
**Lý do:** Test pyramid — cần nhiều API tests hơn UI tests.

```java
public abstract class APIBaseTest extends BaseTest {
    protected RequestSpecification requestSpec;

    @BeforeMethod
    public void setUpApi() {
        requestSpec = RestAssured.given()
            .baseUri(ConfigManager.getInstance().getString("api.base.url"))
            .contentType(ContentType.JSON);
    }
}
```

---

### 2.4 Database Verification
**Lý do:** Verify dữ liệu được lưu đúng sau UI actions.

```java
public class DatabaseUtils {
    public static ResultSet query(String sql) {
        // JDBC connection từ config
    }
}

// Trong test
@Test
public void testOrderCreated() {
    // UI: complete purchase
    purchasePage.completePurchase();

    // DB: verify order exists
    ResultSet rs = DatabaseUtils.query("SELECT * FROM orders WHERE user_id = 1");
    AssertionUtils.assertTrue(rs.next(), "Order should exist in database");
}
```

---

### 2.5 Visual Testing
**Lý do:** Phát hiện UI regression mà functional tests bỏ qua.

```java
// Dùng Applitools Eyes hoặc Percy
@Test
public void testLoginPageVisual() {
    eyes.open(driver, "MyApp", "Login Page");
    loginPage.open();
    eyes.checkWindow("Login Page");
    eyes.close();
}
```

---

## Priority 3 — Cải tiến dài hạn (High Effort, High Value)

### 3.1 Docker + Selenium Grid
**Lý do:** Chạy parallel trên nhiều máy, nhiều browser version.

```yaml
# docker-compose.yml
services:
  selenium-hub:
    image: selenium/hub:4.21.0
  chrome:
    image: selenium/node-chrome:4.21.0
  firefox:
    image: selenium/node-firefox:4.21.0
```

```java
// DriverFactory — Remote WebDriver
WebDriver driver = new RemoteWebDriver(
    new URL("http://selenium-hub:4444/wd/hub"),
    options
);
```

---

### 3.2 Self-Healing Locators
**Lý do:** Giảm maintenance khi UI thay đổi locators.

Dùng **Healenium** — tự động tìm locator mới khi locator cũ bị broken:

```xml
<dependency>
    <groupId>com.epam.healenium</groupId>
    <artifactId>healenium-web</artifactId>
    <version>3.4.4</version>
</dependency>
```

```java
// Wrap driver với SelfHealingDriver
WebDriver driver = SelfHealingDriver.create(originalDriver);
```

---

### 3.3 Test Data Management
**Lý do:** Test data cần được tạo, dùng và cleanup tự động.

```java
public class TestDataManager {
    // Tạo user trước test
    public static User createTestUser() {
        return ApiClient.post("/users", new UserRequest(...));
    }

    // Cleanup sau test
    public static void deleteTestUser(String userId) {
        ApiClient.delete("/users/" + userId);
    }
}
```

---

### 3.4 AI-Assisted Test Generation
**Xu hướng 2024-2025:** Dùng AI để generate test cases từ user stories.

Tools đang được các công ty lớn nghiên cứu:
- **Applitools Autonomous** — AI visual testing
- **Testim** — AI-powered test authoring
- **Mabl** — ML-based test maintenance

---

## Tóm tắt Roadmap

```
Q1 (Ngay bây giờ)
├── Soft Assertions
├── Fluent Interface
├── Environment-aware credentials
└── Screenshot on step (optional)

Q2 (1-3 tháng)
├── ExtentReports
├── Cucumber BDD
└── API Testing Layer

Q3-Q4 (3-6 tháng)
├── Docker + Selenium Grid
├── Database Verification
└── Self-Healing Locators

Long-term (6+ tháng)
├── Visual Testing
├── AI-Assisted Testing
└── Full Test Data Management
```
