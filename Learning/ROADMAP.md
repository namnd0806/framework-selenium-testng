# Learning Roadmap — Lộ trình học tổng quan
> Dành cho người 2 năm kinh nghiệm muốn ôn lại bài bản và nâng cao
> Trạng thái: ✅ Có tài liệu | 🚧 Đang xây dựng | 📋 Chưa có

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

```
┌─────────────────────────────────────────────────────────────┐
│                  AUTOMATION FRAMEWORK                        │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │  TEST LAYER (viết test)                              │   │
│  │  BDD: Cucumber / Serenity  ←→  Plain: TestNG/JUnit  │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     │                                        │
│  ┌──────────────────▼──────────────────────────────────┐   │
│  │  INTERACTION LAYER (tương tác với app)               │   │
│  │  Web: Selenium / Playwright                          │   │
│  │  Mobile: Appium                                      │   │
│  │  API: REST Assured / Playwright API                  │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     │                                        │
│  ┌──────────────────▼──────────────────────────────────┐   │
│  │  SUPPORT LAYER (hỗ trợ)                              │   │
│  │  Config │ Test Data │ Logging │ Screenshot │ Retry   │   │
│  └──────────────────┬──────────────────────────────────┘   │
│                     │                                        │
│  ┌──────────────────▼──────────────────────────────────┐   │
│  │  REPORTING LAYER                                     │   │
│  │  Allure │ ExtentReports │ Serenity Reports           │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
         │                    │                    │
    Build Tool           Version Control        CI/CD
    Maven/Gradle         Git/GitHub             Jenkins/GH Actions
```

### Các thành phần cần chọn khi build framework

| Thành phần | Vai trò | Lựa chọn phổ biến |
|---|---|---|
| **Test Runner** | Quản lý, chạy, sắp xếp test | TestNG, JUnit 5 |
| **Assertion Library** | Kiểm tra kết quả đúng/sai | TestNG Assert, AssertJ, Hamcrest |
| **Web Driver** | Tương tác với browser | Selenium WebDriver, Playwright |
| **Mobile Driver** | Tương tác với mobile app | Appium |
| **API Client** | Gọi và test REST API | REST Assured, Playwright API |
| **BDD Layer** | Viết test bằng ngôn ngữ tự nhiên | Cucumber, Serenity BDD |
| **Design Pattern** | Tổ chức code test | Page Object Model, Screenplay |
| **Build Tool** | Quản lý dependency, build | Maven, Gradle |
| **Reporting** | Báo cáo kết quả test | Allure, ExtentReports |
| **CI/CD** | Tự động chạy test | GitHub Actions, Jenkins |

### So sánh nhanh các lựa chọn

**TestNG vs JUnit 5:**
- TestNG: phổ biến hơn trong automation, có @DataProvider, parallel dễ hơn
- JUnit 5: phổ biến trong unit test, Spring Boot test, cú pháp hiện đại hơn
- **Chọn TestNG** nếu làm automation framework thuần

**Selenium vs Playwright:**
- Selenium: mature, nhiều tài liệu, hỗ trợ nhiều ngôn ngữ, cần setup nhiều hơn
- Playwright: mới hơn, nhanh hơn, ít flaky hơn, auto-wait tốt hơn
- **Chọn Selenium** nếu team đã quen, **Playwright** nếu project mới

**AssertJ vs Hamcrest vs TestNG Assert:**
- TestNG Assert: đơn giản, đủ dùng cho basic assertion
- Hamcrest: dùng nhiều với REST Assured, matcher syntax
- AssertJ: fluent API, error message rõ nhất, được ưa chuộng nhất hiện tại

---

## 0. Java Core — Nền tảng bắt buộc ✅

> Tài liệu chi tiết: [Learning/Java/](./Java/README.md)
> Không có Java vững thì học tool nào cũng bị hổng. 2 năm kinh nghiệm cần ôn lại phần này.

### Các file tài liệu

| File | Nội dung |
|------|----------|
| [01-oop.md](./Java/01-oop.md) | OOP — Class, Object, 4 tính chất, Interface vs Abstract |
| [02-collections.md](./Java/02-collections.md) | List, Map, Set, Queue — khi nào dùng cái nào |
| [03-exception-handling.md](./Java/03-exception-handling.md) | try/catch/finally, checked vs unchecked, custom exception |
| [04-string-and-io.md](./Java/04-string-and-io.md) | String, StringBuilder, File I/O, đọc test data |
| [05-java8-features.md](./Java/05-java8-features.md) | Lambda, Stream API, Optional, Method Reference |
| [06-generics-annotations.md](./Java/06-generics-annotations.md) | Generics, Annotations, Reflection cơ bản |
| [07-multithreading.md](./Java/07-multithreading.md) | Thread, ThreadLocal — quan trọng cho parallel Selenium |
| [08-design-patterns.md](./Java/08-design-patterns.md) | Singleton, Factory, Builder, Strategy, Page Object |

### Checklist kiến thức (từ cơ bản đến nâng cao)

**Cơ bản — phải biết:**
- [ ] OOP 4 tính chất: Encapsulation, Inheritance, Polymorphism, Abstraction
- [ ] Interface vs Abstract class — khi nào dùng cái nào
- [ ] Collections: ArrayList, HashMap, HashSet — dùng đúng chỗ
- [ ] Exception: try/catch/finally, throw/throws, checked vs unchecked
- [ ] String vs StringBuilder — tại sao String immutable
- [ ] File I/O — đọc file properties, CSV, JSON (dùng cho test data)
- [ ] Generics — `List<String>`, `Map<String, Object>`

**Trung cấp — nên biết:**
- [ ] Lambda & Stream API — filter, map, collect, forEach
- [ ] Optional — tránh NullPointerException
- [ ] Static vs Instance — tránh bug trong parallel test
- [ ] Varargs, Enum, inner class
- [ ] Annotations — @Override, tạo custom annotation
- [ ] Reflection cơ bản — TestNG dùng bên trong

**Nâng cao — SDET cần biết:**
- [ ] ThreadLocal — quản lý WebDriver trong parallel test
- [ ] Synchronized, volatile — thread safety cơ bản
- [ ] Design Patterns: Singleton, Factory, Builder, Strategy
- [ ] Dependency Injection concept

### Câu hỏi phỏng vấn thực tế

```
OOP:
1. 4 tính chất OOP là gì? Cho ví dụ thực tế trong automation framework
2. Interface vs Abstract class — khi nào dùng cái nào?
3. Polymorphism là gì? Cho ví dụ

Collections:
4. ArrayList vs LinkedList — khi nào dùng cái nào?
5. HashMap hoạt động thế nào? Điều gì xảy ra khi 2 key có cùng hashCode?
6. HashSet vs TreeSet — khác nhau thế nào?

Exception:
7. Checked vs Unchecked exception — ví dụ thực tế?
8. finally block có chạy không nếu có return trong try?

String & Java 8:
9. Tại sao String immutable? String vs StringBuilder vs StringBuffer?
10. Stream API: filter, map, collect — cho ví dụ thực tế với test data

Threading:
11. ThreadLocal là gì? Tại sao dùng trong Selenium parallel test?
12. Singleton pattern — implement thread-safe thế nào?

Misc:
13. == vs .equals() trong Java?
14. final, finally, finalize — khác nhau thế nào?
15. static method/variable — khi nào dùng? Rủi ro trong parallel test?
```

---

## 1. AI trong Coding ✅

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

## 2. Test Runner & Assertion ✅

> Tài liệu: [Learning/TestRunner/](./TestRunner/README.md)
> Đây là **xương sống** của mọi framework — cần hiểu sâu trước khi học tool khác.

### 2.1 TestNG

**Cơ bản:**
- [ ] Annotations: @Test, @BeforeMethod, @AfterMethod, @BeforeClass, @AfterClass, @BeforeSuite
- [ ] Assertions: assertEquals, assertTrue, assertNotNull, assertThrows
- [ ] Test execution order và priority
- [ ] Groups — @Test(groups="smoke"), chạy subset test
- [ ] @DataProvider — data-driven testing
- [ ] testng.xml — cấu hình suite, parallel, include/exclude

**Trung cấp:**
- [ ] Parallel execution — methods, classes, tests, instances
- [ ] @Factory — tạo nhiều instance test với data khác nhau
- [ ] Listeners — ITestListener, ISuiteListener (custom report, retry)
- [ ] Retry Analyzer — tự retry khi test fail
- [ ] Soft Assertions — kiểm tra nhiều điều, không dừng khi fail
- [ ] Dependency test — @Test(dependsOnMethods)

**Nâng cao:**
- [ ] Custom Annotations + Reflection
- [ ] TestNG với Spring (dependency injection)
- [ ] Parallel WebDriver management với ThreadLocal

### 2.2 JUnit 5 (biết để so sánh)

- [ ] @Test, @BeforeEach, @AfterEach, @BeforeAll, @AfterAll
- [ ] @ParameterizedTest, @ValueSource, @CsvSource
- [ ] @ExtendWith — extension model
- [ ] Assertions.assertAll — soft assertion
- [ ] Khác biệt chính với TestNG

### 2.3 Assertion Libraries

- [ ] **TestNG Assert** — basic, dùng khi không cần gì phức tạp
- [ ] **AssertJ** — fluent API, error message rõ, được ưa chuộng nhất
- [ ] **Hamcrest** — matcher-based, dùng nhiều với REST Assured
- [ ] So sánh: `assertEquals(expected, actual)` vs `assertThat(actual).isEqualTo(expected)`

### Câu hỏi phỏng vấn thực tế

```
1. @BeforeMethod vs @BeforeClass vs @BeforeSuite — khác nhau thế nào?
2. Làm thế nào để chạy test song song trong TestNG?
3. ThreadLocal dùng để làm gì trong parallel Selenium test?
4. Soft assertion là gì? Khi nào dùng thay vì hard assertion?
5. @DataProvider hoạt động thế nào? Cho ví dụ data-driven test
6. Retry Analyzer implement thế nào?
7. Sự khác nhau giữa TestNG và JUnit 5?
8. Làm thế nào để chạy chỉ @smoke tests trong TestNG?
9. AssertJ có ưu điểm gì so với TestNG Assert?
10. testng.xml dùng để làm gì? Cấu hình parallel ở đâu?
```

---

## 3. Web Automation — Selenium ✅

> Tài liệu: [Learning/Selenium/](./Selenium/README.md)

### Kiến thức cần có (từ cơ bản đến nâng cao)

**Cơ bản:**
- [ ] WebDriver architecture — browser driver, W3C protocol
- [ ] Setup: ChromeDriver, WebDriverManager (tự quản lý driver version)
- [ ] Locators: id, name, className, tagName, linkText, cssSelector, xpath
- [ ] CSS Selector vs XPath — ưu nhược điểm, khi nào dùng cái nào
- [ ] Basic actions: click, sendKeys, clear, submit, getText, getAttribute
- [ ] Navigation: get, navigate().to(), back(), forward(), refresh()
- [ ] Browser options: headless, window size, download path

**Trung cấp:**
- [ ] Waits: implicit wait, explicit wait (WebDriverWait), fluent wait
- [ ] ExpectedConditions: elementToBeClickable, visibilityOf, presenceOf...
- [ ] Actions class: hover, drag-drop, right-click, double-click, keyboard
- [ ] JavaScript Executor: scroll, click, get value, highlight element
- [ ] Handle: Alert/Confirm/Prompt, iFrame, multiple windows/tabs
- [ ] File upload, download handling
- [ ] Select class — dropdown
- [ ] Screenshot: full page, element screenshot
- [ ] Page Object Model (POM) — design pattern bắt buộc
- [ ] @FindBy, @FindBys, @FindAll, PageFactory

**Nâng cao:**
- [ ] Selenium Grid — hub/node, chạy test song song nhiều browser/OS
- [ ] Docker + Selenium Grid
- [ ] Custom WebDriver factory — quản lý driver lifecycle
- [ ] Stale Element — nguyên nhân và cách xử lý
- [ ] Flaky test — nguyên nhân và cách phòng tránh
- [ ] Shadow DOM — cách tìm element trong shadow root
- [ ] Performance: giảm thời gian chạy test
- [ ] Selenium 4 features: relative locators, CDP (Chrome DevTools Protocol)

### Câu hỏi phỏng vấn thực tế

```
1. Sự khác nhau giữa implicit wait, explicit wait, fluent wait?
2. Tại sao không nên dùng Thread.sleep() trong Selenium?
3. StaleElementReferenceException là gì? Cách xử lý?
4. CSS Selector vs XPath — khi nào dùng cái nào? Cái nào nhanh hơn?
5. Page Object Model là gì? Tại sao cần dùng?
6. Làm thế nào để handle dynamic element (id thay đổi mỗi lần load)?
7. Làm thế nào để test trong iframe?
8. Selenium Grid là gì? Cách setup?
9. Flaky test là gì? Nguyên nhân và cách fix?
10. WebDriverManager là gì? Tại sao dùng thay vì download driver thủ công?
11. Làm thế nào để chạy test headless?
12. JavaScript Executor dùng khi nào? Cho ví dụ thực tế
```

---

## 4. Web Automation — Playwright ✅

> Tài liệu: [Learning/Playwright/](./Playwright/README.md)

### Kiến thức cần có

**Cơ bản:**
- [ ] So sánh Playwright vs Selenium — architecture, ưu nhược điểm
- [ ] Setup: Java hoặc TypeScript (nên học TypeScript nếu có thể)
- [ ] Locators: getByRole, getByText, getByLabel, getByTestId, getByPlaceholder
- [ ] Auto-waiting — Playwright tự chờ, không cần explicit wait như Selenium
- [ ] Basic actions: click, fill, press, selectOption, check, uncheck
- [ ] Assertions: expect(locator).toBeVisible(), toHaveText(), toHaveValue()

**Trung cấp:**
- [ ] Page Object Model với Playwright
- [ ] Fixtures — setup/teardown, dependency injection
- [ ] Screenshots, videos, traces (Playwright Trace Viewer)
- [ ] API testing tích hợp — request() context
- [ ] Network interception — mock API response
- [ ] Multiple browser contexts — test isolation
- [ ] Playwright Codegen — record test tự động

**Nâng cao:**
- [ ] Parallel execution — workers
- [ ] Playwright với CI/CD
- [ ] Visual comparison testing
- [ ] Component testing (Playwright CT)

### Câu hỏi phỏng vấn thực tế

```
1. Playwright khác Selenium thế nào? Ưu điểm chính là gì?
2. Auto-waiting trong Playwright hoạt động thế nào?
3. Tại sao Playwright ít flaky hơn Selenium?
4. Locator trong Playwright khác gì so với Selenium?
5. Playwright Trace Viewer dùng để làm gì?
6. Khi nào nên chọn Playwright thay vì Selenium?
```

---

## 5. API Testing — REST Assured ✅

> Tài liệu: [Learning/RestAssured/](./RestAssured/README.md)

### Kiến thức cần có

**Cơ bản:**
- [ ] HTTP fundamentals: methods, status codes, headers, body
- [ ] REST vs SOAP — khác nhau thế nào
- [ ] Postman — dùng thành thạo trước khi học REST Assured
- [ ] REST Assured syntax: given().when().then()
- [ ] Request: GET, POST, PUT, DELETE, PATCH
- [ ] Request params: query params, path params, headers, body
- [ ] Response: statusCode(), body(), header(), time()
- [ ] Assertions với Hamcrest: equalTo, hasItems, containsString

**Trung cấp:**
- [ ] JSONPath — trích xuất data từ JSON response
- [ ] XML Path — trích xuất data từ XML response
- [ ] Authentication: Basic Auth, Bearer Token, OAuth2, API Key
- [ ] JSON Schema Validation
- [ ] Request/Response specification — tái sử dụng config
- [ ] Serialization/Deserialization — POJO với Jackson/Gson
- [ ] Logging: log().all(), log().ifValidationFails()
- [ ] TestNG integration — data-driven API test

**Nâng cao:**
- [ ] Request filters — custom logging, authentication filter
- [ ] Multipart form data — file upload API
- [ ] GraphQL testing với REST Assured
- [ ] Contract testing concept — Pact
- [ ] Performance: response time assertion
- [ ] Kết hợp API setup + UI test (end-to-end)

### Câu hỏi phỏng vấn thực tế

```
1. REST Assured given/when/then có nghĩa là gì?
2. JSONPath là gì? Cho ví dụ trích xuất nested JSON
3. Làm thế nào để test API cần authentication?
4. JSON Schema Validation dùng để làm gì?
5. Sự khác nhau giữa query param và path param?
6. Làm thế nào để deserialize JSON response thành Java object?
7. Khi nào dùng API test thay vì UI test?
8. Contract testing là gì? Tại sao quan trọng trong microservices?
9. Làm thế nào để test API upload file?
10. Response time assertion trong REST Assured thế nào?
```

---

## 6. Mobile Automation — Appium ✅

> Tài liệu: [Learning/Appium/](./Appium/README.md)

### Kiến thức cần có

**Cơ bản:**
- [ ] Mobile app types: Native, Hybrid, Mobile Web — khác nhau thế nào
- [ ] Appium architecture: Appium Server, Client, Driver (UIAutomator2, XCUITest)
- [ ] Setup: Android SDK, Appium Server, Appium Inspector
- [ ] Desired Capabilities / Appium Options
- [ ] Mobile locators: id, accessibility id, xpath, UIAutomator, class name
- [ ] Appium Inspector — tìm locator trên device/emulator
- [ ] Basic actions: tap, sendKeys, clear, getText

**Trung cấp:**
- [ ] Mobile gestures: swipe, scroll, pinch, zoom, long press
- [ ] Handle: alerts, permissions popup, keyboard
- [ ] Hybrid app — switch giữa native và WebView context
- [ ] Page Object Model cho mobile
- [ ] Real device vs Emulator/Simulator — ưu nhược điểm
- [ ] Appium với TestNG

**Nâng cao:**
- [ ] Parallel mobile testing — nhiều device cùng lúc
- [ ] Cloud testing: BrowserStack, Sauce Labs, AWS Device Farm
- [ ] iOS testing — XCUITest driver, setup trên Mac
- [ ] Deep links, push notifications testing
- [ ] Performance testing trên mobile

### Câu hỏi phỏng vấn thực tế

```
1. Native app vs Hybrid app vs Mobile Web — khác nhau thế nào?
2. Appium architecture hoạt động thế nào?
3. Sự khác nhau giữa UIAutomator2 và XCUITest driver?
4. Làm thế nào để tìm locator trên mobile app?
5. Accessibility ID là gì? Tại sao ưu tiên dùng?
6. Làm thế nào để test Hybrid app (WebView)?
7. Real device vs Emulator — khi nào dùng cái nào?
8. Làm thế nào để handle permission popup trên Android?
```

---

## 7. BDD — Cucumber ✅

> Tài liệu: [Learning/Cucumber/](./Cucumber/README.md)

### Kiến thức cần có

**Cơ bản:**
- [ ] BDD là gì — Behavior Driven Development, tại sao cần
- [ ] Gherkin syntax: Feature, Scenario, Given, When, Then, And, But
- [ ] Step Definitions — map Gherkin steps với Java code
- [ ] Feature files — tổ chức, đặt tên
- [ ] Cucumber Runner — @CucumberOptions, TestNG/JUnit runner
- [ ] Tags: @smoke, @regression — chạy subset

**Trung cấp:**
- [ ] Scenario Outline + Examples — data-driven BDD
- [ ] Background — steps chạy trước mỗi scenario
- [ ] Hooks: @Before, @After, @BeforeStep, @AfterStep
- [ ] Data Tables — truyền data dạng bảng vào step
- [ ] Doc Strings — truyền multiline text
- [ ] Sharing state giữa steps — dependency injection (PicoContainer)
- [ ] Cucumber + Selenium integration
- [ ] Cucumber + REST Assured integration
- [ ] Cucumber Reports — HTML, JSON, pretty

**Nâng cao:**
- [ ] Custom Parameter Types — @ParameterType
- [ ] Parallel execution với Cucumber
- [ ] Cucumber với Spring — dependency injection
- [ ] Living Documentation — Cucumber + Serenity

### Câu hỏi phỏng vấn thực tế

```
1. BDD là gì? Lợi ích so với test thông thường?
2. Given/When/Then có nghĩa là gì? Cho ví dụ thực tế
3. Scenario Outline dùng khi nào?
4. Làm thế nào để share state giữa các step trong Cucumber?
5. Hooks trong Cucumber là gì? @Before vs @BeforeStep?
6. Làm thế nào để chạy chỉ @smoke scenarios?
7. Cucumber Runner cấu hình thế nào?
8. Sự khác nhau giữa Background và @Before hook?
9. Làm thế nào để pass data phức tạp vào step (Data Table)?
10. Cucumber có thể chạy parallel không? Cách setup?
```

---

## 8. BDD + Reporting — Serenity BDD ✅

> Tài liệu: [Learning/SerenityBDD/](./SerenityBDD/README.md)

### Kiến thức cần có

**Cơ bản:**
- [ ] Serenity là gì — kết hợp BDD + automation + living documentation
- [ ] Serenity vs Cucumber thuần — khác nhau thế nào
- [ ] Setup: Maven dependencies, Serenity runner
- [ ] @Steps — tạo reusable step library
- [ ] Serenity với Selenium — @Managed WebDriver
- [ ] Serenity Reports — cấu trúc report, requirements, test outcomes

**Trung cấp:**
- [ ] Serenity + Cucumber integration
- [ ] Serenity với REST Assured — API steps
- [ ] Page Object với Serenity — @DefaultUrl, @FindBy
- [ ] Serenity Screenplay Pattern — actor, task, question, interaction
- [ ] Tags và requirements hierarchy
- [ ] Screenshots tự động trong Serenity

**Nâng cao:**
- [ ] Screenplay Pattern sâu hơn — so sánh với POM
- [ ] Custom reporters
- [ ] Serenity với Spring
- [ ] Serenity BDD với CI/CD — publish report

### Câu hỏi phỏng vấn thực tế

```
1. Serenity BDD là gì? Khác Cucumber thuần thế nào?
2. Living Documentation là gì?
3. Screenplay Pattern là gì? Ưu điểm so với POM?
4. @Steps trong Serenity dùng thế nào?
5. Serenity report có những thông tin gì?
6. Khi nào nên dùng Serenity thay vì Cucumber thuần?
```

---

## 9. Framework Infrastructure ✅

> Tài liệu: [Learning/Framework-Infrastructure/](./Framework-Infrastructure/README.md)
> Đây là phần tạo ra sự khác biệt giữa framework amateur và professional.

### 9.1 Build Tool — Maven

**Cơ bản:**
- [ ] POM.xml structure — groupId, artifactId, version, dependencies
- [ ] Maven lifecycle: validate, compile, test, package, install, deploy
- [ ] Dependency management — scope: compile, test, provided, runtime
- [ ] Maven plugins: Surefire (TestNG), Failsafe (integration test)

**Trung cấp:**
- [ ] Maven profiles — chạy test theo môi trường (dev/staging/prod)
- [ ] Properties trong POM — tái sử dụng version
- [ ] Multi-module project
- [ ] Maven Wrapper (mvnw) — không cần cài Maven trên máy

**Câu hỏi phỏng vấn:**
```
1. Maven lifecycle là gì? Sự khác nhau giữa mvn test và mvn verify?
2. Dependency scope: compile vs test vs provided?
3. Maven profiles dùng để làm gì? Cho ví dụ thực tế
4. Surefire plugin vs Failsafe plugin — khác nhau thế nào?
```

---

### 9.2 Version Control — Git

**Cơ bản:**
- [ ] Git workflow: init, add, commit, push, pull, clone
- [ ] Branching: branch, checkout, merge
- [ ] .gitignore — những gì không nên commit

**Trung cấp:**
- [ ] Git Flow / Trunk-based development
- [ ] Pull Request / Code Review workflow
- [ ] Rebase vs Merge — khi nào dùng cái nào
- [ ] Resolve conflict
- [ ] Git stash, cherry-pick

**Câu hỏi phỏng vấn:**
```
1. Git rebase vs merge — khác nhau thế nào? Khi nào dùng cái nào?
2. Làm thế nào để undo commit đã push lên remote?
3. Git stash dùng để làm gì?
4. Branching strategy của team bạn là gì?
```

---

### 9.3 Design Patterns trong Framework

**Cần biết:**
- [ ] **Page Object Model (POM)** — tách locator và action khỏi test
- [ ] **Singleton** — WebDriver instance, config manager
- [ ] **Factory** — tạo driver theo browser type
- [ ] **Builder** — tạo test data object phức tạp
- [ ] **Strategy** — switch giữa các implementation (web/mobile)
- [ ] **Facade** — đơn giản hóa API phức tạp

**Câu hỏi phỏng vấn:**
```
1. Page Object Model là gì? Tại sao cần?
2. Singleton pattern trong Selenium dùng thế nào?
3. Factory pattern dùng để làm gì trong framework?
4. Builder pattern dùng khi nào? Cho ví dụ với test data
```

---

### 9.4 Test Data Management

- [ ] Hardcoded data — tệ nhất, tránh dùng
- [ ] Properties/YAML files — config data theo môi trường
- [ ] Excel/CSV — data-driven testing
- [ ] JSON files — test data phức tạp
- [ ] Faker library — sinh data ngẫu nhiên, tránh conflict
- [ ] Database — setup/teardown data qua JDBC
- [ ] API — tạo data qua API trước khi test UI

**Câu hỏi phỏng vấn:**
```
1. Làm thế nào để quản lý test data cho nhiều môi trường?
2. Faker library dùng để làm gì?
3. Tại sao nên tạo test data qua API thay vì UI?
4. Làm thế nào để cleanup test data sau khi test?
```

---

### 9.5 Reporting — Allure

- [ ] Allure annotations: @Step, @Description, @Severity, @Feature, @Story
- [ ] Attach: screenshot, log, file vào report
- [ ] Allure với TestNG — listener setup
- [ ] Allure với Cucumber
- [ ] Generate và serve Allure report
- [ ] Allure trong CI/CD — publish report

**Câu hỏi phỏng vấn:**
```
1. Allure report có những thông tin gì?
2. Làm thế nào để attach screenshot vào Allure khi test fail?
3. @Step annotation dùng thế nào?
4. Cách publish Allure report trong GitHub Actions?
```

---

## 10. CI/CD & DevOps cho Tester ✅

> Tài liệu: [Learning/CICD/](./CICD/README.md)

### Kiến thức cần có

**Cơ bản:**
- [ ] CI/CD là gì — Continuous Integration, Continuous Delivery
- [ ] GitHub Actions: workflow, job, step, trigger
- [ ] Chạy Maven test trong GitHub Actions
- [ ] Publish Allure report trong pipeline

**Trung cấp:**
- [ ] Jenkins: pipeline, Jenkinsfile, declarative vs scripted
- [ ] Docker cơ bản: image, container, Dockerfile
- [ ] Docker Compose — chạy Selenium Grid với Docker
- [ ] Environment variables và secrets trong CI
- [ ] Test parallelism trong CI

**Nâng cao:**
- [ ] Kubernetes cơ bản — biết để làm việc với DevOps team
- [ ] Monitoring test results — dashboard, trend
- [ ] Shift-left testing trong pipeline

### Câu hỏi phỏng vấn thực tế

```
1. CI/CD là gì? Tại sao automation test cần tích hợp CI/CD?
2. Làm thế nào để chạy Selenium test trong Docker?
3. Selenium Grid là gì? Cách setup với Docker Compose?
4. Làm thế nào để chạy test song song trong GitHub Actions?
5. Secrets/credentials quản lý thế nào trong CI pipeline?
6. Khi test fail trong CI, làm thế nào để debug?
7. Shift-left testing là gì?
```

---

## 11. English cho Tester/Dev ✅

> Tài liệu: [Learning/English/](./English/README.md)

### Lộ trình từ mất gốc đến giao tiếp công việc

**Giai đoạn 1 — Nền tảng (Mất gốc → A2):**
- [ ] Ngữ pháp cơ bản — 12 thì, câu điều kiện, bị động
- [ ] Từ vựng thiết yếu — 1000 từ thông dụng nhất
- [ ] Phát âm — IPA, âm khó với người Việt (th, v/w, r)
- [ ] Nghe hiểu — podcast chậm (VOA Learning English), video phụ đề

**Giai đoạn 2 — English cho IT (A2 → B1):**
- [ ] Từ vựng IT/Testing — 200 từ quan trọng nhất
- [ ] Đọc tài liệu kỹ thuật — documentation, README, Stack Overflow
- [ ] Viết comment code, commit message bằng tiếng Anh
- [ ] Viết bug report chuẩn tiếng Anh
- [ ] Email cơ bản — hỏi clarification, báo cáo tiến độ, xin hỗ trợ

**Giai đoạn 3 — Giao tiếp công việc (B1 → B2):**
- [ ] Daily standup — nói trôi chảy "Yesterday/Today/Blocker"
- [ ] Meeting — đặt câu hỏi, đưa ý kiến, đồng ý/không đồng ý lịch sự
- [ ] Giải thích technical issue cho non-technical stakeholder
- [ ] Code review — viết comment constructive, phản hồi feedback
- [ ] Slack/Teams — informal but professional communication
- [ ] Demo/Presentation — trình bày kết quả test, báo cáo sprint

**Giai đoạn 4 — Nâng cao (B2+):**
- [ ] Phỏng vấn kỹ thuật bằng tiếng Anh
- [ ] Viết technical documentation, test plan
- [ ] Tham gia discussion trong team quốc tế
- [ ] Negotiation — estimate, deadline, scope

### Từ vựng IT/Testing quan trọng nhất

```
Testing:        regression, smoke, sanity, exploratory, edge case,
                flaky, blocker, critical, severity, priority
Development:    deployment, release, hotfix, rollback, refactor,
                technical debt, code review, pull request
Agile:          sprint, backlog, story point, velocity, retrospective,
                definition of done, acceptance criteria
Infrastructure: pipeline, environment, staging, production,
                container, instance, endpoint, latency
```

---

---

## Lộ trình học theo giai đoạn {#lo-trinh}

> Dành cho người **2 năm kinh nghiệm** — ôn lại bài bản, lấp lỗ hổng, nâng lên SDET level.

```
GIAI ĐOẠN 1 — Ôn nền tảng (3-4 tuần)
├── Java Core (ôn lại)
│   ├── OOP, Collections, Exception, Lambda/Stream
│   └── ThreadLocal, Design Patterns cơ bản
├── TestNG sâu hơn
│   ├── Parallel execution, Listeners, Retry
│   └── Soft assertions, DataProvider nâng cao
├── Git nâng cao
│   └── Rebase, conflict, branching strategy
└── AI Phần 1-3
    └── Setup Kiro steering cho project, dùng AI hỗ trợ học

GIAI ĐOẠN 2 — Selenium bài bản (4-6 tuần)
├── Selenium nâng cao
│   ├── WebDriverManager, custom factory
│   ├── Flaky test prevention, StaleElement handling
│   ├── JavaScript Executor, Shadow DOM
│   └── Selenium Grid + Docker
├── POM pattern chuẩn
│   ├── BasePage, PageFactory, @FindBy
│   └── Component pattern (reusable UI components)
├── Allure Report
│   └── @Step, attach screenshot, CI integration
└── AI Phần 4-5
    └── Dùng AI viết test, review code, debug

GIAI ĐOẠN 3 — API Testing (3-4 tuần)
├── REST Assured đầy đủ
│   ├── JSONPath, Schema validation, Auth
│   ├── POJO serialization/deserialization
│   └── Request/Response specification
├── Kết hợp API + UI test
│   └── Tạo data qua API, test UI, cleanup qua API
└── English: đọc tài liệu, viết bug report

GIAI ĐOẠN 4 — BDD & CI/CD (4-5 tuần)
├── Cucumber đầy đủ
│   ├── Scenario Outline, Hooks, Data Tables
│   ├── Dependency injection (PicoContainer)
│   └── Parallel Cucumber
├── GitHub Actions
│   ├── Chạy test tự động khi push/PR
│   └── Publish Allure report
├── Docker cơ bản
│   └── Selenium Grid với Docker Compose
└── English: daily standup, email, Slack

GIAI ĐOẠN 5 — Nâng cao & Mở rộng (2-3 tháng)
├── Playwright (nếu project cần)
│   └── So sánh với Selenium, migrate dần
├── Serenity BDD (nếu project enterprise)
│   └── Screenplay Pattern
├── Appium (nếu cần mobile)
│   └── Android trước, iOS sau
├── AI Phần 6
│   └── Agent, Ollama local
└── English: meeting, presentation, phỏng vấn
```

### Checklist đánh giá bản thân (2 năm kinh nghiệm)

Tự hỏi: bạn có thể làm được những việc này không?

**Selenium:**
- [ ] Giải thích tại sao test bị flaky và fix được
- [ ] Setup Selenium Grid chạy parallel trên nhiều browser
- [ ] Viết custom WebDriver factory với ThreadLocal
- [ ] Handle StaleElement, dynamic element không cần hỏi AI

**TestNG:**
- [ ] Cấu hình parallel test trong testng.xml
- [ ] Implement Retry Analyzer
- [ ] Viết custom Listener để chụp screenshot khi fail

**Framework:**
- [ ] Build framework từ đầu với POM, config, reporting
- [ ] Tích hợp vào GitHub Actions pipeline
- [ ] Giải thích design decision cho người khác

**API:**
- [ ] Viết API test đầy đủ từ Swagger spec
- [ ] Kết hợp API setup + UI test

**Nếu chưa làm được** → đó là lỗ hổng cần lấp trước.

---

> **Ghi chú:** Dùng AI (Kiro/Claude) để học nhanh hơn ở mọi giai đoạn.
> Mỗi concept học xong → dùng AI để review code, hỏi câu hỏi phỏng vấn, tìm edge case.
