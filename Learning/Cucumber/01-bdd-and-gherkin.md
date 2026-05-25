# BDD và Gherkin

## 1. BDD là gì?

**Behavior Driven Development** — phát triển phần mềm dựa trên hành vi mong đợi.

**3 Amigos:**
- **BA (Business Analyst):** Mô tả business requirement
- **Developer:** Hiểu cần implement gì
- **Tester:** Biết cần test gì

Cả 3 cùng viết Gherkin scenarios → đảm bảo hiểu đúng requirement trước khi code.

**Lợi ích:**
- Living documentation: feature file = tài liệu luôn up-to-date
- Shared understanding: mọi người đọc được, không chỉ tester
- Collaboration: BA/Dev/Tester cùng tham gia từ đầu

---

## 2. Gherkin Syntax đầy đủ

```gherkin
# Feature: mô tả tính năng
Feature: User Authentication
  As a registered user
  I want to login to the application
  So that I can access my account

  # Background: chạy trước mỗi Scenario trong Feature
  Background:
    Given the application is running
    And the database has test users

  # Scenario: 1 test case cụ thể
  Scenario: Successful login with valid credentials
    Given I am on the login page
    When I enter email "user@test.com" and password "Pass@123"
    And I click the Login button
    Then I should be redirected to the dashboard
    And I should see the welcome message "Hello, John"

  # Scenario Outline: data-driven test
  Scenario Outline: Login with invalid credentials
    Given I am on the login page
    When I enter email "<email>" and password "<password>"
    And I click the Login button
    Then I should see error message "<error>"

    Examples:
      | email              | password    | error                    |
      | wrong@test.com     | Pass@123    | Invalid email or password |
      | user@test.com      | wrongpass   | Invalid email or password |
      |                    | Pass@123    | Email is required         |
      | user@test.com      |             | Password is required      |
```

---

## 3. Given/When/Then/And/But

```gherkin
# Given: Trạng thái ban đầu (precondition)
Given I am logged in as admin
Given the product "iPhone 15" exists in the catalog

# When: Hành động của user
When I click "Add to Cart"
When I submit the checkout form

# Then: Kết quả mong đợi (assertion)
Then the cart should contain 1 item
Then I should receive a confirmation email

# And: Tiếp tục Given/When/Then trước đó
Given I am on the checkout page
And my cart has 2 items
When I enter payment details
And I click "Place Order"
Then the order should be created
And I should see order number

# But: Phủ định (ít dùng)
Then the order should be created
But the payment should not be charged yet
```

**Best practices:**
- **Declarative** (mô tả WHAT): `When I login` ✅
- **Imperative** (mô tả HOW): `When I type "user" in field id="username" and click button` ❌
- Mỗi step nên ngắn gọn, 1 hành động
- Không đặt assertion trong Given/When

---

## 4. Tags

```gherkin
@smoke @regression
Feature: Shopping Cart

  @smoke
  Scenario: Add item to cart
    Given I am on the product page
    When I click "Add to Cart"
    Then the cart count should be 1

  @regression @payment
  Scenario: Complete checkout
    Given I have items in cart
    When I complete checkout
    Then order should be confirmed

  @wip
  Scenario: Apply discount code
    # Work in progress - chưa implement
    Given I have items in cart
    When I apply code "SAVE10"
    Then discount should be applied
```

```bash
# Chạy theo tag
mvn test -Dcucumber.filter.tags="@smoke"
mvn test -Dcucumber.filter.tags="@smoke and @regression"
mvn test -Dcucumber.filter.tags="@regression and not @wip"
mvn test -Dcucumber.filter.tags="@smoke or @sanity"
```

---

## 5. Data Tables

```gherkin
# Simple table - truyền nhiều data vào 1 step
Scenario: Create user with details
  Given I create a user with the following details:
    | Field    | Value           |
    | Name     | John Doe        |
    | Email    | john@test.com   |
    | Role     | Admin           |
    | Status   | Active          |

# List table - không có header
Scenario: Add multiple products to cart
  Given I add the following products to cart:
    | iPhone 15 Pro  |
    | AirPods Pro    |
    | MacBook Air    |

# Maps table - nhiều rows, mỗi row là 1 object
Scenario: Verify search results
  Then the search results should contain:
    | Name          | Price  | Category    |
    | iPhone 15     | $999   | Electronics |
    | iPhone 15 Pro | $1199  | Electronics |
```

```java
// Step definition cho Data Table
@Given("I create a user with the following details:")
public void createUserWithDetails(DataTable dataTable) {
    // Cách 1: Map<String, String>
    Map<String, String> userData = dataTable.asMap(String.class, String.class);
    String name  = userData.get("Name");
    String email = userData.get("Email");

    // Cách 2: List<Map<String, String>> (nhiều rows)
    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    for (Map<String, String> row : rows) {
        System.out.println(row.get("Name") + " - " + row.get("Price"));
    }
}
```

---

## 6. Doc Strings - Multiline text

```gherkin
Scenario: Send email with body
  When I send an email with body:
    """
    Dear John,

    Your order #12345 has been confirmed.
    Total: $99.99

    Thank you for shopping with us.
    """
  Then the email should be sent successfully
```

```java
@When("I send an email with body:")
public void sendEmailWithBody(String emailBody) {
    emailService.send(emailBody);
}
```

---

## 7. Viết Gherkin tốt

```gherkin
# ❌ Imperative - quá chi tiết về implementation
Scenario: Login
  Given I open Chrome browser
  And I navigate to "https://app.example.com/login"
  When I find element with id "email" and type "user@test.com"
  And I find element with id "password" and type "Pass@123"
  And I find button with text "Login" and click it
  Then I find element with id "dashboard" and verify it is visible

# ✅ Declarative - mô tả hành vi, không phải implementation
Scenario: Login with valid credentials
  Given I am on the login page
  When I login as "user@test.com" with password "Pass@123"
  Then I should be on the dashboard
```

---

## 8. Câu hỏi phỏng vấn

**Q1: BDD khác TDD như thế nào?**
> **Trả lời:** TDD (Test Driven Development) viết unit test trước khi code, tập trung vào developer. BDD viết behavior scenarios bằng ngôn ngữ tự nhiên (Gherkin), tập trung vào collaboration giữa BA/Dev/Tester và business value.
>
> **Gợi nhớ:** TDD = developer test code, BDD = team test behavior

**Q2: Scenario Outline dùng khi nào?**
> **Trả lời:** Khi cùng 1 flow nhưng với nhiều bộ data khác nhau (data-driven). Thay vì viết nhiều Scenario giống nhau, dùng Scenario Outline + Examples table.
>
> **Gợi nhớ:** Scenario Outline = template + bảng data = nhiều test case từ 1 template

**Q3: Background trong Cucumber dùng để làm gì?**
> **Trả lời:** Chứa các steps chạy trước MỖI Scenario trong Feature file. Tương tự @BeforeMethod trong TestNG. Dùng để setup precondition chung (navigate to page, login, setup data).
>
> **Gợi nhớ:** Background = @BeforeMethod của Cucumber, chạy trước mỗi scenario

**Q4: Tại sao nên viết Gherkin theo style declarative?**
> **Trả lời:** Declarative tập trung vào WHAT (hành vi), không phải HOW (implementation). Khi UI thay đổi, chỉ cần sửa step definition, không cần sửa feature file. Feature file trở thành tài liệu business thực sự.
>
> **Gợi nhớ:** Declarative = feature file sống lâu, imperative = feature file chết sớm khi UI đổi

---

[Tiếp theo: 02-step-definitions-and-hooks.md](./02-step-definitions-and-hooks.md) | [Quay lại README](./README.md)
