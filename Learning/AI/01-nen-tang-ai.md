# Phần 1 — Nền tảng AI

> Đọc phần này trước. Hiểu được phần này thì các phần sau sẽ dễ hơn nhiều.

---

## 1.1 AI là gì — Giải thích đơn giản nhất

**AI (Artificial Intelligence)** = máy tính làm được những việc mà trước đây chỉ con người làm được.

Ví dụ dễ hiểu:
- Nhận ra khuôn mặt trong ảnh → AI
- Dịch văn bản từ tiếng Anh sang tiếng Việt → AI
- Trả lời câu hỏi, viết code, debug lỗi → AI (loại bạn đang dùng)

**AI cần gì để hoạt động?**

AI không chạy trên máy tính bình thường của bạn (trừ khi bạn chạy local — xem Phần 6).
Các AI như ChatGPT, Claude, Gemini chạy trên **server cực mạnh của công ty**:

```
Bạn gõ câu hỏi trên máy tính
        ↓ (gửi qua internet)
Server của OpenAI/Anthropic/Google
(hàng nghìn GPU, tiêu thụ điện như cả tòa nhà)
        ↓ (xử lý, trả kết quả)
Bạn nhận được câu trả lời
```

Đó là lý do:
- Cần internet để dùng ChatGPT, Claude, Gemini
- Các công ty tốn hàng tỷ đô để vận hành
- Bạn trả $20/tháng hoặc dùng miễn phí có giới hạn

---

## 1.2 Các loại AI — Từ đơn giản đến phức tạp

Không phải AI nào cũng giống nhau. Có nhiều loại, mỗi loại làm việc khác nhau:

**AI cũ (Rule-based AI) — trước 2015:**
Lập trình cứng từng rule. Ví dụ:
```
NẾU email có "@" VÀ có "." SAU "@"
THÌ email hợp lệ
NGƯỢC LẠI email không hợp lệ
```
Giới hạn: chỉ làm được đúng những gì được lập trình sẵn.

**Machine Learning — máy tự học:**
Thay vì lập trình rule, cho máy xem **hàng triệu ví dụ** rồi tự tìm ra rule:
```
Cho máy xem 1 triệu email:
- 500.000 email hợp lệ (gắn nhãn "valid")
- 500.000 email không hợp lệ (gắn nhãn "invalid")
→ Máy tự học ra rule để phân biệt
```
Mạnh hơn AI cũ vì tự học được pattern phức tạp.

**Deep Learning — học sâu hơn:**
Dùng mạng neural (mô phỏng não người) với nhiều lớp xử lý.
Học được pattern rất phức tạp như: nhận diện khuôn mặt, hiểu giọng nói, hiểu ngôn ngữ.

**LLM — chuyên về ngôn ngữ:**
Deep Learning áp dụng riêng cho ngôn ngữ (text, code).
Train trên hàng tỷ trang văn bản → hiểu và tạo ra ngôn ngữ tự nhiên.
GPT, Claude, Gemini đều là LLM.

**Tóm tắt bằng hình ảnh:**
```
AI cũ          → Robot làm theo kịch bản cố định
Machine Learning → Học từ ví dụ, tự tìm ra rule
Deep Learning   → Học được pattern rất phức tạp
LLM             → Chuyên về ngôn ngữ và code
ChatGPT/Claude  → Sản phẩm bạn đang dùng, là LLM
```

---

## 1.3 LLM tự học như thế nào? Ai train?

### Ai train?

Các công ty lớn train LLM:
- **OpenAI** train GPT-4, GPT-4o, o1...
- **Anthropic** train Claude
- **Google** train Gemini
- **Meta** train Llama (open source, ai cũng dùng được)

Train LLM tốn **hàng trăm triệu đến hàng tỷ đô la** vì cần:
- Hàng nghìn GPU đắt tiền (NVIDIA H100, ~$30.000/cái)
- Điện năng khổng lồ
- Đội ngũ kỹ sư AI hàng trăm người
- Vài tháng đến vài năm để train

### Train cụ thể là làm gì?

Hình dung đơn giản nhất:

**Bước 1 — Thu thập dữ liệu:**
```
Thu thập hàng tỷ trang văn bản:
- Toàn bộ Wikipedia
- Hàng triệu cuốn sách
- Hàng tỷ trang web
- Hàng triệu repo code trên GitHub
- Diễn đàn, Stack Overflow, Reddit...
```

**Bước 2 — Học dự đoán từ tiếp theo:**
```
Cho model xem câu: "Hàm này bị lỗi vì ___"
Model đoán: "thiếu null check"
Đúng → tăng xác suất cho đáp án này
Sai  → giảm xác suất, học lại

Lặp đi lặp lại hàng tỷ lần với hàng tỷ câu
→ Model học được cách ngôn ngữ hoạt động
→ Học được logic, code, kiến thức từ văn bản
```

**Bước 3 — Fine-tuning (tinh chỉnh):**
```
Sau khi train cơ bản, tinh chỉnh để model:
- Trả lời như assistant (không phải chỉ hoàn thành câu)
- Từ chối yêu cầu có hại
- Theo format nhất định
```

### AI có học tài liệu nội bộ của công ty không?

**Câu trả lời ngắn: KHÔNG** — trừ khi bạn chủ động cấp cho nó.

```
Model GPT-4, Claude... được train trên dữ liệu công khai.
Chúng KHÔNG biết:
- Tài liệu nội bộ công ty bạn
- Code private repo của bạn
- Quy trình, convention riêng của team
```

**Để AI biết tài liệu nội bộ, có 3 cách:**

1. **Paste trực tiếp vào chat** — đơn giản nhất, nhưng phải làm lại mỗi lần
```
Bạn: "Đây là coding convention của team tôi: [paste file]
      Bây giờ review code này theo convention đó"
```

2. **Steering files / Custom Instructions** — cấu hình 1 lần, AI nhớ mãi
```
Kiro: .kiro/steering/conventions.md
Cursor: .cursorrules
ChatGPT: Custom Instructions
→ AI tự động áp dụng mỗi khi bạn hỏi
```

3. **RAG (Retrieval Augmented Generation)** — cho AI truy cập kho tài liệu
```
Upload toàn bộ tài liệu nội bộ vào hệ thống
→ Khi hỏi, AI tự tìm tài liệu liên quan rồi trả lời
→ Dùng trong Claude Projects, hoặc tự build với LangChain
```

---

## 1.4 Model là gì? Phân biệt Model vs Tool vs Platform

Đây là khái niệm **hay bị nhầm nhất**. Nhiều người nghĩ ChatGPT = 1 thứ duy nhất — thực ra không phải.

**Hình dung như điện thoại:**
- **ChatGPT** = cái điện thoại (giao diện, app bạn cầm tay)
- **GPT-4o** = con chip bên trong (model — bộ não thật sự xử lý)

```
Tool / Platform (giao diện bạn dùng)    Model (bộ não bên trong)
────────────────────────────────────────────────────────────────
ChatGPT (chat.openai.com)           →   GPT-4o mini / GPT-4o / o1 / o3
Claude (claude.ai)                  →   Claude Haiku / Sonnet / Opus
Gemini (gemini.google.com)          →   Gemini Flash / Pro / Ultra
Cursor (IDE)                        →   Bạn chọn: GPT-4o hoặc Claude Sonnet
Kiro (IDE extension)                →   Claude Sonnet
GitHub Copilot                      →   GPT-4o
```

**Tại sao cùng 1 tool lại có nhiều model?**

| Loại model | Đặc điểm | Dùng khi nào |
|---|---|---|
| Model nhỏ (Flash, Haiku, mini) | Nhanh, rẻ | Việc đơn giản: viết comment, giải thích |
| Model vừa (Sonnet, Pro, 4o) | Cân bằng | Việc hàng ngày: debug, viết test |
| Model lớn (Opus, o3, Ultra) | Chậm, đắt, mạnh | Việc phức tạp: thiết kế architecture |

---

## 1.5 Các công ty AI chính

| Công ty | Quốc gia | Model nổi bật | Đặc điểm |
|---|---|---|---|
| **OpenAI** | Mỹ | GPT-4o, o1, o3 | Phổ biến nhất, ChatGPT |
| **Anthropic** | Mỹ | Claude 3.5/3.7 Sonnet, Opus | An toàn, context dài, code sạch |
| **Google** | Mỹ | Gemini 1.5/2.0 Pro, Flash | Tích hợp Google, context khổng lồ |
| **Meta** | Mỹ | Llama 3.1, 3.2 | Open source, miễn phí, chạy local |
| **Mistral** | Pháp | Mistral 7B, Mixtral | Open source, nhẹ, nhanh |
| **DeepSeek** | Trung Quốc | DeepSeek R1, V3 | Open source, mạnh ngang o1, miễn phí |
| **Microsoft** | Mỹ | Copilot, Azure AI | Dùng model OpenAI, tích hợp Office |
| **Amazon/AWS** | Mỹ | Bedrock, Kiro | Nhiều model, tích hợp AWS |

---

## 1.6 Token là gì?

Token là **đơn vị nhỏ nhất** mà AI đọc — không phải từ, không phải ký tự, mà là mảnh văn bản:

```
"hello"           → 1 token
"unhappy"         → 2 token  (un + happy)
"xin chào"        → 3-4 token
1 dòng code       → khoảng 5-15 token
1000 từ tiếng Anh → khoảng 750 token
```

**Tại sao quan trọng?**
- Dùng API → trả tiền theo số token (input + output)
- AI có giới hạn token trong 1 cuộc trò chuyện → gọi là **context window**

---

## 1.7 Context Window là gì?

Là **bộ nhớ tạm** của AI trong 1 cuộc trò chuyện. Vượt quá giới hạn → AI quên phần đầu.

| Model | Context Window | Tương đương |
|---|---|---|
| GPT-3.5 (2022) | 4K token | ~3 trang A4 |
| GPT-4 (2023) | 8K-32K token | ~25 trang A4 |
| GPT-4o (2024) | 128K token | ~100 trang A4 |
| Claude 3.5 Sonnet | 200K token | ~150.000 từ |
| Gemini 1.5 Pro | 1 triệu token | Cả cuốn sách dày |
| Gemini 2.0 | 2 triệu token | Cả dự án lớn |

> **Codebase** = toàn bộ code của 1 dự án (tất cả file, thư mục).
> Ví dụ: project automation framework của bạn với 50 file Java = codebase của bạn.
> Claude 200K token = paste cả codebase 10.000 dòng code vào vẫn còn chỗ.

**Lưu ý:** Context window là bộ nhớ **trong 1 session**. Mở chat mới = AI quên hết.

---

## 1.8 Hallucination — AI bịa thông tin

AI **bịa ra thông tin trông có vẻ đúng nhưng sai hoàn toàn**. Nguy hiểm vì AI nói rất tự tin.

```java
// Bạn hỏi:
"Hàm Arrays.sortDescending() trong Java dùng thế nào?"

// AI trả lời rất tự tin:
"Bạn dùng Arrays.sortDescending(arr) để sắp xếp giảm dần..."

// Thực tế:
// Hàm đó KHÔNG TỒN TẠI trong Java → AI bịa ra
```

Tại sao AI bịa? Vì AI không "biết" sự thật — nó chỉ **dự đoán token tiếp theo có xác suất cao nhất**. Đôi khi xác suất cao nhất lại là thông tin sai.

**Nguyên tắc:** Luôn chạy thử code AI viết, đừng tin 100% mà không kiểm tra.

---

## 1.9 Cutoff Date — AI không biết thông tin mới

Ngày AI ngừng học dữ liệu mới. Sau ngày đó nó không biết gì thêm.

| Model | Cutoff |
|---|---|
| GPT-4 | Tháng 4/2023 |
| GPT-4o | Tháng 10/2023 |
| Claude 3.7 Sonnet | Tháng 11/2024 |
| Gemini 2.0 | Đầu 2025 |

> Ví dụ: Hỏi về thư viện ra mắt sau cutoff → AI không biết hoặc bịa.

Một số tool có **web search** để vượt qua giới hạn này (ChatGPT, Gemini, Perplexity).

---

## 1.10 Open Source vs Closed Source

**Closed Source** (GPT-4, Claude, Gemini):
- Công ty giữ bí mật code và cách train
- Chỉ dùng qua API hoặc tool của họ
- Thường mạnh hơn, phải trả tiền

**Open Source** (Llama, Mistral, DeepSeek):
- Công khai code, ai cũng tải về dùng được
- Tải về chạy trên máy mình (dùng Ollama — xem Phần 6)
- Miễn phí, bảo mật hơn vì code không rời khỏi máy
- Yếu hơn một chút so với model lớn của OpenAI/Anthropic

---

## 1.11 API là gì và khi nào dùng?

**API** = cổng kết nối để phần mềm gọi AI trực tiếp, không qua giao diện web.

Hình dung:
- Dùng **ChatGPT web** = vào nhà hàng, gọi món, ngồi ăn tại chỗ ($20/tháng cố định)
- Dùng **OpenAI API** = gọi đồ ăn về nhà, tự bày biện theo ý mình (trả theo lượng dùng)

Khi nào dùng API:
- Muốn tích hợp AI vào script/tool tự viết
- Muốn tự động hóa (chạy không cần người)
- Muốn dùng nhiều hơn giới hạn gói $20/tháng

**Giá API thực tế (tham khảo 2025):**

| Model | Giá per 1M token input | Giá per 1M token output |
|---|---|---|
| GPT-4o mini | $0.15 | $0.60 |
| GPT-4o | $2.50 | $10.00 |
| Claude Haiku | $0.25 | $1.25 |
| Claude Sonnet | $3.00 | $15.00 |
| Gemini 1.5 Flash | $0.075 | $0.30 |

> $1 dùng GPT-4o mini ≈ 6 triệu token input — rất rẻ cho automation script.

---

## 1.12 MCP — Kết nối AI với thế giới bên ngoài

### MCP là gì?

**MCP = Model Context Protocol** — chuẩn kết nối do Anthropic tạo ra năm 2024.

**Vấn đề trước MCP:**
AI chỉ biết những gì bạn paste vào chat. Muốn AI đọc file, query database, hay đọc Jira ticket → mỗi tool phải tự làm connector riêng → loạn, không thống nhất.

**MCP giải quyết:**
Tạo ra 1 chuẩn duy nhất để AI kết nối với bất kỳ tool nào:

```
Không có MCP:
AI ←→ [connector tự làm] ←→ GitHub
AI ←→ [connector khác]   ←→ Jira
AI ←→ [connector khác]   ←→ Database
(mỗi cái làm khác nhau, không thống nhất)

Có MCP:
AI ←→ MCP ←→ GitHub
           ←→ Jira
           ←→ Database
           ←→ Browser
           ←→ File system
(1 chuẩn duy nhất, ai cũng implement được)
```

### MCP hoạt động thế nào?

```
1. Bạn cài MCP server cho tool muốn kết nối (ví dụ: GitHub MCP server)
2. AI biết có tool này và có thể dùng
3. Khi bạn hỏi liên quan → AI tự gọi tool để lấy thông tin

Ví dụ:
Bạn: "Tạo test case cho bug #123 trên Jira"
AI:  → Gọi Jira MCP server → Đọc bug #123
     → Hiểu nội dung bug
     → Viết test case
```

### Các loại MCP server phổ biến

| MCP Server | Làm gì | Ứng dụng với tester |
|---|---|---|
| **GitHub MCP** | Đọc/tạo issue, PR, code | Đọc bug report, tạo test từ issue |
| **Jira MCP** | Đọc/cập nhật ticket | Lấy requirement, cập nhật test status |
| **Browser MCP** | Mở trang, click, lấy data | Tự động hóa thao tác web |
| **Database MCP** | Query database | Kiểm tra data sau khi test |
| **File system MCP** | Đọc/ghi file local | Đọc test data, ghi kết quả |
| **Slack MCP** | Gửi/đọc message | Thông báo kết quả test |

### MCP trong Automation Testing

Ví dụ workflow với MCP:
```
Bạn: "Chạy regression test cho sprint này và báo cáo kết quả"

AI với MCP:
1. Jira MCP → lấy danh sách story trong sprint
2. GitHub MCP → xem code thay đổi trong sprint
3. File system MCP → đọc test suite hiện có
4. Run command → chạy test
5. Slack MCP → gửi kết quả vào channel #qa-results
```

### Setup MCP trong Kiro

Kiro hỗ trợ MCP qua file cấu hình `.kiro/settings/mcp.json`:
```json
{
  "mcpServers": {
    "github": {
      "command": "uvx",
      "args": ["mcp-server-github"],
      "env": { "GITHUB_TOKEN": "your-token" }
    },
    "jira": {
      "command": "uvx",
      "args": ["mcp-server-jira"],
      "env": { "JIRA_URL": "...", "JIRA_TOKEN": "..." }
    }
  }
}
```

---

## 1.13 Multimodal — AI không chỉ đọc text

**Multimodal** = AI xử lý được nhiều loại dữ liệu, không chỉ text:

| Loại input | AI làm được gì | Ứng dụng với tester |
|---|---|---|
| **Ảnh/Screenshot** | Đọc, mô tả, phân tích | Paste screenshot lỗi → AI giải thích |
| **File PDF/DOCX** | Đọc tài liệu, trích xuất | Paste spec → AI viết test case |
| **Code** | Hiểu, viết, debug | Công việc hàng ngày |
| **Audio** | Chuyển giọng nói thành text | Ghi âm meeting → AI tóm tắt |

Ví dụ thực tế:
```
1. Chụp screenshot màn hình lỗi
2. Paste vào Claude hoặc ChatGPT
3. Hỏi: "Đây là lỗi gì? Nguyên nhân và cách fix?"
→ AI đọc ảnh, nhận ra lỗi, giải thích chi tiết
```

---

## Tóm lại Phần 1

| Khái niệm | Nhớ gì |
|---|---|
| AI | Máy làm được việc của người, chạy trên server mạnh của công ty |
| LLM | Loại AI chuyên về ngôn ngữ/code, train trên hàng tỷ văn bản |
| Train | Công ty AI cho model học từ dữ liệu công khai, tốn hàng tỷ đô |
| Tài liệu nội bộ | AI không tự biết — phải paste vào chat hoặc dùng steering/RAG |
| Model | Bộ não cụ thể: GPT-4o, Claude Sonnet... |
| Tool | Giao diện bạn dùng: ChatGPT, Cursor, Kiro... |
| Codebase | Toàn bộ code của 1 dự án (tất cả file) |
| Token | Đơn vị AI đọc, ảnh hưởng chi phí và giới hạn |
| Context window | Bộ nhớ tạm trong 1 session, càng lớn càng tốt |
| Hallucination | AI bịa — luôn verify lại code AI viết |
| Cutoff date | AI không biết thông tin mới sau ngày này |
| MCP | Chuẩn kết nối AI với GitHub, Jira, DB, Browser... |
| Open source | Tải về chạy local miễn phí (Llama, DeepSeek) |
| API | Tích hợp AI vào tool/script, trả theo lượng dùng |

---

**Tiếp theo:** [Phần 2 — Cách làm việc với AI](./02-cach-lam-viec-voi-ai.md)
