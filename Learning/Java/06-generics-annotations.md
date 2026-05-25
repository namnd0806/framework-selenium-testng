# Generics & Annotations — Kiểu an toàn và metadata

> Generics giúp code type-safe, tái sử dụng được.
> Annotations là nền tảng của TestNG, Selenium PageFactory, Allure Report.

---

## 1. Generics

### Tại sao cần Generics?

```java
// Không có Generics — unsafe
List list = new ArrayList();
list.add("hello");
list.add(123);
String s = (String) list.get(1); // ClassCastException lúc runtime!

// Có Generics — safe, lỗi phát hiện lúc compile
List<String> list = new ArrayList<>();
list.add("hello");
list.add(123);  // Compile error ngay! Không thể add Integer vào List<String>
String s = list.get(0); // Không cần cast
```

### Generic Class

```java
// Generic class — T là type parameter
public class TestDataHolder<T> {
    private T data;
    private String description;

    public TestDataHolder(T data, String description) {
        this.data = data;
        this.description = description;
    }

    public T getData() { return data; }
    public String getDescription() { return description; }
}

// Dùng với nhiều kiểu khác nhau
TestDataHolder<String> emailHolder = new TestDataHolder<>("test@gmail.com", "Valid email");
TestDataHolder<Integer> ageHolder = new TestDataHolder<>(25, "Valid age");
TestDataHolder<Map<String, String>> userHolder = new TestDataHolder<>(
    Map.of("email", "test@gmail.com", "password", "Pass@123"),
    "Valid user credentials"
);
```

### Generic Method

```java
public class PageFactory {
    // Generic method — T extends BasePage
    public static <T extends BasePage> T createPage(Class<T> pageClass, WebDriver driver) {
        try {
            return pageClass.getDeclaredConstructor(WebDriver.class).newInstance(driver);
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo page: " + pageClass.getName(), e);
        }
    }
}

// Dùng — type-safe, không cần cast
LoginPage loginPage = PageFactory.createPage(LoginPage.class, driver);
DashboardPage dashboard = PageFactory.createPage(DashboardPage.class, driver);
```

### Wildcard — ? (dấu hỏi)

```java
// ? extends T — upper bounded wildcard (T hoặc subclass của T)
public void printPageTitles(List<? extends BasePage> pages) {
    for (BasePage page : pages) {
        System.out.println(page.getPageTitle());
    }
}
// Chấp nhận: List<LoginPage>, List<DashboardPage>... (đều extends BasePage)

// ? super T — lower bounded wildcard
public void addPages(List<? super LoginPage> list) {
    list.add(new LoginPage(driver));
}

// ? — unbounded wildcard (bất kỳ type nào)
public void printSize(List<?> list) {
    System.out.println("Size: " + list.size());
}
```

---

## 2. Annotations

### Annotation là gì?

Annotation = metadata gắn vào code, cung cấp thông tin cho compiler, framework, tool.

```java
// Annotation không thay đổi logic code
// Nhưng framework (TestNG, Selenium, Allure) đọc annotation để biết phải làm gì

@Test                    // TestNG biết đây là test method
@BeforeMethod            // TestNG chạy trước mỗi @Test
@FindBy(id = "email")    // Selenium PageFactory tìm element
@Step("Click login")     // Allure ghi lại bước này trong report
```

### Các Annotation hay dùng trong automation

**TestNG Annotations:**
```java
@Test(
    description = "Verify login với valid credentials",
    groups = {"smoke", "regression"},
    priority = 1,
    enabled = true,
    timeOut = 5000,
    dataProvider = "loginData"
)
public void testLogin(String email, String password) { }

@BeforeSuite  // Chạy 1 lần trước toàn bộ suite
@AfterSuite   // Chạy 1 lần sau toàn bộ suite
@BeforeClass  // Chạy 1 lần trước tất cả test trong class
@AfterClass   // Chạy 1 lần sau tất cả test trong class
@BeforeMethod // Chạy trước mỗi @Test method
@AfterMethod  // Chạy sau mỗi @Test method

@DataProvider(name = "loginData", parallel = true)
public Object[][] getLoginData() { }
```

**Selenium PageFactory:**
```java
public class LoginPage {
    @FindBy(id = "email")
    private WebElement emailInput;

    @FindBy(css = ".btn-login")
    private WebElement loginButton;

    @FindBys({
        @FindBy(css = ".form"),
        @FindBy(id = "email")
    })
    private WebElement emailInForm; // tìm email trong form

    @FindAll({
        @FindBy(id = "email"),
        @FindBy(name = "email")
    })
    private WebElement emailAny; // tìm bằng id HOẶC name
}
```

**Allure Annotations:**
```java
@Feature("Authentication")
@Story("Login")
@Severity(SeverityLevel.CRITICAL)
@Description("Verify user can login with valid credentials")
@Test
public void testLogin() {
    Allure.step("Open login page", () -> {
        driver.get(baseUrl + "/login");
    });

    Allure.step("Enter credentials", () -> {
        loginPage.enterEmail("test@gmail.com");
        loginPage.enterPassword("Pass@123");
    });

    Allure.step("Click login button", () -> {
        loginPage.clickLogin();
    });
}
```

### Tạo Custom Annotation

```java
// Định nghĩa annotation
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)  // annotation tồn tại lúc runtime
@Target(ElementType.METHOD)          // chỉ dùng cho method
public @interface TestInfo {
    String author() default "Unknown";
    String[] tags() default {};
    boolean automated() default true;
}

// Dùng annotation
@TestInfo(author = "Nam", tags = {"smoke", "login"})
@Test
public void testLogin() { }

// Đọc annotation bằng Reflection (trong Listener)
public class CustomListener implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        Method method = result.getMethod().getConstructorOrMethod().getMethod();
        if (method.isAnnotationPresent(TestInfo.class)) {
            TestInfo info = method.getAnnotation(TestInfo.class);
            System.out.println("Author: " + info.author());
            System.out.println("Tags: " + Arrays.toString(info.tags()));
        }
    }
}
```

---

## 3. Reflection cơ bản

Reflection = khả năng inspect và thao tác với class, method, field lúc runtime.

```java
// Lấy thông tin class
Class<?> clazz = LoginPage.class;
System.out.println(clazz.getName());        // "com.example.pages.LoginPage"
System.out.println(clazz.getSimpleName());  // "LoginPage"

// Lấy tất cả method
Method[] methods = clazz.getDeclaredMethods();
for (Method method : methods) {
    System.out.println(method.getName());
}

// Tạo object bằng Reflection (dùng trong PageFactory)
Constructor<?> constructor = clazz.getDeclaredConstructor(WebDriver.class);
LoginPage page = (LoginPage) constructor.newInstance(driver);

// Đọc annotation (dùng trong Listener, custom framework)
for (Method method : methods) {
    if (method.isAnnotationPresent(Test.class)) {
        Test testAnnotation = method.getAnnotation(Test.class);
        System.out.println("Test: " + method.getName() +
                           ", Priority: " + testAnnotation.priority());
    }
}
```

**Trong automation, Reflection dùng để:**
- TestNG tìm và chạy @Test methods
- PageFactory khởi tạo @FindBy elements
- Custom Listener đọc custom annotation
- Generic PageFactory tạo Page object

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: Generics là gì? Tại sao cần dùng?**

```
Generics = tham số hóa kiểu dữ liệu — viết code 1 lần, dùng với nhiều kiểu.

Lợi ích:
1. Type safety — lỗi phát hiện lúc compile, không phải runtime
2. Không cần cast — List<String> trả về String, không cần (String)
3. Tái sử dụng — viết 1 class/method, dùng với nhiều kiểu

Ví dụ: List<WebElement>, Map<String, String>, Optional<String>
```

**Q2: Annotation là gì? Cho ví dụ trong automation.**

```
Annotation = metadata gắn vào code, không thay đổi logic.
Framework đọc annotation để biết phải làm gì.

Ví dụ:
@Test → TestNG biết đây là test method, chạy nó
@FindBy(id="email") → PageFactory tìm element với id="email"
@BeforeMethod → TestNG chạy method này trước mỗi @Test
@Step("Click login") → Allure ghi bước này vào report
```

**Q3: @Retention và @Target trong custom annotation là gì?**

```
@Retention — annotation tồn tại đến khi nào:
- SOURCE: chỉ trong source code, bị bỏ khi compile
- CLASS: trong .class file, không có lúc runtime
- RUNTIME: tồn tại lúc runtime, đọc được bằng Reflection
→ Custom annotation trong framework: dùng RUNTIME

@Target — annotation dùng ở đâu:
- METHOD: chỉ cho method
- TYPE: cho class/interface
- FIELD: cho field
- PARAMETER: cho parameter
```

**Q4: Reflection là gì? Dùng trong automation thế nào?**

```
Reflection = inspect và thao tác với class/method/field lúc runtime.

Trong automation:
- TestNG dùng Reflection để tìm @Test methods và chạy
- PageFactory dùng Reflection để khởi tạo @FindBy elements
- Custom Listener dùng Reflection để đọc custom annotation
- Generic PageFactory tạo Page object: clazz.getDeclaredConstructor(WebDriver.class).newInstance(driver)

Lưu ý: Reflection chậm hơn code thông thường, dùng khi thực sự cần
```

---

**Tiếp theo:** [07 — Multithreading & ThreadLocal](./07-multithreading.md)
