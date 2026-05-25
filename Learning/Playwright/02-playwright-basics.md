# Playwright Basics — Setup, Locators, Actions, POM

---

## 1. Setup Maven (Java)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.microsoft.playwright</groupId>
    <artifactId>playwright</artifactId>
    <version>1.44.0</version>
    <scope>test</scope>
</dependency>
```

```bash
# Download browser binaries lần đầu
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
# Hoặc
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

---

## 2. Playwright Architecture — 3 lớp

```java
// Playwright → Browser → BrowserContext → Page
Playwright playwright = Playwright.create();
Browser browser = playwright.chromium().launch(
    new BrowserType.LaunchOptions().setHeadless(false)
);
BrowserContext context = browser.newContext(
    new Browser.NewContextOptions()
        .setViewportSize(1920, 1080)
        .setLocale("en-US")
);
Page page = context.newPage();

// BrowserContext = profile riêng biệt (cookies, storage)
// Mỗi test nên có context riêng → test isolation
```

---

## 3. Locators — Cách tìm element

```java
// Ưu tiên từ trên xuống (theo Playwright best practices)

// 1. getByRole — tốt nhất, theo accessibility
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
page.getByRole(AriaRole.TEXTBOX, new Page.GetByRoleOptions().setName("Email"));
page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Sign up"));
page.getByRole(AriaRole.CHECKBOX, new Page.GetByRoleOptions().setName("Remember me"));

// 2. getByLabel — tìm input theo label
page.getByLabel("Email address");
page.getByLabel("Password");

// 3. getByPlaceholder — tìm theo placeholder
page.getByPlaceholder("Enter your email");

// 4. getByText — tìm theo text content
page.getByText("Login");
page.getByText("Welcome, John", new Page.GetByTextOptions().setExact(true));

// 5. getByTestId — tìm theo data-testid (stable)
page.getByTestId("login-button");
page.getByTestId("email-input");

// 6. locator() — CSS/XPath (khi không có cách khác)
page.locator("#email");
page.locator(".btn-login");
page.locator("//button[@type='submit']");

// Chaining locators
page.locator(".login-form").getByLabel("Email");
page.locator("table").locator("tr").nth(2).locator("td").first();
```

---

## 4. Basic Actions

```java
// Click
page.click("#submit");
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();

// Fill (clear + type)
page.fill("#email", "test@example.com");
page.getByLabel("Email").fill("test@example.com");

// Type (từng ký tự, dùng khi cần trigger events)
page.type("#search", "iPhone");

// Press key
page.press("#search", "Enter");
page.keyboard().press("Tab");
page.keyboard().press("Control+A");

// Select dropdown
page.selectOption("#country", "Vietnam");
page.selectOption("#country", new SelectOption().setLabel("Vietnam"));

// Checkbox
page.check("#remember-me");
page.uncheck("#remember-me");

// Upload file
page.setInputFiles("#file-upload", Paths.get("test-data/sample.pdf"));

// Hover
page.hover(".menu-item");

// Double click
page.dblclick("#editable-cell");

// Right click
page.click("#element", new Page.ClickOptions().setButton(MouseButton.RIGHT));

// Navigate
page.navigate("https://example.com");
page.goBack();
page.goForward();
page.reload();

// Wait for navigation
page.waitForURL("**/dashboard");
page.waitForLoadState(LoadState.NETWORKIDLE);
```

---

## 5. Assertions — Tự retry

```java
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

// Page assertions
assertThat(page).hasTitle("Dashboard");
assertThat(page).hasTitle(Pattern.compile(".*Dashboard.*"));
assertThat(page).hasURL("https://example.com/dashboard");
assertThat(page).hasURL(Pattern.compile(".*/dashboard"));

// Locator assertions (tự retry đến timeout)
Locator emailInput = page.getByLabel("Email");
assertThat(emailInput).isVisible();
assertThat(emailInput).isEnabled();
assertThat(emailInput).isEditable();
assertThat(emailInput).hasValue("test@example.com");
assertThat(emailInput).isFocused();

Locator errorMsg = page.getByTestId("error-message");
assertThat(errorMsg).hasText("Invalid credentials");
assertThat(errorMsg).containsText("Invalid");
assertThat(errorMsg).isVisible();

Locator checkbox = page.getByLabel("Remember me");
assertThat(checkbox).isChecked();

Locator items = page.locator(".product-item");
assertThat(items).hasCount(10);

// Negative assertions
assertThat(page.locator(".loading-spinner")).not().isVisible();
assertThat(page.getByTestId("error")).not().isVisible();
```

---

## 6. Screenshots & Tracing

```java
// Screenshot
page.screenshot(new Page.ScreenshotOptions()
    .setPath(Paths.get("screenshots/test.png"))
    .setFullPage(true)
);

// Element screenshot
page.locator(".product-card").screenshot(
    new Locator.ScreenshotOptions().setPath(Paths.get("screenshots/card.png"))
);

// Video recording — cấu hình trong BrowserContext
BrowserContext context = browser.newContext(
    new Browser.NewContextOptions()
        .setRecordVideoDir(Paths.get("videos/"))
        .setRecordVideoSize(1280, 720)
);

// Trace — debug tool mạnh nhất của Playwright
context.tracing().start(new Tracing.StartOptions()
    .setScreenshots(true)
    .setSnapshots(true)
);
// ... chạy test ...
context.tracing().stop(new Tracing.StopOptions()
    .setPath(Paths.get("trace.zip"))
);
// Xem trace: npx playwright show-trace trace.zip
```

---

## 7. Page Object Model với Playwright

```java
// BasePage
public class BasePage {
    protected final Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    protected void click(Locator locator) {
        locator.click();
    }

    protected void fill(Locator locator, String text) {
        locator.fill(text);
    }

    protected String getText(Locator locator) {
        return locator.textContent();
    }

    protected void waitForVisible(Locator locator) {
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE));
    }
}

// LoginPage
public class LoginPage extends BasePage {

    // Locators — định nghĩa 1 lần, dùng nhiều lần
    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    public LoginPage(Page page) {
        super(page);
        this.emailInput   = page.getByLabel("Email");
        this.passwordInput = page.getByLabel("Password");
        this.loginButton  = page.getByRole(AriaRole.BUTTON,
            new Page.GetByRoleOptions().setName("Login"));
        this.errorMessage = page.getByTestId("error-message");
    }

    public LoginPage navigate() {
        page.navigate("/login");
        return this;
    }

    public LoginPage enterEmail(String email) {
        emailInput.fill(email);
        return this;
    }

    public LoginPage enterPassword(String password) {
        passwordInput.fill(password);
        return this;
    }

    public DashboardPage clickLogin() {
        loginButton.click();
        return new DashboardPage(page);
    }

    public DashboardPage loginAs(String email, String password) {
        return navigate()
            .enterEmail(email)
            .enterPassword(password)
            .clickLogin();
    }

    public String getErrorMessage() {
        return errorMessage.textContent();
    }

    public boolean isErrorVisible() {
        return errorMessage.isVisible();
    }
}

// Test với TestNG
public class LoginTest {
    private Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeClass
    public void setupBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @BeforeMethod
    public void setupContext() {
        context = browser.newContext(
            new Browser.NewContextOptions().setBaseURL("https://staging.example.com")
        );
        page = context.newPage();
    }

    @AfterMethod
    public void teardown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            page.screenshot(new Page.ScreenshotOptions()
                .setPath(Paths.get("screenshots/" + result.getName() + ".png")));
        }
        context.close();
    }

    @AfterClass
    public void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @Test
    public void testValidLogin() {
        DashboardPage dashboard = new LoginPage(page)
            .loginAs("test@example.com", "Pass@123");

        assertThat(page).hasURL(Pattern.compile(".*/dashboard"));
        assertThat(page.getByTestId("welcome-message"))
            .containsText("Welcome");
    }

    @Test
    public void testInvalidLogin() {
        LoginPage loginPage = new LoginPage(page).navigate();
        loginPage.enterEmail("wrong@example.com")
                 .enterPassword("wrongpass")
                 .clickLogin();

        // Playwright tự chờ error message xuất hiện
        assertThat(page.getByTestId("error-message"))
            .hasText("Invalid email or password");
    }
}
```

---

## Câu hỏi phỏng vấn

**Q1: Locator trong Playwright khác gì so với Selenium?**
```
Selenium: locator theo DOM structure (id, css, xpath)
Playwright: locator theo semantic (getByRole, getByLabel, getByText)

Playwright ưu tiên:
1. getByRole — theo accessibility role
2. getByLabel — theo form label
3. getByTestId — theo data-testid
4. locator() — CSS/XPath khi cần

Tại sao tốt hơn: ít phụ thuộc DOM structure → ít vỡ khi UI thay đổi

Gợi nhớ: Playwright locator = tìm theo ý nghĩa, không phải địa chỉ
```

**Q2: Playwright Trace Viewer dùng để làm gì?**
```
Trace = recording đầy đủ của test run:
- Screenshots từng bước
- Network requests/responses
- Console logs
- DOM snapshots

Dùng để debug test fail trong CI (không có màn hình):
1. Bật tracing trong test
2. Test fail → lưu trace.zip
3. npx playwright show-trace trace.zip
4. Xem lại từng bước như video

Gợi nhớ: Trace = hộp đen máy bay, xem lại khi có sự cố
```

**Q3: BrowserContext trong Playwright là gì?**
```
BrowserContext = profile riêng biệt trong browser
- Cookies, localStorage, sessionStorage riêng
- Không share state với context khác

Tại sao quan trọng:
- Mỗi test có context riêng → test isolation
- Không cần logout giữa các test
- Có thể tạo context với auth state sẵn (login 1 lần, dùng nhiều test)

Gợi nhớ: BrowserContext = tab ẩn danh riêng cho mỗi test
```

---

**Tiếp theo:** [03-playwright-advanced.md](./03-playwright-advanced.md) | [Quay lại README](./README.md)
