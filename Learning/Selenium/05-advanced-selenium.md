# Advanced Selenium

## 1. Selenium Grid

### Architecture
```
Hub (điều phối)
├── Node 1 (Chrome trên Windows)
├── Node 2 (Firefox trên Linux)
└── Node 3 (Edge trên Mac)
```

### Setup local Grid (Selenium 4 - Standalone)
```bash
# Download selenium-server.jar
# Chạy Standalone (Hub + Node trong 1)
java -jar selenium-server-4.x.jar standalone

# Hoặc Hub + Node riêng
java -jar selenium-server-4.x.jar hub
java -jar selenium-server-4.x.jar node --hub http://localhost:4444
```

### Kết nối test đến Grid
```java
public WebDriver createRemoteDriver(String browser) throws MalformedURLException {
    URL gridUrl = new URL("http://localhost:4444");

    DesiredCapabilities caps = new DesiredCapabilities();
    switch (browser) {
        case "chrome":
            ChromeOptions chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless=new");
            return new RemoteWebDriver(gridUrl, chromeOptions);
        case "firefox":
            FirefoxOptions firefoxOptions = new FirefoxOptions();
            return new RemoteWebDriver(gridUrl, firefoxOptions);
        default:
            throw new IllegalArgumentException("Unsupported browser: " + browser);
    }
}
```

---

## 2. Docker + Selenium Grid

```yaml
# docker-compose.yml
version: "3.8"
services:
  selenium-hub:
    image: selenium/hub:4.18.1
    container_name: selenium-hub
    ports:
      - "4442:4442"
      - "4443:4443"
      - "4444:4444"

  chrome-node:
    image: selenium/node-chrome:4.18.1
    shm_size: 2gb
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
      - SE_NODE_MAX_SESSIONS=3
    deploy:
      replicas: 2  # 2 Chrome nodes

  firefox-node:
    image: selenium/node-firefox:4.18.1
    shm_size: 2gb
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
```

```bash
docker-compose up -d
# Xem Grid UI: http://localhost:4444
docker-compose down
```

---

## 3. Shadow DOM

```java
// Shadow DOM: component có DOM riêng, không tìm được bằng findElement thông thường
// Ví dụ: <custom-element> có shadow root

JavascriptExecutor js = (JavascriptExecutor) driver;

// Lấy shadow root
WebElement shadowHost = driver.findElement(By.cssSelector("custom-element"));
SearchContext shadowRoot = shadowHost.getShadowRoot(); // Selenium 4

// Tìm element trong shadow root
WebElement shadowElement = shadowRoot.findElement(By.cssSelector("input.shadow-input"));
shadowElement.sendKeys("text in shadow DOM");

// Nested shadow DOM
WebElement outerHost = driver.findElement(By.cssSelector("outer-component"));
SearchContext outerShadow = outerHost.getShadowRoot();
WebElement innerHost = outerShadow.findElement(By.cssSelector("inner-component"));
SearchContext innerShadow = innerHost.getShadowRoot();
innerShadow.findElement(By.cssSelector("button")).click();
```

---

## 4. CDP - Chrome DevTools Protocol

```java
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v120.network.Network;
import org.openqa.selenium.devtools.v120.performance.Performance;

ChromeDriver chromeDriver = (ChromeDriver) driver;
DevTools devTools = chromeDriver.getDevTools();
devTools.createSession();

// Network throttling (simulate slow 3G)
devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
devTools.send(Network.emulateNetworkConditions(
    false,
    100,    // latency ms
    500000, // download bytes/s (500 KB/s)
    200000, // upload bytes/s
    Optional.of(Network.ConnectionType.CELLULAR3G)
));

// Capture console logs
devTools.addListener(Log.entryAdded(), logEntry -> {
    System.out.println("Console [" + logEntry.getLevel() + "]: " + logEntry.getText());
});

// Mock API response
devTools.send(Fetch.enable(Optional.empty(), Optional.empty()));
devTools.addListener(Fetch.requestPaused(), request -> {
    if (request.getRequest().getUrl().contains("/api/users")) {
        devTools.send(Fetch.fulfillRequest(
            request.getRequestId(),
            200,
            Optional.empty(),
            Optional.empty(),
            Optional.of("{\"users\": []}"), // Mock empty response
            Optional.empty()
        ));
    } else {
        devTools.send(Fetch.continueRequest(request.getRequestId(),
            Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
    }
});
```

---

## 5. Relative Locators (Selenium 4)

```java
import static org.openqa.selenium.support.locators.RelativeLocator.with;

// Tìm element dựa trên vị trí tương đối với element khác
WebElement emailLabel = driver.findElement(By.tagName("label"));

// Input field bên dưới label "Email"
WebElement emailInput = driver.findElement(
    with(By.tagName("input")).below(emailLabel)
);

// Button bên phải input
WebElement submitBtn = driver.findElement(
    with(By.tagName("button")).toRightOf(emailInput)
);

// Element phía trên
WebElement header = driver.findElement(
    with(By.tagName("h2")).above(emailInput)
);

// near() - trong vòng 50px
WebElement nearElement = driver.findElement(
    with(By.tagName("span")).near(emailLabel)
);
```

---

## 6. Screenshot

```java
// Screenshot toàn trang
public String takeScreenshot(String testName) {
    TakesScreenshot ts = (TakesScreenshot) driver;
    File srcFile = ts.getScreenshotAs(OutputType.FILE);
    String destPath = "target/screenshots/" + testName + "_" + 
                      LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".png";
    try {
        FileUtils.copyFile(srcFile, new File(destPath));
    } catch (IOException e) {
        e.printStackTrace();
    }
    return destPath;
}

// Screenshot element cụ thể
public void takeElementScreenshot(WebElement element, String name) throws IOException {
    File screenshot = element.getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(screenshot, new File("target/screenshots/" + name + ".png"));
}

// Screenshot dưới dạng Base64 (dùng cho Allure)
public String takeScreenshotAsBase64() {
    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
}

// Attach vào Allure
@Attachment(value = "Screenshot", type = "image/png")
public byte[] attachScreenshot() {
    return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
}
```

---

## 7. Performance Tips

```java
// 1. Reuse driver trong cùng test class (không tạo mới mỗi test)
@BeforeClass
public void setupOnce() {
    driver = DriverFactory.createDriver("chrome", false);
}

// 2. Parallel execution với ThreadLocal
// (xem 02-testng-advanced.md)

// 3. Minimize waits - chỉ wait khi cần
// ❌ Không wait cho element đã sẵn sàng
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("static-element")));
// ✅ Chỉ wait cho dynamic element
wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ajax-result")));

// 4. Headless cho CI/CD - nhanh hơn 20-30%
chromeOptions.addArguments("--headless=new");

// 5. Disable images (tăng tốc load)
Map<String, Object> prefs = new HashMap<>();
prefs.put("profile.managed_default_content_settings.images", 2);
chromeOptions.setExperimentalOption("prefs", prefs);
```

---

## 8. Câu hỏi phỏng vấn

**Q1: Selenium Grid dùng để làm gì?**
> **Trả lời:** Chạy test song song trên nhiều máy/browser/OS cùng lúc. Hub điều phối, Node thực thi. Giảm thời gian chạy test suite lớn từ hàng giờ xuống còn vài phút.
>
> **Gợi nhớ:** Grid = nhiều máy chạy cùng lúc, Hub = tổng đài điều phối

**Q2: Tại sao dùng Docker cho Selenium Grid?**
> **Trả lời:** Môi trường nhất quán (không lo version browser khác nhau), dễ scale (thêm node bằng replicas), không cần cài browser trên máy CI, dễ cleanup.
>
> **Gợi nhớ:** Docker = hộp đóng gói, mở ra đâu cũng giống nhau

**Q3: CDP trong Selenium 4 dùng để làm gì?**
> **Trả lời:** Truy cập Chrome DevTools Protocol để: mock network response, simulate slow network, capture console logs, intercept requests. Mở ra khả năng test nâng cao mà WebDriver API không có.
>
> **Gợi nhớ:** CDP = cửa hậu vào Chrome DevTools từ code Java

**Q4: Khi nào dùng Relative Locators?**
> **Trả lời:** Khi element không có id/class ổn định nhưng vị trí tương đối với element khác thì ổn định. Ví dụ: input field luôn nằm dưới label tương ứng.
>
> **Gợi nhớ:** Relative Locator = tìm nhà bằng địa chỉ tương đối "nhà bên cạnh siêu thị"

---

[Tiếp theo: ../RestAssured/README.md](../RestAssured/README.md) | [Quay lại README](./README.md)
