# WebDriver Basics

## 1. Selenium Architecture

```
Test Code (Java)
    ↓ WebDriver API
WebDriver (ChromeDriver/GeckoDriver)
    ↓ W3C WebDriver Protocol (HTTP/JSON)
Browser (Chrome/Firefox/Edge)
```

- **W3C Protocol:** Selenium 4 dùng W3C chuẩn → ổn định hơn, không cần JSON Wire Protocol cũ
- **Browser Driver:** Cầu nối giữa Selenium và browser. ChromeDriver cho Chrome, GeckoDriver cho Firefox.
- **WebDriverManager:** Tự động download đúng version driver → không cần quản lý thủ công

---

## 2. Setup với WebDriverManager

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.github.bonigarcia</groupId>
    <artifactId>webdrivermanager</artifactId>
    <version>5.8.0</version>
    <scope>test</scope>
</dependency>
```

```java
public class DriverFactory {

    public static WebDriver createDriver(String browser, boolean headless) {
        WebDriver driver;

        switch (browser.toLowerCase()) {
            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions chromeOptions = new ChromeOptions();
                if (headless) chromeOptions.addArguments("--headless=new");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--disable-popup-blocking");
                chromeOptions.addArguments("--no-sandbox");           // CI/CD
                chromeOptions.addArguments("--disable-dev-shm-usage"); // CI/CD

                // Cấu hình download directory
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("download.default_directory", System.getProperty("user.dir") + "/downloads");
                prefs.put("download.prompt_for_download", false);
                chromeOptions.setExperimentalOption("prefs", prefs);

                driver = new ChromeDriver(chromeOptions);
                break;

            case "firefox":
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (headless) firefoxOptions.addArguments("--headless");
                driver = new FirefoxDriver(firefoxOptions);
                break;

            case "edge":
                WebDriverManager.edgedriver().setup();
                driver = new EdgeDriver();
                break;

            default:
                throw new IllegalArgumentException("Browser not supported: " + browser);
        }

        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0)); // Tắt implicit wait
        return driver;
    }
}
```

---

## 3. Navigation

```java
// Mở URL
driver.get("https://example.com");           // Load URL, chờ page load xong
driver.navigate().to("https://example.com"); // Tương tự get()

// Navigation history
driver.navigate().back();
driver.navigate().forward();
driver.navigate().refresh();

// Browser info
String title   = driver.getTitle();
String url     = driver.getCurrentUrl();
String source  = driver.getPageSource();
```

---

## 4. Locators đầy đủ

```java
// 8 loại locator cơ bản
driver.findElement(By.id("username"));
driver.findElement(By.name("email"));
driver.findElement(By.className("btn-primary"));
driver.findElement(By.tagName("button"));
driver.findElement(By.linkText("Click here"));
driver.findElement(By.partialLinkText("Click"));
driver.findElement(By.cssSelector("#username"));
driver.findElement(By.xpath("//input[@id='username']"));

// findElements - trả về List (không throw exception nếu không tìm thấy)
List<WebElement> items = driver.findElements(By.className("product-item"));
```

---

## 5. CSS Selector - Cú pháp đầy đủ

```css
/* Tag */
input

/* ID */
#username
input#username

/* Class */
.btn-primary
button.btn.btn-primary

/* Attribute */
input[type='text']
input[placeholder='Enter email']
a[href*='login']        /* href chứa 'login' */
a[href^='https']        /* href bắt đầu bằng 'https' */
a[href$='.pdf']         /* href kết thúc bằng '.pdf' */

/* Child / Descendant */
div.container > p       /* p là con trực tiếp của div.container */
div.container p         /* p là con bất kỳ cấp của div.container */

/* Sibling */
label + input           /* input ngay sau label */
label ~ input           /* tất cả input sau label */

/* Pseudo-class */
li:first-child
li:last-child
li:nth-child(2)
input:not([disabled])
```

```java
// Ví dụ thực tế
driver.findElement(By.cssSelector("input[data-testid='email-input']"));
driver.findElement(By.cssSelector("button[type='submit'].btn-primary"));
driver.findElement(By.cssSelector("table tbody tr:nth-child(1) td:nth-child(2)"));
driver.findElements(By.cssSelector("ul.product-list > li.product-item"));
```

---

## 6. XPath - Cú pháp đầy đủ

```xpath
// Absolute (tránh dùng - dễ vỡ)
/html/body/div/form/input

// Relative (nên dùng)
//input[@id='username']
//button[text()='Login']
//button[contains(text(),'Log')]
//input[contains(@class,'btn') and @type='submit']

// Axes
//label[text()='Email']/following-sibling::input   // input sau label Email
//td[text()='John']/parent::tr                     // row chứa cell "John"
//div[@class='modal']//button[text()='OK']          // button trong modal
(//table//tr)[3]                                   // row thứ 3

// Functions
//input[contains(@placeholder,'email')]
//div[starts-with(@id,'product-')]
//li[last()]
//li[position()=2]
normalize-space(//h1/text())
```

---

## 7. CSS vs XPath - So sánh

| Tiêu chí | CSS Selector | XPath |
|----------|-------------|-------|
| Tốc độ | ✅ Nhanh hơn | ⚠️ Chậm hơn một chút |
| Độ ổn định | ✅ Ổn định | ⚠️ Dễ vỡ nếu dùng absolute |
| Traverse lên parent | ❌ Không thể | ✅ Có thể |
| Text matching | ❌ Không có | ✅ text(), contains() |
| Độ phức tạp | ✅ Đơn giản | ⚠️ Phức tạp hơn |
| Browser support | ✅ Tốt hơn | ✅ Tốt |

**Quy tắc chọn locator (ưu tiên từ trên xuống):**
1. `data-testid` attribute (tốt nhất - dành riêng cho test)
2. `id` (nếu unique và stable)
3. CSS Selector (ngắn gọn, nhanh)
4. XPath (khi cần traverse hoặc text matching)

---

## 8. Câu hỏi phỏng vấn

**Q1: Sự khác nhau giữa findElement và findElements?**
> **Trả lời:** findElement trả về 1 WebElement, throw NoSuchElementException nếu không tìm thấy. findElements trả về List<WebElement>, trả về list rỗng nếu không tìm thấy (không throw exception). Dùng findElements để kiểm tra element có tồn tại không.
>
> **Gợi nhớ:** findElements không throw exception → dùng để check existence

**Q2: Khi nào dùng CSS, khi nào dùng XPath?**
> **Trả lời:** Dùng CSS khi có thể — nhanh hơn và đơn giản hơn. Dùng XPath khi cần traverse lên parent, tìm theo text, hoặc cần logic phức tạp mà CSS không hỗ trợ.
>
> **Gợi nhớ:** CSS first, XPath khi CSS bó tay

**Q3: Tại sao nên dùng data-testid?**
> **Trả lời:** data-testid là attribute dành riêng cho testing, không bị thay đổi khi dev refactor CSS class hay ID. Tạo ra contract giữa dev và tester.
>
> **Gợi nhớ:** data-testid = địa chỉ nhà riêng cho test, không ai đụng vào

**Q4: WebDriverManager giải quyết vấn đề gì?**
> **Trả lời:** Tự động download và setup đúng version ChromeDriver/GeckoDriver tương thích với browser đang cài. Không cần download thủ công hay cập nhật khi browser update.
>
> **Gợi nhớ:** WebDriverManager = quản lý driver tự động, không lo version mismatch

---

[Tiếp theo: 02-interactions.md](./02-interactions.md) | [Quay lại README](./README.md)
