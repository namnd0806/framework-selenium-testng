# Phần 1 — Nền tảng AI

> Đọc phần này trước. Hiểu được phần này thì các phần sau sẽ dễ hơn nhiều.

---

## 1.1 AI, Machine Learning, Deep Learning — khác nhau thế nào?

Nhiều người dùng lẫn lộn 3 khái niệm này. Thực ra chúng là các tầng lồng nhau:

```
AI (Artificial Intelligence)         ← rộng nhất, máy làm được việc của người
└── Machine Learning                 ← máy tự học từ dữ liệu, không cần lập trình cứng
    └── Deep Learning                ← dùng mạng neural nhiều lớp, học pattern phức tạp
        └── LLM                      ← chuyên về ngôn ngữ & code
            └── GPT, Claude, Gemini  ← sản phẩm bạn đang dùng hàng ngày
```

Ví dụ dễ hiểu:
- **AI cũ (trước 2020):** lập trình cứng từng rule — "nếu email có @, thì hợp lệ"
- **Machine Learning:** cho máy xem 1 triệu email hợp lệ/không hợp lệ → máy tự học rule
- **LLM:** cho máy đọc toàn bộ internet + GitHub → máy hiểu được ngôn ngữ và code

---

## 1.2 LLM là gì và hoạt động thế nào?

**LLM = Large Language Model** — mô hình AI được train để hiểu và tạo ra ngôn ngữ (bao gồm code).

Cách train đơn giản:
1. Thu thập hàng tỷ trang văn bản từ internet, sách, GitHub...
2. Cho model học cách **dự đoán từ/token tiếp theo**
3. Lặp đi lặp lại hàng tỷ lần → model "hiểu" được ngôn ngữ, logic, code

> GPT-4 train trên ~1 nghìn tỷ token. Claude train tương tự nhưng có thêm bước
> **Constitutional AI** — dạy model từ chối yêu cầu có hại.

**Điều quan trọng cần nhớ:** LLM không "biết" sự thật. Nó chỉ **dự đoán token tiếp theo có xác suất cao nhất**. Đây là lý do AI đôi khi sai.

---

## 1.3 Model là gì? Phân biệt Model vs Tool vs Platform

Đây là khái niệm **hay bị nhầm nhất**. Nhiều người nghĩ ChatGPT = 1 thứ duy nhất — thực ra không phải.

Hình dung như điện thoại:
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
| Model nhỏ (Flash, Haiku, mini) | Nhanh, rẻ, đủ dùng | Việc đơn giản: viết comment, giải thích code |
| Model vừa (Sonnet, Pro, 4o) | Cân bằng tốc độ & chất lượng | Việc hàng ngày: debug, viết test |
| Model lớn (Opus, o3, Ultra) | Chậm hơn, đắt hơn, mạnh hơn | Việc phức tạp: thiết kế architecture, bài toán khó |

---

## 1.4 Các công ty AI chính

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

## 1.5 Token là gì?

Token là **đơn vị nhỏ nhất** mà AI đọc — không phải từ, không phải ký tự, mà là mảnh văn bản:

```
"hello"          → 1 token
"unhappy"        → 2 token  (un + happy)
"xin chào"       → 3-4 token
1 dòng code      → khoảng 5-15 token
1000 từ tiếng Anh → khoảng 750 token
```

**Tại sao quan trọng với dev/tester?**
- Dùng API → **trả tiền theo số token** (input + output)
- AI có giới hạn token trong 1 cuộc trò chuyện → gọi là **context window**

---

## 1.6 Context Window là gì?

Là **bộ nhớ tạm** của AI trong 1 cuộc trò chuyện. Vượt quá giới hạn → AI quên phần đầu.

| Model | Context Window | Tương đương |
|---|---|---|
| GPT-3.5 (2022) | 4K token | ~3 trang A4 |
| GPT-4 (2023) | 8K-32K token | ~25 trang A4 |
| GPT-4o (2024) | 128K token | ~100 trang A4 |
| Claude 3.5 Sonnet | 200K token | ~150.000 từ |
| Gemini 1.5 Pro | 1 triệu token | Cả cuốn sách dày |
| Gemini 2.0 | 2 triệu token | Cả codebase lớn |

> Ví dụ thực tế: Claude 200K token = paste cả project 10.000 dòng code vào vẫn còn chỗ.

**Lưu ý quan trọng:** Context window là bộ nhớ **trong 1 session**. Mở chat mới = AI quên hết.
Cách giải quyết vấn đề này sẽ nói ở Phần 2.

---

## 1.7 Hallucination — AI bịa thông tin

AI **bịa ra thông tin trông có vẻ đúng nhưng sai hoàn toàn**. Nguy hiểm vì AI nói rất tự tin.

```java
// Bạn hỏi:
"Hàm Arrays.sortDescending() trong Java dùng thế nào?"

// AI trả lời rất tự tin:
"Bạn dùng Arrays.sortDescending(arr) để sắp xếp giảm dần..."

// Thực tế:
// Hàm đó KHÔNG TỒN TẠI trong Java → AI bịa ra
```

Các tình huống hay bị hallucination:
- Hỏi về API/function cụ thể của thư viện ít phổ biến
- Hỏi về version mới ra sau cutoff date
- Hỏi về thông tin nội bộ công ty, dự án cụ thể

**Nguyên tắc:** Luôn chạy thử code AI viết, đừng tin 100% mà không kiểm tra.

---

## 1.8 Cutoff Date — AI không biết thông tin mới

Ngày AI ngừng học dữ liệu mới. Sau ngày đó nó không biết gì thêm trừ khi có web search.

| Model | Cutoff |
|---|---|
| GPT-4 | Tháng 4/2023 |
| GPT-4o | Tháng 10/2023 |
| Claude 3.7 Sonnet | Tháng 11/2024 |
| Gemini 2.0 | Đầu 2025 |

> Ví dụ: Hỏi về thư viện ra mắt sau cutoff → AI không biết hoặc bịa.

Một số tool có tích hợp **web search** để vượt qua giới hạn này (ChatGPT, Gemini, Perplexity).

---

## 1.9 Open Source vs Closed Source

**Closed Source** (GPT-4, Claude, Gemini):
- Công ty giữ bí mật code và cách train
- Chỉ dùng qua API hoặc tool của họ
- Thường mạnh hơn
- Phải trả tiền khi dùng nhiều

**Open Source** (Llama, Mistral, DeepSeek):
- Công khai code, ai cũng tải về dùng được
- Tải về chạy trên máy mình (dùng Ollama — xem Phần 6)
- Miễn phí hoàn toàn
- Bảo mật hơn vì code không rời khỏi máy bạn
- Yếu hơn một chút so với model lớn của OpenAI/Anthropic

---

## 1.10 API là gì và khi nào dùng?

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
| Gemini 1.5 Pro | $1.25 | $5.00 |

> Ví dụ: $1 dùng GPT-4o mini = khoảng 6 triệu token input — rất rẻ cho automation script.

---

## 1.11 Multimodal — AI không chỉ đọc text

**Multimodal** = AI xử lý được nhiều loại dữ liệu, không chỉ text:

| Loại input | AI làm được gì | Ứng dụng với tester |
|---|---|---|
| **Ảnh/Screenshot** | Đọc, mô tả, phân tích | Paste screenshot lỗi → AI giải thích |
| **File PDF/DOCX** | Đọc tài liệu, trích xuất thông tin | Paste spec → AI viết test case |
| **Code** | Hiểu, viết, debug | Công việc hàng ngày |
| **Audio** | Chuyển giọng nói thành text | Ghi âm meeting → AI tóm tắt |
| **Video** | Phân tích nội dung (Gemini) | Quay lỗi UI → AI phân tích |

Ví dụ thực tế với tester:
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
| LLM | Bộ não AI, train trên hàng tỷ văn bản và code |
| Model | Bộ não cụ thể: GPT-4o, Claude Sonnet... |
| Tool | Giao diện bạn dùng: ChatGPT, Cursor, Kiro... |
| Token | Đơn vị AI đọc, ảnh hưởng chi phí và giới hạn |
| Context window | Bộ nhớ tạm trong 1 session, càng lớn càng tốt |
| Hallucination | AI bịa — luôn verify lại code AI viết |
| Cutoff date | AI không biết thông tin mới sau ngày này |
| Open source | Tải về chạy local miễn phí (Llama, DeepSeek) |
| API | Tích hợp AI vào tool/script, trả theo lượng dùng |
| Multimodal | AI đọc được ảnh, PDF, không chỉ text |

---

**Tiếp theo:** [Phần 2 — Cách làm việc với AI](./02-cach-lam-viec-voi-ai.md)
