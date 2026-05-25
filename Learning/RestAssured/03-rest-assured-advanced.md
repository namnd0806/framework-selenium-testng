# REST Assured Advanced

## 1. JSONPath - Trích xuất data

```java
// JSON response:
// {
//   "user": { "id": 1, "name": "John", "address": { "city": "Hanoi" } },
//   "orders": [
//     { "id": 101, "total": 99.99, "status": "PAID" },
//     { "id": 102, "total": 49.99, "status": "PENDING" }
//   ]
// }

Response response = get("/users/1");

// Trích xuất giá trị đơn
String name    = response.jsonPath().getString("user.name");
String city    = response.jsonPath().getString("user.address.city");
int userId     = response.jsonPath().getInt("user.id");

// Trích xuất array
List<Integer> orderIds = response.jsonPath().getList("orders.id");
List<Map<String, Object>> orders = response.jsonPath().getList("orders");

// Trích xuất phần tử cụ thể
int firstOrderId = response.jsonPath().getInt("orders[0].id");
String lastStatus = response.jsonPath().getString("orders[-1].status");

// Filter - lấy orders có status = PAID
List<Map<String, Object>> paidOrders = response.jsonPath()
    .getList("orders.findAll { it.status == 'PAID' }");

// Assertion với JSONPath trong then()
given().when().get("/users/1")
    .then()
    .body("user.name", equalTo("John"))
    .body("orders.size()", equalTo(2))
    .body("orders.status", hasItems("PAID", "PENDING"))
    .body("orders.find { it.status == 'PAID' }.total", equalTo(99.99f));
```

---

## 2. Authentication

```java
// Basic Auth
given()
    .auth().basic("username", "password")
.when()
    .get("/protected-resource");

// Bearer Token (JWT)
String token = getAuthToken(); // Lấy token từ login API
given()
    .header("Authorization", "Bearer " + token)
.when()
    .get("/api/users");

// OAuth2
given()
    .auth().oauth2(accessToken)
.when()
    .get("/api/profile");

// API Key trong header
given()
    .header("X-API-Key", "your-api-key-here")
.when()
    .get("/api/data");

// API Key trong query param
given()
    .queryParam("api_key", "your-api-key-here")
.when()
    .get("/api/data");

// Lấy token từ login và dùng cho các request tiếp theo
public class AuthHelper {
    private static String cachedToken;

    public static String getToken() {
        if (cachedToken == null) {
            cachedToken = given()
                .contentType(ContentType.JSON)
                .body("{\"email\":\"admin@test.com\",\"password\":\"Admin@123\"}")
            .when()
                .post("/auth/login")
            .then()
                .statusCode(200)
                .extract().path("token");
        }
        return cachedToken;
    }
}
```

---

## 3. Request Specification - Tái sử dụng config

```java
// Tạo spec dùng chung
public class ApiSpecifications {

    public static RequestSpecification getAuthSpec() {
        return new RequestSpecBuilder()
            .setBaseUri(ConfigReader.get("api.base.url"))
            .setBasePath("/api/v1")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("Authorization", "Bearer " + AuthHelper.getToken())
            .log(LogDetail.IF_VALIDATION_FAILS)
            .build();
    }

    public static ResponseSpecification getSuccessSpec() {
        return new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(3000L))
            .build();
    }

    public static ResponseSpecification getCreatedSpec() {
        return new ResponseSpecBuilder()
            .expectStatusCode(201)
            .expectContentType(ContentType.JSON)
            .build();
    }
}

// Sử dụng trong test
@Test
public void testGetUser() {
    given()
        .spec(ApiSpecifications.getAuthSpec())
        .pathParam("id", 1)
    .when()
        .get("/users/{id}")
    .then()
        .spec(ApiSpecifications.getSuccessSpec())
        .body("name", notNullValue());
}
```

---

## 4. POJO Serialization / Deserialization

```java
// POJO class
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private int id;
    private String name;
    private String email;
    private List<String> roles;
    // getters, setters, constructors
}

// Serialization: Java object → JSON body (POST/PUT)
User newUser = new User();
newUser.setName("John Doe");
newUser.setEmail("john@test.com");

given()
    .contentType(ContentType.JSON)
    .body(newUser)  // Jackson tự convert sang JSON
.when()
    .post("/users")
.then()
    .statusCode(201);

// Deserialization: JSON response → Java object
User createdUser = given()
    .when()
    .get("/users/1")
    .then()
    .statusCode(200)
    .extract().as(User.class); // Jackson tự convert từ JSON

System.out.println("User name: " + createdUser.getName());
Assert.assertEquals(createdUser.getEmail(), "john@test.com");

// Deserialize list
List<User> users = given()
    .when()
    .get("/users")
    .then()
    .extract().jsonPath().getList(".", User.class);
```

---

## 5. JSON Schema Validation

```json
// src/test/resources/schemas/user-schema.json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["id", "name", "email"],
  "properties": {
    "id":    { "type": "integer", "minimum": 1 },
    "name":  { "type": "string", "minLength": 1 },
    "email": { "type": "string", "format": "email" },
    "roles": {
      "type": "array",
      "items": { "type": "string" }
    },
    "address": {
      "type": "object",
      "properties": {
        "city":    { "type": "string" },
        "country": { "type": "string" }
      }
    }
  }
}
```

```java
// pom.xml dependency
// <artifactId>rest-assured</artifactId> đã bao gồm json-schema-validator

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@Test
public void testUserResponseSchema() {
    given()
        .when()
        .get("/users/1")
        .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
}
```

---

## 6. Filters - Custom Logging và Auth

```java
// Custom Request/Response Filter
public class LoggingFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        System.out.println("→ " + requestSpec.getMethod() + " " + requestSpec.getURI());
        Response response = ctx.next(requestSpec, responseSpec);
        System.out.println("← " + response.getStatusCode() + " (" + response.getTime() + "ms)");
        return response;
    }
}

// Auth Filter - tự động thêm token
public class AuthFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        requestSpec.header("Authorization", "Bearer " + AuthHelper.getToken());
        return ctx.next(requestSpec, responseSpec);
    }
}

// Đăng ký filter
RestAssured.filters(new LoggingFilter(), new AuthFilter());
// Hoặc per-request
given().filter(new AuthFilter()).when().get("/users");
```

---

## 7. Multipart - File Upload

```java
given()
    .multiPart("file", new File("src/test/resources/testfiles/document.pdf"))
    .multiPart("description", "Test document")
    .multiPart("category", "reports")
.when()
    .post("/api/files/upload")
.then()
    .statusCode(200)
    .body("filename", equalTo("document.pdf"))
    .body("size", greaterThan(0));
```

---

## 8. Extract - Lấy data từ response

```java
// extract().path() - lấy giá trị cụ thể
String name = given().when().get("/users/1")
    .then().extract().path("name");

// extract().response() - lấy toàn bộ Response
Response response = given().when().get("/users/1")
    .then().statusCode(200).extract().response();
String body = response.getBody().asString();
int statusCode = response.getStatusCode();

// extract().as() - deserialize sang POJO
User user = given().when().get("/users/1")
    .then().extract().as(User.class);

// extract().header()
String contentType = given().when().get("/users/1")
    .then().extract().header("Content-Type");

// Lấy ID sau POST để dùng tiếp
int newUserId = given()
    .contentType(ContentType.JSON)
    .body(userPayload)
.when()
    .post("/users")
.then()
    .statusCode(201)
    .extract().path("id");

// Dùng ID vừa tạo để GET
given().pathParam("id", newUserId)
    .when().get("/users/{id}")
    .then().statusCode(200);
```

---

## 9. Câu hỏi phỏng vấn

**Q1: JSONPath là gì và dùng như thế nào?**
> **Trả lời:** JSONPath là cú pháp để navigate và trích xuất data từ JSON, tương tự XPath cho XML. Dùng dấu chấm cho nested object, dấu ngoặc vuông cho array. REST Assured tích hợp sẵn JSONPath.
>
> **Gợi nhớ:** JSONPath = XPath nhưng cho JSON, dấu chấm để đi sâu vào

**Q2: Tại sao dùng RequestSpecBuilder?**
> **Trả lời:** Tránh duplicate code — base URL, headers, auth token lặp lại ở mọi test. Định nghĩa 1 lần trong spec, tất cả test dùng chung. Khi thay đổi (ví dụ đổi base URL) chỉ sửa 1 chỗ.
>
> **Gợi nhớ:** RequestSpec = template cho request, không copy-paste

**Q3: JSON Schema Validation kiểm tra gì?**
> **Trả lời:** Kiểm tra cấu trúc response: các field bắt buộc có tồn tại không, kiểu dữ liệu đúng không (string/int/array), format đúng không (email, date). Phát hiện breaking changes trong API contract.
>
> **Gợi nhớ:** Schema validation = kiểm tra "hình dạng" của response, không chỉ giá trị

**Q4: Sự khác nhau giữa extract().path() và extract().as()?**
> **Trả lời:** extract().path("field") lấy giá trị của 1 field cụ thể (trả về String/int/List). extract().as(User.class) deserialize toàn bộ JSON response sang Java object dùng Jackson.
>
> **Gợi nhớ:** path = lấy 1 mảnh, as() = lấy cả cái bánh và đổi thành object Java

---

[Tiếp theo: ../Cucumber/README.md](../Cucumber/README.md) | [Quay lại README](./README.md)
