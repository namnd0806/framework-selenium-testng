# Framework Components

## BasePage

Lớp cha trừu tượng cho tất cả Page Objects. Đóng gói WebDriver interactions với Explicit Wait và StaleElement retry.

### Các method chính

| Method | Mô tả |
|---|---|
| `findElement(By)` | Tìm element với explicit wait, retry khi stale |
| `click(By)` | Click với `elementToBeClickable` wait |
| `sendKeys(By, String)` | Clear + type text |
| `getText(By)` | Lấy visible text |
| `isDisplayed(By)` | Kiểm tra element có hiển thị không |
| `navigateTo(String)` | Navigate đến URL + waitForPageLoad |
| `openByKey(String)` | Navigate đến URL từ config key |
| `waitForPageLoad()` | Chờ `document.readyState == "complete"` |
| `executeScript(String, Object...)` | Thực thi JavaScript |
| `scrollToElement(By)` | Scroll đến element |

### Cách dùng

```java
public class LoginPage extends BasePage {
    private static final By USERNAME = By.id("username");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Step("Mở trang Login")
    public LoginPage open() {
        openByKey("login"); // đọc page.url.login từ config
        return this;
    }

    @Step("Nhập username: {username}")
    public void enterUsername(String username) {
        sendKeys(USERNAME, username);
    }
}
```

---

## UIBaseTest

Lớp cha cho tất cả UI test cases. Quản lý WebDriver lifecycle.

### Lifecycle

```
@BeforeMethod setUpDriver()
    → ConfigManager.loadConfig(env)
    → WebDriverManager.initDriver(browser)
    → beforeTest() hook ← override tại đây

@Test method

@AfterMethod tearDownDriver()
    → afterTest() hook ← override tại đây
    → handleTestFailure() nếu fail
    → WebDriverManager.quitDriver()
```

### Override hooks

```java
public class MyTest extends UIBaseTest {

    @Override
    protected void beforeTest() {
        // Khởi tạo page objects, navigate đến URL
        loginPage = new LoginPage(driver);
        loginPage.open();
    }

    @Override
    protected void afterTest() {
        // Cleanup sau test: logout, reset state
    }
}
```

---

## ConfigManager

Singleton đọc cấu hình với priority chain:

```
System property (-Dkey=value)
    ↓
Environment variable (KEY=value)
    ↓
config-{env}.properties
    ↓
config.properties (default)
```

### Các tham số cấu hình

| Key | Default | Mô tả |
|---|---|---|
| `base.url` | `http://localhost:8080` | URL gốc của ứng dụng |
| `browser` | `chrome` | Browser: chrome/firefox/edge/safari |
| `headless` | `false` | Chạy không có GUI |
| `explicit.wait.timeout` | `10` | Timeout explicit wait (giây) |
| `thread.count` | `1` | Số luồng parallel |
| `screenshot.on.failure` | `true` | Chụp ảnh khi test fail |
| `retry.count` | `1` | Số lần retry khi fail |
| `env` | `dev` | Profile môi trường |
| `ci.mode` | `false` | Tự động headless trong CI |
| `page.url.{key}` | — | URL của từng trang |

---

## DriverFactory

Factory tạo WebDriver với Chrome prefs tối ưu:

- Disable password manager popup
- Disable extensions và infobars
- Hỗ trợ headless mode
- Auto-download driver binary qua WebDriverManager

### Browsers hỗ trợ

| Browser | Headless | Notes |
|---|---|---|
| Chrome | ✅ | `--headless=new` |
| Firefox | ✅ | `--headless` |
| Edge | ✅ | `--headless=new` |
| Safari | ❌ | macOS only |

---

## RetryAnalyzer

Tự động retry test thất bại do lỗi kỹ thuật (không retry AssertionError).

```java
// Áp dụng cho test method
@Test(retryAnalyzer = RetryAnalyzer.class)
public void myTest() { ... }
```

Cấu hình số lần retry:
```properties
retry.count=2
```

---

## Utility Classes

### WaitUtils
```java
WaitUtils wait = new WaitUtils(driver);
wait.waitForElementVisible(By.id("element"));
wait.waitForElementClickable(By.id("button"));
wait.waitForUrlContains("dashboard");
```

### AssertionUtils
```java
AssertionUtils.assertEquals(actual, expected, "context message");
AssertionUtils.assertTrue(condition, "context message");
AssertionUtils.assertContains(actual, expected, "context message");
```

### DataGenerator
```java
String name  = DataGenerator.generateFullName();
String email = DataGenerator.generateEmail();
String phone = DataGenerator.generatePhoneNumber();
```

---

## Data Providers

### Excel
```java
Object[][] data = ExcelDataProvider.getData("src/test/resources/testdata/users.xlsx");
```

### CSV
```java
Object[][] data = CsvDataProvider.getData("src/test/resources/testdata/users.csv");
```

### JSON
```java
Object[][] data = JsonDataProvider.getData("src/test/resources/testdata/users.json");
```

### Dùng với TestNG @DataProvider
```java
@DataProvider(name = "users")
public Object[][] userData() {
    return ExcelDataProvider.getData("src/test/resources/testdata/users.xlsx");
}

@Test(dataProvider = "users")
public void testLogin(String username, String password) {
    loginPage.login(username, password);
}
```
