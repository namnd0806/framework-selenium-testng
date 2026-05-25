# Playwright Advanced — Fixtures, Network Mock, Parallel, CI/CD

---

## 1. Fixtures — Dependency Injection cho Test

Fixtures = cơ chế setup/teardown có thể tái sử dụng và compose.

```java
// Java không có fixtures như TypeScript
// Thay vào đó dùng TestNG @BeforeMethod + ThreadLocal

public class PlaywrightManager {
    private static final ThreadLocal<Playwright> playwrightLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageLocal = new ThreadLocal<>();

    public static void setup(String browserName, boolean headless) {
        Playwright playwright = Playwright.create();
        Browser browser;

        switch (browserName.toLowerCase()) {
            case "firefox":
                browser = playwright.firefox().launch(
                    new BrowserType.LaunchOptions().setHeadless(headless));
                break;
            case "webkit":
                browser = playwright.webkit().launch(
                    new BrowserType.LaunchOptions().setHeadless(headless));
                break;
            default:
                browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(headless));
        }

        BrowserContext context = browser.newContext(
            new Browser.NewContextOptions()
                .setBaseURL(System.getProperty("base.url", "https://staging.example.com"))
                .setViewportSize(1920, 1080)
        );
        Page page = context.newPage();

        playwrightLocal.set(playwright);
        browserLocal.set(browser);
        contextLocal.set(context);
        pageLocal.set(page);
    }

    public static Page getPage() { return pageLocal.get(); }

    public static void teardown() {
        if (contextLocal.get() != null) contextLocal.get().close();
        if (browserLocal.get() != null) browserLocal.get().close();
        if (playwrightLocal.get() != null) playwrightLocal.get().close();
        pageLocal.remove();
        contextLocal.remove();
        browserLocal.remove();
        playwrightLocal.remove();
    }
}

// BaseTest
public class BasePlaywrightTest {

    @Parameters({"browser", "headless"})
    @BeforeMethod
    public void setUp(
            @Optional("chromium") String browser,
            @Optional("true") String headless) {
        PlaywrightManager.setup(browser, Boolean.parseBoolean(headless));
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        if (result.getStatus() == ITestResult.FAILURE) {
            Page page = PlaywrightManager.getPage();
            byte[] screenshot = page.screenshot();
            Allure.addAttachment("Screenshot", new ByteArrayInputStream(screenshot));
        }
        PlaywrightManager.teardown();
    }

    protected Page getPage() {
        return PlaywrightManager.getPage();
    }
}
```

---

## 2. Saved Auth State — Login 1 lần, dùng nhiều test

```java
// Setup: login 1 lần, lưu state
public class AuthSetup {

    public static void saveAuthState(String email, String password) throws IOException {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch();
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // Login
            page.navigate("https://staging.example.com/login");
            page.getByLabel("Email").fill(email);
            page.getByLabel("Password").fill(password);
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
            page.waitForURL("**/dashboard");

            // Lưu cookies + localStorage
            context.storageState(new BrowserContext.StorageStateOptions()
                .setPath(Paths.get("src/test/resources/auth/user-state.json")));

            browser.close();
        }
    }
}

// Dùng trong test — không cần login lại
BrowserContext context = browser.newContext(
    new Browser.NewContextOptions()
        .setStorageStatePath(Paths.get("src/test/resources/auth/user-state.json"))
);
Page page = context.newPage();
page.navigate("/dashboard"); // Đã login sẵn
```

---

## 3. Network Interception — Mock API

```java
// Mock API response — không cần backend thật
page.route("**/api/users", route -> {
    route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("application/json")
        .setBody("[{\"id\":1,\"name\":\"John\",\"email\":\"john@test.com\"}]")
    );
});

// Mock với file JSON
page.route("**/api/products", route -> {
    route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setBodyPath(Paths.get("src/test/resources/mocks/products.json"))
    );
});

// Intercept và modify request
page.route("**/api/checkout", route -> {
    // Thêm header vào request
    Map<String, String> headers = new HashMap<>(route.request().headers());
    headers.put("X-Test-Mode", "true");
    route.continue_(new Route.ContinueOptions().setHeaders(headers));
});

// Simulate network error
page.route("**/api/payment", route -> {
    route.abort("failed");
});

// Simulate slow network
page.route("**/api/slow-endpoint", route -> {
    try { Thread.sleep(3000); } catch (InterruptedException e) {}
    route.continue_();
});

// Ví dụ thực tế — test error handling
@Test
public void testApiErrorHandling() {
    // Mock API trả về 500
    page.route("**/api/users", route ->
        route.fulfill(new Route.FulfillOptions()
            .setStatus(500)
            .setBody("{\"error\":\"Internal Server Error\"}")
        )
    );

    page.navigate("/users");

    // Verify app hiển thị error message
    assertThat(page.getByTestId("error-banner"))
        .hasText("Failed to load users. Please try again.");
}
```

---

## 4. Parallel Execution

```xml
<!-- testng.xml -->
<suite name="Playwright Parallel" parallel="methods" thread-count="4">
    <test name="Chrome Tests">
        <parameter name="browser" value="chromium"/>
        <parameter name="headless" value="true"/>
        <classes>
            <class name="com.example.tests.LoginTest"/>
            <class name="com.example.tests.CheckoutTest"/>
        </classes>
    </test>
</suite>
```

```xml
<!-- Cross-browser parallel -->
<suite name="Cross Browser" parallel="tests" thread-count="3">
    <test name="Chromium">
        <parameter name="browser" value="chromium"/>
        <classes><class name="com.example.tests.SmokeTest"/></classes>
    </test>
    <test name="Firefox">
        <parameter name="browser" value="firefox"/>
        <classes><class name="com.example.tests.SmokeTest"/></classes>
    </test>
    <test name="WebKit">
        <parameter name="browser" value="webkit"/>
        <classes><class name="com.example.tests.SmokeTest"/></classes>
    </test>
</suite>
```

---

## 5. Playwright trong CI/CD

```yaml
# .github/workflows/playwright.yml
name: Playwright Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Cache Maven
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Install Playwright browsers
        run: mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install --with-deps chromium"

      - name: Run Playwright tests
        run: mvn test -Dheadless=true -Dbrowser=chromium
        env:
          BASE_URL: ${{ secrets.STAGING_URL }}

      - name: Upload traces on failure
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: playwright-traces
          path: traces/
          retention-days: 7

      - name: Upload screenshots on failure
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: screenshots
          path: screenshots/
```

---

## 6. API Testing với Playwright

```java
// Playwright có built-in API testing — không cần REST Assured
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

public class ApiTest {

    private APIRequestContext apiContext;

    @BeforeClass
    public void setupApi() {
        Playwright playwright = Playwright.create();
        apiContext = playwright.request().newContext(
            new APIRequest.NewContextOptions()
                .setBaseURL("https://api.example.com")
                .addExtraHTTPHeaders(Map.of(
                    "Authorization", "Bearer " + getToken(),
                    "Content-Type", "application/json"
                ))
        );
    }

    @Test
    public void testGetUsers() {
        APIResponse response = apiContext.get("/users");

        assertEquals(200, response.status());
        assertTrue(response.ok());

        // Parse JSON
        JsonObject body = new Gson().fromJson(response.text(), JsonObject.class);
        assertTrue(body.has("data"));
    }

    @Test
    public void testCreateUser() {
        APIResponse response = apiContext.post("/users",
            RequestOptions.create().setData(Map.of(
                "name", "John Doe",
                "email", "john@test.com"
            ))
        );

        assertEquals(201, response.status());
    }
}
```

---

## Câu hỏi phỏng vấn

**Q1: Làm thế nào để mock API trong Playwright?**
```
Dùng page.route() để intercept và mock response:

page.route("**/api/users", route ->
    route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setBody("[{\"id\":1,\"name\":\"John\"}]")
    )
);

Ứng dụng:
- Test error handling (mock 500 response)
- Test offline mode (mock network failure)
- Test với data cố định (không phụ thuộc backend)
- Tăng tốc test (không cần gọi API thật)

Gợi nhớ: page.route() = chặn đường, trả về data giả
```

**Q2: Saved auth state giải quyết vấn đề gì?**
```
Vấn đề: Mỗi test phải login → chậm, tốn thời gian

Giải pháp: Login 1 lần, lưu cookies/localStorage vào file JSON
Các test sau load state từ file → đã login sẵn

Lợi ích:
- Tiết kiệm thời gian (không login lại)
- Test ổn định hơn (không phụ thuộc login flow)
- Có thể có nhiều state: admin-state.json, user-state.json

Gợi nhớ: Saved state = thẻ từ đã quẹt sẵn, vào thẳng không cần quẹt lại
```

**Q3: Playwright có thể test API không?**
```
Có — Playwright có built-in APIRequestContext

Ưu điểm so với REST Assured:
- Cùng 1 tool cho UI và API test
- Có thể kết hợp: tạo data qua API, test UI, verify qua API

Nhược điểm:
- Ít tính năng hơn REST Assured (không có JSONPath, Schema validation)
- REST Assured vẫn tốt hơn cho API testing thuần

Gợi nhớ: Playwright API = đủ dùng cho simple API test, REST Assured cho complex
```

---

**Quay lại:** [README](./README.md)
