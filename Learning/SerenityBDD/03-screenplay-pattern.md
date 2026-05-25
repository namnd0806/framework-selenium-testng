# Screenplay Pattern — Nâng cao hơn Page Object Model

---

## 1. Screenplay là gì?

Screenplay Pattern = cách tổ chức test theo góc nhìn của **người dùng (Actor)**, không phải theo trang (Page).

```
Page Object Model:          Screenplay Pattern:
─────────────────           ──────────────────────────────
LoginPage.login()           Actor James thực hiện Login
CheckoutPage.checkout()     Actor James thực hiện Checkout
                            Actor James kiểm tra OrderConfirmed
```

**4 thành phần chính:**

```
Actor    — người thực hiện hành động ("James", "Admin User")
Task     — hành động cấp cao ("Login", "AddToCart", "Checkout")
Action   — hành động cấp thấp ("Click", "Enter", "Select")
Question — câu hỏi về trạng thái ("Is the dashboard displayed?")
```

---

## 2. So sánh POM vs Screenplay

```java
// Page Object Model
LoginPage loginPage = new LoginPage(driver);
loginPage.enterEmail("james@test.com");
loginPage.enterPassword("Pass@123");
loginPage.clickLogin();
DashboardPage dashboard = new DashboardPage(driver);
assertTrue(dashboard.isDisplayed());

// Screenplay Pattern — đọc như ngôn ngữ tự nhiên
Actor james = Actor.named("James");
james.attemptsTo(
    Login.withCredentials("james@test.com", "Pass@123")
);
assertThat(james.asksAbout(CurrentPage.title()))
    .isEqualTo("Dashboard");
```

---

## 3. Setup Actor

```java
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.abilities.BrowseTheWeb;

// Actor với ability BrowseTheWeb
Actor james = Actor.named("James")
    .whoCan(BrowseTheWeb.with(driver));

// Hoặc trong Cucumber step defs
public class LoginStepDefs {

    @Actor("James")
    Actor james;  // Serenity inject actor tự động

    @Given("James is on the login page")
    public void jamesIsOnLoginPage() {
        james.attemptsTo(
            Open.url("https://staging.example.com/login")
        );
    }
}
```

---

## 4. Task — Hành động cấp cao

```java
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.serenitybdd.screenplay.actions.Click;
import net.serenitybdd.screenplay.actions.Enter;

// Task = tập hợp các Action
public class Login implements Task {

    private final String email;
    private final String password;

    private Login(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Factory method — đọc như tiếng Anh
    public static Login withCredentials(String email, String password) {
        return Tasks.instrumented(Login.class, email, password);
    }

    @Override
    @Step("{0} logs in as #email")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
            Enter.theValue(email).into(LoginPage.EMAIL_FIELD),
            Enter.theValue(password).into(LoginPage.PASSWORD_FIELD),
            Click.on(LoginPage.LOGIN_BUTTON)
        );
    }
}

// Dùng trong test
james.attemptsTo(
    Login.withCredentials("james@test.com", "Pass@123")
);
```

---

## 5. Action — Hành động cấp thấp

```java
// Serenity có sẵn nhiều Action:
import net.serenitybdd.screenplay.actions.*;

// Click
Click.on(By.id("submit"))
Click.on(LoginPage.LOGIN_BUTTON)

// Enter text
Enter.theValue("hello").into(By.id("search"))
Enter.theValue(email).into(LoginPage.EMAIL_FIELD)

// Select dropdown
SelectFromOptions.byVisibleText("Vietnam").from(By.id("country"))

// Scroll
Scroll.to(By.id("footer"))

// Wait
WaitUntil.the(LoginPage.ERROR_MESSAGE, isVisible())

// Navigate
Open.url("https://example.com")
Open.browserOn().the(LoginPage.class)  // Dùng @DefaultUrl
```

---

## 6. Question — Kiểm tra trạng thái

```java
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.questions.Text;
import net.serenitybdd.screenplay.questions.Visibility;

// Question có sẵn
Text.of(LoginPage.ERROR_MESSAGE)           // Lấy text
Visibility.of(LoginPage.DASHBOARD)         // Kiểm tra visible
Value.of(LoginPage.EMAIL_FIELD)            // Lấy value của input

// Custom Question
public class WelcomeMessage implements Question<String> {

    public static WelcomeMessage displayed() {
        return new WelcomeMessage();
    }

    @Override
    public String answeredBy(Actor actor) {
        return Text.of(DashboardPage.WELCOME_MESSAGE).answeredBy(actor);
    }
}

// Dùng trong assertion
assertThat(james.asksAbout(WelcomeMessage.displayed()))
    .isEqualTo("Hello, James!");

assertThat(james.asksAbout(Text.of(LoginPage.ERROR_MESSAGE)))
    .contains("Invalid credentials");

assertThat(james.asksAbout(Visibility.of(DashboardPage.MAIN_CONTENT)))
    .isTrue();
```

---

## 7. Target — Định nghĩa locator

```java
import net.serenitybdd.screenplay.targets.Target;

// Định nghĩa locators trong Page class
public class LoginPage {

    // Target = locator trong Screenplay
    public static final Target EMAIL_FIELD =
        Target.the("email input").locatedBy("#email");

    public static final Target PASSWORD_FIELD =
        Target.the("password input").locatedBy("#password");

    public static final Target LOGIN_BUTTON =
        Target.the("login button").locatedBy(".btn-login");

    public static final Target ERROR_MESSAGE =
        Target.the("error message").locatedBy(".error-msg");

    // Dynamic target với parameter
    public static Target productNamed(String name) {
        return Target.the("product " + name)
            .locatedBy("//div[@class='product'][.//h3[text()='" + name + "']]");
    }
}

// Dùng
james.attemptsTo(
    Click.on(LoginPage.productNamed("iPhone 15"))
);
```

---

## 8. Ví dụ đầy đủ — Cucumber + Screenplay

```java
// Feature file
/*
Scenario: James logs in successfully
  Given James is a registered user
  When James logs in with valid credentials
  Then James should see the dashboard
*/

// Step Definitions
public class LoginStepDefs {

    @Actor("James")
    Actor james;

    @Given("James is a registered user")
    public void jamesIsRegistered() {
        // Setup via API
        james.attemptsTo(
            CreateUserViaApi.withEmail("james@test.com").andPassword("Pass@123")
        );
    }

    @When("James logs in with valid credentials")
    public void jamesLogsIn() {
        james.attemptsTo(
            Login.withCredentials("james@test.com", "Pass@123")
        );
    }

    @Then("James should see the dashboard")
    public void jamesSeesDashboard() {
        james.should(
            seeThat(Visibility.of(DashboardPage.MAIN_CONTENT), is(true)),
            seeThat(WelcomeMessage.displayed(), containsString("James"))
        );
    }
}
```

---

## 9. POM vs Screenplay — Khi nào dùng cái nào?

| | Page Object Model | Screenplay |
|---|---|---|
| Độ phức tạp | Đơn giản hơn | Phức tạp hơn |
| Học | Dễ học | Cần thời gian |
| Đọc code | Tốt | Rất tốt (như tiếng Anh) |
| Tái sử dụng | Tốt | Rất tốt |
| Multi-actor | Khó | Dễ (nhiều Actor) |
| Dùng khi | Hầu hết dự án | Dự án lớn, nhiều actor |

**Gợi nhớ:** POM = tổ chức theo trang, Screenplay = tổ chức theo người dùng

---

## Câu hỏi phỏng vấn

**Q1: Screenplay Pattern là gì? Khác POM thế nào?**
```
Screenplay = tổ chức test theo Actor (người dùng), không phải Page
4 thành phần: Actor, Task, Action, Question

Khác POM:
- POM: LoginPage.login() — tập trung vào trang
- Screenplay: james.attemptsTo(Login.as("james")) — tập trung vào người dùng
- Screenplay dễ đọc hơn, tái sử dụng tốt hơn, hỗ trợ multi-actor

Gợi nhớ: POM = trang web là trung tâm, Screenplay = người dùng là trung tâm
```

**Q2: Task vs Action trong Screenplay khác nhau thế nào?**
```
Action = hành động cấp thấp, atomic (Click, Enter, Select)
Task = tập hợp Actions, cấp cao hơn (Login = Enter email + Enter password + Click)

Ví dụ:
Action: Click.on(LoginPage.LOGIN_BUTTON)
Task: Login.withCredentials(email, password)
      → Enter email + Enter password + Click login

Gợi nhớ: Action = bước nhỏ, Task = nhiều bước nhỏ gộp lại
```

**Q3: Khi nào nên dùng Screenplay thay vì POM?**
```
Dùng Screenplay khi:
- Dự án lớn, nhiều tester cùng làm
- Cần test với nhiều actor (admin, user, guest)
- Muốn code đọc như ngôn ngữ tự nhiên
- Team đã quen với Serenity

Dùng POM khi:
- Dự án nhỏ/vừa
- Team mới, cần học nhanh
- Không dùng Serenity

Gợi nhớ: Screenplay = enterprise, POM = đủ dùng cho hầu hết dự án
```

---

**Quay lại:** [README](./README.md)
