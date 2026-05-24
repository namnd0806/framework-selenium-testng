# Phần 5 — AI cho Automation Tester & SDET

> Phần này dành riêng cho người viết automation test và muốn phát triển thành SDET.

---

## 5.1 AI viết Automation Script

### Viết từ đầu với context đầy đủ

Thay vì hỏi AI viết từng hàm nhỏ, hãy cho AI đủ context để viết đúng ngay lần đầu:

```
Prompt:
Project: E-commerce automation framework
Tech: Selenium 4 + Java 17 + TestNG + Maven + Page Object Model
BasePage có sẵn: [paste BasePage.java]

Viết LoginPage.java cho trang login với:
- URL: /login
- Elements: email input (#email), password input (#password),
  login button (.btn-login), error message (.error-msg)
- Methods: login(email, password), getErrorMessage(), isLoggedIn()
- Dùng WebDriverWait, không dùng Thread.sleep()
- Extend BasePage
```

### Viết test từ manual test case

```
Prompt:
Chuyển manual test case dưới đây thành Selenium TestNG test:

TC001 - Login thành công
Precondition: User test@example.com / Test@123 đã tồn tại
Steps:
1. Mở https://staging.example.com/login
2. Nhập email: test@example.com
3. Nhập password: Test@123
4. Click Login
Expected: Redirect về /dashboard, hiển thị "Welcome, Test User"

TC002 - Login với password sai
Steps: [...]
Expected: Hiển thị "Invalid credentials"

Yêu cầu:
- Dùng @DataProvider cho multiple test data
- Dùng Allure @Step annotation
- Dùng LoginPage đã có sẵn: [paste LoginPage.java]
```

### Debug automation script

```
Prompt:
Test Selenium bị fail với lỗi sau:
org.openqa.selenium.StaleElementReferenceException:
stale element reference: element is not attached to the page document

Stack trace: [paste]
Code đang dùng: [paste]
Trang web: có AJAX reload sau khi click

Giải thích tại sao lỗi xảy ra và fix với code cụ thể.
```

---

## 5.2 AI tự Heal Selector khi UI thay đổi

### Vấn đề phổ biến

UI thay đổi → selector cũ không còn đúng → test fail hàng loạt.

```java
// Selector cũ (bị break sau khi dev đổi UI):
By.xpath("//div[@class='old-login-form']//input[@type='email']")

// Cần tìm selector mới
```

### Dùng AI tìm selector tốt hơn

```
Prompt:
Đây là HTML của form login sau khi UI được cập nhật:
[paste HTML]

Tìm selector tốt nhất cho:
1. Email input
2. Password input
3. Login button
4. Error message

Ưu tiên: id > data-testid > cssSelector > xpath
Giải thích tại sao chọn selector đó (stability, readability).
```

### Best practice: data-testid

```
Prompt:
Đây là React component LoginForm:
[paste component code]

Đề xuất thêm data-testid attribute vào các element quan trọng
để automation test ổn định hơn khi UI thay đổi.
```

### Tool AI tự heal selector

- **Testim** — AI tự nhận diện element dù selector thay đổi
- **Healenium** — open source, tích hợp với Selenium, tự heal khi element không tìm thấy
- **Applitools** — AI visual testing, không phụ thuộc selector

---

## 5.3 AI review & Refactor Test Code

### Review code quality

```
Prompt:
Bạn là senior SDET với 10 năm kinh nghiệm.
Review test code dưới đây và chỉ ra:
1. Anti-patterns (test dependency, hardcoded data, magic numbers...)
2. Vấn đề về stability (flaky test risks)
3. Vấn đề về maintainability
4. Missing test coverage
5. Đề xuất refactor cụ thể

[paste test code]
```

### Refactor từ bad code sang POM

```
Prompt:
Đây là test code cũ, viết trực tiếp không theo pattern:
[paste code cũ]

Refactor sang Page Object Model:
- Tách element locators và actions vào Page class
- Test class chỉ chứa test logic
- Dùng @FindBy annotation
- Giữ nguyên test coverage, không bỏ sót case nào
```

### Tìm duplicate và consolidate

```
Prompt:
Đây là 3 test file trong project:
[paste LoginTest.java, CheckoutTest.java, ProfileTest.java]

Tìm:
1. Code duplicate giữa các file
2. Helper method nên extract vào BasePage hoặc utility class
3. Test data nên centralize ở đâu
Đề xuất refactor plan cụ thể.
```

---

## 5.4 AI trong Framework Design

### Thiết kế framework từ đầu

```
Prompt:
Tôi cần thiết kế automation framework cho:
- Web app: React frontend, REST API backend
- Mobile: Android + iOS (React Native)
- Tech team: 3 automation engineers, Java background
- CI/CD: Jenkins + GitHub
- Reporting: cần dashboard đẹp cho management

Đề xuất:
1. Framework architecture phù hợp
2. Tech stack (test framework, libraries, tools)
3. Folder structure
4. Design patterns nên dùng
5. Những gì nên/không nên tự build vs dùng tool có sẵn
```

### Review framework hiện tại

```
Prompt:
Đây là folder structure của automation framework hiện tại:
[paste cấu trúc thư mục]

Đây là BasePage.java:
[paste code]

Đây là một test file mẫu:
[paste code]

Đánh giá:
1. Điểm mạnh của framework hiện tại
2. Điểm yếu và technical debt
3. Đề xuất cải thiện theo thứ tự ưu tiên
4. Những gì nên làm ngay vs để sau
```

### BDD với Cucumber

```
Prompt:
Chuyển test case dưới đây sang Cucumber BDD format:
[paste test case]

Yêu cầu:
- Viết feature file (.feature) với Gherkin syntax
- Viết Step Definitions class (Java)
- Dùng scenario outline cho data-driven cases
- Tích hợp với TestNG runner
```

---

## 5.5 AI tích hợp CI/CD Pipeline

### GitHub Actions

```
Prompt:
Viết GitHub Actions workflow để:
1. Trigger khi có PR vào branch main
2. Chạy Selenium test trên Chrome headless
3. Generate Allure report
4. Comment kết quả vào PR
5. Fail PR nếu có test fail

Tech: Maven + TestNG + Allure
```

AI sẽ tạo file `.github/workflows/test.yml` hoàn chỉnh.

### Jenkins Pipeline

```
Prompt:
Viết Jenkinsfile (declarative pipeline) để:
1. Checkout code
2. Build Maven project
3. Chạy smoke test trước, nếu pass thì chạy full regression
4. Publish Allure report
5. Gửi Slack notification khi fail
6. Archive test artifacts

Môi trường: Jenkins + Maven + Allure + Slack plugin
```

### Docker cho test environment

```
Prompt:
Viết Dockerfile và docker-compose.yml để:
- Chạy Selenium test trong container
- Dùng Selenium Grid với Chrome và Firefox
- Không cần cài browser trên máy CI

Tech: Selenium 4 Grid + Docker
```

---

## 5.6 Lộ trình dùng AI để trở thành SDET chuyên nghiệp

### SDET là gì?

**SDET = Software Development Engineer in Test**
- Không chỉ viết test — còn build framework, tool, infrastructure cho testing
- Biết code như developer, hiểu testing như tester
- Tham gia từ sớm trong SDLC, không chỉ giai đoạn testing

### Kỹ năng SDET cần có

```
Technical Skills:
├── Programming: Java/Python/JavaScript (chọn 1 thành thạo)
├── Test frameworks: Selenium, Playwright, Cypress, Appium
├── API testing: RestAssured, Postman, Karate
├── Performance: JMeter, Gatling, k6
├── CI/CD: Jenkins, GitHub Actions, GitLab CI
├── Containerization: Docker, Kubernetes cơ bản
├── Version control: Git thành thạo
└── Cloud: AWS/Azure/GCP cơ bản

Soft Skills:
├── Đọc hiểu requirement, tìm edge case
├── Giao tiếp với dev, BA, PO
├── Tư duy phân tích, problem solving
└── Chủ động, không chờ bug mới test
```

### Dùng AI để học nhanh hơn

**Học concept mới:**
```
Prompt:
Giải thích [concept] cho người đã biết Selenium Java.
Cho ví dụ thực tế, so sánh với cách làm cũ.
Sau đó cho bài tập nhỏ để tôi thực hành.
```

**Review code khi học:**
```
Prompt:
Tôi mới học [Playwright/RestAssured/...].
Đây là code tôi viết: [paste code]
Review và chỉ ra:
1. Tôi hiểu đúng chưa
2. Có cách viết tốt hơn không
3. Tôi đang bỏ sót best practice nào
```

**Chuẩn bị phỏng vấn:**
```
Prompt:
Tôi đang chuẩn bị phỏng vấn SDET position tại công ty product.
Hỏi tôi 10 câu hỏi kỹ thuật về [Selenium/TestNG/CI-CD/...],
sau đó đánh giá câu trả lời của tôi và chỉ ra điểm cần cải thiện.
```

### Lộ trình 6 tháng với AI

```
Tháng 1-2: Automation Foundation
├── Selenium + Java thành thạo
├── TestNG, Maven, POM pattern
├── AI hỗ trợ: viết code, debug, review
└── Mục tiêu: tự build framework cơ bản

Tháng 3: API Testing
├── RestAssured hoặc Karate
├── Postman → automation
├── AI hỗ trợ: viết test từ Swagger spec
└── Mục tiêu: test được REST API đầy đủ

Tháng 4: CI/CD Integration
├── Git workflow, branching strategy
├── GitHub Actions hoặc Jenkins
├── Docker cơ bản
├── AI hỗ trợ: viết pipeline, Dockerfile
└── Mục tiêu: test chạy tự động trên CI

Tháng 5: Advanced Topics
├── Performance testing (JMeter/k6)
├── Mobile testing (Appium) hoặc BDD (Cucumber)
├── AI hỗ trợ: thiết kế framework, review architecture
└── Mục tiêu: có thể tư vấn framework cho team

Tháng 6: Portfolio & Interview Prep
├── Build 1 framework hoàn chỉnh trên GitHub
├── Viết README, documentation
├── AI hỗ trợ: review portfolio, mock interview
└── Mục tiêu: sẵn sàng apply SDET position
```

### AI làm thay đổi SDET như thế nào?

**Trước AI (2022):**
- Viết boilerplate code mất nhiều thời gian
- Debug mất hàng giờ tìm nguyên nhân
- Học framework mới mất vài tuần

**Với AI (2025):**
- Boilerplate code: AI viết trong vài giây
- Debug: AI phân tích stack trace, gợi ý nguyên nhân ngay
- Học framework mới: AI giải thích, cho ví dụ, review code → nhanh gấp 3-5 lần

**Kỹ năng SDET quan trọng hơn bao giờ hết:**
- Biết đánh giá code AI viết có đúng không
- Biết đặt câu hỏi đúng (prompt engineering)
- Hiểu architecture để AI không đi sai hướng
- Tư duy testing — AI không thay thế được

---

**Tiếp theo:** [Phần 6 — AI Nâng cao](./06-ai-nang-cao.md)
