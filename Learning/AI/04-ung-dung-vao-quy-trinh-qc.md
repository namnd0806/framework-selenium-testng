# Phần 4 — Ứng dụng AI vào quy trình QC

> AI không thay thế tester — nhưng tester dùng AI tốt sẽ làm việc nhanh gấp 3-5 lần.

---

## 4.1 AI trong SDLC — Từng giai đoạn AI hỗ trợ gì

**SDLC = Software Development Life Cycle** — vòng đời phát triển phần mềm.

```
Requirements → Design → Development → Testing → Deployment → Maintenance
```

| Giai đoạn | AI hỗ trợ gì | Ví dụ cụ thể |
|---|---|---|
| **Requirements** | Phân tích, làm rõ yêu cầu mơ hồ | "User story này thiếu acceptance criteria nào?" |
| **Design** | Review design, tìm edge case | "API design này có vấn đề gì về security?" |
| **Development** | Viết code, review, debug | Copilot, Cursor viết code |
| **Testing** | Viết test case, test data, automation | Phần chính của tài liệu này |
| **Deployment** | Viết script deploy, review config | "Review Dockerfile này có vấn đề gì?" |
| **Maintenance** | Phân tích log, tìm root cause | "Phân tích stack trace này" |

---

## 4.2 AI trong STLC — Test Life Cycle

**STLC = Software Testing Life Cycle** — vòng đời kiểm thử phần mềm.

```
Requirement Analysis → Test Planning → Test Design → 
Test Environment Setup → Test Execution → Test Closure
```

### Requirement Analysis — AI phân tích yêu cầu

```
Prompt:
Đây là user story:
"As a user, I want to login with email and password
so that I can access my account"

Hãy:
1. Liệt kê tất cả acceptance criteria cần có
2. Tìm các edge case và negative case
3. Chỉ ra những gì còn mơ hồ cần clarify với BA/PO
```

AI sẽ tìm ra những thứ bạn có thể bỏ sót:
- Điều gì xảy ra khi email đúng nhưng password sai 5 lần liên tiếp?
- Session timeout sau bao lâu?
- Có "Remember me" không?
- Login trên nhiều device cùng lúc thì sao?

### Test Planning — AI hỗ trợ lập kế hoạch

```
Prompt:
Feature: Checkout flow trong e-commerce app
Tech stack: React frontend, Java Spring Boot backend
Timeline: 2 tuần test

Hãy tạo test plan bao gồm:
1. Scope (in/out of scope)
2. Test types cần thực hiện
3. Risk assessment
4. Resource estimate
5. Entry/Exit criteria
```

### Test Design — AI viết test case

```
Prompt:
Dựa vào API spec dưới đây, viết test case cho endpoint POST /api/login:

API Spec:
- Request: { email: string, password: string }
- Response 200: { token: string, user: { id, name, email } }
- Response 401: { error: "Invalid credentials" }
- Response 422: { error: "Validation failed", fields: [...] }

Viết test case theo format:
- ID, Title, Precondition, Steps, Expected Result, Priority
Bao gồm: happy path, negative cases, boundary values, security cases
```

### Test Execution — AI hỗ trợ trong lúc test

```
// Gặp bug không rõ nguyên nhân:
Prompt:
Tôi đang test chức năng checkout.
Bug: Sau khi click "Place Order", trang bị trắng và không có error message.
Console log: [paste log]
Network tab: [paste request/response]

Phân tích nguyên nhân có thể và cách reproduce để viết bug report.
```

### Test Closure — AI tổng hợp báo cáo

```
Prompt:
Dựa vào dữ liệu test dưới đây, viết test summary report:
- Total test cases: 150
- Passed: 132, Failed: 12, Blocked: 6
- Critical bugs: 2, Major: 5, Minor: 8
- Test duration: 5 ngày

Viết report bao gồm: executive summary, risk assessment,
recommendation cho release decision.
```

---

## 4.3 AI viết Test Case từ Requirement

### Từ User Story

```
Prompt:
User Story:
"As a registered user, I want to reset my password via email
so that I can regain access if I forget my password"

Acceptance Criteria:
- User nhập email → nhận link reset trong 5 phút
- Link chỉ dùng được 1 lần, hết hạn sau 24h
- Password mới phải khác password cũ
- Sau reset thành công, tất cả session cũ bị logout

Viết test case đầy đủ bao gồm:
- Happy path
- Negative cases (email không tồn tại, link hết hạn, link đã dùng...)
- Security cases (brute force, link manipulation...)
- Boundary cases
Format: TestRail style với ID, Title, Steps, Expected
```

### Từ API Document

```
Prompt:
[Paste Swagger/OpenAPI spec hoặc Postman collection]

Từ API spec trên, tạo:
1. Test case cho từng endpoint
2. Test data cho từng case
3. Chỉ ra các case cần test đặc biệt về security/performance
```

### Từ UI Mockup (Multimodal)

```
[Chụp ảnh màn hình mockup/figma]

Prompt:
Đây là màn hình checkout của e-commerce app.
Viết test case cho tất cả element trên màn hình này.
Bao gồm: validation, UI/UX, responsive, accessibility.
```

---

## 4.4 AI sinh Test Data

### Test data cơ bản

```
Prompt:
Tạo test data cho form đăng ký user với các field:
- Email (required, valid format)
- Password (required, min 8 chars, có uppercase, số, ký tự đặc biệt)
- Phone (optional, Vietnam format)
- Date of birth (required, phải >= 18 tuổi)

Tạo:
1. Valid data (5 bộ)
2. Invalid data cho từng field (boundary values)
3. SQL injection attempts
4. XSS attempts
5. Special characters
Format: JSON array
```

### Test data phức tạp

```
Prompt:
Tạo test data cho scenario: User mua hàng với voucher giảm giá.

Rules:
- Voucher SAVE10: giảm 10%, tối đa 50k, đơn tối thiểu 200k
- Voucher FREESHIP: miễn phí ship, đơn tối thiểu 150k
- Không stack 2 voucher
- Voucher hết hạn 31/12/2025

Tạo 10 bộ test data bao gồm các case:
- Áp dụng thành công
- Không đủ điều kiện
- Voucher hết hạn
- Stack voucher (invalid)
Format: JSON với expected result cho mỗi case
```

---

## 4.5 AI review và viết Bug Report

### Phân tích bug

```
Prompt:
Tôi gặp bug sau khi test chức năng thanh toán:

Môi trường: Chrome 120, Windows 11, Staging env
Steps to reproduce:
1. Thêm 3 sản phẩm vào giỏ hàng
2. Áp dụng voucher SAVE10
3. Chọn thanh toán COD
4. Click "Đặt hàng"

Actual: Trang trắng, không có order confirmation
Expected: Hiển thị trang "Đặt hàng thành công" với order ID

Console error: [paste error]
Network: POST /api/orders trả về 500

Hãy:
1. Phân tích root cause có thể
2. Viết bug report theo format chuẩn
3. Đề xuất thêm thông tin cần thu thập để dev fix
```

### Review bug report của người khác

```
Prompt:
Review bug report dưới đây và chỉ ra:
1. Thông tin còn thiếu
2. Steps to reproduce có rõ ràng không
3. Severity/Priority có đúng không
4. Cách cải thiện

[paste bug report]
```

---

## 4.6 AI trong Regression & Exploratory Testing

### Xác định regression scope

```
Prompt:
Đây là code diff của sprint này:
[paste git diff hoặc danh sách file thay đổi]

Dựa vào thay đổi trên:
1. Liệt kê các chức năng bị ảnh hưởng (trực tiếp và gián tiếp)
2. Đề xuất regression test scope
3. Ưu tiên test case nào cần chạy trước
4. Risk assessment nếu bỏ qua một số test
```

### Exploratory testing với AI

```
Prompt:
Tôi đang exploratory test chức năng search sản phẩm.
Đã test: basic search, filter, sort, pagination.

Gợi ý thêm các area chưa test:
1. Edge cases về input
2. Performance scenarios
3. Security scenarios
4. Concurrent user scenarios
5. Integration points cần kiểm tra
```

---

## Tóm lại Phần 4

AI hỗ trợ tester ở **mọi giai đoạn** của STLC:

| Giai đoạn | AI làm được gì | Tiết kiệm thời gian |
|---|---|---|
| Requirement analysis | Tìm edge case, clarify yêu cầu | 30-50% |
| Test planning | Tạo test plan, risk assessment | 40-60% |
| Test design | Viết test case từ spec/mockup | 50-70% |
| Test data | Sinh test data đa dạng | 60-80% |
| Bug reporting | Phân tích, viết report chuẩn | 30-50% |
| Regression | Xác định scope, ưu tiên | 40-60% |

**Quan trọng:** AI hỗ trợ, không thay thế. Tester vẫn cần:
- Hiểu business logic để đánh giá kết quả AI
- Exploratory testing — tìm bug không ai nghĩ đến
- Giao tiếp với team, clarify yêu cầu
- Quyết định release hay không

---

**Tiếp theo:** [Phần 5 — AI cho Automation Tester & SDET](./05-ai-cho-automation-sdet.md)
