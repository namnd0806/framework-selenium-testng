# Phần 7 — Xu hướng & Tương lai

> Phần này không cần học thuộc — đọc để biết ngành đang đi đâu và chuẩn bị trước.

---

## 7.1 Reasoning Model — Thế hệ AI mới

### Khác gì model thường?

**Model thường:** nhận câu hỏi → trả lời ngay (fast thinking)
**Reasoning model:** nhận câu hỏi → **suy nghĩ trước** → trả lời (slow thinking)

Giống như:
- Bạn hỏi "2+2 bằng mấy?" → trả lời ngay: 4
- Bạn hỏi "Thiết kế database cho hệ thống đặt vé máy bay" → cần suy nghĩ kỹ trước

Reasoning model "nháp" ra giấy trước (chain of thought nội bộ), rồi mới trả lời.

### Các Reasoning Model nổi bật

| Model | Công ty | Đặc điểm |
|---|---|---|
| **o1** | OpenAI | Đầu tiên, mạnh về toán và logic |
| **o3** | OpenAI | Mạnh nhất hiện tại, đắt nhất |
| **o4-mini** | OpenAI | Nhanh hơn, rẻ hơn o3, vẫn mạnh |
| **Claude 3.7 Sonnet** | Anthropic | Hybrid: vừa nhanh vừa có thể reasoning |
| **DeepSeek R1** | DeepSeek | Open source, chất lượng ngang o1, **miễn phí** |
| **Gemini 2.0 Flash Thinking** | Google | Nhanh + reasoning |
| **QwQ** | Alibaba | Open source, mạnh về toán/code |

### Khi nào dùng Reasoning Model?

**Nên dùng:**
- Thiết kế architecture phức tạp
- Debug bug khó, nhiều nguyên nhân có thể
- Phân tích security vulnerability
- Bài toán thuật toán phức tạp
- Review code tìm logic error tinh vi

**Không cần dùng (tốn tiền/thời gian):**
- Viết boilerplate code
- Giải thích concept đơn giản
- Format code, viết comment
- Câu hỏi có câu trả lời rõ ràng

Ví dụ thực tế:
```
Bug: Race condition trong multi-thread test
→ Model thường: đưa ra giải pháp chung chung
→ o3: suy nghĩ 30 giây, phân tích từng thread, tìm đúng root cause
```

---

## 7.2 Vibe Coding — AI build cả app

### Vibe Coding là gì?

Khái niệm do **Andrej Karpathy** (cựu OpenAI, cựu Tesla AI) đặt ra năm 2025:

> "Bạn mô tả bằng lời, AI build cả app, bạn không cần đọc từng dòng code"

Không phải thay thế developer — mà là cách **prototype cực nhanh**.

### Các tool Vibe Coding

**Lovable** (lovable.dev)
- Mô tả app → AI tạo full-stack app (React + Supabase)
- Có thể connect GitHub, deploy lên Vercel
- Phù hợp: SaaS app, dashboard, tool nội bộ

**Bolt.new** (bolt.new)
- Chạy trên browser, không cần cài gì
- Tạo app chạy được ngay trong browser
- Miễn phí tier rộng
- Phù hợp: prototype nhanh, demo

**v0 (Vercel)** (v0.dev)
- Chuyên tạo React UI component
- Mô tả UI → AI tạo code Tailwind + shadcn/ui
- Phù hợp: frontend developer cần UI nhanh

**GitHub Spark**
- Của GitHub, tích hợp với ecosystem GitHub
- Tạo "micro app" từ mô tả tự nhiên

### Ứng dụng thực tế cho tester/SDET

```
Tình huống: Cần tool nội bộ quản lý test case nhanh

Thay vì: mất 2 tuần build từ đầu
Dùng Bolt.new:
"Tạo web app quản lý test case với:
- Bảng danh sách test case, filter theo status/priority
- Form thêm/sửa test case
- Chart tỷ lệ pass/fail theo sprint
- Export ra Excel
Tech: React, lưu localStorage"

→ Có app chạy được trong 10 phút
→ Dùng làm prototype, demo cho team
→ Nếu cần production: thuê dev build proper
```

**Giới hạn của Vibe Coding:**
- Code thường không clean, khó maintain
- Không phù hợp production app phức tạp
- Cần người biết code để review và fix khi AI sai
- Không thay thế được SDET/developer thật

---

## 7.3 AI-native IDE — Tương lai của IDE

### Xu hướng

IDE truyền thống (VS Code) + AI extension = AI là tính năng **phụ**
AI-native IDE = AI là **trung tâm**, mọi thứ xây quanh AI

**Hiện tại (2025-2026):**
- Cursor và Windsurf đang dẫn đầu
- VS Code đang bắt kịp với Copilot ngày càng mạnh hơn
- JetBrains tích hợp AI sâu hơn vào IntelliJ, PyCharm

**Dự đoán 2027+:**
- Hầu hết developer dùng AI-native IDE
- IDE sẽ tự động suggest refactor, tìm bug trước khi bạn chạy
- AI sẽ hiểu toàn bộ codebase và business context

---

## 7.4 AI sẽ thay thế QC/Developer không?

### Thực tế hiện tại (2026)

**AI giỏi:**
- Viết boilerplate code, CRUD, utility function
- Fix bug đơn giản, rõ ràng
- Viết unit test, test case cơ bản từ spec
- Giải thích code, viết documentation
- Refactor theo pattern có sẵn
- Tìm syntax error, common bug patterns

**AI kém:**
- Hiểu business logic phức tạp, domain-specific
- Architecture decision (trade-off, context-dependent)
- Debug race condition, distributed system
- Exploratory testing — tìm bug không ai nghĩ đến
- Giao tiếp với stakeholder, clarify yêu cầu mơ hồ
- Đánh giá UX, accessibility từ góc nhìn người dùng thật
- Quyết định release hay không (risk assessment)

### Tác động thực tế với nghề

**Tester/QC:**
- Manual testing đơn giản, repetitive → AI dần thay thế
- Test case viết từ spec rõ ràng → AI làm được
- Exploratory testing, domain expertise → vẫn cần người
- **Kỹ năng mới cần có:** biết dùng AI tool, review output của AI

**Automation Tester:**
- Viết script cơ bản → AI làm nhanh hơn
- Framework design, architecture → vẫn cần người
- **Kỹ năng mới cần có:** SDET mindset, biết build tool với AI

**SDET/Developer:**
- Coding productivity tăng 2-5x với AI
- Vai trò chuyển từ "viết code" sang "review và guide AI"
- **Kỹ năng mới cần có:** system thinking, AI collaboration

### Kết luận thực tế

```
Người dùng AI tốt > Người không dùng AI
(không phải AI > người)
```

- AI không thay thế tester/developer — nhưng **người dùng AI sẽ thay thế người không dùng**
- Kỹ năng quan trọng nhất: biết cách làm việc hiệu quả với AI
- Đầu tư học AI tool ngay bây giờ = lợi thế cạnh tranh lớn

---

## 7.5 Timeline AI Coding (2022 → 2026)

```
2022
├── Nov: ChatGPT ra mắt → bùng nổ AI
└── Dec: GitHub Copilot GA (generally available)

2023
├── Mar: GPT-4 ra mắt — bước nhảy lớn về chất lượng
├── Mar: Claude 1 ra mắt (Anthropic)
├── May: Google Bard (nay là Gemini)
└── Nov: Cursor ra mắt — IDE AI-native đầu tiên phổ biến

2024
├── Mar: Claude 3 (Haiku, Sonnet, Opus) — context 200K
├── May: GPT-4o — nhanh hơn, multimodal
├── Sep: o1 ra mắt — reasoning model đầu tiên
├── Nov: MCP (Model Context Protocol) ra mắt
└── Dec: Windsurf IDE ra mắt

2025
├── Jan: DeepSeek R1 — open source ngang o1, gây chấn động
├── Feb: Claude 3.7 Sonnet — hybrid reasoning
├── Mar: GPT-4.5, o3 ra mắt
├── Apr: Vibe coding trend bùng nổ
└── Mid: Kiro ra mắt (Amazon)

2026
├── AI agent trở nên phổ biến trong daily workflow
├── Multi-agent systems trong production
└── AI-native IDE chiếm >50% thị phần
```

---

## Tóm lại Phần 7

- Reasoning model (o1, o3, DeepSeek R1) mạnh hơn cho bài toán phức tạp — dùng đúng chỗ
- Vibe coding phù hợp prototype nhanh, không thay thế dev/tester thật
- AI-native IDE đang thay thế dần IDE truyền thống
- AI không thay thế người — người dùng AI tốt sẽ thay thế người không dùng
- Học AI tool ngay bây giờ = lợi thế cạnh tranh

---

**Quay lại:** [README — Mục lục tổng](./README.md)
