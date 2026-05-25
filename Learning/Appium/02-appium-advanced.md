# Appium Advanced — Gestures, Hybrid App, Parallel, Cloud

---

## 1. Mobile Gestures

### Swipe

```java
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

public class GestureHelper {

    private AndroidDriver driver;

    public GestureHelper(AndroidDriver driver) {
        this.driver = driver;
    }

    // Swipe từ dưới lên (scroll up)
    public void swipeUp() {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.8);
        int endY   = (int) (size.height * 0.2);
        swipe(startX, startY, startX, endY);
    }

    // Swipe từ trên xuống (scroll down)
    public void swipeDown() {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.2);
        int endY   = (int) (size.height * 0.8);
        swipe(startX, startY, startX, endY);
    }

    // Swipe trái (next page)
    public void swipeLeft() {
        Dimension size = driver.manage().window().getSize();
        int startX = (int) (size.width * 0.8);
        int endX   = (int) (size.width * 0.2);
        int y      = size.height / 2;
        swipe(startX, y, endX, y);
    }

    // Core swipe method (Appium 2.x / W3C Actions)
    private void swipe(int startX, int startY, int endX, int endY) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600),
                PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(swipe));
    }

    // Scroll đến element
    public void scrollToElement(String text) {
        driver.findElement(AppiumBy.androidUIAutomator(
            "new UiScrollable(new UiSelector().scrollable(true))" +
            ".scrollIntoView(new UiSelector().text(\"" + text + "\"))"
        ));
    }

    // Long press
    public void longPress(By locator) {
        WebElement element = driver.findElement(locator);
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence longPress = new Sequence(finger, 1);
        longPress.addAction(finger.createPointerMove(Duration.ZERO,
                PointerInput.Origin.fromElement(element), 0, 0));
        longPress.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        longPress.addAction(finger.createPointerMove(Duration.ofMillis(2000),
                PointerInput.Origin.fromElement(element), 0, 0));
        longPress.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(List.of(longPress));
    }
}
```

---

## 2. Handle Alerts & Permissions

```java
// Handle Android Alert
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

// Accept alert
try {
    wait.until(ExpectedConditions.alertIsPresent());
    driver.switchTo().alert().accept();
} catch (TimeoutException e) {
    // Không có alert, tiếp tục
}

// Handle Permission Popup (Android)
// Cách 1: autoGrantPermissions = true trong capabilities (tự động)
options.setAutoGrantPermissions(true);

// Cách 2: Click nút Allow thủ công
try {
    WebElement allowBtn = wait.until(ExpectedConditions.elementToBeClickable(
        AppiumBy.id("com.android.permissioncontroller:id/permission_allow_button")
    ));
    allowBtn.click();
} catch (TimeoutException e) {
    // Không có permission popup
}

// Cách 3: Dùng UIAutomator2 (tìm bất kỳ nút Allow)
try {
    driver.findElement(AppiumBy.androidUIAutomator(
        "new UiSelector().textContains(\"Allow\")"
    )).click();
} catch (NoSuchElementException e) {
    // Không có popup
}
```

---

## 3. Hybrid App — Switch Context

```java
// Hybrid app có 2 context:
// NATIVE_APP  — native UI elements
// WEBVIEW_xxx — web content trong WebView

// Lấy tất cả context
Set<String> contexts = driver.getContextHandles();
System.out.println("Available contexts: " + contexts);
// Output: [NATIVE_APP, WEBVIEW_com.example.app]

// Switch sang WebView
for (String context : contexts) {
    if (context.contains("WEBVIEW")) {
        driver.context(context);
        break;
    }
}

// Bây giờ dùng Selenium locators như web
driver.findElement(By.id("web-element-id")).click();
driver.findElement(By.cssSelector(".web-button")).click();

// Switch về Native
driver.context("NATIVE_APP");

// Ví dụ thực tế
public class HybridPage extends BaseMobilePage {

    public void clickWebButton(String buttonText) {
        // Switch sang WebView
        switchToWebView();

        // Tương tác với web element
        driver.findElement(By.xpath("//button[text()='" + buttonText + "']")).click();

        // Switch về native
        switchToNative();
    }

    private void switchToWebView() {
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(d -> {
            Set<String> contexts = ((AndroidDriver) d).getContextHandles();
            return contexts.stream().anyMatch(c -> c.contains("WEBVIEW"));
        });
        Set<String> contexts = ((AndroidDriver) driver).getContextHandles();
        contexts.stream()
            .filter(c -> c.contains("WEBVIEW"))
            .findFirst()
            .ifPresent(c -> ((AndroidDriver) driver).context(c));
    }

    private void switchToNative() {
        ((AndroidDriver) driver).context("NATIVE_APP");
    }
}
```

---

## 4. Parallel Mobile Testing

### Nhiều emulator cùng lúc

```java
// DriverManager với ThreadLocal (giống Selenium)
public class MobileDriverManager {
    private static final ThreadLocal<AndroidDriver> driverLocal = new ThreadLocal<>();

    public static AndroidDriver getDriver() {
        return driverLocal.get();
    }

    public static void setDriver(AndroidDriver driver) {
        driverLocal.set(driver);
    }

    public static void removeDriver() {
        if (driverLocal.get() != null) {
            driverLocal.get().quit();
            driverLocal.remove();
        }
    }
}

// testng.xml cho parallel mobile
```

```xml
<suite name="Mobile Parallel" parallel="tests" thread-count="2">
    <test name="Android Pixel 7">
        <parameter name="deviceName" value="Pixel_7_API_34"/>
        <parameter name="udid" value="emulator-5554"/>
        <classes>
            <class name="com.example.tests.LoginTest"/>
        </classes>
    </test>
    <test name="Android Pixel 6">
        <parameter name="deviceName" value="Pixel_6_API_33"/>
        <parameter name="udid" value="emulator-5556"/>
        <classes>
            <class name="com.example.tests.LoginTest"/>
        </classes>
    </test>
</suite>
```

---

## 5. Cloud Testing — BrowserStack

```java
// Chạy test trên real device qua BrowserStack
public class BrowserStackDriver {

    public static AndroidDriver createDriver(String deviceName, String osVersion)
            throws MalformedURLException {

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName(deviceName);
        options.setPlatformVersion(osVersion);
        options.setApp("bs://your-app-hash"); // Upload app lên BrowserStack trước

        // BrowserStack specific
        HashMap<String, Object> bsOptions = new HashMap<>();
        bsOptions.put("userName", System.getenv("BROWSERSTACK_USERNAME"));
        bsOptions.put("accessKey", System.getenv("BROWSERSTACK_ACCESS_KEY"));
        bsOptions.put("projectName", "My App");
        bsOptions.put("buildName", "Build " + System.getenv("BUILD_NUMBER"));
        bsOptions.put("sessionName", "Login Test");
        options.setCapability("bstack:options", bsOptions);

        return new AndroidDriver(
            new URL("https://hub-cloud.browserstack.com/wd/hub"),
            options
        );
    }
}
```

---

## 6. Appium với TestNG — BaseTest

```java
public class MobileBaseTest {
    protected AndroidDriver driver;
    protected WebDriverWait wait;

    @Parameters({"deviceName", "udid"})
    @BeforeMethod
    public void setUp(@Optional("Pixel_7_API_34") String deviceName,
                      @Optional("emulator-5554") String udid)
            throws MalformedURLException {

        UiAutomator2Options options = new UiAutomator2Options();
        options.setDeviceName(deviceName);
        options.setUdid(udid);
        options.setAppPackage("com.example.myapp");
        options.setAppActivity(".MainActivity");
        options.setAutoGrantPermissions(true);
        options.setNoReset(false);

        driver = new AndroidDriver(new URL("http://localhost:4723"), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        MobileDriverManager.setDriver(driver);
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            // Chụp screenshot
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot on Failure", new ByteArrayInputStream(screenshot));
        }
        MobileDriverManager.removeDriver();
    }
}
```

---

## Câu hỏi phỏng vấn

**Q1: Làm thế nào để test Hybrid app trong Appium?**
```
Hybrid app có 2 context: NATIVE_APP và WEBVIEW_xxx

Quy trình:
1. driver.getContextHandles() → lấy danh sách context
2. driver.context("WEBVIEW_xxx") → switch sang WebView
3. Dùng Selenium locators (By.id, By.cssSelector) như web
4. driver.context("NATIVE_APP") → switch về native

Gợi nhớ: Hybrid = 2 thế giới, phải switch đúng context
```

**Q2: Làm thế nào để scroll đến element trong Appium?**
```
Cách 1: UIAutomator2 scrollIntoView (Android, đơn giản nhất)
driver.findElement(AppiumBy.androidUIAutomator(
    "new UiScrollable(new UiSelector().scrollable(true))" +
    ".scrollIntoView(new UiSelector().text(\"Target Text\"))"
));

Cách 2: W3C Actions (cross-platform)
Tính tọa độ start/end, dùng PointerInput để swipe

Gợi nhớ: UIAutomator2 scrollIntoView = cách nhanh nhất cho Android
```

**Q3: Real device vs Emulator — khi nào dùng cái nào?**
```
Emulator:
- Setup nhanh, không cần thiết bị vật lý
- Chạy được trên CI/CD
- Không test được: camera, GPS thật, performance thật
→ Dùng cho: development, CI/CD, regression

Real device:
- Test được tất cả hardware features
- Performance thật, battery, network thật
- Tốn kém, khó maintain
→ Dùng cho: final testing trước release, performance test

Cloud (BrowserStack/Sauce Labs):
- Real device không cần mua
- Nhiều device/OS version
- Tốn tiền theo giờ
→ Dùng cho: cross-device testing, CI/CD với real device
```

**Q4: Làm thế nào để handle permission popup tự động?**
```
Cách 1 (đơn giản nhất): autoGrantPermissions = true trong capabilities
→ Appium tự động grant tất cả permissions khi app khởi động

Cách 2: Click nút Allow trong @BeforeMethod
→ Dùng khi cần kiểm soát từng permission

Cách 3: Dùng adb command
driver.executeScript("mobile: shell", Map.of(
    "command", "pm grant com.example.app android.permission.CAMERA"
));

Gợi nhớ: autoGrantPermissions = true là cách đơn giản nhất
```

---

**Quay lại:** [README](./README.md)
