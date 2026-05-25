# Learning Roadmap — Lộ trình học tổng quan

> Trạng thái:
> - ✅ Có tài liệu
> - 🚧 Đang xây dựng
> - 📋 Chưa có

---

## Mục lục

- [Tổng quan: Anatomy của 1 Automation Framework](#tong-quan)
- [0. Java Core — Nền tảng bắt buộc](#0-java-core)
- [1. AI trong Coding](#1-ai-trong-coding)
- [2. Test Runner & Assertion](#2-test-runner--assertion)
- [3. Web Automation — Selenium](#3-web-automation--selenium)
- [4. Web Automation — Playwright](#4-web-automation--playwright)
- [5. API Testing — REST Assured](#5-api-testing--rest-assured)
- [6. Mobile Automation — Appium](#6-mobile-automation--appium)
- [7. BDD — Cucumber](#7-bdd--cucumber)
- [8. BDD + Reporting — Serenity BDD](#8-bdd--reporting--serenity-bdd)
- [9. Framework Infrastructure](#9-framework-infrastructure)
- [10. CI/CD & DevOps cho Tester](#10-cicd--devops-cho-tester)
- [11. English cho Tester/Dev](#11-english-cho-testerdev)
- [Lộ trình học theo giai đoạn](#lo-trinh)

---

## Tổng quan: Anatomy của 1 Automation Framework {#tong-quan}

> Trước khi học từng tool, cần hiểu **tại sao cần từng thành phần** và chúng kết hợp thế nào.

### Sơ đồ kiến trúc

```
╔══════════════════════════════════════════════════════════════╗
║               AUTOMATION FRAMEWORK                           ║
╠══════════════════════════════════════════════════════════════╣
║  TEST LAYER — Viết & tổ chức test                            ║
║  ┌─────────────────┐     ┌──────────────────────────────┐   ║
║  │  BDD Style      │     │  Plain Style                 │   ║
║  │  Cucumber       │     │  TestNG / JUnit 5            │   ║
║  │  Serenity BDD   │     │                              │   ║
║  └────────┬────────┘     └──────────────┬───────────────┘   ║
╠═══════════╪══════════════════════════════╪═══════════════════╣
║  INTERACTION LAYER — Tương tác với ứng dụng                  ║
║  ┌─────────────┐  ┌──────────────┐  ┌───────────────────┐   ║
║  │  Web        │  │  Mobile      │  │  API              │   ║
║  │  Selenium   │  │  Appium      │  │  REST Assured     │   ║
║  │  Playwright │  │              │  │  Playwright API   │   ║
║  └─────────────┘  └──────────────┘  └───────────────────┘   ║
╠══════════════════════════════════════════════════════════════╣
║  SUPPORT LAYER — Hỗ trợ vận hành                             ║
║  Config │ Test Data │ Logging │ Screenshot │ Retry Logic      ║
╠══════════════════════════════════════════════════════════════╣
║  REPORTING LAYER — Báo cáo kết quả                           ║
║  Allure Report │ ExtentReports │ Serenity Reports             ║
╚══════════════════════════════════════════════════════════════╝
         │                    │                    │
    Build Tool           Version Control        CI/CD
    Maven / Gradle       Git / GitHub           Jenkins / GitHub Actions
```

---

### Vai trò từng thành phần

**TEST LAYER — Viết và tổ chức test**

Đây là nơi bạn viết test case. Có 2 hướng tiếp cận:
- **Plain style (TestNG/JUnit):** Viết test bằng Java thuần, phù hợp với automation engineer
- **BDD style (Cucumber/Serenity):** Viết test bằng ngôn ngữ tự nhiên (Gherkin), phù hợp khi cần BA/PO đọc được test

**INTERACTION LAYER — Tương tác với ứng dụng**

Đây là nơi code thực sự "chạm" vào ứng dụng:
- **Web:** Selenium (mature, phổ biến) hoặc Playwright (mới, ít flaky hơn)
- **Mobile:** Appium — dùng WebDriver API cho Android/iOS
- **API:** REST Assured — test REST API trực tiếp, không qua UI

**SUPPORT LAYER — Hỗ trợ vận hành**

Các thành phần giúp framework chạy ổn định:
- **Config:** Quản lý URL, credentials theo môi trường (dev/staging/prod)
- **Test Data:** Đọc từ file CSV/JSON, sinh ngẫu nhiên bằng Faker
- **Logging:** Ghi log để debug khi test fail
- **Screenshot:** Chụp màn hình tự động khi fail
- **Retry:** Tự retry test flaky một số lần trước khi báo fail

**REPORTING LAYER — Báo cáo kết quả**

Hiển thị kết quả test cho team và stakeholder:
- **Allure:** Đẹp, tích hợp tốt với TestNG/Cucumber, phổ biến nhất
- **ExtentReports:** HTML report tùy chỉnh cao
- **Serenity Reports:** Living documentation, tự động từ feature files

**BUILD TOOL — Quản lý dependency và build**
- **Maven:** Phổ biến nhất trong Java ecosystem, dùng pom.xml
- **Gradle:** Linh hoạt hơn, cú pháp ngắn gọn hơn

**VERSION CONTROL — Quản lý code**
- **Git + GitHub/GitLab:** Bắt buộc, không thể thiếu

**CI/CD — Tự động hóa pipeline**
- **GitHub Actions:** Dễ setup, tích hợp tốt với GitHub
- **Jenkins:** Phổ biến trong enterprise, linh hoạt hơn

---

### So sánh nhanh các lựa chọn

| Thành phần | Lựa chọn A | Lựa chọn B | Khi nào chọn A | Khi nào chọn B |
|---|---|---|---|---|
| **Test Runner** | TestNG | JUnit 5 | Automation framework, cần parallel/groups | Unit test, Spring Boot |
| **Web Driver** | Selenium | Playwright | Team đã quen, project cũ | Project mới, muốn ít flaky |
| **API Testing** | REST Assured | Playwright API | API test phức tạp, cần JSONPath/Schema | Đã dùng Playwright, test đơn giản |
| **BDD** | Cucumber | Serenity BDD | Project nhỏ/vừa | Enterprise, cần report đẹp |
| **Assertion** | AssertJ | TestNG Assert | Error message rõ, fluent API | Đơn giản, đủ dùng |
| **Reporting** | Allure | ExtentReports | Tích hợp CI/CD, TestNG/Cucumber | Cần HTML report tùy chỉnh |
| **Build Tool** | Maven | Gradle | Hầu hết dự án Java | Cần build script linh hoạt |

> Chi tiết từng lựa chọn sẽ được giải thích trong file tài liệu tương ứng.

---

## 0. Java Core — Nền tảng bắt buộc ✅ {#0-java-core}

> Tài liệu chi tiết: [Learning/Java/](./Java/README.md)
> Không có Java vững thì học tool nào cũng bị hổng.

### Đánh giá kiến thức Java cho Automation Tester

Dưới đây là đánh giá đầy đủ những gì cần biết để vừa viết test tốt, vừa build framework chuyên nghiệp:

**Cơ bản — Phải biết để viết test:**
- [ ] OOP 4 tính chất: Encapsulation, Inheritance, Polymorphism, Abstraction
- [ ] Interface vs Abstract class — dùng nhiều trong POM pattern
- [ ] Collections: ArrayList, HashMap, HashSet — lưu element, test data, config
- [ ] Exception: try/catch/finally, throw/throws, checked vs unchecked
- [ ] String vs StringBuilder — xử lý text từ UI, build locator động
- [ ] File I/O — đọc file properties, CSV, JSON cho test data
- [ ] Generics — `List<WebElement>`, `Map<String, String>`

**Trung cấp — Cần để viết framework sạch:**
- [ ] Lambda & Stream API — xử lý list element gọn hơn
- [ ] Optional — tránh NullPointerException khi tìm element
- [ ] Static vs Instance — hiểu rõ để tránh bug trong parallel test
- [ ] Enum — quản lý browser type, environment, test status
- [ ] Annotations — tạo custom annotation cho framework
- [ ] Reflection — TestNG/Serenity dùng bên trong, cần hiểu để debug

**Nâng cao — Cần để build framework production-ready:**
- [ ] ThreadLocal — quản lý WebDriver trong parallel test (bắt buộc)
- [ ] Synchronized, volatile — thread safety khi ghi report, log
- [ ] Design Patterns: Singleton (ConfigManager), Factory (DriverFactory), Builder (TestData), Strategy (WaitStrategy)
- [ ] Dependency Injection concept — hiểu để dùng PicoContainer trong Cucumber, Spring trong Serenity

**Bổ sung quan trọng để build framework ngon:**
- [ ] Maven/Gradle — quản lý dependency, build lifecycle, profiles
- [ ] Logging (Log4j/SLF4J) — ghi log có cấu trúc, không dùng System.out.println
- [ ] Jackson/Gson — serialize/deserialize JSON (test data, API response)
- [ ] Apache POI — đọc Excel test data
- [ ] Java Faker — sinh test data ngẫu nhiên
- [ ] JDBC cơ bản — setup/teardown test data qua database

### Các file tài liệu

| File | Nội dung |
|------|----------|
| [01-oop.md](./Java/01-oop.md) | OOP — 4 tính chất, Interface vs Abstract, this/super |
| [02-collections.md](./Java/02-collections.md) | List, Map, Set — khi nào dùng cái nào |
| [03-exception-handling.md](./Java/03-exception-handling.md) | try/catch/finally, custom exception |
| [04-string-and-io.md](./Java/04-string-and-io.md) | String, StringBuilder, đọc file test data |
| [05-java8-features.md](./Java/05-java8-features.md) | Lambda, Stream, Optional |
| [06-generics-annotations.md](./Java/06-generics-annotations.md) | Generics, Annotations, Reflection |
| [07-multithreading.md](./Java/07-multithreading.md) | ThreadLocal — parallel Selenium |
| [08-design-patterns.md](./Java/08-design-patterns.md) | Singleton, Factory, Builder, Strategy, POM |

---

## 1. AI trong Coding ✅ {#1-ai-trong-coding}

> Tài liệu: [Learning/AI/README.md](./AI/README.md)

| Phần | Nội dung | Trạng thái |
|------|----------|------------|
| Phần 1 | Nền tảng AI — LLM, model, token, MCP | ✅ |
| Phần 2 | Prompt, steering, memory, agent cơ bản | ✅ |
| Phần 3 | ChatGPT, Claude, Kiro, Cursor, CLI | ✅ |
| Phần 4 | SDLC, STLC, ATLC, test case, bug report | ✅ |
| Phần 5 | Selenium, Playwright, RestAssured, CI/CD | ✅ |
| Phần 6 | Agent sâu, Multi-agent, Ollama local | ✅ |
| Phần 7 | Xu hướng & Tương lai | ✅ |

---

## 2. Test Runner & Assertion ✅ {#2-test-runner--assertion}

> Tài liệu: [Learning/TestRunner/](./TestRunner/README.md)

**Cơ bản:**
- [ ] Annotations: @Test, @BeforeMethod, @AfterMethod, @BeforeClass, @AfterClass, @BeforeSuite
- [ ] Test execution order, priority
- [ ] Groups — @Test(groups="smoke"), chạy subset test
- [ ] @DataProvider — data-driven testing
- [ ] testng.xml — cấu hình suite, parallel, include/exclude
- [ ] Assertions: assertEquals, assertTrue, assertNotNull, SoftAssert

**Trung cấp:**
- [ ] Parallel execution — methods, classes, tests, instances
- [ ] @Factory — tạo nhiều instance test với data khác nhau
- [ ] Listeners — ITestListener, ISuiteListener
- [ ] Retry Analyzer — tự retry khi test fail
- [ ] AssertJ — fluent assertion, error message rõ hơn
- [ ] Hamcrest — matcher syntax, dùng với REST Assured

**Nâng cao:**
- [ ] Custom Annotations + Reflection
- [ ] Parallel WebDriver management với ThreadLocal
- [ ] JUnit 5 — biết để so sánh, dùng trong Spring Boot

---

## 3. Web Automation — Selenium ✅ {#3-web-automation--selenium}

> Tài liệu: [Learning/Selenium/](./Selenium/README.md)

**Cơ bản:**
- [ ] WebDriver architecture — browser driver, W3C protocol
- [ ] Setup: WebDriverManager
- [ ] Locators: id, name, cssSelector, xpath — ưu nhược điểm
- [ ] Basic actions: click, sendKeys, getText, getAttribute
- [ ] Navigation, Browser options (headless, window size)

**Trung cấp:**
- [ ] Waits: implicit, explicit (WebDriverWait), fluent wait
- [ ] ExpectedConditions đầy đủ
- [ ] Actions class: hover, drag-drop, keyboard
- [ ] JavaScript Executor: scroll, click via JS
- [ ] Handle: Alert, iFrame, multiple windows, file upload
- [ ] Select class — dropdown
- [ ] Page Object Model (POM) — bắt buộc
- [ ] @FindBy, PageFactory

**Nâng cao:**
- [ ] Selenium Grid + Docker
- [ ] Custom WebDriver factory với ThreadLocal
- [ ] Flaky test prevention, StaleElement handling
- [ ] Shadow DOM, CDP (Chrome DevTools Protocol)
- [ ] Selenium 4 relative locators

---

## 4. Web Automation — Playwright ✅ {#4-web-automation--playwright}

> Tài liệu: [Learning/Playwright/](./Playwright/README.md)

**Cơ bản:**
- [ ] So sánh Playwright vs Selenium — khi nào dùng cái nào
- [ ] Setup Java
- [ ] Locators: getByRole, getByLabel, getByTestId, getByText
- [ ] Auto-waiting — không cần explicit wait như Selenium
- [ ] Basic actions: click, fill, press, selectOption
- [ ] Assertions: assertThat(locator).isVisible(), hasText()

**Trung cấp:**
- [ ] Page Object Model với Playwright
- [ ] BrowserContext — test isolation
- [ ] Screenshots, videos, Trace Viewer
- [ ] Network interception — mock API response
- [ ] Saved auth state — login 1 lần dùng nhiều test

**Nâng cao:**
- [ ] Parallel execution
- [ ] Playwright trong CI/CD
- [ ] API testing tích hợp

---

## 5. API Testing — REST Assured ✅ {#5-api-testing--rest-assured}

> Tài liệu: [Learning/RestAssured/](./RestAssured/README.md)

**Cơ bản:**
- [ ] HTTP fundamentals: methods, status codes, headers
- [ ] REST vs SOAP
- [ ] given().when().then() syntax
- [ ] GET, POST, PUT, PATCH, DELETE
- [ ] Assertions với Hamcrest

**Trung cấp:**
- [ ] JSONPath — trích xuất data từ response
- [ ] Authentication: Basic Auth, Bearer Token, OAuth2
- [ ] JSON Schema Validation
- [ ] Request/Response Specification — tái sử dụng config
- [ ] POJO Serialization/Deserialization (Jackson)
- [ ] Logging: log().ifValidationFails()

**Nâng cao:**
- [ ] Request filters — custom auth filter
- [ ] Multipart — file upload API
- [ ] Contract testing concept (Pact)
- [ ] Kết hợp API setup + UI test

---

## 6. Mobile Automation — Appium ✅ {#6-mobile-automation--appium}

> Tài liệu: [Learning/Appium/](./Appium/README.md)

**Cơ bản:**
- [ ] Native vs Hybrid vs Mobile Web
- [ ] Appium architecture: Server, UIAutomator2, XCUITest
- [ ] Setup: Android SDK, Appium Server, Appium Inspector
- [ ] Mobile locators: accessibility id, id, UIAutomator
- [ ] Basic actions: tap, sendKeys, getText

**Trung cấp:**
- [ ] Mobile gestures: swipe, scroll, long press
- [ ] Handle permissions popup
- [ ] Hybrid app — switch context (NATIVE ↔ WEBVIEW)
- [ ] Page Object Model cho mobile

**Nâng cao:**
- [ ] Parallel mobile testing
- [ ] Cloud testing: BrowserStack, Sauce Labs
- [ ] iOS testing (XCUITest)

---

## 7. BDD — Cucumber ✅ {#7-bdd--cucumber}

> Tài liệu: [Learning/Cucumber/](./Cucumber/README.md)

**Cơ bản:**
- [ ] BDD là gì, tại sao cần
- [ ] Gherkin: Feature, Scenario, Given/When/Then
- [ ] Step Definitions
- [ ] Cucumber Runner, @CucumberOptions
- [ ] Tags: @smoke, @regression

**Trung cấp:**
- [ ] Scenario Outline + Examples — data-driven
- [ ] Background, Hooks (@Before, @After)
- [ ] Data Tables, Doc Strings
- [ ] Sharing state — PicoContainer
- [ ] Cucumber + Selenium + REST Assured

**Nâng cao:**
- [ ] Custom Parameter Types
- [ ] Parallel Cucumber
- [ ] Cucumber Reports, Allure với Cucumber

---

## 8. BDD + Reporting — Serenity BDD ✅ {#8-bdd--reporting--serenity-bdd}

> Tài liệu: [Learning/SerenityBDD/](./SerenityBDD/README.md)

**Cơ bản:**
- [ ] Serenity vs Cucumber thuần
- [ ] Setup Maven, serenity.conf
- [ ] @Steps — reusable step library
- [ ] @Managed WebDriver
- [ ] Serenity Reports — living documentation

**Trung cấp:**
- [ ] Serenity + Cucumber integration
- [ ] Serenity + REST Assured
- [ ] Page Object với Serenity (@DefaultUrl)
- [ ] Tags và requirements hierarchy

**Nâng cao:**
- [ ] Screenplay Pattern — Actor, Task, Action, Question
- [ ] So sánh Screenplay vs POM
- [ ] Serenity trong CI/CD

---

## 9. Framework Infrastructure ✅ {#9-framework-infrastructure}

> Tài liệu: [Learning/Framework-Infrastructure/](./Framework-Infrastructure/README.md)

**Maven:**
- [ ] POM.xml structure, lifecycle, dependency scope
- [ ] Surefire/Failsafe plugin
- [ ] Maven profiles — dev/staging/prod

**Git:**
- [ ] Workflow: branch → commit → PR → merge
- [ ] Rebase vs Merge
- [ ] Branching strategy (Git Flow / Trunk-based)

**Design Patterns:**
- [ ] Page Object Model, Singleton, Factory, Builder, Strategy

**Test Data Management:**
- [ ] Properties/YAML, CSV, JSON, Faker, Database, API

**Allure Report:**
- [ ] @Step, @Feature, @Story, attach screenshot
- [ ] Allure trong CI/CD

---

## 10. CI/CD & DevOps cho Tester ✅ {#10-cicd--devops-cho-tester}

> Tài liệu: [Learning/CICD/](./CICD/README.md)

**Cơ bản:**
- [ ] CI/CD là gì
- [ ] GitHub Actions: workflow, job, step, trigger
- [ ] Chạy Maven test trong CI
- [ ] Publish Allure report

**Trung cấp:**
- [ ] Jenkins: Jenkinsfile, declarative pipeline
- [ ] Docker: image, container, Dockerfile
- [ ] Docker Compose — Selenium Grid
- [ ] Secrets management trong CI

**Nâng cao:**
- [ ] Matrix strategy — cross-browser trong CI
- [ ] Shift-left testing
- [ ] Monitoring test trends

---

## 11. English cho Tester/Dev ✅ {#11-english-cho-testerdev}

> Tài liệu: [Learning/English/](./English/README.md)

| Giai đoạn | Mục tiêu | Nội dung |
|---|---|---|
| 1 — Nền tảng | Mất gốc → A2 | Ngữ pháp cơ bản, 1000 từ thông dụng, phát âm |
| 2 — IT English | A2 → B1 | 200 từ IT/Testing, đọc docs, viết bug report, email |
| 3 — Giao tiếp | B1 → B2 | Daily standup, meeting, code review, Slack |
| 4 — Nâng cao | B2+ | Phỏng vấn, technical docs, team quốc tế |

---

---

## Lộ trình học theo giai đoạn {#lo-trinh}

```
GIAI ĐOẠN 1 — Nền tảng (3-4 tuần)
├── Java Core
│   ├── OOP, Collections, Exception, Lambda/Stream
│   └── ThreadLocal, Design Patterns
├── TestNG cơ bản → nâng cao
│   ├── Parallel execution, Listeners, Retry
│   └── Soft assertions, DataProvider
├── Git workflow
│   └── Branch, PR, rebase, conflict
└── AI Phần 1-3
    └── Setup Kiro steering, dùng AI hỗ trợ học

GIAI ĐOẠN 2 — Selenium bài bản (4-6 tuần)
├── Selenium đầy đủ
│   ├── Waits, Actions, JS Executor
│   ├── Flaky test prevention, StaleElement
│   └── Selenium Grid + Docker
├── POM pattern chuẩn
│   ├── BasePage, PageFactory, @FindBy
│   └── Component pattern
├── Allure Report
│   └── @Step, screenshot, CI integration
└── AI Phần 4-5
    └── Dùng AI viết test, review code

GIAI ĐOẠN 3 — API Testing (3-4 tuần)
├── REST Assured đầy đủ
│   ├── JSONPath, Schema validation, Auth
│   └── POJO, Request/Response Spec
├── Kết hợp API + UI test
└── English: đọc docs, viết bug report

GIAI ĐOẠN 4 — BDD & CI/CD (4-5 tuần)
├── Cucumber đầy đủ
│   ├── Scenario Outline, Hooks, Data Tables
│   └── PicoContainer, Parallel
├── GitHub Actions
│   ├── Chạy test tự động khi push/PR
│   └── Publish Allure report
├── Docker cơ bản
│   └── Selenium Grid với Docker Compose
└── English: daily standup, email, Slack

GIAI ĐOẠN 5 — Nâng cao & Mở rộng (2-3 tháng)
├── Playwright (nếu project cần)
├── Serenity BDD (nếu project enterprise)
├── Appium (nếu cần mobile)
├── AI Phần 6 — Agent, Ollama local
└── English: meeting, presentation, phỏng vấn
```

---

### Checklist tự đánh giá

Bạn có thể làm được những việc này không?

**Selenium & TestNG:**
- [ ] Giải thích tại sao test bị flaky và fix được
- [ ] Setup Selenium Grid chạy parallel trên nhiều browser
- [ ] Viết custom WebDriver factory với ThreadLocal
- [ ] Implement Retry Analyzer, custom Listener chụp screenshot

**Framework:**
- [ ] Build framework từ đầu với POM, config, reporting
- [ ] Tích hợp vào GitHub Actions pipeline
- [ ] Giải thích design decision cho người khác

**API:**
- [ ] Viết API test đầy đủ từ Swagger spec
- [ ] Kết hợp API setup + UI test + API cleanup

> Chưa làm được → đó là lỗ hổng cần lấp trước.
> Dùng AI (Kiro/Claude) để học nhanh hơn ở mọi giai đoạn.
