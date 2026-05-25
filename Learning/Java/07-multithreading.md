# Multithreading & ThreadLocal — Parallel Test

> Đây là phần quan trọng nhất với SDET.
> Parallel Selenium test mà không dùng ThreadLocal đúng cách = test fail ngẫu nhiên, khó debug.

---

## 1. Thread là gì?

Thread = luồng thực thi độc lập trong chương trình.

```
Chương trình bình thường (single-thread):
Test 1 → Test 2 → Test 3 → Test 4
(chạy tuần tự, tổng 40 giây nếu mỗi test 10 giây)

Parallel (multi-thread):
Thread 1: Test 1 → Test 3
Thread 2: Test 2 → Test 4
(chạy song song, tổng ~20 giây)
```

---

## 2. Vấn đề khi dùng static WebDriver

```java
// SAI — static driver bị share giữa các thread
public class BaseTest {
    protected static WebDriver driver; // NGUY HIỂM khi parallel!

    @BeforeMethod
    public void setUp() {
        driver = new ChromeDriver(); // Thread 1 và Thread 2 cùng ghi vào driver
    }
}

// Điều gì xảy ra khi parallel:
// Thread 1: driver = new ChromeDriver() → driver trỏ đến Chrome 1
// Thread 2: driver = new ChromeDriver() → driver trỏ đến Chrome 2 (ghi đè!)
// Thread 1: driver.findElement(...) → thực ra đang dùng Chrome 2!
// → Test fail ngẫu nhiên, rất khó debug
```

---

## 3. ThreadLocal — Giải pháp

**ThreadLocal** = mỗi thread có bản sao riêng của biến, không share với thread khác.

**Gợi nhớ:** Như tủ đồ cá nhân — mỗi người có ngăn riêng, không ai lấy đồ của người khác.

```java
// ĐÚNG — dùng ThreadLocal
public class DriverManager {
    // ThreadLocal<WebDriver> — mỗi thread có WebDriver riêng
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void setDriver(WebDriver driver) {
        driverThreadLocal.set(driver);
    }

    public static void removeDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            driver.quit();
            driverThreadLocal.remove(); // QUAN TRỌNG: phải remove để tránh memory leak
        }
    }
}

// BaseTest dùng DriverManager
public class BaseTest {
    @BeforeMethod
    public void setUp() {
        WebDriver driver = new ChromeDriver();
        DriverManager.setDriver(driver); // lưu vào ThreadLocal của thread hiện tại
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.removeDriver(); // xóa khỏi ThreadLocal, quit driver
    }
}

// BasePage dùng DriverManager
public class BasePage {
    protected WebDriver driver;

    public BasePage() {
        this.driver = DriverManager.getDriver(); // lấy driver của thread hiện tại
    }
}

// Kết quả:
// Thread 1: getDriver() → Chrome 1 (của Thread 1)
// Thread 2: getDriver() → Chrome 2 (của Thread 2)
// Không bao giờ bị nhầm lẫn!
```

---

## 4. Setup Parallel trong TestNG

### testng.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "http://testng.org/testng-1.9.dtd">
<suite name="Parallel Suite" parallel="methods" thread-count="4">
    <!-- parallel="methods" — mỗi @Test method chạy trên thread riêng -->
    <!-- parallel="classes" — mỗi class chạy trên thread riêng -->
    <!-- parallel="tests"   — mỗi <test> tag chạy trên thread riêng -->
    <!-- thread-count="4"   — tối đa 4 thread cùng lúc -->

    <test name="Login Tests">
        <classes>
            <class name="com.example.tests.LoginTest"/>
            <class name="com.example.tests.CheckoutTest"/>
        </classes>
    </test>
</suite>
```

### Parallel theo browser

```xml
<suite name="Cross Browser" parallel="tests" thread-count="3">
    <test name="Chrome Tests">
        <parameter name="browser" value="chrome"/>
        <classes>
            <class name="com.example.tests.LoginTest"/>
        </classes>
    </test>
    <test name="Firefox Tests">
        <parameter name="browser" value="firefox"/>
        <classes>
            <class name="com.example.tests.LoginTest"/>
        </classes>
    </test>
    <test name="Edge Tests">
        <parameter name="browser" value="edge"/>
        <classes>
            <class name="com.example.tests.LoginTest"/>
        </classes>
    </test>
</suite>
```

### BaseTest với @Parameters

```java
public class BaseTest {
    @BeforeMethod
    @Parameters({"browser"})
    public void setUp(@Optional("chrome") String browser) {
        WebDriver driver;
        switch (browser.toLowerCase()) {
            case "firefox":
                driver = new FirefoxDriver();
                break;
            case "edge":
                driver = new EdgeDriver();
                break;
            default:
                driver = new ChromeDriver();
        }
        DriverManager.setDriver(driver);
        DriverManager.getDriver().manage().window().maximize();
    }

    @AfterMethod
    public void tearDown() {
        DriverManager.removeDriver();
    }
}
```

---

## 5. Thread Safety — Các vấn đề thường gặp

### Race Condition

```java
// Vấn đề: 2 thread cùng đọc/ghi biến shared
public class Counter {
    private static int count = 0; // shared variable

    public static void increment() {
        count++; // KHÔNG thread-safe! count++ = đọc + tăng + ghi (3 bước)
    }
}

// Thread 1 đọc count = 5
// Thread 2 đọc count = 5
// Thread 1 ghi count = 6
// Thread 2 ghi count = 6 (mất 1 lần increment!)

// Fix: dùng synchronized hoặc AtomicInteger
private static AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet(); // thread-safe
```

### synchronized

```java
// synchronized method — chỉ 1 thread chạy tại 1 thời điểm
public synchronized void writeReport(String content) {
    // chỉ 1 thread vào đây cùng lúc
    reportFile.write(content);
}

// synchronized block — khóa trên object cụ thể
private final Object lock = new Object();
public void writeReport(String content) {
    synchronized (lock) {
        reportFile.write(content);
    }
}
```

### Những gì KHÔNG nên share giữa threads trong automation

```java
// KHÔNG share:
private static WebDriver driver;        // dùng ThreadLocal
private static WebDriverWait wait;      // dùng ThreadLocal
private static String currentTestName;  // dùng ThreadLocal

// CÓ THỂ share (read-only):
private static final String BASE_URL = "https://example.com"; // constant
private static final Properties config = loadConfig();         // read-only sau khi load
```

---

## 6. ThreadLocal nâng cao — Lưu nhiều thứ

```java
// Lưu nhiều thông tin của test hiện tại
public class TestContext {
    private static final ThreadLocal<Map<String, Object>> context =
        ThreadLocal.withInitial(HashMap::new);

    public static void set(String key, Object value) {
        context.get().put(key, value);
    }

    public static Object get(String key) {
        return context.get().get(key);
    }

    public static void clear() {
        context.get().clear();
        context.remove();
    }
}

// Dùng trong test
@BeforeMethod
public void setUp() {
    TestContext.set("driver", new ChromeDriver());
    TestContext.set("testName", "loginTest");
    TestContext.set("startTime", System.currentTimeMillis());
}

@AfterMethod
public void tearDown() {
    WebDriver driver = (WebDriver) TestContext.get("driver");
    if (driver != null) driver.quit();
    TestContext.clear();
}
```

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: ThreadLocal là gì? Tại sao dùng trong Selenium parallel test?**

```
Gợi nhớ: "Tủ đồ cá nhân — mỗi thread có ngăn riêng"

ThreadLocal = biến mà mỗi thread có bản sao riêng, không share.

Tại sao cần trong parallel Selenium:
- Nếu dùng static WebDriver → tất cả thread dùng chung 1 driver
- Thread 1 đang test Login, Thread 2 ghi đè driver → test fail ngẫu nhiên
- ThreadLocal → Thread 1 có Chrome 1, Thread 2 có Chrome 2, không nhầm lẫn

Cách dùng:
ThreadLocal<WebDriver> driverLocal = new ThreadLocal<>();
driverLocal.set(new ChromeDriver());  // set cho thread hiện tại
driverLocal.get();                    // get của thread hiện tại
driverLocal.remove();                 // PHẢI remove sau khi dùng xong
```

**Q2: Sự khác nhau giữa parallel="methods", "classes", "tests" trong TestNG?**

```
parallel="methods": mỗi @Test method chạy trên thread riêng
  → Nhiều method trong cùng class chạy song song
  → Cần ThreadLocal vì cùng class instance

parallel="classes": mỗi class chạy trên thread riêng
  → Các method trong cùng class vẫn chạy tuần tự
  → Ít rủi ro hơn methods

parallel="tests": mỗi <test> tag trong testng.xml chạy trên thread riêng
  → An toàn nhất, dùng cho cross-browser testing
  → Mỗi <test> có config riêng (browser, environment)
```

**Q3: Race condition là gì? Cho ví dụ trong automation.**

```
Race condition = 2 thread cùng đọc/ghi shared resource, kết quả không đoán được.

Ví dụ trong automation:
- 2 thread cùng ghi vào 1 file report → file bị corrupt
- 2 thread cùng dùng static WebDriver → test fail ngẫu nhiên
- 2 thread cùng tăng biến đếm → mất count

Fix:
- ThreadLocal cho WebDriver
- synchronized cho file writing
- AtomicInteger cho counter
```

**Q4: Tại sao phải gọi ThreadLocal.remove() sau khi dùng xong?**

```
Memory leak!

Thread trong thread pool được tái sử dụng.
Nếu không remove → ThreadLocal value của thread cũ vẫn còn
→ Thread mới lấy được value cũ → bug khó tìm
→ Tích lũy nhiều → OutOfMemoryError

Luôn gọi remove() trong @AfterMethod:
@AfterMethod
public void tearDown() {
    DriverManager.removeDriver(); // quit driver + remove ThreadLocal
}
```

---

**Tiếp theo:** [08 — Design Patterns](./08-design-patterns.md)
