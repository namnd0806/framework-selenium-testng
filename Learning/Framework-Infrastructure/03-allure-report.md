# Allure Report

## 1. Allure là gì? Tại sao dùng?

| Tiêu chí | Allure | ExtentReports |
|----------|--------|---------------|
| Giao diện | ✅ Đẹp, interactive | ⚠️ Cơ bản hơn |
| Tích hợp CI/CD | ✅ Jenkins, GitHub Actions | ⚠️ Cần cấu hình thêm |
| Trend/History | ✅ Có | ❌ Không |
| Categories | ✅ Phân loại failures | ❌ Không |
| Annotations | ✅ Phong phú | ⚠️ Ít hơn |
| Open source | ✅ | ✅ |

---

## 2. Setup

```xml
<!-- pom.xml -->
<properties>
    <allure.version>2.25.0</allure.version>
    <aspectj.version>1.9.21</aspectj.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.qameta.allure</groupId>
        <artifactId>allure-testng</artifactId>
        <version>${allure.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
            <configuration>
                <!-- AspectJ weaver BẮT BUỘC cho @Step annotation -->
                <argLine>
                    -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar"
                </argLine>
            </configuration>
        </plugin>
        <!-- Allure Maven Plugin -->
        <plugin>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-maven</artifactId>
            <version>2.12.0</version>
        </plugin>
    </plugins>
</build>
```

```properties
# src/test/resources/allure.properties
allure.results.directory=target/allure-results
```

---

## 3. Annotations

```java
import io.qameta.allure.*;

@Feature("User Authentication")          // Nhóm theo feature
@Story("Login")                          // User story
@Description("Verify login with valid credentials")
@Severity(SeverityLevel.CRITICAL)        // BLOCKER, CRITICAL, NORMAL, MINOR, TRIVIAL
@Link(name = "JIRA", url = "https://jira.example.com/PROJ-123")
@Issue("PROJ-123")                       // Link đến issue tracker
@TmsLink("TC-456")                       // Link đến test management
@Owner("john.doe")
public class LoginTest {

    @Test
    @DisplayName("Login with valid email and password")
    public void testValidLogin() {
        // test code
    }
}
```

---

## 4. @Step - Nested Steps

```java
// @Step tự động log vào Allure report
@Step("Login with email {email}")
public void login(String email, String password) {
    enterEmail(email);
    enterPassword(password);
    clickLoginButton();
}

@Step("Enter email: {email}")
private void enterEmail(String email) {
    driver.findElement(By.id("email")).sendKeys(email);
}

@Step("Click Login button")
private void clickLoginButton() {
    driver.findElement(By.id("login-btn")).click();
}

// Lambda steps (không cần annotation)
@Test
public void testLogin() {
    Allure.step("Open login page", () -> {
        driver.get(baseUrl + "/login");
    });

    Allure.step("Enter credentials", () -> {
        driver.findElement(By.id("email")).sendKeys("user@test.com");
        driver.findElement(By.id("password")).sendKeys("pass123");
    });

    Allure.step("Submit form", () -> {
        driver.findElement(By.id("submit")).click();
    });
}
```

---

## 5. Attachments

```java
// Screenshot
@Attachment(value = "Page Screenshot", type = "image/png")
public byte[] takeScreenshot() {
    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
}

// Text
@Attachment(value = "Page Source", type = "text/html")
public String getPageSource() {
    return driver.getPageSource();
}

// Attach trong test
Allure.addAttachment("Request Body", "application/json", requestBody, ".json");
Allure.addAttachment("Screenshot", new ByteArrayInputStream(screenshotBytes));

// Trong TestNG Listener
@Override
public void onTestFailure(ITestResult result) {
    byte[] screenshot = ((TakesScreenshot) DriverManager.getDriver())
        .getScreenshotAs(OutputType.BYTES);
    Allure.getLifecycle().addAttachment(
        "Screenshot on failure", "image/png", "png", screenshot
    );
}
```

---

## 6. Allure với TestNG

```java
// Cách 1: Dùng AllureTestNg listener trong testng.xml
// (Allure tự động đăng ký khi có allure-testng dependency)

// Cách 2: Explicit trong testng.xml
// <listeners>
//     <listener class-name="io.qameta.allure.testng.AllureTestNg"/>
// </listeners>

// Ví dụ test đầy đủ với Allure
@Feature("Shopping Cart")
@Story("Add to Cart")
public class CartTest {

    @Test
    @Description("Verify item is added to cart successfully")
    @Severity(SeverityLevel.CRITICAL)
    public void testAddToCart() {
        Allure.step("Navigate to product page", () ->
            driver.get(baseUrl + "/products/iphone-15")
        );

        Allure.step("Click Add to Cart", () ->
            driver.findElement(By.id("add-to-cart")).click()
        );

        Allure.step("Verify cart count is 1", () -> {
            String count = driver.findElement(By.id("cart-count")).getText();
            assertThat(count).isEqualTo("1");
        });
    }
}
```

---

## 7. Generate và xem Report

```bash
# Chạy test (tạo allure-results/)
mvn test

# Serve report (mở browser tự động)
allure serve target/allure-results

# Generate static report
allure generate target/allure-results -o target/allure-report --clean

# Mở report đã generate
allure open target/allure-report
```

---

## 8. Allure trong GitHub Actions

```yaml
# .github/workflows/test.yml
- name: Run Tests
  run: mvn test

- name: Upload Allure Results
  uses: actions/upload-artifact@v4
  if: always()  # Upload dù pass hay fail
  with:
    name: allure-results
    path: target/allure-results

- name: Get Allure History
  uses: actions/checkout@v4
  if: always()
  with:
    ref: gh-pages
    path: gh-pages

- name: Generate Allure Report
  uses: simple-elf/allure-report-action@master
  if: always()
  with:
    allure_results: target/allure-results
    allure_history: allure-history

- name: Deploy to GitHub Pages
  uses: peaceiris/actions-gh-pages@v3
  if: always()
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_branch: gh-pages
    publish_dir: allure-history
```

---

## 9. Câu hỏi phỏng vấn

**Q1: Tại sao cần AspectJ weaver khi dùng Allure?**
> **Trả lời:** @Step annotation dùng AOP (Aspect Oriented Programming) để tự động intercept method calls và log vào Allure. AspectJ weaver là agent thực hiện AOP tại runtime. Thiếu nó thì @Step không hoạt động.
>
> **Gợi nhớ:** AspectJ = người ghi chép tự động, @Step = đánh dấu "ghi chép chỗ này"

**Q2: Sự khác nhau giữa @Feature, @Story, @Step?**
> **Trả lời:** @Feature = nhóm lớn (tính năng). @Story = user story trong feature. @Step = bước thực hiện trong test. Tạo ra hierarchy trong Allure: Feature → Story → Test → Steps.
>
> **Gợi nhớ:** Feature > Story > Test > Step (từ lớn đến nhỏ)

**Q3: Làm thế nào để attach screenshot khi test fail?**
> **Trả lời:** Implement ITestListener, trong onTestFailure() chụp screenshot bằng TakesScreenshot và attach vào Allure bằng Allure.getLifecycle().addAttachment() hoặc dùng @Attachment annotation.
>
> **Gợi nhớ:** onTestFailure → chụp ảnh → attach vào Allure = bằng chứng khi fail

**Q4: allure serve vs allure generate khác nhau thế nào?**
> **Trả lời:** allure serve tạo report tạm thời và mở browser ngay (dùng khi debug local). allure generate tạo static HTML files (dùng để deploy lên server, GitHub Pages, CI artifacts).
>
> **Gợi nhớ:** serve = xem ngay tại chỗ, generate = tạo file để chia sẻ

---

[Tiếp theo: 04-test-data-management.md](./04-test-data-management.md) | [Quay lại README](./README.md)
