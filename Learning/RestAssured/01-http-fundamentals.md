# HTTP Fundamentals

## 1. HTTP Request/Response Cycle

```
Client (Test Code)                    Server (API)
      |                                    |
      |--- HTTP Request ------------------>|
      |    Method: POST                    |
      |    URL: /api/users                 |
      |    Headers: Content-Type: JSON     |
      |    Body: {"name": "John"}          |
      |                                    |
      |<-- HTTP Response ------------------|
      |    Status: 201 Created             |
      |    Headers: Content-Type: JSON     |
      |    Body: {"id": 1, "name": "John"} |
```

---

## 2. HTTP Methods

| Method | Mục đích | Idempotent | Body | Ví dụ |
|--------|----------|-----------|------|-------|
| GET | Lấy data | ✅ | ❌ | GET /api/users |
| POST | Tạo mới | ❌ | ✅ | POST /api/users |
| PUT | Update toàn bộ | ✅ | ✅ | PUT /api/users/1 |
| PATCH | Update một phần | ✅ | ✅ | PATCH /api/users/1 |
| DELETE | Xóa | ✅ | ❌ | DELETE /api/users/1 |

**Idempotent:** Gọi nhiều lần cho kết quả giống nhau.
- POST tạo user 2 lần → 2 user khác nhau (không idempotent)
- PUT update user 2 lần → kết quả giống nhau (idempotent)

---

## 3. Status Codes quan trọng

```
1xx - Informational
  100 Continue

2xx - Success ✅
  200 OK              - GET thành công
  201 Created         - POST tạo resource thành công
  204 No Content      - DELETE thành công (không có body)

3xx - Redirection
  301 Moved Permanently  - URL đã đổi vĩnh viễn
  302 Found              - Redirect tạm thời
  304 Not Modified       - Cache còn hợp lệ

4xx - Client Error ❌
  400 Bad Request        - Request sai format/validation fail
  401 Unauthorized       - Chưa authenticate (chưa login)
  403 Forbidden          - Đã authenticate nhưng không có quyền
  404 Not Found          - Resource không tồn tại
  405 Method Not Allowed - Method không được phép
  409 Conflict           - Conflict (email đã tồn tại)
  422 Unprocessable      - Validation error (dữ liệu không hợp lệ)
  429 Too Many Requests  - Rate limit

5xx - Server Error 💥
  500 Internal Server Error - Lỗi server không xác định
  502 Bad Gateway           - Upstream server lỗi
  503 Service Unavailable   - Server quá tải hoặc maintenance
  504 Gateway Timeout       - Upstream server timeout
```

---

## 4. Headers quan trọng

```
Request Headers:
  Content-Type: application/json        - Body gửi lên là JSON
  Accept: application/json              - Muốn nhận JSON
  Authorization: Bearer <token>         - JWT token
  Authorization: Basic <base64>         - Basic auth
  X-API-Key: abc123                     - API key
  X-Request-ID: uuid                    - Tracking ID

Response Headers:
  Content-Type: application/json        - Body trả về là JSON
  X-RateLimit-Remaining: 99            - Còn bao nhiêu request
  X-RateLimit-Reset: 1700000000        - Reset lúc nào
  Cache-Control: no-cache              - Không cache
  Location: /api/users/123             - URL của resource vừa tạo (201)
```

---

## 5. Request Body formats

```json
// JSON (phổ biến nhất)
{
  "name": "John Doe",
  "email": "john@test.com",
  "age": 30,
  "address": {
    "street": "123 Main St",
    "city": "Hanoi"
  },
  "roles": ["USER", "ADMIN"]
}
```

```
// Form Data (application/x-www-form-urlencoded)
name=John+Doe&email=john%40test.com&age=30

// Multipart Form Data (file upload)
Content-Type: multipart/form-data; boundary=----boundary
------boundary
Content-Disposition: form-data; name="file"; filename="doc.pdf"
Content-Type: application/pdf
<binary data>
```

---

## 6. REST vs SOAP

| Tiêu chí | REST | SOAP |
|----------|------|------|
| Format | JSON, XML, text | XML only |
| Protocol | HTTP | HTTP, SMTP, TCP |
| Style | Architectural style | Protocol |
| Complexity | Đơn giản | Phức tạp |
| Performance | Nhanh hơn | Chậm hơn (XML verbose) |
| Security | HTTPS + OAuth | WS-Security built-in |
| Phổ biến | ✅ Hiện đại | ⚠️ Legacy, banking, enterprise |
| Testing tool | REST Assured, Postman | SoapUI |

---

## 7. Postman - Dùng trước khi code

**Workflow khuyến nghị:**
1. Dùng Postman test API thủ công trước
2. Hiểu request/response structure
3. Lưu vào Collection
4. Sau đó mới code automation với REST Assured

**Postman tips:**
- Environment variables: `{{base_url}}`, `{{token}}`
- Pre-request Script: lấy token trước mỗi request
- Tests tab: viết assertion bằng JavaScript
- Export collection → import vào Newman để chạy CI

---

## 8. Câu hỏi phỏng vấn

**Q1: Sự khác nhau giữa 401 và 403?**
> **Trả lời:** 401 = chưa authenticate (chưa có token/credentials). 403 = đã authenticate nhưng không có quyền truy cập resource đó. Ví dụ: user thường cố truy cập admin endpoint → 403.
>
> **Gợi nhớ:** 401 = không biết bạn là ai, 403 = biết bạn là ai nhưng không cho vào

**Q2: PUT vs PATCH khác nhau thế nào?**
> **Trả lời:** PUT thay thế toàn bộ resource (phải gửi đầy đủ fields). PATCH chỉ update một phần (chỉ gửi fields cần thay đổi). Ví dụ: chỉ đổi email → dùng PATCH.
>
> **Gợi nhớ:** PUT = thay cả tờ giấy, PATCH = sửa 1 chỗ trên tờ giấy

**Q3: Idempotent nghĩa là gì? Method nào không idempotent?**
> **Trả lời:** Idempotent = gọi nhiều lần cho kết quả giống nhau. POST không idempotent vì mỗi lần gọi tạo ra resource mới. GET, PUT, DELETE, PATCH đều idempotent.
>
> **Gợi nhớ:** POST = bấm nút đặt hàng 2 lần → 2 đơn hàng (không idempotent)

**Q4: Khi nào API trả về 204 thay vì 200?**
> **Trả lời:** 204 No Content dùng khi operation thành công nhưng không có data để trả về. Thường dùng cho DELETE hoặc PUT/PATCH khi không cần trả về resource đã update.
>
> **Gợi nhớ:** 204 = "Xong rồi, không có gì để nói thêm"

---

[Tiếp theo: 02-rest-assured-basics.md](./02-rest-assured-basics.md) | [Quay lại README](./README.md)
