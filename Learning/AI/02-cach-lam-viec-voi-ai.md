# Phần 2 — Cách làm việc với AI

> Biết dùng tool chưa đủ — biết cách ra lệnh và kiểm soát AI mới quan trọng.

---

## 2.1 Prompt Engineering — Cách ra lệnh cho AI đúng

### Prompt là gì?

Prompt = **câu lệnh/yêu cầu bạn gửi cho AI**.
Prompt tốt → AI trả lời đúng, ít phải sửa.
Prompt tệ → AI trả lời lung tung, mất thời gian.

### Prompt tệ vs Prompt tốt

**Tệ:**
```
fix my code
```

**Tốt:**
```
Tôi đang dùng Selenium Java + TestNG.
Hàm clickElement() bên dưới bị lỗi "StaleElementReferenceException"
khi chạy trên trang có dynamic content (AJAX).
Hãy fix và giải thích tại sao lỗi xảy ra.

[paste code vào đây]
```

**Công thức prompt tốt:**
```
[Ngữ cảnh] + [Vấn đề cụ thể] + [Yêu cầu rõ ràng] + [Code/data liên quan]
```

### Kỹ thuật 1: Role Prompting — Gán vai cho AI

```
Bạn là senior automation engineer với 10 năm kinh nghiệm Selenium + Java.
Review đoạn code dưới đây và chỉ ra vấn đề về stability, maintainability.
Ưu tiên các vấn đề nghiêm trọng trước.
```

AI sẽ trả lời theo góc nhìn chuyên môn, không phải trả lời chung chung.

### Kỹ thuật 2: Few-shot — Cho ví dụ mẫu

```
Viết test case theo format này:

**TC001 - Login thành công**
- Precondition: User đã có tài khoản hợp lệ
- Steps:
  1. Mở trang login
  2. Nhập email: test@example.com
  3. Nhập password: Test@123
  4. Click button Login
- Expected: Redirect về dashboard, hiển thị tên user

---
Bây giờ viết test case cho chức năng "Forgot Password"
```

AI sẽ theo đúng format bạn muốn.

### Kỹ thuật 3: Chain-of-thought — Bắt AI suy nghĩ từng bước

```
Phân tích bug này theo từng bước:
1. Code đang làm gì
2. Lỗi xảy ra ở đâu và tại sao
3. Các cách fix có thể (liệt kê ít nhất 2 cách)
4. Cách fix tốt nhất và lý do chọn
```

Thay vì AI đưa ra đáp án ngay (dễ sai), nó suy nghĩ có hệ thống hơn.

### Kỹ thuật 4: Constraint — Đặt ràng buộc rõ ràng

```
Viết hàm validate email trong Java.
Yêu cầu:
- Không dùng thư viện bên ngoài
- Dùng regex
- Có unit test kèm theo
- Comment giải thích regex pattern
```

### Ví dụ thực tế — Debug flaky test

```
Test Selenium của tôi bị flaky — lúc pass lúc fail không rõ lý do.
Môi trường: Selenium 4.x, ChromeDriver, Java 17, TestNG 7.x
Lỗi thường gặp: "Element not interactable" hoặc "StaleElementReferenceException"
Test chạy trên trang có nhiều AJAX call và animation.

Code hiện tại:
[paste code]

Hãy:
1. Giải thích tại sao test bị flaky trong trường hợp này
2. Đề xuất fix cụ thể với code mẫu
3. Best practice để tránh flaky test với dynamic content
```

### Tóm lại 2.1

- Prompt tốt = ngữ cảnh + vấn đề + yêu cầu + code liên quan
- Role prompting → AI trả lời theo góc nhìn chuyên môn
- Few-shot → AI theo đúng format bạn muốn
- Chain-of-thought → AI suy nghĩ có hệ thống, ít sai hơn
- Đầu tư 2 phút viết prompt tốt tiết kiệm 20 phút sửa đi sửa lại

---

## 2.2 Cách AI "nhớ" và "tuân theo" quy tắc

### Vấn đề cốt lõi

AI không có bộ nhớ dài hạn. Mỗi lần mở chat mới = AI quên hết.
Không nhớ project của bạn, không nhớ convention team, không nhớ hôm qua làm gì.

Giải pháp: **Nhúng quy tắc vào context** để AI luôn biết phải làm gì.

### Khái niệm chung — trước khi nói về từng tool

Dù tool nào gọi tên khác nhau, bản chất đều là **cách truyền thông tin nền cho AI**:

| Khái niệm chung | Là gì | Ví dụ |
|---|---|---|
| **Rule / Quy tắc** | Điều AI phải/không được làm | "Không dùng Thread.sleep()" |
| **Skill** | Gói kiến thức/hành vi dạy cho AI | "Biết cách viết POM pattern" |
| **Memory** | AI nhớ thông tin giữa các session | "Nhớ project này dùng Java 17" |
| **Knowledge Base** | Kho tài liệu AI tham chiếu | File spec, API doc, convention |
| **System Prompt** | Lệnh nền chạy ngầm, AI luôn tuân theo | "Luôn viết code có comment" |

### Từng tool gọi thế nào?

**ChatGPT — Custom Instructions + Memory**
```
Settings → Personalization → Custom Instructions

Ô 1 - "What would you like ChatGPT to know about you?":
  Tôi là automation tester, dùng Selenium Java + TestNG + Maven.
  Project theo chuẩn Page Object Model.

Ô 2 - "How would you like ChatGPT to respond?":
  Luôn viết code Java, không dùng Thread.sleep().
  Giải thích ngắn gọn, ưu tiên code example.
```

ChatGPT còn có **Memory** — tự nhớ thông tin bạn chia sẻ qua các session:
```
Bạn: "Nhớ cho tôi là project tôi dùng Java 17 và TestNG"
ChatGPT: "Đã nhớ rồi" → lần sau tự áp dụng
```

**Ví dụ thực tế dùng Memory hàng ngày:**
```
Lần 1 (thứ Hai):
Bạn: "Tôi là automation tester, dùng Selenium Java + TestNG.
      Project theo POM pattern. Nhớ điều này nhé."
ChatGPT: Đã lưu vào memory.

Lần 2 (thứ Ba, mở chat mới):
Bạn: "Viết hàm click element"
ChatGPT: [tự biết dùng Java, Selenium, POM — không cần nhắc lại]
→ Viết đúng Java, đúng pattern, không hỏi lại bạn dùng gì
```

Kiểm tra và quản lý Memory: Settings → Personalization → Manage Memory.

**Claude — Projects + System Prompt**

Claude có **Projects** — tạo project riêng, upload tài liệu, AI nhớ trong project đó:
```
Tạo Project "Automation Framework"
→ Upload: coding-convention.md, pom.xml, BasePage.java
→ AI đọc hết, hiểu context project của bạn
→ Mọi câu hỏi trong project này AI đều biết context
```

**Ví dụ thực tế dùng Claude Projects hàng ngày:**
```
Setup 1 lần:
1. Vào claude.ai → Projects → New Project
2. Đặt tên: "E-commerce Automation"
3. Upload: BasePage.java, coding-convention.md, test-data-guide.md
4. Viết Project Instructions:
   "Đây là automation framework dùng Selenium 4 + Java 17 + TestNG.
    Luôn follow POM pattern. Không dùng Thread.sleep()."

Dùng hàng ngày:
Bạn: "Viết LoginPage cho trang /login"
Claude: [tự biết convention, tự biết BasePage có gì, viết đúng ngay]
→ Không cần paste lại convention mỗi lần
```

**Cursor — .cursorrules / Cursor Rules**

File `.cursorrules` đặt ở root project, AI đọc mỗi khi bạn hỏi:
```
# .cursorrules
You are an expert in Selenium Java automation testing.

Rules:
- Always use Page Object Model pattern
- Never use Thread.sleep(), use WebDriverWait instead
- Prefer By.id > By.cssSelector > By.xpath for selectors
- All test methods must have @Test annotation
- Use explicit waits with ExpectedConditions
```

**Kiro — Steering Files + Skills**

Kiro có 2 cơ chế:

*Steering Files* — đặt trong `.kiro/steering/`, luôn được đính kèm vào context:
```markdown
# .kiro/steering/test-conventions.md
## Automation Framework Conventions

- Framework: Selenium 4 + Java 17 + TestNG + Maven
- Pattern: Page Object Model
- Waits: WebDriverWait only, no Thread.sleep()
- Selectors priority: id > cssSelector > xpath
- Test naming: test_[feature]_[scenario]_[expectedResult]
```

**Hướng dẫn setup Kiro Steering step-by-step:**

```
Bước 1: Tạo thư mục steering
  → Trong project của bạn, tạo thư mục: .kiro/steering/

Bước 2: Tạo file steering
  → Tạo file: .kiro/steering/conventions.md
  → Nội dung ví dụ:

  ## Project Info
  - Framework: Selenium 4 + Java 17 + TestNG + Maven
  - Pattern: Page Object Model (strict)
  - Report: Allure

  ## Coding Rules
  - KHÔNG dùng Thread.sleep() — dùng WebDriverWait
  - Tất cả Page class phải extend BasePage
  - Selector ưu tiên: By.id > By.cssSelector > By.xpath
  - Test method naming: test_[feature]_[scenario]_[expected]

  ## Project Structure
  - src/main/pages/   → Page Object classes
  - src/main/utils/   → Helper, utility classes
  - src/test/tests/   → Test classes

Bước 3: Kiro tự đọc file này
  → Mỗi khi bạn chat với Kiro, nó tự đính kèm file steering
  → Không cần nhắc lại convention mỗi lần

Bước 4: Kiểm tra
  → Hỏi Kiro: "Viết LoginPage"
  → Kiro sẽ tự biết extend BasePage, dùng WebDriverWait, đúng naming
```

Có thể tạo nhiều steering file cho nhiều mục đích:
```
.kiro/steering/
├── conventions.md    ← coding convention (luôn bật)
├── project-info.md   ← thông tin project, tech stack
└── test-strategy.md  ← chiến lược test của team
```

*Skills* — gói kiến thức chuyên biệt, bật/tắt theo nhu cầu:
```
Skill "Write Selenium Test" → AI biết cách viết test đúng chuẩn project
Skill "Review Code Quality" → AI biết tiêu chí review của team
```

**GitHub Copilot — Custom Instructions**

File `.github/copilot-instructions.md` trong repo:
```markdown
# Copilot Instructions

This is a Selenium Java automation project.
- Use TestNG for test framework
- Follow Page Object Model
- Use WebDriverWait for all waits
- Add JavaDoc for all public methods
```

### So sánh nhanh

| Tool | Tên gọi | Cách setup |
|---|---|---|
| ChatGPT | Custom Instructions, Memory | Settings trong web |
| Claude | Projects, System Prompt | Tạo Project, upload file |
| Cursor | .cursorrules | File ở root project |
| Kiro | Steering files, Skills | Folder .kiro/steering/ |
| Copilot | Custom Instructions | File .github/copilot-instructions.md |

### Tóm lại 2.2

- AI không nhớ giữa các session — phải chủ động nhúng quy tắc vào
- Mỗi tool có cách riêng nhưng bản chất giống nhau: truyền context nền cho AI
- Steering/Rules giúp AI luôn theo convention của team mà không cần nhắc lại
- Đây là kỹ năng quan trọng để dùng AI hiệu quả trong team

---

## 2.3 Context & Memory — Hiểu sâu hơn

### AI nhớ gì trong 1 session?

Trong 1 cuộc trò chuyện, AI nhớ **toàn bộ lịch sử chat** cho đến khi đầy context window.

```
Bạn: "Project tôi dùng Java 17"          ← AI nhớ
Bạn: "Viết hàm login"                    ← AI nhớ context trên, viết Java 17
...50 tin nhắn sau...
Bạn: "Viết thêm hàm logout"
→ Nếu context window đầy → AI có thể quên "Java 17" ở đầu
```

### RAG — Tìm đúng thông tin trước khi hỏi AI

**RAG = Retrieval Augmented Generation**

Thay vì nhúng cả project vào context (tốn token), RAG tìm và nhúng **phần liên quan**:

```
Project có 500 file
Bạn hỏi: "Fix bug trong LoginPage"

RAG làm:
1. Tìm file liên quan: LoginPage.java, LoginTest.java, BasePage.java
2. Chỉ nhúng 3 file đó vào context
3. Gửi cho AI kèm câu hỏi

→ Tiết kiệm token, AI trả lời đúng hơn
```

Cursor, Kiro dùng RAG để index codebase — khi bạn hỏi, tool tự biết cần đọc file nào.

### MCP — Kết nối AI với thế giới bên ngoài

**MCP = Model Context Protocol** — chuẩn do Anthropic tạo ra năm 2024.

Trước MCP: mỗi tool tự làm connector riêng → loạn, không thống nhất
Sau MCP: 1 chuẩn duy nhất → AI kết nối được với mọi thứ

```
AI ←→ MCP ←→ GitHub    (đọc issue, PR, tạo branch)
           ←→ Jira      (đọc ticket, cập nhật status)
           ←→ Database  (query trực tiếp)
           ←→ Browser   (mở trang, click, lấy data)
           ←→ Slack     (gửi message, đọc channel)
           ←→ File system (đọc/ghi file local)
```

Ví dụ thực tế với tester:
```
"Lấy tất cả bug label 'regression' trên Jira sprint này và viết test case"
→ AI dùng MCP đọc Jira → lấy danh sách → tự viết test case → tạo file
```

### Tóm lại 2.3

- AI nhớ trong session, quên khi mở chat mới
- RAG = tìm đúng file liên quan, không nhúng cả project
- MCP = chuẩn kết nối AI với GitHub, Jira, DB, Browser...
- Các tool như Cursor, Kiro đã tích hợp sẵn RAG và MCP

---

## 2.4 Agent — Hiểu cơ bản trước khi dùng

### Agent là gì?

**Chat thường:** Bạn hỏi → AI trả lời → Bạn làm theo → Bạn hỏi tiếp...
**Agent:** Bạn giao nhiệm vụ → AI tự lên kế hoạch và thực thi đến khi xong

```
// Chat thường — bạn phải dẫn dắt từng bước:
Bạn: "Viết LoginPage.java theo POM"
AI: [trả lời code]
Bạn: copy vào file
Bạn: "Viết LoginTest.java cho LoginPage đó"
AI: [trả lời]
Bạn: copy tiếp...

// Agent — bạn chỉ cần giao việc:
Bạn: "Thêm login feature vào automation framework theo POM"
AI: [tự đọc project → tạo LoginPage.java → tạo LoginTest.java
     → cập nhật BasePage nếu cần → chạy test → fix lỗi → báo cáo]
```

### Vòng lặp Agent

Agent hoạt động theo vòng lặp: **Suy nghĩ → Hành động → Quan sát → Lặp lại**

```
Thought: Cần đọc BasePage.java để hiểu structure hiện tại
Action:  read_file("src/main/BasePage.java")
Observe: [nội dung file]

Thought: Tạo LoginPage theo pattern của BasePage
Action:  write_file("src/main/LoginPage.java", [...])
Observe: File created ✓

Thought: Chạy test để verify
Action:  run_command("mvn test -Dtest=LoginTest")
Observe: BUILD SUCCESS ✓

Done: Đã tạo LoginPage.java và LoginTest.java
```

### Autopilot vs Supervised

**Autopilot** — AI làm hết, bạn xem kết quả sau:
- Phù hợp: tác vụ rõ ràng, project không quá quan trọng
- Rủi ro: AI có thể sửa file bạn không muốn

**Supervised** — AI làm từng bước, bạn approve từng thay đổi:
- Phù hợp: project quan trọng, muốn kiểm soát chặt
- Kiro hỗ trợ mode này — hiện diff từng file, bạn accept/reject

### Tóm lại 2.4

- Agent = AI tự lên kế hoạch và thực thi nhiều bước
- Khác chat thường ở chỗ: AI chủ động, không cần bạn dẫn dắt từng bước
- Autopilot = tự làm hết, Supervised = approve từng bước
- Chi tiết hơn về Agent ở [Phần 6](./06-ai-nang-cao.md)

---

**Tiếp theo:** [Phần 3 — Công cụ AI](./03-cong-cu-ai.md)
