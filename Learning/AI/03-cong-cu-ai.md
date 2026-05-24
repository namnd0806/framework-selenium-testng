# Phần 3 — Công cụ AI

> Biết tool nào tồn tại, mạnh gì, yếu gì — để chọn đúng cho từng việc.

---

## 3.1 Nhóm Chat thuần — Dùng qua web, không cần cài

### ChatGPT (OpenAI) — chat.openai.com

Phổ biến nhất, điểm khởi đầu của hầu hết mọi người.

| | Chi tiết |
|---|---|
| Ra mắt | Tháng 11/2022 |
| Model miễn phí | GPT-4o mini |
| Model trả phí | GPT-4o, o1, o3 ($20/tháng) |
| Điểm mạnh | Đa năng, plugin/tool phong phú, web search |
| Điểm yếu | Context window nhỏ hơn Claude |

Tính năng đáng chú ý:
- **Web search** — tìm kiếm internet real-time
- **Code interpreter** — chạy Python code ngay trong chat
- **Custom GPTs** — tạo chatbot riêng với instruction và knowledge base
- **Memory** — nhớ thông tin giữa các session

### Claude (Anthropic) — claude.ai

Được nhiều developer/tester đánh giá cao nhất cho coding.

| | Chi tiết |
|---|---|
| Ra mắt | 2023 |
| Model | Haiku (nhanh) → Sonnet (cân bằng) → Opus (mạnh nhất) |
| Điểm mạnh | Context 200K, code sạch, ít hallucinate, Projects |
| Điểm yếu | Đôi khi từ chối tác vụ vì safety policy |

Tính năng đáng chú ý:
- **Projects** — tạo project riêng, upload file, AI nhớ context
- **Artifacts** — hiển thị code/UI preview ngay trong chat
- **Extended thinking** — Claude 3.7 có thể "suy nghĩ" trước khi trả lời

### Gemini (Google) — gemini.google.com

Mạnh nhất về tích hợp hệ sinh thái Google.

| | Chi tiết |
|---|---|
| Ra mắt | 2023 (tên cũ: Bard) |
| Model | Flash (nhanh/rẻ) → Pro → Ultra |
| Điểm mạnh | Context 2M token, tích hợp Google Workspace, web search |
| Điểm yếu | Coding không mạnh bằng GPT-4o hay Claude |

Tính năng đáng chú ý:
- **Google Workspace integration** — đọc Gmail, Drive, Docs, Sheets
- **Gemini in Google Docs** — AI ngay trong tài liệu
- **Context 2M token** — paste cả codebase lớn vào vẫn được

### So sánh cho dev/tester

| | ChatGPT | Claude | Gemini |
|---|---|---|---|
| Viết code mới | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| Debug lỗi | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| Hiểu codebase lớn | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Viết test case | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| Web search | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| Miễn phí | Giới hạn | Giới hạn | Khá rộng |

---

## 3.2 Nhóm Extension — Cài thêm vào IDE

### GitHub Copilot (2022) — Người tiên phong

| | Chi tiết |
|---|---|
| Phát triển bởi | GitHub + OpenAI |
| Model | GPT-4o |
| Giá | $10/tháng cá nhân, miễn phí cho sinh viên |
| Tích hợp | VS Code, JetBrains, Vim, Neovim |

Tính năng chính:
- **Inline suggestion** — gõ đến đâu AI gợi ý đến đó, Tab để chấp nhận
- **Copilot Chat** — chat trong IDE, hỏi về code đang mở
- **Copilot Workspace** — agent mode, làm nhiều file cùng lúc

```java
// Bạn gõ comment:
// wait for element clickable then click with retry on StaleElement

// Copilot gợi ý ngay:
public void clickWithRetry(By locator) {
    int attempts = 0;
    while (attempts < 3) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
            return;
        } catch (StaleElementReferenceException e) {
            attempts++;
        }
    }
}
```

**Custom Instructions cho Copilot:**
Tạo file `.github/copilot-instructions.md` trong repo để AI theo convention của team.

### Kiro (2025) — Bạn đang dùng

| | Chi tiết |
|---|---|
| Phát triển bởi | Amazon/AWS |
| Model | Claude Sonnet |
| Giá | Đang trong giai đoạn preview |
| Tích hợp | VS Code |

Tính năng nổi bật:
- **Spec workflow** — Requirements → Design → Tasks → Implementation
- **Steering files** — `.kiro/steering/*.md` luôn được đính kèm vào context
- **Skills** — gói kiến thức chuyên biệt, bật/tắt theo nhu cầu
- **Hooks** — tự động trigger AI theo sự kiện (save file, push code...)
- **Autopilot / Supervised mode**
- **MCP support** — kết nối với GitHub, Jira, DB...

Steering files ví dụ:
```markdown
# .kiro/steering/conventions.md
## Project: E-commerce Automation Framework

Tech stack: Selenium 4 + Java 17 + TestNG + Maven + Allure

Conventions:
- Pattern: Page Object Model
- Waits: WebDriverWait only, timeout 10s default
- Selectors: id > cssSelector > xpath
- Test naming: test_[feature]_[scenario]_[expected]
- All pages extend BasePage
```

### Codeium / Windsurf Extension (2022)

| | Chi tiết |
|---|---|
| Phát triển bởi | Codeium |
| Giá | Miễn phí (tier rộng) |
| Tích hợp | VS Code, JetBrains, Vim, 40+ IDE |

Điểm mạnh: hoàn toàn miễn phí, hỗ trợ nhiều IDE nhất.
Phù hợp khi: không muốn trả tiền, dùng JetBrains (IntelliJ, PyCharm...).

---

## 3.3 Nhóm IDE AI-native — IDE xây riêng cho AI

### Cursor (2023) — Phổ biến nhất hiện tại

Không chỉ là extension — là **IDE riêng** fork từ VS Code (giao diện y hệt, plugin tương thích).

| | Chi tiết |
|---|---|
| Giá | Free (giới hạn) / $20/tháng Pro |
| Model | Chọn được: GPT-4o, Claude Sonnet, Gemini... |
| Tích hợp | Standalone IDE |

Tính năng killer:
- **Composer** — chat với AI, AI sửa nhiều file cùng lúc
- **Cursor Rules** — file `.cursorrules` quy định convention
- **@codebase** — hỏi AI về toàn bộ codebase
- **Tab completion** — thông minh hơn Copilot, đoán cả đoạn code dài

Ví dụ dùng Cursor:
```
Bạn: "@codebase Refactor toàn bộ test suite theo Page Object Model,
      hiện tại đang dùng driver trực tiếp trong test"

Cursor: [đọc tất cả file test → tạo Page classes → refactor test → báo cáo]
```

**.cursorrules ví dụ:**
```
You are an expert Selenium Java automation engineer.

# Project Context
- Framework: Selenium 4 + TestNG + Maven
- Pattern: Page Object Model (strict)
- Java version: 17

# Rules
- Never use Thread.sleep() - use WebDriverWait
- All page classes must extend BasePage
- Use @FindBy annotation for element locators
- Test methods must be independent (no order dependency)
```

### Windsurf IDE (2024)

Tương tự Cursor, cũng fork từ VS Code.

| | Chi tiết |
|---|---|
| Giá | Free tier rộng hơn Cursor |
| Model | Claude, GPT-4o |
| Tính năng đặc biệt | **Cascade** — AI hiểu flow làm việc của bạn |

Phù hợp khi: muốn dùng IDE AI-native nhưng không muốn trả $20/tháng.

---

## 3.4 Nhóm CLI — Chạy trong Terminal

Phù hợp khi: cần automation, tích hợp CI/CD, không muốn mở IDE.

### Claude Code

```bash
# Cài đặt
npm install -g @anthropic-ai/claude-code

# Dùng
claude                                    # mở interactive mode
claude "fix tất cả flaky test trong tests/"
claude "review code và tìm security issue"
```

### Aider

```bash
# Cài đặt
pip install aider-chat

# Dùng với GPT-4o
aider --model gpt-4o LoginTest.java BasePage.java

# Dùng với Claude
aider --model claude-3-5-sonnet-20241022 LoginTest.java
```

Aider tự động commit code sau mỗi thay đổi — tiện cho workflow git.

### Gemini CLI

```bash
# Cài đặt
npm install -g @google/gemini-cli

# Dùng
gemini "explain this error: NullPointerException at line 45"
```

### GitHub Copilot CLI

```bash
# Cài đặt (cần GitHub Copilot subscription)
gh extension install github/gh-copilot

# Dùng
gh copilot suggest "tìm tất cả file Java sửa trong 7 ngày qua"
gh copilot explain "git rebase -i HEAD~3"
```

---

## 3.5 So sánh & Chọn tool phù hợp

### Theo nhu cầu

| Nhu cầu | Tool đề xuất |
|---|---|
| Mới bắt đầu, thử AI | ChatGPT hoặc Claude (web) |
| Inline suggestion khi code | GitHub Copilot hoặc Codeium (miễn phí) |
| Multi-file edit, refactor lớn | Cursor hoặc Windsurf |
| Dùng trong team, có spec/task | Kiro |
| Automation, CI/CD pipeline | Claude Code hoặc Aider (CLI) |
| Không muốn trả tiền | Codeium + Gemini web |
| Bảo mật cao, không muốn gửi code lên cloud | Ollama local (xem Phần 6) |

### Theo vai trò

| Vai trò | Tool đề xuất | Lý do |
|---|---|---|
| Manual Tester | Claude web + ChatGPT | Viết test case, phân tích bug |
| Automation Tester | Kiro hoặc Cursor + Copilot | IDE integration, multi-file |
| SDET | Cursor + Claude Code CLI | IDE + automation pipeline |
| Developer | Cursor hoặc Kiro | Full workflow |

### Gợi ý setup ban đầu (không tốn tiền)

```
1. Cài Kiro (đang dùng rồi) → dùng cho project chính
2. Dùng Claude.ai web (free tier) → hỏi nhanh, paste code
3. Dùng Gemini web (free) → khi cần context dài hoặc web search
→ Tổng chi phí: $0
```

---

**Tiếp theo:** [Phần 4 — Ứng dụng vào quy trình QC](./04-ung-dung-vao-quy-trinh-qc.md)
