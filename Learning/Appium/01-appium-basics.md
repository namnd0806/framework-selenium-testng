# Appium Basics — Nền tảng Mobile Testing

---

## 1. Mobile App Types — Phân biệt 3 loại

```
Native App          Hybrid App              Mobile Web
─────────────       ─────────────────       ─────────────
Viết bằng           Viết bằng HTML/JS       Website chạy
Java/Kotlin         bọc trong native        trên mobile
(Android) hoặc      shell (WebView)         browser
Swift/ObjC (iOS)
─────────────       ─────────────────       ─────────────
Nhanh nhất          Trung bình              Chậm nhất
UX tốt nhất         Code share web/mobile   Không cần cài
Khó test nhất       Phức tạp nhất để test   Dễ test nhất
                    (switch context)        (dùng Selenium)
─────────────       ─────────────────       ─────────────
Ví dụ:              Ví dụ:                  Ví dụ:
Zalo, Grab          Ionic apps              m.facebook.com
```

**Gợi nhớ:** Native = thuần, Hybrid = lai, Mobile Web = web thu nhỏ

---

## 2. Appium Architecture

```
Test Code (Java)
    ↓ Appium Java Client
Appium Server (Node.js, port 4723)
    ↓ W3C WebDriver Protocol
UIAutomator2 (Android) / XCUITest (iOS)
    ↓
Device / Emulator / Simulator
```

**Các thành phần:**
- **Appium Server** — trung gian nhận lệnh từ test, gửi đến device
- **UIAutomator2** — driver cho Android (Google)
- **XCUITest** — driver cho iOS (Apple)
- **Appium Inspector** — tool tìm locator trên device

---

## 3. Setup môi trường Android

```bash
# 1. Cài Java JDK 17+
# 2. Cài Android Studio → Android SDK
# 3. Set environment variables
ANDROID_HOME=C:\Users\<user>\AppData\Local\Android\Sdk
PATH += %ANDROID_HOME%\platform-tools
PATH += %ANDROID_HOME%\tools

# 4. Cài Node.js
# 5. Cài Appium Server
npm install -g appium

# 6. Cài UIAutomator2 driver
appium driver install uiautomator2

# 7. Kiểm tra setup
appium doctor --android

# 8. Khởi động Appium Server
appium server --port 4723
```

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.appium</groupId>
    <artifactId>java-client</artifactId>
    <version>9.2.3</version>
</dependency>
```

---

## 4. Desired Capabilities / Appium Options

```java
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

public class DriverSetup {

    public static AndroidDriver createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        // Device
        options.setDeviceName("Pixel_7_API_34");     // Emulator name
        // options.setUdid("emulator-5554");          // Real device UDID

        // App
        options.setApp(System.getProperty("user.dir") + "/apps/myapp.apk");
        // Hoặc nếu app đã cài:
        // options.setAppPackage("com.example.myapp");
        // options.setAppActivity("com.example.myapp.MainActivity");

        // Behavior
        options.setAutoGrantPermissions(true);  // Tự động grant permissions
        options.setNoReset(false);              // Reset app state trước mỗi test
        options.setNewCommandTimeout(Duration.ofSeconds(60));

        return new AndroidDriver(
            new URL("http://localhost:4723"),
            options
        );
    }
}
```

---

## 5. Mobile Locators — Tìm element

### Appium Inspector — Tool tìm locator

```
1. Mở Appium Inspector
2. Kết nối với device/emulator
3. Click vào element trên màn hình
4. Xem locator suggestions ở panel bên phải
```

### Các loại locator (ưu tiên từ trên xuống)

```java
// 1. Accessibility ID (tốt nhất — stable, cross-platform)
driver.findElement(AppiumBy.accessibilityId("login-button"));
// Android: content-desc attribute
// iOS: accessibilityIdentifier

// 2. ID (Android: resource-id)
driver.findElement(AppiumBy.id("com.example.app:id/btn_login"));

// 3. XPath (dùng khi không có cách khác)
driver.findElement(AppiumBy.xpath("//android.widget.Button[@text='Login']"));

// 4. UIAutomator2 (Android only — mạnh nhất)
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiSelector().text(\"Login\").className(\"android.widget.Button\")"
));

// 5. Class Name
driver.findElement(AppiumBy.className("android.widget.EditText"));

// findElements — trả về list
List<MobileElement> items = driver.findElements(AppiumBy.className("android.widget.TextView"));
```

---

## 6. Basic Actions

```java
// Tap (click)
driver.findElement(AppiumBy.accessibilityId("login-button")).click();

// Type text
WebElement emailField = driver.findElement(AppiumBy.id("com.example:id/email"));
emailField.clear();
emailField.sendKeys("test@example.com");

// Get text
String text = driver.findElement(AppiumBy.id("com.example:id/title")).getText();

// Check visibility
boolean visible = driver.findElement(AppiumBy.id("com.example:id/btn")).isDisplayed();

// Hide keyboard
driver.hideKeyboard();

// Back button
driver.navigate().back();

// Get current activity (Android)
String activity = ((AndroidDriver) driver).currentActivity();
```

---

## 7. Waits — Giống Selenium

```java
// Explicit wait
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement element = wait.until(
    ExpectedConditions.visibilityOfElementLocated(
        AppiumBy.accessibilityId("dashboard")
    )
);

// Fluent wait
Wait<AndroidDriver> fluentWait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))
    .pollingEvery(Duration.ofMillis(500))
    .ignoring(NoSuchElementException.class);

WebElement el = fluentWait.until(d ->
    d.findElement(AppiumBy.id("com.example:id/result"))
);
```

---

## 8. Page Object Model cho Mobile

```java
// BaseMobilePage
public abstract class BaseMobilePage {
    protected AndroidDriver driver;
    protected WebDriverWait wait;

    public BaseMobilePage(AndroidDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected void tap(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        el.clear();
        el.sendKeys(text);
    }

    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }

    protected boolean isDisplayed(By locator) {
        return !driver.findElements(locator).isEmpty();
    }
}

// LoginScreen
public class LoginScreen extends BaseMobilePage {
    private final By emailField = AppiumBy.id("com.example:id/email");
    private final By passwordField = AppiumBy.id("com.example:id/password");
    private final By loginButton = AppiumBy.accessibilityId("login-button");
    private final By errorMessage = AppiumBy.id("com.example:id/error");

    public LoginScreen(AndroidDriver driver) {
        super(driver);
    }

    public HomeScreen login(String email, String password) {
        type(emailField, email);
        type(passwordField, password);
        driver.hideKeyboard();
        tap(loginButton);
        return new HomeScreen(driver);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }
}

// Test
public class LoginTest {
    private AndroidDriver driver;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        driver = DriverSetup.createAndroidDriver();
    }

    @AfterMethod
    public void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testValidLogin() {
        HomeScreen home = new LoginScreen(driver)
            .login("test@example.com", "Pass@123");
        Assert.assertTrue(home.isLoaded(), "Home screen should be displayed");
    }
}
```

---

## Câu hỏi phỏng vấn

**Q1: Native app vs Hybrid app vs Mobile Web — khác nhau thế nào?**
```
Native: viết bằng Java/Kotlin (Android) hoặc Swift (iOS) — nhanh, UX tốt
Hybrid: HTML/JS bọc trong native shell (WebView) — code share, phức tạp khi test
Mobile Web: website trên mobile browser — dễ test nhất (dùng Selenium)

Gợi nhớ: Native = thuần chủng, Hybrid = lai, Mobile Web = web thu nhỏ
```

**Q2: Appium architecture hoạt động thế nào?**
```
Test code → Appium Java Client → Appium Server (port 4723)
→ UIAutomator2/XCUITest → Device/Emulator

Appium Server là trung gian, dịch WebDriver commands sang
lệnh native của Android/iOS.

Gợi nhớ: Appium Server = phiên dịch viên giữa test code và device
```

**Q3: Accessibility ID là gì? Tại sao ưu tiên dùng?**
```
Accessibility ID = content-desc (Android) hoặc accessibilityIdentifier (iOS)
Attribute dành cho accessibility features (screen reader)

Tại sao ưu tiên:
1. Stable — dev ít thay đổi accessibility attributes
2. Cross-platform — cùng locator dùng được cho Android và iOS
3. Semantic — có ý nghĩa rõ ràng

Gợi nhớ: Accessibility ID = data-testid của mobile
```

**Q4: Làm thế nào để tìm locator trên mobile app?**
```
Dùng Appium Inspector:
1. Khởi động Appium Server
2. Mở Appium Inspector, kết nối với device
3. Click vào element trên màn hình
4. Xem locator suggestions (Accessibility ID, ID, XPath)
5. Test locator ngay trong Inspector

Ngoài ra: Android Studio Layout Inspector, uiautomatorviewer
```

---

**Tiếp theo:** [02-appium-advanced.md](./02-appium-advanced.md) | [Quay lại README](./README.md)
