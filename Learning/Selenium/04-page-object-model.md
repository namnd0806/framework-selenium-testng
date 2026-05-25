# Page Object Model (POM)

## 1. POM là gì? Tại sao cần?

**Vấn đề không có POM:**
```java
// ❌ Test code lẫn lộn với locator và logic
@Test
public void testLogin() {
    driver.findElement(By.id("email")).sendKeys("user@test.com");
    driver.findElement(By.id("password")).sendKeys("pass123");
    driver.findElement(By.cssSelector("button[type='submit']")).click();
    Assert.assertTrue(driver.findElement(By.id("dashboard")).isDisplayed());
}
// Nếu id="email" đổi thành id="username" → phải sửa TẤT CẢ test dùng locator này
```

**Lợi ích của POM:**
- **Tái sử dụng:** Locator và action định nghĩa 1 lần, dùng nhiều nơi
- **Dễ bảo trì:** Locator thay đổi → chỉ sửa 1 chỗ trong Page class
- **Dễ đọc:** Test code mô tả hành vi, không phải implementation
- **Separation of concerns:** Test logic tách khỏi UI interaction

---

## 2. Cấu trúc project chuẩn

```
src/
├── main/java/com/example/
│   ├── pages/
│   │   ├── BasePage.java
│   │   ├── LoginPage.java
│   │   ├── DashboardPage.java
│   │   ├── CheckoutPage.java
│   │   └── components/
│   │       ├── Header.java
│   │       ├── Footer.java
│   │       └── NavigationMenu.java
│   ├── utils/
│   │   ├── DriverManager.java
│   │   ├── ConfigReader.java
│   │   └── ScreenshotUtil.java
│   └── config/
│       └── ConfigReader.java
├── test/java/com/example/
│   ├── tests/
│   │   ├── BaseTest.java
│   │   ├── LoginTest.java
│   │   └── CheckoutTest.java
│   └── testdata/
│       └── TestDataProvider.java
└── test/resources/
    ├── config.properties
    ├── testng.xml
    └── testdata/
        └── users.json
```

---

## 3. BasePage - Nền tảng của mọi Page

```java
public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Common actions - tất cả page dùng chung
    protected void click(By locator) {
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
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    protected void waitForInvisibility(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void selectByText(By locator, String text) {
        new Select(driver.findElement(locator)).selectByVisibleText(text);
    }

    protected String getTitle() {
        return driver.getTitle();
    }

    protected String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
}
```

---

## 4. Page Class - Locators và Actions

```java
public class LoginPage extends BasePage {

    // Locators - private, chỉ dùng trong class này
    private final By emailInput    = By.id("email");
    private final By passwordInput = By.id("password");
    private final By loginButton   = By.cssSelector("button[type='submit']");
    private final By errorMessage  = By.cssSelector(".alert-danger");
    private final By forgotPassLink = By.linkText("Forgot Password?");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // Actions - public, trả về Page object để chaining
    public LoginPage enterEmail(String email) {
        type(emailInput, email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        type(passwordInput, password);
        return this;
    }

    public DashboardPage clickLogin() {
        click(loginButton);
        return new DashboardPage(driver); // Trả về trang tiếp theo
    }

    // Convenience method - login hoàn chỉnh
    public DashboardPage login(String email, String password) {
        return enterEmail(email)
               .enterPassword(password)
               .clickLogin();
    }

    // Login khi expect fail
    public LoginPage loginExpectingFailure(String email, String password) {
        type(emailInput, email);
        type(passwordInput, password);
        click(loginButton);
        return this; // Ở lại LoginPage vì login fail
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public ForgotPasswordPage clickForgotPassword() {
        click(forgotPassLink);
        return new ForgotPasswordPage(driver);
    }
}
```

---

## 5. PageFactory và @FindBy

```java
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPageWithFactory extends BasePage {

    // @FindBy - lazy initialization, element được tìm khi dùng
    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(id = "password")
    private WebElement passwordInput;

    @FindBy(css = "button[type='submit']")
    private WebElement loginButton;

    @FindBy(css = ".alert-danger")
    private WebElement errorMessage;

    // Tìm nhiều elements
    @FindBy(css = ".product-item")
    private List<WebElement> productItems;

    public LoginPageWithFactory(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this); // BẮT BUỘC
    }

    public DashboardPage login(String email, String password) {
        emailInput.clear();
        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        loginButton.click();
        return new DashboardPage(driver);
    }
}
```

**By.id() vs @FindBy - So sánh:**

| Tiêu chí | By.id() (By locator) | @FindBy (PageFactory) |
|----------|---------------------|----------------------|
| Khi tìm element | Mỗi lần gọi method | Lazy (khi access field) |
| StaleElement | Tự động re-find | Có thể bị stale |
| Explicit Wait | Dễ kết hợp | Khó kết hợp |
| Khuyến nghị | ✅ Nên dùng | ⚠️ Cẩn thận với dynamic page |

---

## 6. Page Chaining

```java
// Test code đọc như user story
@Test
public void testCompleteCheckout() {
    new LoginPage(driver)
        .login("user@test.com", "pass123")          // → DashboardPage
        .searchProduct("iPhone 15")                  // → SearchResultPage
        .selectFirstProduct()                        // → ProductPage
        .addToCart()                                 // → CartPage
        .proceedToCheckout()                         // → CheckoutPage
        .fillShippingAddress("123 Main St", "Hanoi") // → CheckoutPage
        .selectPaymentMethod("Credit Card")          // → CheckoutPage
        .placeOrder()                                // → OrderConfirmationPage
        .verifyOrderConfirmed();
}
```

---

## 7. Component Pattern

```java
// Reusable component - Header xuất hiện trên nhiều trang
public class Header extends BasePage {
    private final By cartIcon    = By.id("cart-icon");
    private final By cartCount   = By.cssSelector(".cart-count");
    private final By userMenu    = By.id("user-menu");
    private final By logoutBtn   = By.id("logout");

    public Header(WebDriver driver) {
        super(driver);
    }

    public CartPage goToCart() {
        click(cartIcon);
        return new CartPage(driver);
    }

    public int getCartItemCount() {
        return Integer.parseInt(getText(cartCount));
    }

    public LoginPage logout() {
        click(userMenu);
        click(logoutBtn);
        return new LoginPage(driver);
    }
}

// Dùng trong Page class
public class DashboardPage extends BasePage {
    private Header header;

    public DashboardPage(WebDriver driver) {
        super(driver);
        this.header = new Header(driver); // Compose component
    }

    public Header getHeader() {
        return header;
    }

    public boolean isLoaded() {
        return getCurrentUrl().contains("/dashboard");
    }
}

// Trong test
dashboardPage.getHeader().getCartItemCount(); // Dùng component
```

---

## 8. Câu hỏi phỏng vấn

**Q1: POM giải quyết vấn đề gì trong automation?**
> **Trả lời:** Giải quyết vấn đề bảo trì — khi UI thay đổi, chỉ cần sửa locator ở 1 chỗ trong Page class thay vì sửa tất cả test. Tách biệt test logic khỏi UI interaction.
>
> **Gợi nhớ:** POM = thay đổi 1 chỗ, không phải 100 chỗ

**Q2: Tại sao Page method nên trả về Page object?**
> **Trả lời:** Để hỗ trợ method chaining (fluent interface), giúp test code đọc như user story. Nếu action dẫn đến trang mới thì trả về trang mới, nếu ở lại trang cũ thì trả về this.
>
> **Gợi nhớ:** Return page = cho phép chain, test đọc như câu chuyện

**Q3: Khi nào dùng PageFactory, khi nào dùng By locator?**
> **Trả lời:** Nên dùng By locator vì dễ kết hợp với Explicit Wait và tránh StaleElementReferenceException. PageFactory có thể gây stale element trên dynamic pages vì element được cache.
>
> **Gợi nhớ:** By locator = an toàn hơn, PageFactory = tiện nhưng có rủi ro stale

**Q4: Component pattern trong POM là gì?**
> **Trả lời:** Tách các UI component tái sử dụng (Header, Footer, Navigation) thành class riêng. Các Page class compose các component này thay vì duplicate code.
>
> **Gợi nhớ:** Component = LEGO block, ghép vào page nào cũng được

---

[Tiếp theo: 05-advanced-selenium.md](./05-advanced-selenium.md) | [Quay lại README](./README.md)
