# Design Patterns — Mẫu thiết kế trong Automation Framework

> Design patterns = giải pháp đã được kiểm chứng cho các vấn đề thường gặp.
> Trong automation framework, 4 pattern dưới đây xuất hiện ở khắp nơi.

---

## 1. Singleton Pattern

### Vấn đề cần giải quyết

Một số thứ chỉ nên có **1 instance duy nhất** trong toàn bộ chương trình:
- Config reader — chỉ đọc file 1 lần
- Driver manager — quản lý driver tập trung
- Logger — ghi log từ 1 nơi

### Implement Singleton

```java
// Cách 1: Eager initialization (đơn giản nhất)
public class ConfigManager {
    // Tạo instance ngay khi class được load
    private static final ConfigManager INSTANCE = new ConfigManager();
    private Properties properties;

    private ConfigManager() { // private constructor — không ai new được
        properties = new Properties();
        try (InputStream is = getClass().getClassLoader()
                .getResourceAsStream("config.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được config", e);
        }
    }

    public static ConfigManager getInstance() {
        return INSTANCE; // luôn trả về cùng 1 instance
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String get(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}

// Dùng:
String baseUrl = ConfigManager.getInstance().get("base.url");
String browser = ConfigManager.getInstance().get("browser", "chrome");
```

```java
// Cách 2: Lazy initialization + thread-safe (Double-checked locking)
public class DriverManager {
    private static volatile DriverManager instance; // volatile quan trọng!
    private static final ThreadLocal<WebDriver> driverLocal = new ThreadLocal<>();

    private DriverManager() {}

    public static DriverManager getInstance() {
        if (instance == null) {
            synchronized (DriverManager.class) {
                if (instance == null) { // check lần 2 trong synchronized
                    instance = new DriverManager();
                }
            }
        }
        return instance;
    }

    public WebDriver getDriver() { return driverLocal.get(); }
    public void setDriver(WebDriver driver) { driverLocal.set(driver); }
    public void removeDriver() {
        if (driverLocal.get() != null) {
            driverLocal.get().quit();
            driverLocal.remove();
        }
    }
}
```

---

## 2. Factory Pattern

### Vấn đề cần giải quyết

Tạo object mà không cần biết class cụ thể — linh hoạt khi cần switch implementation.

**Ví dụ:** Tạo WebDriver theo browser type mà không cần if/else ở khắp nơi.

```java
// Factory Method
public class DriverFactory {
    public static WebDriver createDriver(String browserName) {
        switch (browserName.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--start-maximized");
                return new ChromeDriver(chromeOptions);

            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                return new FirefoxDriver(firefoxOptions);

            case "edge":
                return new EdgeDriver();

            case "chrome-headless":
                ChromeOptions headless = new ChromeOptions();
                headless.addArguments("--headless", "--no-sandbox");
                return new ChromeDriver(headless);

            default:
                throw new IllegalArgumentException("Browser không hỗ trợ: " + browserName);
        }
    }
}

// Dùng — không cần biết ChromeDriver hay FirefoxDriver
String browser = ConfigManager.getInstance().get("browser", "chrome");
WebDriver driver = DriverFactory.createDriver(browser);
DriverManager.getInstance().setDriver(driver);
```

```java
// Abstract Factory — tạo cả "gia đình" object liên quan
public interface PageFactory {
    LoginPage createLoginPage();
    DashboardPage createDashboardPage();
    CheckoutPage createCheckoutPage();
}

// Factory cho web
public class WebPageFactory implements PageFactory {
    private WebDriver driver;

    public WebPageFactory(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public LoginPage createLoginPage() {
        return new LoginPage(driver);
    }
    // ...
}

// Factory cho mobile (nếu cần)
public class MobilePageFactory implements PageFactory {
    private AppiumDriver driver;
    // ...
}
```

---

## 3. Builder Pattern

### Vấn đề cần giải quyết

Tạo object phức tạp với nhiều optional field — tránh constructor có quá nhiều tham số.

```java
// Vấn đề: constructor quá nhiều tham số
User user = new User("John", "Doe", "john@test.com", "Pass@123",
                     "Vietnam", "Ho Chi Minh", "0901234567",
                     true, "admin", null, null); // khó đọc, dễ nhầm thứ tự

// Builder Pattern — rõ ràng, linh hoạt
public class UserBuilder {
    // Required fields
    private final String email;
    private final String password;

    // Optional fields
    private String firstName = "";
    private String lastName = "";
    private String phone = "";
    private String country = "Vietnam";
    private String role = "user";

    public UserBuilder(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public UserBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this; // trả về this để chain
    }

    public UserBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public UserBuilder phone(String phone) {
        this.phone = phone;
        return this;
    }

    public UserBuilder country(String country) {
        this.country = country;
        return this;
    }

    public UserBuilder role(String role) {
        this.role = role;
        return this;
    }

    public User build() {
        // Validate trước khi tạo
        if (email == null || email.isEmpty()) {
            throw new IllegalStateException("Email không được để trống");
        }
        return new User(this);
    }
}

// Dùng — rõ ràng, dễ đọc
User adminUser = new UserBuilder("admin@test.com", "Admin@123")
    .firstName("Admin")
    .lastName("User")
    .role("admin")
    .country("Vietnam")
    .build();

User basicUser = new UserBuilder("user@test.com", "User@123")
    .firstName("Basic")
    .build(); // chỉ set những gì cần

// Ứng dụng thực tế — build test data
public class TestDataBuilder {
    public static UserBuilder validUser() {
        return new UserBuilder("valid@test.com", "Valid@123")
            .firstName("Test")
            .lastName("User");
    }

    public static UserBuilder adminUser() {
        return new UserBuilder("admin@test.com", "Admin@123")
            .role("admin");
    }
}

// Trong test:
User user = TestDataBuilder.validUser().country("USA").build();
```

---

## 4. Strategy Pattern

### Vấn đề cần giải quyết

Có nhiều cách thực hiện cùng 1 hành động — muốn switch dễ dàng mà không sửa code nhiều.

```java
// Ví dụ: nhiều cách chờ element
public interface WaitStrategy {
    WebElement waitFor(WebDriver driver, By locator);
}

// Strategy 1: Explicit wait
public class ExplicitWaitStrategy implements WaitStrategy {
    private final int timeoutSeconds;

    public ExplicitWaitStrategy(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public WebElement waitFor(WebDriver driver, By locator) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
            .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}

// Strategy 2: Fluent wait (retry với interval)
public class FluentWaitStrategy implements WaitStrategy {
    @Override
    public WebElement waitFor(WebDriver driver, By locator) {
        return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(30))
            .pollingEvery(Duration.ofMillis(500))
            .ignoring(NoSuchElementException.class)
            .until(d -> d.findElement(locator));
    }
}

// BasePage dùng Strategy
public class BasePage {
    private WaitStrategy waitStrategy;

    public BasePage(WebDriver driver, WaitStrategy waitStrategy) {
        this.driver = driver;
        this.waitStrategy = waitStrategy;
    }

    protected WebElement findElement(By locator) {
        return waitStrategy.waitFor(driver, locator);
    }
}

// Dùng — switch strategy dễ dàng
BasePage page = new LoginPage(driver, new ExplicitWaitStrategy(10));
BasePage slowPage = new CheckoutPage(driver, new FluentWaitStrategy());
```

---

## 5. Page Object Model — Pattern quan trọng nhất

POM không phải design pattern trong Gang of Four, nhưng là **pattern bắt buộc** trong automation.

```java
// Cấu trúc POM chuẩn
public class LoginPage extends BasePage {
    // 1. Locators — private, không ai truy cập trực tiếp
    private final By emailInput = By.id("email");
    private final By passwordInput = By.id("password");
    private final By loginButton = By.cssSelector(".btn-login");
    private final By errorMessage = By.cssSelector(".error-msg");
    private final By forgotPasswordLink = By.linkText("Forgot Password?");

    // 2. Constructor
    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // 3. Actions — public, test gọi vào đây
    public LoginPage enterEmail(String email) {
        type(emailInput, email);
        return this; // return this để chain
    }

    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public DashboardPage clickLogin() {
        click(loginButton);
        return new DashboardPage(driver); // trả về page tiếp theo
    }

    // Shortcut method
    public DashboardPage loginAs(String email, String password) {
        return enterEmail(email)
               .enterPassword(password)
               .clickLogin();
    }

    // 4. Getters — lấy thông tin từ page
    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    // 5. Navigation
    public ForgotPasswordPage clickForgotPassword() {
        click(forgotPasswordLink);
        return new ForgotPasswordPage(driver);
    }
}

// Test dùng POM — rõ ràng, dễ đọc
@Test
public void testLoginSuccess() {
    DashboardPage dashboard = new LoginPage(driver)
        .loginAs("test@gmail.com", "Pass@123");

    assertTrue(dashboard.isLoaded());
    assertEquals(dashboard.getWelcomeMessage(), "Welcome, Test User!");
}

@Test
public void testLoginFail() {
    LoginPage loginPage = new LoginPage(driver)
        .enterEmail("wrong@gmail.com")
        .enterPassword("wrongpass");

    loginPage.clickLoginExpectingError(); // method riêng khi expect fail

    assertTrue(loginPage.isErrorDisplayed());
    assertEquals(loginPage.getErrorMessage(), "Invalid credentials");
}
```

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: Singleton pattern là gì? Implement thread-safe thế nào?**

```
Singleton = chỉ có 1 instance của class trong toàn bộ chương trình.

Dùng khi: ConfigManager, DriverManager, Logger — chỉ cần 1 instance.

Thread-safe implementations:
1. Eager: private static final INSTANCE = new Singleton(); (đơn giản nhất)
2. Double-checked locking: dùng volatile + synchronized (lazy + thread-safe)
3. Enum Singleton: cách an toàn nhất, Java đảm bảo thread-safe

Gợi nhớ: "1 instance, private constructor, static getInstance()"
```

**Q2: Factory pattern dùng để làm gì trong automation?**

```
Factory = tạo object mà không cần biết class cụ thể.

Trong automation:
- DriverFactory.createDriver("chrome") → ChromeDriver
- DriverFactory.createDriver("firefox") → FirefoxDriver
- Test không cần biết ChromeDriver hay FirefoxDriver, chỉ cần WebDriver

Lợi ích:
- Thêm browser mới → chỉ sửa Factory, không sửa test
- Switch browser qua config, không sửa code
```

**Q3: Builder pattern khác gì với constructor nhiều tham số?**

```
Constructor nhiều tham số:
new User("John", null, "john@test.com", null, null, "admin")
→ Khó đọc, dễ nhầm thứ tự, không biết null là field nào

Builder:
new UserBuilder("john@test.com", "Pass@123")
    .firstName("John")
    .role("admin")
    .build()
→ Rõ ràng, chỉ set field cần thiết, dễ đọc

Dùng Builder khi: object có nhiều optional field (>3-4 field)
```

**Q4: Page Object Model là gì? Tại sao cần?**

```
POM = tách locator và action của page vào class riêng, test chỉ gọi method.

Tại sao cần:
1. Maintainability: locator thay đổi → chỉ sửa Page class, không sửa test
2. Reusability: nhiều test dùng chung LoginPage
3. Readability: test đọc như ngôn ngữ tự nhiên
4. Separation of concerns: test logic ≠ page interaction

Không có POM:
driver.findElement(By.id("email")).sendKeys("test@gmail.com"); // trong test
→ Thay đổi id → phải sửa tất cả test

Có POM:
loginPage.enterEmail("test@gmail.com"); // trong test
→ Thay đổi id → chỉ sửa LoginPage.emailInput
```

**Q5: Strategy pattern dùng khi nào trong automation?**

```
Strategy = nhiều cách thực hiện cùng 1 việc, switch dễ dàng.

Ví dụ trong automation:
- WaitStrategy: ExplicitWait vs FluentWait vs CustomWait
- BrowserStrategy: Chrome vs Firefox vs Edge
- ReportStrategy: Allure vs ExtentReports vs Console

Lợi ích: thêm strategy mới không sửa code cũ (Open/Closed Principle)
```

---

**Quay lại:** [README — Mục lục Java](./README.md)
