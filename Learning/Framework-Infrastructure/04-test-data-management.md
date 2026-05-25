# Test Data Management

## 1. Tại sao quan trọng?

Test data kém → test không ổn định, khó maintain, không chạy được trên nhiều môi trường.

**Nguyên tắc:**
- Mỗi test độc lập với data riêng
- Không hardcode data trong test code
- Cleanup sau test (không để lại "rác")
- Dễ thay đổi theo môi trường (dev/staging/prod)

---

## 2. Properties File - Cấu hình môi trường

```properties
# src/test/resources/config/staging.properties
base.url=https://staging.example.com
api.base.url=https://api-staging.example.com
browser=chrome
headless=false
implicit.wait=0
explicit.wait=15
admin.email=admin@staging.com
admin.password=Admin@Staging123
```

```java
// ConfigReader.java - Singleton pattern
public class ConfigReader {
    private static Properties properties;
    private static final String DEFAULT_ENV = "staging";

    public static void load() {
        String env = System.getProperty("env", DEFAULT_ENV);
        String configFile = "config/" + env + ".properties";
        properties = new Properties();
        try (InputStream is = ConfigReader.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            if (is == null) throw new RuntimeException("Config not found: " + configFile);
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config: " + e.getMessage());
        }
    }

    public static String get(String key) {
        if (properties == null) load();
        // System property overrides file property
        return System.getProperty(key, properties.getProperty(key));
    }

    public static int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }
}

// Sử dụng
String baseUrl = ConfigReader.get("base.url");
int timeout    = ConfigReader.getInt("explicit.wait");
```

---

## 3. JSON với Jackson

```json
// src/test/resources/testdata/users.json
{
  "validUsers": [
    { "email": "admin@test.com",  "password": "Admin@123",  "role": "ADMIN" },
    { "email": "user@test.com",   "password": "User@123",   "role": "USER" }
  ],
  "invalidUsers": [
    { "email": "wrong@test.com",  "password": "wrong",      "expectedError": "Invalid credentials" },
    { "email": "",                "password": "pass",        "expectedError": "Email is required" }
  ]
}
```

```java
// POJO
public class UserData {
    private List<User> validUsers;
    private List<User> invalidUsers;
    // getters, setters
}

public class User {
    private String email;
    private String password;
    private String role;
    private String expectedError;
    // getters, setters
}

// JsonDataReader.java
public class JsonDataReader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T readData(String filePath, Class<T> clazz) {
        try (InputStream is = JsonDataReader.class.getClassLoader()
                .getResourceAsStream(filePath)) {
            return mapper.readValue(is, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON: " + filePath, e);
        }
    }
}

// Dùng trong @DataProvider
@DataProvider(name = "validUsers")
public Object[][] validUsersData() {
    UserData data = JsonDataReader.readData("testdata/users.json", UserData.class);
    return data.getValidUsers().stream()
        .map(u -> new Object[]{u.getEmail(), u.getPassword()})
        .toArray(Object[][]::new);
}
```

---

## 4. CSV với OpenCSV

```csv
# src/test/resources/testdata/products.csv
name,price,category,inStock
iPhone 15,999.99,Electronics,true
Samsung S24,899.99,Electronics,true
Nike Air Max,129.99,Shoes,false
```

```java
// pom.xml: <artifactId>opencsv</artifactId>

public class CsvDataReader {

    public static List<Map<String, String>> readCsv(String filePath) {
        List<Map<String, String>> data = new ArrayList<>();
        try (CSVReader reader = new CSVReaderBuilder(
                new InputStreamReader(
                    CsvDataReader.class.getClassLoader().getResourceAsStream(filePath)))
                .withSkipLines(1) // Skip header
                .build()) {

            // Đọc header
            // Dùng CsvToBean với HeaderColumnNameMappingStrategy
            CsvToBean<ProductData> csvToBean = new CsvToBeanBuilder<ProductData>(
                new InputStreamReader(
                    CsvDataReader.class.getClassLoader().getResourceAsStream(filePath)))
                .withType(ProductData.class)
                .withIgnoreLeadingWhiteSpace(true)
                .build();

            return csvToBean.parse().stream()
                .map(p -> Map.of("name", p.getName(), "price", p.getPrice()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV: " + filePath, e);
        }
    }
}
```

---

## 5. Faker - Sinh data ngẫu nhiên

```xml
<!-- pom.xml -->
<dependency>
    <groupId>com.github.javafaker</groupId>
    <artifactId>javafaker</artifactId>
    <version>1.0.2</version>
    <scope>test</scope>
</dependency>
```

```java
import com.github.javafaker.Faker;

public class TestDataGenerator {
    private static final Faker faker = new Faker(new Locale("en-US"));

    public static String randomEmail() {
        return faker.internet().emailAddress();
        // → "john.doe123@example.com"
    }

    public static String randomName() {
        return faker.name().fullName();
        // → "John Doe"
    }

    public static String randomPhone() {
        return faker.phoneNumber().phoneNumber();
    }

    public static String randomAddress() {
        return faker.address().streetAddress();
    }

    public static String randomPassword() {
        // Faker không có password, tự tạo
        return "Test@" + faker.number().digits(6);
        // → "Test@123456"
    }

    public static Map<String, String> randomUser() {
        return Map.of(
            "name",     faker.name().fullName(),
            "email",    faker.internet().emailAddress(),
            "phone",    faker.phoneNumber().phoneNumber(),
            "address",  faker.address().streetAddress()
        );
    }
}

// Dùng trong test
@Test
public void testCreateNewUser() {
    String email    = TestDataGenerator.randomEmail();
    String password = TestDataGenerator.randomPassword();

    // Tạo user với data ngẫu nhiên → không conflict với data cũ
    userPage.createUser(email, password);
    Assert.assertTrue(userPage.isUserCreated(email));
}
```

---

## 6. API-based Test Data

```java
// Tạo data qua API trước test, cleanup sau test
public class UserApiHelper {
    private int createdUserId;

    public void createTestUser(String email, String password) {
        createdUserId = given()
            .contentType(ContentType.JSON)
            .body(Map.of("email", email, "password", password, "role", "USER"))
        .when()
            .post("/api/admin/users")
        .then()
            .statusCode(201)
            .extract().path("id");
    }

    public void deleteTestUser() {
        if (createdUserId > 0) {
            given()
                .header("Authorization", "Bearer " + AuthHelper.getAdminToken())
                .pathParam("id", createdUserId)
            .when()
                .delete("/api/admin/users/{id}")
            .then()
                .statusCode(204);
        }
    }
}

// Trong test
public class LoginTest {
    private UserApiHelper userHelper = new UserApiHelper();
    private String testEmail;

    @BeforeMethod
    public void createData() {
        testEmail = TestDataGenerator.randomEmail();
        userHelper.createTestUser(testEmail, "Test@123");
    }

    @AfterMethod
    public void cleanupData() {
        userHelper.deleteTestUser(); // Cleanup sau mỗi test
    }

    @Test
    public void testLogin() {
        loginPage.login(testEmail, "Test@123");
        Assert.assertTrue(dashboardPage.isLoaded());
    }
}
```

---

## 7. Câu hỏi phỏng vấn

**Q1: Tại sao không nên hardcode test data trong test code?**
> **Trả lời:** Hardcode data khó maintain (phải sửa code khi data thay đổi), không chạy được trên nhiều môi trường, test phụ thuộc vào nhau nếu dùng chung data. Nên đọc từ file hoặc tạo qua API.
>
> **Gợi nhớ:** Hardcode = bê tông, data file = đất sét — cái nào dễ thay đổi hơn?

**Q2: Faker library dùng để làm gì trong testing?**
> **Trả lời:** Sinh test data ngẫu nhiên (email, tên, địa chỉ, số điện thoại) để tránh conflict với data cũ, test isolation tốt hơn, và không cần maintain danh sách data cố định.
>
> **Gợi nhớ:** Faker = máy tạo data ảo, mỗi lần chạy có data mới

**Q3: API-based test data setup có lợi ích gì so với UI setup?**
> **Trả lời:** Nhanh hơn nhiều (không cần render browser), ổn định hơn (không bị ảnh hưởng bởi UI changes), dễ cleanup (gọi DELETE API), và có thể tạo data phức tạp mà UI không hỗ trợ.
>
> **Gợi nhớ:** API setup = đường tắt, UI setup = đường vòng

**Q4: Test data isolation nghĩa là gì?**
> **Trả lời:** Mỗi test có data riêng, không phụ thuộc vào data của test khác. Test A không bị ảnh hưởng bởi test B dù chạy theo thứ tự nào. Đạt được bằng cách tạo data trong @BeforeMethod và cleanup trong @AfterMethod.
>
> **Gợi nhớ:** Isolation = mỗi test là hòn đảo riêng, không ảnh hưởng nhau

---

[Tiếp theo: ../CICD/README.md](../CICD/README.md) | [Quay lại README](./README.md)
