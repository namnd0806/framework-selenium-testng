# Exception Handling — Xử lý lỗi trong Java

> Automation framework đầy rẫy exception: element không tìm thấy, timeout, file không tồn tại...
> Xử lý exception đúng cách = test ổn định, báo lỗi rõ ràng.

---

## 1. Exception là gì?

Exception = sự kiện bất thường xảy ra khi chạy chương trình, làm gián đoạn luồng bình thường.

```
Chương trình chạy bình thường
        ↓
Gặp tình huống bất thường (element không tìm thấy, file không tồn tại...)
        ↓
Java tạo ra Exception object
        ↓
Nếu không xử lý → chương trình crash
Nếu xử lý đúng → chương trình tiếp tục hoặc báo lỗi có ý nghĩa
```

---

## 2. Cây phân cấp Exception

```
Throwable
├── Error (không nên catch — lỗi nghiêm trọng của JVM)
│   ├── OutOfMemoryError
│   └── StackOverflowError
└── Exception
    ├── Checked Exception (phải xử lý — compiler bắt buộc)
    │   ├── IOException
    │   ├── FileNotFoundException
    │   └── SQLException
    └── RuntimeException (Unchecked — không bắt buộc xử lý)
        ├── NullPointerException
        ├── ArrayIndexOutOfBoundsException
        ├── ClassCastException
        ├── NumberFormatException
        └── (Selenium exceptions cũng là RuntimeException)
            ├── NoSuchElementException
            ├── StaleElementReferenceException
            ├── TimeoutException
            └── ElementNotInteractableException
```

---

## 3. Checked vs Unchecked Exception

### Checked Exception — Compiler bắt buộc xử lý

```java
// FileNotFoundException là checked — phải try/catch hoặc throws
public String readTestData(String filePath) throws IOException {
    // Nếu không có throws IOException → compile error
    FileReader reader = new FileReader(filePath);
    // ...
}

// Hoặc dùng try/catch
public String readTestData(String filePath) {
    try {
        FileReader reader = new FileReader(filePath);
        // đọc file...
        return content;
    } catch (FileNotFoundException e) {
        System.out.println("File không tồn tại: " + filePath);
        return null;
    } catch (IOException e) {
        System.out.println("Lỗi đọc file: " + e.getMessage());
        return null;
    }
}
```

### Unchecked Exception — Không bắt buộc, nhưng nên xử lý

```java
// NullPointerException — hay gặp nhất
WebElement element = driver.findElement(By.id("email"));
// Nếu element = null → NullPointerException khi gọi .click()
element.click(); // NPE nếu element null

// Cách xử lý
if (element != null) {
    element.click();
}
// Hoặc dùng Optional (Java 8+)

// NoSuchElementException — Selenium
try {
    WebElement el = driver.findElement(By.id("nonexistent"));
} catch (NoSuchElementException e) {
    System.out.println("Element không tìm thấy: " + e.getMessage());
}
```

---

## 4. try / catch / finally

### Cú pháp cơ bản

```java
try {
    // Code có thể gây ra exception
    WebElement element = driver.findElement(By.id("submit"));
    element.click();
} catch (NoSuchElementException e) {
    // Xử lý khi element không tìm thấy
    System.out.println("Không tìm thấy nút submit: " + e.getMessage());
    takeScreenshot("submit_not_found");
} catch (ElementNotInteractableException e) {
    // Xử lý khi element không thể click
    System.out.println("Nút submit không thể click: " + e.getMessage());
} catch (Exception e) {
    // Catch tất cả exception còn lại (để cuối cùng)
    System.out.println("Lỗi không xác định: " + e.getMessage());
} finally {
    // LUÔN chạy dù có exception hay không
    // Dùng để cleanup: đóng file, đóng connection...
    System.out.println("Bước này đã hoàn thành (dù pass hay fail)");
}
```

### finally luôn chạy — kể cả khi có return

```java
public boolean clickButton() {
    try {
        driver.findElement(By.id("btn")).click();
        return true;  // return ở đây
    } catch (Exception e) {
        return false; // hoặc ở đây
    } finally {
        System.out.println("Finally luôn chạy!"); // vẫn chạy trước khi return
    }
}
```

### Multi-catch (Java 7+)

```java
try {
    // code
} catch (NoSuchElementException | TimeoutException e) {
    // Xử lý cả 2 loại exception giống nhau
    takeScreenshot("element_issue");
    throw new RuntimeException("Element issue: " + e.getMessage(), e);
}
```

---

## 5. throw vs throws

```java
// throws — khai báo method có thể ném exception (checked)
public void readFile(String path) throws IOException {
    // ...
}

// throw — ném exception tại một điểm cụ thể
public void login(String email, String password) {
    if (email == null || email.isEmpty()) {
        throw new IllegalArgumentException("Email không được để trống");
    }
    // tiếp tục login...
}

// Kết hợp — bắt exception rồi ném lại với thông tin rõ hơn
public void clickElement(By locator) {
    try {
        driver.findElement(locator).click();
    } catch (NoSuchElementException e) {
        throw new RuntimeException(
            "Không tìm thấy element: " + locator.toString(), e
        );
        // Lưu ý: truyền e vào để giữ stack trace gốc
    }
}
```

---

## 6. Custom Exception — Tạo exception riêng

Tạo custom exception giúp báo lỗi rõ ràng hơn trong framework:

```java
// Custom exception cho framework
public class PageNotLoadedException extends RuntimeException {
    private final String pageName;
    private final String currentUrl;

    public PageNotLoadedException(String pageName, String currentUrl) {
        super(String.format("Page '%s' chưa load xong. Current URL: %s",
              pageName, currentUrl));
        this.pageName = pageName;
        this.currentUrl = currentUrl;
    }

    // Constructor với cause (giữ stack trace gốc)
    public PageNotLoadedException(String pageName, String currentUrl, Throwable cause) {
        super(String.format("Page '%s' chưa load xong. Current URL: %s",
              pageName, currentUrl), cause);
        this.pageName = pageName;
        this.currentUrl = currentUrl;
    }

    public String getPageName() { return pageName; }
    public String getCurrentUrl() { return currentUrl; }
}

// Dùng trong BasePage
public abstract class BasePage {
    public abstract boolean isPageLoaded();

    public void waitForPageLoad() {
        try {
            wait.until(driver -> isPageLoaded());
        } catch (TimeoutException e) {
            throw new PageNotLoadedException(
                this.getClass().getSimpleName(),
                driver.getCurrentUrl(),
                e
            );
        }
    }
}

// Kết quả khi fail:
// PageNotLoadedException: Page 'LoginPage' chưa load xong. Current URL: https://...
// Rõ ràng hơn nhiều so với TimeoutException chung chung
```

---

## 7. Exception trong Automation — Các tình huống thực tế

### Retry khi gặp StaleElementReferenceException

```java
public void clickWithRetry(By locator, int maxRetries) {
    int attempts = 0;
    while (attempts < maxRetries) {
        try {
            driver.findElement(locator).click();
            return; // thành công, thoát
        } catch (StaleElementReferenceException e) {
            attempts++;
            if (attempts == maxRetries) {
                throw new RuntimeException(
                    "Không thể click sau " + maxRetries + " lần thử: " + locator, e
                );
            }
            // Chờ một chút rồi thử lại
            try { Thread.sleep(500); } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
```

### Kiểm tra element tồn tại không (không throw exception)

```java
// Cách sai — dùng exception để control flow
public boolean isElementPresent(By locator) {
    try {
        driver.findElement(locator);
        return true;
    } catch (NoSuchElementException e) {
        return false;
    }
}

// Cách tốt hơn — dùng findElements (không throw exception)
public boolean isElementPresent(By locator) {
    return !driver.findElements(locator).isEmpty();
}

// Cách tốt nhất — dùng findElements với size check
public boolean isElementPresent(By locator) {
    return driver.findElements(locator).size() > 0;
}
```

### Xử lý exception trong @AfterMethod

```java
@AfterMethod
public void tearDown(ITestResult result) {
    try {
        if (result.getStatus() == ITestResult.FAILURE) {
            takeScreenshot(result.getName());
        }
    } catch (Exception e) {
        // Không để exception trong tearDown làm fail test
        System.err.println("Lỗi trong tearDown: " + e.getMessage());
    } finally {
        // Luôn quit driver dù có lỗi hay không
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                System.err.println("Lỗi khi quit driver: " + e.getMessage());
            }
        }
    }
}
```

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: Checked vs Unchecked exception — khác nhau thế nào? Cho ví dụ thực tế.**

```
Gợi nhớ:
- Checked = "compiler kiểm tra" — phải xử lý, không compile được
- Unchecked = "runtime mới biết" — không bắt buộc xử lý

Checked (phải xử lý):
- IOException — đọc/ghi file
- SQLException — query database
- FileNotFoundException — file không tồn tại
→ Trong automation: đọc test data từ file

Unchecked (không bắt buộc):
- NullPointerException — gọi method trên null
- NoSuchElementException — Selenium không tìm thấy element
- StaleElementReferenceException — element đã bị DOM thay đổi
→ Trong automation: gặp hàng ngày khi test
```

**Q2: finally block có chạy không nếu có return trong try?**

```
CÓ — finally luôn chạy trước khi return thực sự được thực thi.

Ngoại lệ duy nhất: System.exit() hoặc JVM crash.

Ứng dụng thực tế:
- finally để đóng file, đóng DB connection, quit WebDriver
- Đảm bảo cleanup luôn xảy ra dù test pass hay fail
```

**Q3: throw vs throws — khác nhau thế nào?**

```
throws (khai báo):
- Đặt sau tên method
- Báo cho người gọi biết method có thể ném exception này
- public void readFile() throws IOException { }

throw (hành động):
- Đặt trong body method
- Thực sự ném exception tại điểm đó
- throw new IllegalArgumentException("Email trống");

Gợi nhớ: throws = "cảnh báo", throw = "ném thật"
```

**Q4: Tại sao nên truyền cause khi re-throw exception?**

```java
// Sai — mất stack trace gốc
catch (NoSuchElementException e) {
    throw new RuntimeException("Không tìm thấy element");
    // Mất thông tin về nguyên nhân gốc
}

// Đúng — giữ stack trace gốc
catch (NoSuchElementException e) {
    throw new RuntimeException("Không tìm thấy element", e);
    // e là cause — giữ đầy đủ thông tin để debug
}
```

**Q5: Khi nào nên tạo Custom Exception?**

```
Nên tạo khi:
1. Exception có thêm thông tin đặc thù (pageName, locator, url...)
2. Muốn phân biệt rõ loại lỗi trong framework
3. Caller cần xử lý khác nhau tùy loại lỗi

Ví dụ trong framework:
- PageNotLoadedException — biết page nào chưa load
- ElementNotFoundException — biết locator nào không tìm thấy
- TestDataException — biết file/key nào bị thiếu
```

**Q6: Làm thế nào để kiểm tra element có tồn tại không mà không dùng exception?**

```java
// Dùng findElements (trả về empty list thay vì throw exception)
boolean exists = !driver.findElements(By.id("popup")).isEmpty();

// Tại sao tốt hơn try/catch:
// 1. Nhanh hơn — không tạo exception object
// 2. Rõ ý định hơn — "tôi muốn kiểm tra, không phải tìm"
// 3. Exception nên dùng cho tình huống bất thường, không phải flow bình thường
```

---

**Tiếp theo:** [04 — String & File I/O](./04-string-and-io.md)
