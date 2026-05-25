# Assertions - TestNG, SoftAssert, AssertJ, Hamcrest

## 1. TestNG Assert (Hard Assert)

```java
import org.testng.Assert;

@Test
public void testHardAssert() {
    // assertEquals(actual, expected, message)
    Assert.assertEquals(page.getTitle(), "Dashboard", "Page title wrong");
    Assert.assertEquals(cart.getItemCount(), 3);

    // assertTrue / assertFalse
    Assert.assertTrue(button.isDisplayed(), "Button not visible");
    Assert.assertFalse(errorMsg.isDisplayed(), "Error should not show");

    // assertNull / assertNotNull
    Assert.assertNotNull(driver.findElement(By.id("logo")));
    Assert.assertNull(page.getErrorMessage());

    // assertSame - so sánh reference (==), không phải value
    Assert.assertSame(DriverManager.getDriver(), driver);

    // assertThrows - kiểm tra exception
    Assert.assertThrows(NoSuchElementException.class, () -> {
        driver.findElement(By.id("nonexistent"));
    });
}
```

**Vấn đề:** Dừng ngay khi assertion đầu tiên fail → không biết các assertion sau có pass không.

---

## 2. SoftAssert - Kiểm tra nhiều điều cùng lúc

```java
@Test
public void testCheckoutPage() {
    CheckoutPage page = new CheckoutPage(driver);
    SoftAssert soft = new SoftAssert();

    // Tất cả assertions đều chạy dù có fail
    soft.assertEquals(page.getOrderTotal(), "$99.99", "Total price wrong");
    soft.assertTrue(page.isPaymentSectionVisible(), "Payment section missing");
    soft.assertEquals(page.getShippingAddress(), "123 Main St", "Address wrong");
    soft.assertFalse(page.isPromoCodeApplied(), "Promo should not be applied");

    // PHẢI gọi assertAll() ở cuối
    soft.assertAll(); // Báo tất cả failures cùng lúc
}
```

**Khi nào dùng SoftAssert?**
- Validate nhiều field trên cùng 1 trang (form, profile page)
- Kiểm tra UI elements (title, buttons, labels)
- Không dùng khi các assertion có dependency (login trước rồi mới check dashboard)

---

## 3. AssertJ - Fluent API (Khuyến nghị dùng)

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
    <version>3.25.3</version>
    <scope>test</scope>
</dependency>
```

### String assertions
```java
import static org.assertj.core.api.Assertions.assertThat;

String pageTitle = driver.getTitle();
assertThat(pageTitle)
    .isNotNull()
    .isNotEmpty()
    .contains("Dashboard")
    .startsWith("My App")
    .endsWith("Dashboard")
    .matches(".*Dashboard.*")
    .doesNotContain("Error")
    .hasSize(20);
```

### List / Collection assertions
```java
List<String> productNames = page.getProductNames();
assertThat(productNames)
    .isNotEmpty()
    .hasSize(5)
    .contains("iPhone 15", "Samsung S24")
    .doesNotContain("Nokia 3310")
    .containsExactly("A", "B", "C")          // exact order
    .containsExactlyInAnyOrder("C", "A", "B") // any order
    .allMatch(name -> name.length() > 0)
    .noneMatch(name -> name.startsWith("Test"));
```

### Number assertions
```java
int itemCount = cart.getItemCount();
assertThat(itemCount)
    .isGreaterThan(0)
    .isLessThanOrEqualTo(10)
    .isBetween(1, 10)
    .isEqualTo(3);

double price = page.getPrice();
assertThat(price)
    .isPositive()
    .isGreaterThan(0.0)
    .isCloseTo(99.99, within(0.01)); // floating point comparison
```

### Object assertions
```java
User user = userService.getUser(1);
assertThat(user)
    .isNotNull()
    .isInstanceOf(User.class)
    .hasFieldOrPropertyWithValue("name", "John")
    .hasFieldOrPropertyWithValue("email", "john@test.com");

// Extracting fields
assertThat(user)
    .extracting("name", "email")
    .containsExactly("John", "john@test.com");
```

### Exception assertions
```java
assertThatThrownBy(() -> loginPage.login("", ""))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("Email cannot be empty");

// Hoặc
assertThatExceptionOfType(NoSuchElementException.class)
    .isThrownBy(() -> driver.findElement(By.id("ghost")));
```

---

## 4. Hamcrest - Dùng với REST Assured

```xml
<dependency>
    <groupId>org.hamcrest</groupId>
    <artifactId>hamcrest</artifactId>
    <version>2.2</version>
    <scope>test</scope>
</dependency>
```

```java
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

// Cú pháp: assertThat(actual, matcher)
assertThat(pageTitle, equalTo("Dashboard"));
assertThat(pageTitle, containsString("Dashboard"));
assertThat(pageTitle, startsWith("My App"));
assertThat(itemCount, greaterThan(0));
assertThat(itemCount, lessThanOrEqualTo(10));
assertThat(itemCount, both(greaterThan(0)).and(lessThan(100)));

// Collection
assertThat(productList, hasSize(5));
assertThat(productList, hasItem("iPhone 15"));
assertThat(productList, hasItems("iPhone 15", "Samsung S24"));
assertThat(productList, not(hasItem("Nokia 3310")));

// Null checks
assertThat(element, notNullValue());
assertThat(errorMsg, nullValue());

// REST Assured (Hamcrest tích hợp sẵn)
given().when().get("/api/users/1")
    .then()
    .body("name", equalTo("John"))
    .body("email", containsString("@test.com"))
    .body("roles", hasItem("ADMIN"))
    .body("orders", hasSize(greaterThan(0)));
```

---

## 5. So sánh 3 thư viện - Cùng 1 assertion

```java
String actual = "Hello World";

// TestNG Assert
Assert.assertEquals(actual, "Hello World");
Assert.assertTrue(actual.contains("World"));

// AssertJ
assertThat(actual).isEqualTo("Hello World");
assertThat(actual).contains("World");

// Hamcrest
assertThat(actual, equalTo("Hello World"));
assertThat(actual, containsString("World"));
```

| Tiêu chí | TestNG Assert | SoftAssert | AssertJ | Hamcrest |
|----------|--------------|------------|---------|---------|
| Dừng khi fail | ✅ Có | ❌ Không | ✅ Có | ✅ Có |
| Fluent API | ❌ | ❌ | ✅ Rất tốt | ⚠️ Trung bình |
| Error message | ⚠️ Cơ bản | ⚠️ Cơ bản | ✅ Chi tiết | ✅ Tốt |
| IDE autocomplete | ⚠️ | ⚠️ | ✅ Xuất sắc | ⚠️ |
| Dùng với REST Assured | ❌ | ❌ | ⚠️ Có thể | ✅ Native |
| Khuyến nghị | UI test đơn giản | Multi-field UI | UI/API test | REST Assured |

---

## 6. Câu hỏi phỏng vấn

**Q1: Khi nào dùng SoftAssert thay Hard Assert?**
> **Trả lời:** Dùng SoftAssert khi cần validate nhiều field độc lập trên cùng 1 trang (form validation, profile page). Dùng Hard Assert khi các bước có dependency — nếu login fail thì không cần check dashboard.
>
> **Gợi nhớ:** Soft = nhiều field độc lập, Hard = có dependency

**Q2: Tại sao AssertJ tốt hơn TestNG Assert?**
> **Trả lời:** AssertJ có fluent API (method chaining), error message chi tiết hơn, IDE autocomplete tốt hơn, và có nhiều built-in matchers cho String/List/Number/Object. Code dễ đọc hơn.
>
> **Gợi nhớ:** AssertJ = đọc như tiếng Anh: assertThat(name).contains("John")

**Q3: Hamcrest dùng ở đâu trong automation?**
> **Trả lời:** Chủ yếu dùng với REST Assured vì REST Assured tích hợp sẵn Hamcrest. Cú pháp .body("field", equalTo("value")) rất tự nhiên khi validate API response.
>
> **Gợi nhớ:** Hamcrest = người bạn thân của REST Assured

---

[Tiếp theo: ../Selenium/README.md](../Selenium/README.md) | [Quay lại README](./README.md)
