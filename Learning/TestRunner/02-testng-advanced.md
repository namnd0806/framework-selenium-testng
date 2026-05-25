# TestNG Advanced

## 1. @DataProvider

### Cơ bản
```java
public class LoginDataTest {

    // DataProvider trả về mảng 2 chiều Object[][]
    @DataProvider(name = "loginData")
    public Object[][] loginData() {
        return new Object[][] {
            {"admin@test.com",    "Admin@123",  true},   // valid
            {"user@test.com",     "User@123",   true},   // valid
            {"wrong@test.com",    "wrongpass",  false},  // invalid
            {"",                  "password",   false},  // empty email
        };
    }

    @Test(dataProvider = "loginData")
    public void testLogin(String email, String password, boolean expectedResult) {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(email, password);
        Assert.assertEquals(loginPage.isLoggedIn(), expectedResult,
            "Login result mismatch for: " + email);
    }
}
```

### DataProvider từ file CSV
```java
@DataProvider(name = "csvData")
public Object[][] readFromCSV() throws Exception {
    List<Object[]> data = new ArrayList<>();
    try (CSVReader reader = new CSVReader(new FileReader("src/test/resources/testdata/login.csv"))) {
        String[] line;
        reader.readNext(); // skip header
        while ((line = reader.readNext()) != null) {
            data.add(new Object[]{line[0], line[1], Boolean.parseBoolean(line[2])});
        }
    }
    return data.toArray(new Object[0][]);
}
```

### DataProvider Parallel
```java
// Chạy song song các data set
@DataProvider(name = "parallelData", parallel = true)
public Object[][] parallelData() {
    return new Object[][] {
        {"chrome",  "https://staging.example.com"},
        {"firefox", "https://staging.example.com"},
    };
}
```

---

## 2. Parallel Execution

```xml
<!-- testng.xml -->
<suite name="Suite" parallel="tests" thread-count="4">

    <!-- parallel="methods"    → mỗi @Test method chạy trên thread riêng -->
    <!-- parallel="classes"    → mỗi class chạy trên thread riêng -->
    <!-- parallel="tests"      → mỗi <test> tag chạy trên thread riêng -->
    <!-- parallel="instances"  → mỗi instance của class chạy trên thread riêng -->

    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes><class name="com.example.tests.LoginTest"/></classes>
    </test>

    <test name="Firefox Tests">
        <parameter name="browser" value="firefox"/>
        <classes><class name="com.example.tests.LoginTest"/></classes>
    </test>
</suite>
```

**ThreadLocal để tránh race condition:**
```java
public class DriverManager {
    // Mỗi thread có driver riêng → không conflict
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            driverThreadLocal.get().quit();
            driverThreadLocal.remove();
        }
    }
}

// Trong @BeforeMethod
@BeforeMethod
@Parameters("browser")
public void setup(String browser) {
    WebDriver driver = createDriver(browser);
    DriverManager.setDriver(driver);
}
```

---

## 3. @Factory

```java
// Factory tạo nhiều instance của test class với config khác nhau
public class CrossBrowserFactory {

    @Factory
    public Object[] createTests() {
        return new Object[] {
            new LoginTest("chrome"),
            new LoginTest("firefox"),
            new LoginTest("edge"),
        };
    }
}

public class LoginTest {
    private String browser;
    private WebDriver driver;

    public LoginTest(String browser) {
        this.browser = browser;
    }

    @BeforeMethod
    public void setup() {
        driver = DriverFactory.create(browser);
    }

    @Test
    public void testLogin() {
        // Test chạy trên mỗi browser
    }
}
```

---

## 4. Listeners

### ITestListener - Listener phổ biến nhất
```java
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("▶ Starting: " + result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("✅ PASSED: " + result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ FAILED: " + result.getName());
        // Chụp screenshot khi fail
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            String screenshotPath = takeScreenshot(result.getName());
            // Attach vào Allure report
            Allure.addAttachment("Screenshot", new FileInputStream(screenshotPath));
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("⏭ SKIPPED: " + result.getName());
    }

    private String takeScreenshot(String testName) {
        TakesScreenshot ts = (TakesScreenshot) DriverManager.getDriver();
        File src = ts.getScreenshotAs(OutputType.FILE);
        String path = "target/screenshots/" + testName + "_" + System.currentTimeMillis() + ".png";
        FileUtils.copyFile(src, new File(path));
        return path;
    }
}
```

### ISuiteListener
```java
public class SuiteListener implements ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        System.out.println("Suite started: " + suite.getName());
        // Gửi Slack notification: "Test suite bắt đầu chạy"
    }

    @Override
    public void onFinish(ISuite suite) {
        Map<String, ISuiteResult> results = suite.getResults();
        // Tổng hợp kết quả, gửi email/Slack report
    }
}
```

---

## 5. Retry Analyzer

```java
// Implement IRetryAnalyzer
public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount = 0;
    private static final int MAX_RETRY = 2; // Retry tối đa 2 lần

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            System.out.println("Retrying test: " + result.getName() + " (attempt " + retryCount + ")");
            return true;  // true = retry
        }
        return false; // false = không retry nữa
    }
}

// Cách 1: Gắn trực tiếp vào @Test
@Test(retryAnalyzer = RetryAnalyzer.class)
public void flakyTest() { }

// Cách 2: Dùng Listener để tự động áp dụng cho tất cả test
public class RetryListener implements IAnnotationTransformer {
    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        annotation.setRetryAnalyzer(RetryAnalyzer.class);
    }
}
```

```xml
<!-- testng.xml - đăng ký RetryListener -->
<listeners>
    <listener class-name="com.example.listeners.RetryListener"/>
</listeners>
```

---

## 6. Soft Assertions

```java
@Test
public void testUserProfile() {
    UserProfilePage page = new UserProfilePage(driver);

    // Hard Assert - dừng ngay khi fail
    // Assert.assertEquals(page.getName(), "John"); // Nếu fail → test dừng

    // Soft Assert - tiếp tục chạy, báo tất cả lỗi ở cuối
    SoftAssert softAssert = new SoftAssert();

    softAssert.assertEquals(page.getName(), "John Doe", "Name mismatch");
    softAssert.assertEquals(page.getEmail(), "john@test.com", "Email mismatch");
    softAssert.assertTrue(page.isAvatarDisplayed(), "Avatar not displayed");
    softAssert.assertEquals(page.getRole(), "Admin", "Role mismatch");

    // BẮT BUỘC phải gọi assertAll() - nếu không thì test luôn pass!
    softAssert.assertAll();
}
```

---

## 7. dependsOnMethods và dependsOnGroups

```java
@Test(groups = "auth")
public void testLogin() {
    // Login trước
}

@Test(dependsOnMethods = "testLogin")
public void testViewProfile() {
    // Chỉ chạy nếu testLogin PASS
    // Nếu testLogin fail → test này bị SKIP (không FAIL)
}

@Test(dependsOnGroups = "auth", alwaysRun = false)
public void testCheckout() {
    // Chạy sau khi tất cả test trong group "auth" pass
}
```

---

## 8. Câu hỏi phỏng vấn

**Q1: @DataProvider khác gì @Parameters?**
> **Trả lời:** @Parameters lấy giá trị từ testng.xml (static, ít linh hoạt). @DataProvider lấy data từ Java method (dynamic, có thể đọc từ file/DB, hỗ trợ nhiều bộ data).
>
> **Gợi nhớ:** Parameters = XML config, DataProvider = Java method → data-driven

**Q2: Tại sao cần ThreadLocal khi chạy parallel?**
> **Trả lời:** Khi nhiều thread cùng chạy, nếu dùng biến static thông thường thì các thread sẽ ghi đè lên nhau. ThreadLocal đảm bảo mỗi thread có bản sao riêng của WebDriver.
>
> **Gợi nhớ:** ThreadLocal = mỗi thread có hộp riêng, không ai lấy đồ của nhau

**Q3: Retry Analyzer giải quyết vấn đề gì?**
> **Trả lời:** Giải quyết flaky tests — test fail do timing issues, network instability, không phải do bug thực sự. Retry tự động giúp giảm false failures trong CI/CD.
>
> **Gợi nhớ:** Retry = lưới an toàn cho flaky test, không phải để che bug

**Q4: Tại sao SoftAssert phải gọi assertAll()?**
> **Trả lời:** SoftAssert chỉ thu thập failures, không throw exception ngay. assertAll() mới throw exception tổng hợp. Nếu quên gọi, test sẽ luôn pass dù có assertion fail.
>
> **Gợi nhớ:** SoftAssert = ghi nợ, assertAll() = đòi nợ — quên đòi thì mất tiền

---

[Tiếp theo: 03-assertions.md](./03-assertions.md) | [Quay lại README](./README.md)
