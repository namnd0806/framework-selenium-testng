# OOP — Lập trình hướng đối tượng

> OOP là nền tảng của Java. Trong automation framework, OOP xuất hiện ở khắp nơi:
> BasePage, PageFactory, WebDriver interface, custom exceptions...

---

## 1. Class & Object

**Class** = bản thiết kế. **Object** = sản phẩm tạo ra từ bản thiết kế đó.

```java
// Class = bản thiết kế cho một trang web
public class LoginPage {
    private WebDriver driver;
    private By emailInput = By.id("email");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    public void login(String email, String password) {
        driver.findElement(emailInput).sendKeys(email);
    }
}

// Object = tạo ra từ class đó
LoginPage loginPage = new LoginPage(driver);  // tạo object
loginPage.login("test@gmail.com", "123456");  // dùng object
```

---

## 2. Bốn tính chất OOP

### 2.1 Encapsulation — Đóng gói

**Ý nghĩa:** Giấu dữ liệu bên trong, chỉ cho phép truy cập qua method.

**Gợi nhớ:** Như ATM — bạn chỉ thấy màn hình và bàn phím, không thấy bên trong máy.

```java
public class LoginPage {
    // private = giấu đi, không ai truy cập trực tiếp
    private By emailInput = By.id("email");
    private By passwordInput = By.id("password");

    // public method = cửa ra vào được kiểm soát
    public void login(String email, String password) {
        driver.findElement(emailInput).sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
    }
    // Bên ngoài không biết locator là gì, chỉ biết gọi login()
}
```

**Tại sao quan trọng trong automation:**
- Thay đổi locator → chỉ sửa trong Page class, test không cần sửa
- Tránh test trực tiếp thao tác với element (vi phạm POM)

---

### 2.2 Inheritance — Kế thừa

**Ý nghĩa:** Class con kế thừa thuộc tính và method của class cha.

**Gợi nhớ:** Con thừa hưởng tài sản của cha — không cần làm lại từ đầu.

```java
// Class cha — chứa những gì mọi Page đều cần
public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    // Method dùng chung cho tất cả page
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).sendKeys(text);
    }
}

// Class con — kế thừa BasePage, không cần viết lại driver, wait, click, type
public class LoginPage extends BasePage {
    private By emailInput = By.id("email");
    private By loginButton = By.cssSelector(".btn-login");

    public LoginPage(WebDriver driver) {
        super(driver); // gọi constructor của BasePage
    }

    public void login(String email, String password) {
        type(emailInput, email);       // dùng method từ BasePage
        click(loginButton);            // dùng method từ BasePage
    }
}
```

**Tại sao quan trọng:** Viết `click()`, `type()`, `waitFor()` một lần trong BasePage → tất cả Page dùng được.

---

### 2.3 Polymorphism — Đa hình

**Ý nghĩa:** Cùng một method nhưng hành vi khác nhau tùy object.

**Gợi nhớ:** Nút "Play" — nhấn trên Spotify thì phát nhạc, nhấn trên YouTube thì phát video. Cùng hành động, khác kết quả.

**Có 2 loại:**

**Override (Runtime polymorphism)** — class con ghi đè method của class cha:
```java
public class BasePage {
    public String getPageTitle() {
        return driver.getTitle();
    }
}

public class LoginPage extends BasePage {
    @Override
    public String getPageTitle() {
        // Override để thêm logic riêng
        return driver.getTitle().replace(" - MyApp", "");
    }
}
```

**Overload (Compile-time polymorphism)** — cùng tên method, khác tham số:
```java
public class WaitHelper {
    // Overload: cùng tên waitForElement, khác tham số
    public WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElement(By locator, int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}

// Dùng:
waitHelper.waitForElement(By.id("email"));          // timeout mặc định
waitHelper.waitForElement(By.id("email"), 30);      // timeout tùy chỉnh
```

---

### 2.4 Abstraction — Trừu tượng hóa

**Ý nghĩa:** Ẩn đi chi tiết phức tạp, chỉ expose những gì cần thiết.

**Gợi nhớ:** Lái xe — bạn chỉ cần biết vô lăng, ga, phanh. Không cần biết động cơ hoạt động thế nào.

**Thực hiện qua Interface hoặc Abstract class:**

```java
// Interface = hợp đồng, định nghĩa "phải làm gì"
public interface Navigable {
    void navigateTo(String url);
    String getCurrentUrl();
    void goBack();
}

// Abstract class = template, định nghĩa "làm thế nào" một phần
public abstract class BasePage implements Navigable {
    protected WebDriver driver;

    // Đã implement sẵn
    @Override
    public void navigateTo(String url) {
        driver.get(url);
    }

    // Abstract method = bắt buộc class con phải implement
    public abstract boolean isPageLoaded();
}

// Class con phải implement isPageLoaded()
public class LoginPage extends BasePage {
    @Override
    public boolean isPageLoaded() {
        return driver.findElements(By.id("email")).size() > 0;
    }
}
```

---

## 3. Interface vs Abstract Class

Đây là câu hỏi phỏng vấn **rất hay gặp**.

| | Interface | Abstract Class |
|---|---|---|
| Từ khóa | `interface` | `abstract class` |
| Kế thừa | `implements`, nhiều interface | `extends`, chỉ 1 class |
| Method | Mặc định abstract (Java 8+ có default) | Có thể có cả abstract và concrete |
| Variable | Chỉ `public static final` | Mọi loại |
| Constructor | Không có | Có |

**Khi nào dùng Interface:**
- Định nghĩa "khả năng" (capability) — `Clickable`, `Scrollable`, `Navigable`
- Muốn 1 class implement nhiều "khả năng"
- Không có code dùng chung

**Khi nào dùng Abstract Class:**
- Có code dùng chung (driver, wait, common methods)
- Muốn bắt buộc class con implement một số method
- Có quan hệ "IS-A" rõ ràng (LoginPage IS-A BasePage)

**Ví dụ thực tế trong framework:**
```java
// Interface — định nghĩa khả năng
public interface Reportable {
    void attachScreenshot(String name);
    void logStep(String message);
}

// Abstract class — có code dùng chung
public abstract class BasePage {
    protected WebDriver driver;  // dùng chung

    public BasePage(WebDriver driver) {
        this.driver = driver;
    }

    protected void click(By locator) { ... }  // dùng chung

    public abstract String getPageUrl();  // bắt buộc override
}

// LoginPage kế thừa BasePage VÀ implement Reportable
public class LoginPage extends BasePage implements Reportable {
    ...
}
```

---

## 4. this vs super

```java
public class BasePage {
    protected WebDriver driver;

    public BasePage(WebDriver driver) {
        this.driver = driver;  // this.driver = field, driver = parameter
    }
}

public class LoginPage extends BasePage {
    private String pageUrl;

    public LoginPage(WebDriver driver, String pageUrl) {
        super(driver);          // gọi constructor BasePage
        this.pageUrl = pageUrl; // this = object hiện tại
    }

    public void goToPage() {
        super.navigateTo(this.pageUrl); // super = gọi method của class cha
    }
}
```

---

## 5. Access Modifiers

| Modifier | Trong class | Trong package | Class con | Bên ngoài |
|---|---|---|---|---|
| `private` | ✅ | ❌ | ❌ | ❌ |
| `(default)` | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

**Trong automation framework:**
```java
public class BasePage {
    private WebDriver driver;      // chỉ BasePage dùng
    protected WebDriverWait wait;  // BasePage + class con dùng
    public String pageTitle;       // ai cũng dùng được (thường tránh)
}
```

**Best practice:** Dùng `private` nhiều nhất có thể, `protected` khi class con cần, `public` chỉ cho API bên ngoài.

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: 4 tính chất OOP là gì? Cho ví dụ trong automation framework.**

```
Gợi nhớ: "APIE" — Abstraction, Polymorphism, Inheritance, Encapsulation

- Encapsulation: Locator trong Page class là private, test không truy cập trực tiếp
- Inheritance: LoginPage extends BasePage → dùng được click(), type(), wait
- Polymorphism: Override getPageTitle() ở từng Page khác nhau
- Abstraction: Interface WebDriver — test chỉ gọi driver.click(), không biết Chrome hay Firefox
```

**Q2: Interface vs Abstract class — khi nào dùng cái nào?**

```
Gợi nhớ: Interface = "CÓ THỂ LÀM GÌ", Abstract = "LÀ GÌ"

Interface khi:
- Định nghĩa capability: Clickable, Loggable, Reportable
- Muốn implement nhiều cái cùng lúc

Abstract class khi:
- Có code dùng chung (BasePage có driver, wait, click())
- Quan hệ IS-A: LoginPage IS-A BasePage
```

**Q3: Polymorphism là gì? Override vs Overload khác nhau thế nào?**

```
Override = class con thay đổi hành vi method của class cha (runtime)
  → @Override, cùng tên, cùng tham số, khác implementation

Overload = cùng tên method, khác tham số (compile-time)
  → waitForElement(By) vs waitForElement(By, int timeout)

Gợi nhớ:
- Override = "ghi đè" — con làm khác cha
- Overload = "quá tải" — cùng tên nhưng nhiều phiên bản
```

**Q4: `this` và `super` dùng để làm gì?**

```
this = tham chiếu đến object hiện tại
  → this.driver = field (phân biệt với parameter cùng tên)
  → this() = gọi constructor khác trong cùng class

super = tham chiếu đến class cha
  → super(driver) = gọi constructor của class cha
  → super.click() = gọi method của class cha
```

**Q5: Tại sao nên dùng `private` cho locator trong Page class?**

```
Encapsulation — giấu implementation detail:
1. Test không phụ thuộc vào locator cụ thể
2. Thay đổi locator → chỉ sửa Page class, test không đổi
3. Tránh test bypass Page class, thao tác trực tiếp với element
```

---

**Tiếp theo:** [02 — Collections](./02-collections.md)
