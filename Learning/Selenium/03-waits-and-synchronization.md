# Waits and Synchronization

## 1. Tại sao cần Wait?

Web app hiện đại không load tất cả cùng lúc:
- **Page load:** Browser cần thời gian render HTML/CSS/JS
- **AJAX calls:** Data load bất đồng bộ sau khi page load xong
- **Animations:** Element fade in/out, slide, spinner
- **Dynamic content:** Element xuất hiện sau khi user action

Nếu Selenium tìm element trước khi nó xuất hiện → `NoSuchElementException`

---

## 2. Implicit Wait

```java
// Set 1 lần, áp dụng cho tất cả findElement
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

// Cách hoạt động: polling mỗi 500ms trong 10 giây
// Nếu tìm thấy → trả về ngay
// Nếu hết 10 giây → throw NoSuchElementException
```

**Nhược điểm của Implicit Wait:**
- Áp dụng toàn cục → không kiểm soát được từng element
- Kết hợp với Explicit Wait gây ra hành vi không đoán được
- Không thể wait cho điều kiện phức tạp (element clickable, text thay đổi)
- Làm chậm test khi element thực sự không tồn tại (phải chờ hết timeout)

**Khuyến nghị:** Set implicit wait = 0, dùng Explicit Wait cho từng trường hợp cụ thể.

---

## 3. Explicit Wait (WebDriverWait) - Nên dùng

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

// Visibility - element có trong DOM VÀ hiển thị
WebElement element = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("result"))
);

// Presence - element có trong DOM (không cần hiển thị)
wait.until(ExpectedConditions.presenceOfElementLocated(By.id("hidden-input")));

// Clickable - element hiển thị VÀ enabled
wait.until(ExpectedConditions.elementToBeClickable(By.id("submit-btn"))).click();

// Invisible - chờ element biến mất (spinner, loading)
wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("loading-spinner")));

// Text present
wait.until(ExpectedConditions.textToBePresentInElementLocated(
    By.id("status"), "Success"
));

// Alert
Alert alert = wait.until(ExpectedConditions.alertIsPresent());

// Number of elements
wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
    By.cssSelector(".product-item"), 0
));

// Staleness - chờ element cũ bị remove (sau page reload)
WebElement oldElement = driver.findElement(By.id("content"));
wait.until(ExpectedConditions.stalenessOf(oldElement));

// URL
wait.until(ExpectedConditions.urlContains("/dashboard"));
wait.until(ExpectedConditions.titleContains("Dashboard"));

// Frame available
wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("iframe-id"));
```

---

## 4. Fluent Wait - Tùy chỉnh polling

```java
Wait<WebDriver> fluentWait = new FluentWait<>(driver)
    .withTimeout(Duration.ofSeconds(30))
    .pollingEvery(Duration.ofMillis(500))          // Check mỗi 500ms
    .ignoring(NoSuchElementException.class)         // Bỏ qua exception này
    .ignoring(StaleElementReferenceException.class)
    .withMessage("Element not found after 30 seconds"); // Custom error message

WebElement element = fluentWait.until(driver ->
    driver.findElement(By.id("dynamic-element"))
);

// Custom condition với Fluent Wait
WebElement result = fluentWait.until(driver -> {
    WebElement el = driver.findElement(By.id("price"));
    String price = el.getText();
    return price.startsWith("$") ? el : null; // null = chưa thỏa điều kiện
});
```

---

## 5. Custom ExpectedCondition

```java
// Viết condition riêng khi built-in không đủ
public static ExpectedCondition<Boolean> elementHasText(By locator, String text) {
    return driver -> {
        try {
            String actualText = driver.findElement(locator).getText();
            return actualText.equals(text);
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    };
}

// Chờ số lượng element đúng bằng expected
public static ExpectedCondition<List<WebElement>> numberOfElementsToBe(By locator, int count) {
    return driver -> {
        List<WebElement> elements = driver.findElements(locator);
        return elements.size() == count ? elements : null;
    };
}

// Sử dụng
wait.until(elementHasText(By.id("counter"), "10 items"));
wait.until(numberOfElementsToBe(By.cssSelector(".row"), 5));
```

---

## 6. Tại sao KHÔNG dùng Thread.sleep()

```java
// ❌ SAI - Thread.sleep()
Thread.sleep(3000); // Luôn chờ đúng 3 giây dù element đã xuất hiện sau 0.5 giây
driver.findElement(By.id("result")).click();

// ✅ ĐÚNG - Explicit Wait
wait.until(ExpectedConditions.elementToBeClickable(By.id("result"))).click();
// Chờ tối đa 15 giây, nhưng click ngay khi element sẵn sàng
```

**Vấn đề với Thread.sleep():**
- Lãng phí thời gian (luôn chờ đủ thời gian)
- Không đủ thời gian khi môi trường chậm
- Không thể handle dynamic timing
- Làm test suite chậm đáng kể

---

## 7. Flaky Tests - Nguyên nhân và cách fix

### Nguyên nhân phổ biến:

**1. Timing issues**
```java
// ❌ Vấn đề
driver.findElement(By.id("submit")).click();
String result = driver.findElement(By.id("result")).getText(); // Có thể chưa load

// ✅ Fix
driver.findElement(By.id("submit")).click();
String result = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("result"))
).getText();
```

**2. StaleElementReferenceException**
```java
// ❌ Vấn đề: element bị stale sau page reload
WebElement btn = driver.findElement(By.id("btn"));
driver.navigate().refresh(); // Page reload → btn reference cũ bị stale
btn.click(); // StaleElementReferenceException!

// ✅ Fix: Re-find element sau khi page thay đổi
driver.navigate().refresh();
wait.until(ExpectedConditions.stalenessOf(btn)); // Chờ element cũ biến mất
driver.findElement(By.id("btn")).click(); // Find lại
```

**3. Retry pattern cho StaleElement**
```java
public WebElement findElementWithRetry(By locator, int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        try {
            return driver.findElement(locator);
        } catch (StaleElementReferenceException e) {
            if (i == maxRetries - 1) throw e;
            try { Thread.sleep(500); } catch (InterruptedException ie) { }
        }
    }
    throw new RuntimeException("Element still stale after " + maxRetries + " retries");
}
```

**4. Dynamic content (loading spinner)**
```java
// Chờ spinner biến mất trước khi thao tác
public void waitForPageLoad() {
    // Chờ loading spinner biến mất
    wait.until(ExpectedConditions.invisibilityOfElementLocated(
        By.cssSelector(".loading-spinner")
    ));
    // Chờ jQuery AJAX xong (nếu dùng jQuery)
    wait.until(driver ->
        (Boolean) ((JavascriptExecutor) driver)
            .executeScript("return jQuery.active == 0")
    );
}
```

---

## 8. Câu hỏi phỏng vấn

**Q1: Sự khác nhau giữa Implicit Wait và Explicit Wait?**
> **Trả lời:** Implicit Wait áp dụng toàn cục cho tất cả findElement, chỉ wait cho element xuất hiện trong DOM. Explicit Wait áp dụng cho từng element cụ thể, có thể wait cho nhiều điều kiện khác nhau (clickable, invisible, text change).
>
> **Gợi nhớ:** Implicit = lưới toàn sân, Explicit = lưới đúng chỗ cần

**Q2: Tại sao không nên kết hợp Implicit và Explicit Wait?**
> **Trả lời:** Khi kết hợp, timeout có thể cộng dồn không đoán được. Ví dụ: implicit 10s + explicit 15s có thể chờ đến 25s thay vì 15s. Nên set implicit = 0 và chỉ dùng explicit.
>
> **Gợi nhớ:** Hai loại wait = hai ông chủ, không ai biết ai ra lệnh

**Q3: StaleElementReferenceException là gì và cách fix?**
> **Trả lời:** Xảy ra khi element đã được tìm thấy nhưng sau đó DOM thay đổi (page reload, AJAX update) làm reference cũ không còn hợp lệ. Fix bằng cách re-find element sau khi DOM thay đổi, hoặc dùng retry pattern.
>
> **Gợi nhớ:** Stale = thiu, element đã "thiu" vì DOM thay đổi → tìm lại

**Q4: Fluent Wait khác WebDriverWait ở điểm nào?**
> **Trả lời:** FluentWait cho phép tùy chỉnh polling interval (mặc định 500ms), ignore specific exceptions, và custom error message. WebDriverWait là subclass của FluentWait với polling 500ms mặc định.
>
> **Gợi nhớ:** FluentWait = WebDriverWait nhưng có thêm tùy chỉnh

---

[Tiếp theo: 04-page-object-model.md](./04-page-object-model.md) | [Quay lại README](./README.md)
