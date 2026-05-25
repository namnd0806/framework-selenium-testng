# Playwright vs Selenium — So sánh chi tiết

---

## 1. Tổng quan so sánh

| Tiêu chí | Selenium | Playwright |
|---|---|---|
| Ra mắt | 2004 | 2020 |
| Phát triển bởi | Selenium HQ | Microsoft |
| Ngôn ngữ | Java, Python, JS, C#, Ruby | Java, Python, JS/TS, C# |
| Browser support | Chrome, Firefox, Edge, Safari | Chrome, Firefox, WebKit (Safari) |
| Architecture | WebDriver → Browser Driver → Browser | Direct browser protocol (CDP/WebSocket) |
| Auto-waiting | ❌ Phải tự viết explicit wait | ✅ Built-in, tự chờ element ready |
| Flaky test | ⚠️ Hay gặp | ✅ Ít hơn nhiều |
| Speed | Trung bình | Nhanh hơn ~2x |
| Setup | Phức tạp hơn | Đơn giản hơn |
| Tài liệu | Rất nhiều | Tốt, đang tăng |
| Community | Lớn nhất | Đang tăng nhanh |
| Selenium Grid | ✅ Có | ✅ Có (Playwright Grid) |
| Mobile testing | Qua Appium | ❌ Không hỗ trợ native mobile |

---

## 2. Tại sao Playwright ít flaky hơn?

### Selenium — Vấn đề timing

```java
// Selenium: phải tự viết wait
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement button = wait.until(
    ExpectedConditions.elementToBeClickable(By.id("submit"))
);
button.click();
// Vẫn có thể fail nếu element clickable nhưng chưa ready thật sự
```

### Playwright — Auto-waiting

```java
// Playwright: tự chờ element ready trước khi action
page.click("#submit");
// Playwright tự chờ: element visible + enabled + stable + not obscured
// Không cần viết wait thủ công
```

**Playwright kiểm tra trước khi action:**
1. Element tồn tại trong DOM
2. Element visible (không hidden)
3. Element enabled (không disabled)
4. Element stable (không đang animate)
5. Element không bị che bởi element khác
6. Element trong viewport

---

## 3. Architecture khác nhau

```
Selenium:
Test → WebDriver API → HTTP → ChromeDriver → Chrome
(qua HTTP, chậm hơn, có latency)

Playwright:
Test → Playwright → CDP/WebSocket → Chrome
(kết nối trực tiếp, nhanh hơn, ổn định hơn)
```

---

## 4. Locators — Khác biệt lớn nhất

```java
// Selenium — locator theo DOM structure
driver.findElement(By.id("email"));
driver.findElement(By.cssSelector(".btn-login"));
driver.findElement(By.xpath("//button[@type='submit']"));

// Playwright — locator theo semantic (ý nghĩa)
page.getByLabel("Email address");          // Tìm theo label
page.getByPlaceholder("Enter email");      // Tìm theo placeholder
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login"));
page.getByText("Login");                   // Tìm theo text
page.getByTestId("login-button");          // Tìm theo data-testid
page.locator("#email");                    // CSS selector (vẫn dùng được)
```

**Tại sao Playwright locators tốt hơn?**
- `getByRole` + `getByLabel` → test theo accessibility → ổn định hơn
- Ít phụ thuộc vào DOM structure → ít vỡ khi UI thay đổi
- Đọc rõ ý nghĩa hơn

---

## 5. Assertions — Playwright tự chờ

```java
// Selenium — assertion ngay lập tức, có thể fail do timing
assertEquals("Dashboard", driver.getTitle());
// Nếu page chưa load xong → fail

// Playwright — assertion tự retry đến khi pass hoặc timeout
assertThat(page).hasTitle("Dashboard");
// Playwright tự retry assertion trong 5 giây (mặc định)
// Không cần wait trước assertion

// Các assertion Playwright
assertThat(page).hasTitle("Dashboard");
assertThat(page).hasURL(Pattern.compile(".*dashboard.*"));
assertThat(locator).isVisible();
assertThat(locator).isEnabled();
assertThat(locator).hasText("Welcome, John");
assertThat(locator).hasValue("test@example.com");
assertThat(locator).isChecked();
assertThat(locator).hasCount(5);
```

---

## 6. Khi nào chọn Selenium, khi nào chọn Playwright?

**Chọn Selenium khi:**
- Team đã có framework Selenium lớn, không muốn migrate
- Cần test trên Safari thật (không phải WebKit)
- Cần tích hợp với Appium (mobile)
- Team quen Selenium, không có thời gian học mới
- Dự án legacy, nhiều test đang chạy

**Chọn Playwright khi:**
- Dự án mới, chưa có framework
- Muốn giảm flaky test
- Team biết JavaScript/TypeScript
- Cần tốc độ nhanh hơn
- Cần network interception, mock API

**Thực tế 2025-2026:**
- Nhiều công ty mới chọn Playwright
- Công ty cũ vẫn dùng Selenium
- Biết cả 2 = lợi thế lớn khi phỏng vấn

---

## 7. Migrate từ Selenium sang Playwright

```java
// Selenium
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement emailInput = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.id("email"))
);
emailInput.clear();
emailInput.sendKeys("test@example.com");
driver.findElement(By.cssSelector(".btn-login")).click();
assertEquals("Dashboard", driver.getTitle());

// Playwright — tương đương, ngắn hơn nhiều
page.getByLabel("Email").fill("test@example.com");
page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
assertThat(page).hasTitle("Dashboard");
```

---

## Câu hỏi phỏng vấn

**Q1: Playwright khác Selenium thế nào? Ưu điểm chính là gì?**
```
Khác biệt chính:
1. Auto-waiting: Playwright tự chờ element ready, Selenium phải tự viết wait
2. Architecture: Playwright kết nối trực tiếp qua CDP, nhanh hơn
3. Locators: Playwright có getByRole, getByLabel — semantic hơn
4. Assertions: Playwright assertions tự retry, ít flaky hơn

Ưu điểm: ít flaky, nhanh hơn, ít boilerplate code

Gợi nhớ: Playwright = Selenium nhưng thông minh hơn về timing
```

**Q2: Auto-waiting trong Playwright hoạt động thế nào?**
```
Trước khi thực hiện action (click, fill...), Playwright tự kiểm tra:
1. Element tồn tại trong DOM
2. Element visible
3. Element enabled
4. Element stable (không đang animate)
5. Element không bị che

Nếu chưa thỏa → retry đến timeout (mặc định 30s)
→ Không cần viết WebDriverWait thủ công

Gợi nhớ: Auto-waiting = Playwright tự kiên nhẫn chờ, không cần nhắc
```

**Q3: Khi nào nên chọn Playwright thay vì Selenium?**
```
Chọn Playwright khi:
- Dự án mới, chưa có framework
- Muốn giảm flaky test
- Team biết JS/TS
- Cần network mock, API interception

Chọn Selenium khi:
- Đã có framework lớn, không muốn migrate
- Cần Safari thật
- Cần Appium integration
- Team quen Selenium

Gợi nhớ: Playwright = dự án mới, Selenium = dự án cũ đang chạy
```

---

**Tiếp theo:** [02-playwright-basics.md](./02-playwright-basics.md) | [Quay lại README](./README.md)
