# TestNG Basics

## 1. TestNG là gì? Tại sao dùng thay JUnit?

TestNG (Test Next Generation) là testing framework cho Java, được thiết kế để khắc phục nhược điểm của JUnit.

| Tiêu chí | TestNG | JUnit 4/5 |
|----------|--------|-----------|
| Parallel execution | ✅ Built-in | ⚠️ Cần plugin |
| Data-driven (@DataProvider) | ✅ Native | ⚠️ @ParameterizedTest (JUnit 5) |
| Grouping tests | ✅ @Test(groups) | ❌ Không có |
| Dependencies giữa tests | ✅ dependsOnMethods | ❌ Không có |
| testng.xml config | ✅ Linh hoạt | ❌ Không có |
| Retry failed tests | ✅ IRetryAnalyzer | ❌ Cần extension |
| Soft assertions | ✅ SoftAssert | ❌ Cần thư viện ngoài |

**Gợi nhớ:** TestNG = JUnit + parallel + groups + retry + DataProvider

---

## 2. Annotations đầy đủ

```java
import org.testng.annotations.*;

public class AnnotationDemo {

    @BeforeSuite
    public void beforeSuite() {
        // Chạy 1 lần trước toàn bộ suite
        // Dùng để: khởi tạo DB connection, đọc config toàn cục
        System.out.println("=== BEFORE SUITE ===");
    }

    @AfterSuite
    public void afterSuite() {
        // Chạy 1 lần sau toàn bộ suite
        // Dùng để: đóng DB connection, cleanup toàn cục
        System.out.println("=== AFTER SUITE ===");
    }

    @BeforeTest
    public void beforeTest() {
        // Chạy trước mỗi <test> tag trong testng.xml
        System.out.println("--- Before Test ---");
    }

    @AfterTest
    public void afterTest() {
        System.out.println("--- After Test ---");
    }

    @BeforeClass
    public void beforeClass() {
        // Chạy 1 lần trước tất cả @Test trong class này
        // Dùng để: khởi tạo object dùng chung trong class
        System.out.println("Before Class");
    }

    @AfterClass
    public void afterClass() {
        System.out.println("After Class");
    }

    @BeforeMethod
    public void beforeMethod() {
        // Chạy trước MỖI @Test method
        // Dùng để: mở browser, navigate to URL
        System.out.println("Before Method");
    }

    @AfterMethod
    public void afterMethod() {
        // Chạy sau MỖI @Test method
        // Dùng để: đóng browser, chụp screenshot nếu fail
        System.out.println("After Method");
    }

    @BeforeGroups("smoke")
    public void beforeSmokeGroup() {
        // Chạy trước khi group "smoke" bắt đầu
        System.out.println("Before smoke group");
    }

    @AfterGroups("smoke")
    public void afterSmokeGroup() {
        System.out.println("After smoke group");
    }

    @Test
    public void testMethod() {
        System.out.println("Test running");
    }
}
```

---

## 3. Thứ tự chạy Annotation (Lifecycle)

```
BeforeSuite
  └── BeforeTest
        └── BeforeClass
              └── BeforeGroups (nếu có)
                    └── BeforeMethod
                          └── @Test
                    └── AfterMethod
              └── AfterGroups (nếu có)
        └── AfterClass
  └── AfterTest
AfterSuite
```

**Ví dụ thực tế với WebDriver:**

```java
public class LoginTest {
    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeClass
    public void setupClass() {
        // Chạy 1 lần - đọc config, setup test data
        ConfigReader.load("config.properties");
    }

    @BeforeMethod
    public void setupMethod() {
        // Mỗi test có browser riêng → test độc lập
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(ConfigReader.get("base.url"));
        loginPage = new LoginPage(driver);
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        // Chụp screenshot nếu test fail
        if (result.getStatus() == ITestResult.FAILURE) {
            takeScreenshot(result.getName());
        }
        if (driver != null) driver.quit();
    }

    @Test
    public void testValidLogin() {
        loginPage.login("user@test.com", "password123");
        Assert.assertTrue(new DashboardPage(driver).isLoaded());
    }
}
```

---

## 4. @Test Attributes

```java
@Test(
    description = "Verify user can login with valid credentials",
    groups = {"smoke", "regression"},
    priority = 1,              // Số nhỏ chạy trước (default = 0)
    enabled = true,            // false = skip test này
    timeOut = 5000,            // ms - fail nếu chạy quá 5 giây
    expectedExceptions = {IllegalArgumentException.class},
    expectedExceptionsMessageRegExp = ".*invalid.*"
)
public void testLogin() {
    // Test sẽ PASS nếu throw IllegalArgumentException với message chứa "invalid"
}

// Priority example - thứ tự chạy: setup(1) → login(2) → checkout(3)
@Test(priority = 1, description = "Setup test data")
public void setupData() { }

@Test(priority = 2, description = "Login to application")
public void login() { }

@Test(priority = 3, description = "Complete checkout", dependsOnMethods = "login")
public void checkout() { }
```

---

## 5. testng.xml - Cấu trúc đầy đủ

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">

<suite name="Automation Suite" verbose="1" thread-count="3">

    <!-- Parameters dùng chung cho toàn suite -->
    <parameter name="browser" value="chrome"/>
    <parameter name="env" value="staging"/>

    <!-- Listeners áp dụng cho toàn suite -->
    <listeners>
        <listener class-name="com.example.listeners.TestListener"/>
        <listener class-name="com.example.listeners.RetryListener"/>
    </listeners>

    <!-- Test 1: Smoke tests -->
    <test name="Smoke Tests" parallel="methods" thread-count="2">
        <groups>
            <run>
                <include name="smoke"/>
                <exclude name="wip"/>
            </run>
        </groups>
        <classes>
            <class name="com.example.tests.LoginTest"/>
            <class name="com.example.tests.HomePageTest">
                <!-- Chỉ chạy method cụ thể -->
                <methods>
                    <include name="testPageTitle"/>
                    <include name="testNavigation"/>
                </methods>
            </class>
        </classes>
    </test>

    <!-- Test 2: Regression tests -->
    <test name="Regression Tests">
        <packages>
            <!-- Chạy tất cả test trong package -->
            <package name="com.example.tests.regression"/>
        </packages>
    </test>

</suite>
```

---

## 6. Groups - Định nghĩa và sử dụng

```java
// Định nghĩa groups trong test class
public class ProductTest {

    @Test(groups = {"smoke", "regression"})
    public void testProductList() { }

    @Test(groups = {"regression"})
    public void testProductFilter() { }

    @Test(groups = {"smoke"})
    public void testAddToCart() { }

    @Test(groups = {"wip"})  // work in progress - exclude khỏi CI
    public void testNewFeature() { }
}
```

**Chạy group cụ thể qua Maven:**
```bash
mvn test -Dgroups="smoke"
mvn test -Dgroups="smoke,regression"
mvn test -DexcludedGroups="wip"
```

---

## 7. Câu hỏi phỏng vấn

**Q1: Sự khác nhau giữa @BeforeMethod và @BeforeClass?**
> **Trả lời:** @BeforeClass chạy 1 lần trước tất cả test trong class (dùng cho setup tốn thời gian như đọc config). @BeforeMethod chạy trước MỖI test (dùng cho setup cần reset state như mở browser mới).
>
> **Gợi nhớ:** Class = 1 lần, Method = mỗi lần

**Q2: Tại sao dùng TestNG thay JUnit trong Selenium?**
> **Trả lời:** TestNG có built-in parallel execution, grouping, DataProvider, retry mechanism và testng.xml để cấu hình linh hoạt — những thứ JUnit không có hoặc cần plugin thêm.
>
> **Gợi nhớ:** TestNG = JUnit + parallel + groups + retry

**Q3: priority trong @Test hoạt động thế nào?**
> **Trả lời:** Số nhỏ hơn chạy trước. Default là 0. Nếu cùng priority thì chạy theo alphabet. Tuy nhiên nên dùng dependsOnMethods thay priority để thể hiện dependency rõ ràng hơn.
>
> **Gợi nhớ:** priority nhỏ = chạy trước, nhưng prefer dependsOnMethods

**Q4: testng.xml dùng để làm gì?**
> **Trả lời:** Cấu hình suite: chọn test nào chạy, parallel hay không, thread count, groups include/exclude, parameters, listeners. Không cần sửa code để thay đổi cách chạy test.
>
> **Gợi nhớ:** testng.xml = bộ điều khiển từ xa cho test suite

---

[Tiếp theo: 02-testng-advanced.md](./02-testng-advanced.md) | [Quay lại README](./README.md)
