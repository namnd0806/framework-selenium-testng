# Industry Standards & Real-World Frameworks

Tài liệu này tổng hợp các tiêu chuẩn, patterns và kinh nghiệm thực tế từ các công ty lớn, ngân hàng và tổ chức tài chính trong việc xây dựng Selenium automation framework.

---

## 1. Test Pyramid — Nền tảng của mọi framework

Theo Google, Microsoft và các công ty FAANG, tỷ lệ test lý tưởng:

```
        /\
       /  \
      / UI \        ← 10% (Selenium, Playwright)
     /------\
    /  API   \      ← 20% (RestAssured, Postman)
   /----------\
  /   Unit     \    ← 70% (JUnit, Mockito)
 /--------------\
```

**Ý nghĩa thực tế:**
- Đừng viết quá nhiều UI tests — chậm, flaky, tốn maintenance
- Mỗi UI test nên cover 1 end-to-end flow quan trọng
- Business logic nên được cover bởi unit/API tests

**Nguồn:** [Google Testing Blog](https://testing.googleblog.com/2015/04/just-say-no-to-more-end-to-end-tests.html)

---

## 2. Patterns được dùng tại các công ty lớn

### 2.1 Page Object Model (POM) — Standard tại mọi nơi

Được dùng tại: Google, Amazon, Microsoft, JPMorgan Chase, Goldman Sachs.

**Nguyên tắc cốt lõi:**
- Page Object chỉ chứa locators và interactions
- Không có assertions trong Page Object
- Mỗi page/component = 1 class

```java
// ✅ Đúng — Page Object chỉ có interactions
public class LoginPage extends BasePage {
    public void login(String user, String pass) {
        sendKeys(USERNAME, user);
        sendKeys(PASSWORD, pass);
        click(LOGIN_BTN);
    }
}

// ❌ Sai — Assertion trong Page Object
public class LoginPage extends BasePage {
    public void login(String user, String pass) {
        sendKeys(USERNAME, user);
        sendKeys(PASSWORD, pass);
        click(LOGIN_BTN);
        Assert.assertTrue(isDisplayed(DASHBOARD)); // KHÔNG làm thế này
    }
}
```

---

### 2.2 Fluent Interface — Được dùng tại Netflix, Spotify

Method chaining giúp test code đọc như ngôn ngữ tự nhiên:

```java
// Không fluent
loginPage.enterUsername("user");
loginPage.enterPassword("pass");
loginPage.clickLogin();
inventoryPage.addFirstNProductsToCart(2);
inventoryPage.goToCart();

// Fluent — dễ đọc hơn nhiều
loginPage
    .enterUsername("user")
    .enterPassword("pass")
    .clickLogin()
    .addProductsToCart(2)
    .goToCart();
```

---

### 2.3 Screenplay Pattern — Xu hướng mới từ 2020+

Được dùng tại: Serenity BDD, các dự án enterprise lớn.

Thay vì "Page does action", Screenplay nói "Actor performs task":

```java
// Page Object Model
loginPage.login("user", "pass");

// Screenplay Pattern
actor.attemptsTo(
    Login.withCredentials("user", "pass")
);
```

**Ưu điểm:** Test scenarios gần với business language hơn, dễ đọc cho non-technical stakeholders.

---

### 2.4 Builder Pattern cho Test Data

Được dùng rộng rãi tại các ngân hàng (Citi, HSBC, Standard Chartered):

```java
User testUser = User.builder()
    .username("john.doe")
    .email("john@example.com")
    .role(Role.CUSTOMER)
    .accountType(AccountType.SAVINGS)
    .build();
```

---

## 3. Kinh nghiệm từ ngành Ngân hàng & Tài chính

### 3.1 Yêu cầu đặc thù

Ngân hàng và tổ chức tài chính (JPMorgan, Goldman Sachs, Citi, HSBC) có yêu cầu cao hơn về:

**Bảo mật:**
- Không hardcode credentials trong code
- Dùng Vault (HashiCorp) hoặc AWS Secrets Manager
- Mask sensitive data trong logs và reports

```java
// ❌ Sai — hardcode credentials
private static final String PASSWORD = "secret_sauce";

// ✅ Đúng — đọc từ secure store
String password = SecretsManager.getSecret("test.user.password");
```

**Audit Trail:**
- Mọi test action phải được log đầy đủ
- Report phải có timestamp, user, environment
- Lưu trữ test results ít nhất 90 ngày

**Compliance:**
- Test phải cover các regulatory requirements (PCI-DSS, SOX, GDPR)
- Regression suite phải chạy trước mỗi release
- Test evidence phải được sign-off bởi QA Lead

---

### 3.2 Framework Stack tại các ngân hàng lớn

**JPMorgan Chase (theo job descriptions và tech blogs):**
- Selenium + Java + TestNG
- RestAssured cho API testing
- Jenkins + Bamboo cho CI/CD
- Allure + JIRA Xray cho reporting

**Goldman Sachs:**
- Selenium + Python/Java
- Pytest hoặc TestNG
- Internal test management platform
- Docker + Kubernetes cho test infrastructure

**HSBC, Standard Chartered (Asia):**
- Selenium + Java + TestNG (phổ biến nhất)
- Cucumber BDD cho business-readable tests
- Jenkins pipeline
- Extent Reports (vì dễ share với stakeholders)

---

### 3.3 Parallel Execution tại Scale

Các công ty lớn chạy hàng nghìn tests song song:

```
Local machine:     4-8 threads
CI server:         16-32 threads
Selenium Grid:     100+ concurrent sessions
Cloud (BrowserStack/Sauce Labs): Unlimited
```

**Chiến lược phân chia:**
- Smoke tests: chạy mỗi commit (5-10 phút)
- Regression tests: chạy mỗi đêm (1-2 giờ)
- Full suite: chạy trước release (4-8 giờ)

---

## 4. Tools & Platforms phổ biến

### 4.1 Cloud Testing Platforms

| Platform | Ưu điểm | Nhược điểm |
|---|---|---|
| **BrowserStack** | 3000+ browser/OS combos, real devices | Đắt |
| **Sauce Labs** | Tốt cho enterprise, compliance | Đắt |
| **LambdaTest** | Rẻ hơn, đủ dùng | Ít features hơn |
| **AWS Device Farm** | Tích hợp AWS ecosystem | Phức tạp setup |

### 4.2 Test Management Tools

| Tool | Dùng tại | Tích hợp |
|---|---|---|
| **JIRA Xray** | Phổ biến nhất | JIRA, Allure |
| **TestRail** | Enterprise | Selenium, Allure |
| **Zephyr** | JIRA plugin | JIRA |
| **qTest** | Banking/Finance | JIRA, Jenkins |

### 4.3 Reporting

| Tool | Ưu điểm | Nhược điểm |
|---|---|---|
| **Allure** | Đẹp, interactive | Cần server để xem |
| **ExtentReports** | 1 file HTML, gửi email được | Ít interactive hơn |
| **ReportPortal** | AI-powered, enterprise | Phức tạp setup |

---

## 5. Anti-patterns cần tránh

### 5.1 Thread.sleep() — Tuyệt đối không dùng

```java
// ❌ Tuyệt đối không làm thế này
Thread.sleep(3000);

// ✅ Dùng explicit wait
wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
```

**Lý do:** `Thread.sleep` là nguyên nhân số 1 gây flaky tests và chạy chậm.

### 5.2 Implicit Wait + Explicit Wait cùng lúc

```java
// ❌ Sai — gây unpredictable behavior
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
```

**Lý do:** Selenium không đảm bảo behavior khi dùng cả hai.

### 5.3 XPath phức tạp

```java
// ❌ Fragile — vỡ khi UI thay đổi
By.xpath("//div[@class='container']/div[2]/ul/li[3]/a/span");

// ✅ Stable — dùng data-testid hoặc id
By.cssSelector("[data-testid='login-button']");
By.id("login-button");
```

### 5.4 Test phụ thuộc nhau

```java
// ❌ Sai — test 2 phụ thuộc test 1
@Test(dependsOnMethods = "testLogin")
public void testDashboard() { ... }

// ✅ Đúng — mỗi test độc lập, tự setup state
@Test
public void testDashboard() {
    loginPage.login(USERNAME, PASSWORD); // tự login
    // test dashboard
}
```

### 5.5 Hardcode test data

```java
// ❌ Sai
loginPage.login("standard_user", "secret_sauce");

// ✅ Đúng — đọc từ config hoặc data provider
loginPage.login(
    config.getString("test.user.standard"),
    config.getString("test.user.password")
);
```

---

## 6. Metrics đo lường chất lượng framework

Các công ty lớn đo lường framework quality bằng:

| Metric | Target | Mô tả |
|---|---|---|
| **Flakiness Rate** | < 2% | % tests fail không nhất quán |
| **Execution Time** | < 30 phút | Thời gian chạy full regression |
| **Maintenance Cost** | < 10% sprint | Thời gian fix broken tests |
| **Coverage** | > 80% | % critical paths được cover |
| **Pass Rate** | > 95% | % tests pass trên môi trường stable |

---

## 7. Tài liệu tham khảo

- [Selenium Official Documentation](https://www.selenium.dev/documentation/)
- [Google Testing Blog](https://testing.googleblog.com/)
- [Martin Fowler — Page Object](https://martinfowler.com/bliki/PageObject.html)
- [LambdaTest — Selenium Best Practices 2026](https://www.lambdatest.com/blog/selenium-best-practices-for-web-testing/)
- [BrowserStack — Selenium Guide](https://www.browserstack.com/selenium)
- [Serenity BDD — Screenplay Pattern](https://serenity-js.org/handbook/design/screenplay-pattern/)
- [ResearchGate — Best Practices for Scalable Test Automation with Selenium (2024)](https://www.researchgate.net/publication/383914679_Best_Practices_for_Scalable_Test_Automation_with_Selenium)
