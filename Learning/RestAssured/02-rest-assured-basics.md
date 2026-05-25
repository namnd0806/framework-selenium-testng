# REST Assured Basics

## 1. Setup

```xml
<!-- pom.xml -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
<!-- JSON path support -->
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>json-path</artifactId>
    <version>5.4.0</version>
    <scope>test</scope>
</dependency>
```

```java
// Static imports - thêm vào đầu file test
import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

// Base config - đặt trong @BeforeClass hoặc @BeforeSuite
RestAssured.baseURI = "https://api.example.com";
RestAssured.basePath = "/v1";
RestAssured.port = 443;
```

---

## 2. given().when().then() - BDD Syntax

```
given()   → Chuẩn bị request (headers, params, body, auth)
when()    → Thực hiện request (GET, POST, PUT, DELETE)
then()    → Kiểm tra response (status, body, headers)
```

```java
given()
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + token)
    .queryParam("page", 1)
    .body("{\"name\": \"John\"}")
.when()
    .post("/users")
.then()
    .statusCode(201)
    .body("name", equalTo("John"))
    .header("Content-Type", containsString("application/json"));
```

---

## 3. GET Request

```java
// GET đơn giản
given()
    .when()
    .get("/users")
    .then()
    .statusCode(200)
    .body("size()", greaterThan(0));

// GET với query parameters
given()
    .queryParam("page", 1)
    .queryParam("size", 10)
    .queryParam("sort", "name,asc")
.when()
    .get("/users")
.then()
    .statusCode(200)
    .body("content.size()", equalTo(10));

// GET với path parameter
int userId = 1;
given()
    .pathParam("id", userId)
.when()
    .get("/users/{id}")
.then()
    .statusCode(200)
    .body("id", equalTo(userId))
    .body("name", notNullValue());

// Lấy giá trị từ response
String userName = given()
    .when()
    .get("/users/1")
    .then()
    .statusCode(200)
    .extract().path("name");

System.out.println("User name: " + userName);
```

---

## 4. POST Request

```java
// POST với JSON body (String)
String requestBody = """
    {
        "name": "John Doe",
        "email": "john@test.com",
        "password": "SecurePass@123"
    }
    """;

int createdUserId = given()
    .contentType(ContentType.JSON)
    .body(requestBody)
.when()
    .post("/users")
.then()
    .statusCode(201)
    .body("name", equalTo("John Doe"))
    .body("email", equalTo("john@test.com"))
    .body("id", notNullValue())
    .extract().path("id");

// POST với Map (tự convert sang JSON)
Map<String, Object> user = new HashMap<>();
user.put("name", "Jane Doe");
user.put("email", "jane@test.com");

given()
    .contentType(ContentType.JSON)
    .body(user)
.when()
    .post("/users")
.then()
    .statusCode(201);

// POST với Form Data
given()
    .contentType(ContentType.URLENC)
    .formParam("username", "john")
    .formParam("password", "pass123")
.when()
    .post("/auth/login")
.then()
    .statusCode(200)
    .body("token", notNullValue());
```

---

## 5. PUT / PATCH / DELETE

```java
// PUT - update toàn bộ
given()
    .contentType(ContentType.JSON)
    .pathParam("id", 1)
    .body("{\"name\": \"John Updated\", \"email\": \"john.new@test.com\"}")
.when()
    .put("/users/{id}")
.then()
    .statusCode(200)
    .body("name", equalTo("John Updated"));

// PATCH - update một phần
given()
    .contentType(ContentType.JSON)
    .pathParam("id", 1)
    .body("{\"email\": \"newemail@test.com\"}")
.when()
    .patch("/users/{id}")
.then()
    .statusCode(200)
    .body("email", equalTo("newemail@test.com"));

// DELETE
given()
    .pathParam("id", 1)
.when()
    .delete("/users/{id}")
.then()
    .statusCode(204); // No Content

// Verify deleted
given()
    .pathParam("id", 1)
.when()
    .get("/users/{id}")
.then()
    .statusCode(404);
```

---

## 6. Response Assertions

```java
Response response = given()
    .when()
    .get("/users/1")
    .then()
    .extract().response();

// Status
response.then().statusCode(200);
response.then().statusCode(anyOf(equalTo(200), equalTo(201)));

// Body assertions với Hamcrest
response.then()
    .body("id", equalTo(1))
    .body("name", equalTo("John"))
    .body("email", containsString("@test.com"))
    .body("roles", hasItem("USER"))
    .body("orders.size()", greaterThan(0))
    .body("address.city", equalTo("Hanoi"));

// Headers
response.then()
    .header("Content-Type", containsString("application/json"))
    .header("X-Request-ID", notNullValue());

// Response time
response.then()
    .time(lessThan(2000L)); // Response trong vòng 2 giây

// Content type
response.then()
    .contentType(ContentType.JSON);
```

---

## 7. Logging

```java
// Log tất cả request và response
given()
    .log().all()
.when()
    .get("/users")
.then()
    .log().all()
    .statusCode(200);

// Chỉ log request
given().log().request()

// Chỉ log response
.then().log().response()

// Chỉ log khi validation fail (khuyến nghị cho CI)
given()
    .log().ifValidationFails()
.when()
    .get("/users")
.then()
    .log().ifValidationFails()
    .statusCode(200);

// Log body only
given().log().body()
.then().log().body()
```

---

## 8. Base URI và Base Path

```java
// Cách 1: Static config (áp dụng toàn bộ)
@BeforeSuite
public void setup() {
    RestAssured.baseURI = "https://api.example.com";
    RestAssured.basePath = "/api/v1";
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
}

// Cách 2: RequestSpecification (linh hoạt hơn)
RequestSpecification spec = new RequestSpecBuilder()
    .setBaseUri("https://api.example.com")
    .setBasePath("/api/v1")
    .addHeader("Accept", "application/json")
    .build();

given()
    .spec(spec)
.when()
    .get("/users");
```

---

## 9. Câu hỏi phỏng vấn

**Q1: given/when/then trong REST Assured có ý nghĩa gì?**
> **Trả lời:** given() = chuẩn bị request (headers, params, body). when() = thực hiện HTTP method. then() = validate response. Theo BDD pattern, đọc như câu chuyện: "Given headers, when GET /users, then status 200".
>
> **Gợi nhớ:** given = chuẩn bị, when = hành động, then = kiểm tra

**Q2: Sự khác nhau giữa queryParam và pathParam?**
> **Trả lời:** pathParam thay thế placeholder trong URL: `/users/{id}` → `/users/1`. queryParam thêm vào sau `?`: `/users?page=1&size=10`. Path param là phần của resource identifier, query param là filter/pagination.
>
> **Gợi nhớ:** path = trong URL, query = sau dấu ?

**Q3: Tại sao dùng log().ifValidationFails() thay vì log().all()?**
> **Trả lời:** log().all() in ra tất cả request/response dù pass hay fail → làm log CI rất dài và khó đọc. log().ifValidationFails() chỉ in khi test fail → log sạch hơn, dễ debug hơn.
>
> **Gợi nhớ:** ifValidationFails = chỉ la hét khi có vấn đề, không la hét vô cớ

**Q4: Làm thế nào để lấy giá trị từ response để dùng trong test tiếp theo?**
> **Trả lời:** Dùng extract().path("fieldName") để lấy giá trị cụ thể, hoặc extract().response() để lấy toàn bộ Response object. Ví dụ: lấy id sau POST để dùng trong GET/DELETE tiếp theo.
>
> **Gợi nhớ:** extract() = lấy data ra khỏi response để dùng tiếp

---

[Tiếp theo: 03-rest-assured-advanced.md](./03-rest-assured-advanced.md) | [Quay lại README](./README.md)
