# String & File I/O — Xử lý văn bản và đọc file

> String dùng khắp nơi trong automation: locator, URL, assertion message, test data...
> File I/O dùng để đọc test data từ properties, CSV, JSON.

---

## 1. String — Bất biến (Immutable)

### Tại sao String immutable?

```java
String s = "hello";
s.toUpperCase();        // KHÔNG thay đổi s
System.out.println(s);  // vẫn là "hello"

// Phải gán lại
String upper = s.toUpperCase();
System.out.println(upper); // "HELLO"

// Hoặc
s = s.toUpperCase();
System.out.println(s); // "HELLO" — s trỏ đến object mới
```

**Tại sao immutable?**
- Thread-safe — nhiều thread dùng cùng String không bị conflict
- String Pool — tiết kiệm bộ nhớ, tái sử dụng
- Security — URL, password không bị thay đổi ngoài ý muốn

### String Pool

```java
String s1 = "hello";           // lưu trong String Pool
String s2 = "hello";           // tái sử dụng từ Pool
String s3 = new String("hello"); // tạo object mới trong Heap

System.out.println(s1 == s2);  // true  — cùng reference trong Pool
System.out.println(s1 == s3);  // false — khác reference
System.out.println(s1.equals(s3)); // true — cùng nội dung

// Bài học: LUÔN dùng .equals() để so sánh String, không dùng ==
```

### Các method String hay dùng trong automation

```java
String url = "  https://staging.example.com/login  ";
String text = "Welcome, John Doe!";
String email = "Test@Example.COM";

// Trim — xóa khoảng trắng đầu/cuối (hay dùng khi đọc từ file/UI)
url.trim();                    // "https://staging.example.com/login"

// Case
email.toLowerCase();           // "test@example.com"
email.toUpperCase();           // "TEST@EXAMPLE.COM"

// Kiểm tra
text.contains("John");         // true
text.startsWith("Welcome");    // true
text.endsWith("!");            // true
text.isEmpty();                // false
text.isBlank();                // false (Java 11+, kiểm tra cả whitespace)
"  ".isBlank();                // true

// Tìm kiếm
text.indexOf("John");          // 9 (vị trí đầu tiên)
text.lastIndexOf("o");         // 14
text.length();                 // 18

// Cắt/thay thế
text.substring(9);             // "John Doe!"
text.substring(9, 13);         // "John"
text.replace("John", "Jane");  // "Welcome, Jane Doe!"
text.replaceAll("\\s+", "_");  // "Welcome,_John_Doe!" (regex)

// Tách
String csv = "Chrome,Firefox,Edge";
String[] browsers = csv.split(",");  // ["Chrome", "Firefox", "Edge"]

// Nối
String.join(", ", "Chrome", "Firefox", "Edge"); // "Chrome, Firefox, Edge"
String.join(", ", browsers);                     // "Chrome, Firefox, Edge"

// Format
String msg = String.format("User %s logged in at %s", "John", "10:00");
// "User John logged in at 10:00"

// Ví dụ thực tế — xử lý text từ UI
String priceText = driver.findElement(By.cssSelector(".price")).getText();
// priceText = "  $1,299.99  "
String cleanPrice = priceText.trim().replace("$", "").replace(",", "");
// cleanPrice = "1299.99"
double price = Double.parseDouble(cleanPrice);
```

---

## 2. StringBuilder — Khi cần nối nhiều String

```java
// Sai — tạo nhiều String object, chậm
String result = "";
for (int i = 0; i < 1000; i++) {
    result += "item" + i + ", "; // tạo object mới mỗi lần
}

// Đúng — dùng StringBuilder
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append("item").append(i).append(", ");
}
String result = sb.toString();

// Ví dụ thực tế — build XPath động
public By buildXPath(String tag, String attribute, String value) {
    StringBuilder xpath = new StringBuilder("//");
    xpath.append(tag)
         .append("[@")
         .append(attribute)
         .append("='")
         .append(value)
         .append("']");
    return By.xpath(xpath.toString());
    // //input[@id='email']
}

// Build error message
public String buildErrorMessage(List<String> errors) {
    StringBuilder sb = new StringBuilder("Validation failed:\n");
    for (String error : errors) {
        sb.append("  - ").append(error).append("\n");
    }
    return sb.toString();
}
```

### String vs StringBuilder vs StringBuffer

| | String | StringBuilder | StringBuffer |
|---|---|---|---|
| Mutable | ❌ | ✅ | ✅ |
| Thread-safe | ✅ | ❌ | ✅ |
| Tốc độ | Chậm (nối nhiều) | Nhanh | Chậm hơn StringBuilder |
| **Dùng khi** | Ít thay đổi | Nối nhiều, 1 thread | Nối nhiều, multi-thread |

**Trong automation:** Hầu hết dùng String và StringBuilder. StringBuffer hiếm khi cần.

---

## 3. File I/O — Đọc test data

### Đọc file .properties (phổ biến nhất trong framework)

```java
// config.properties
// base.url=https://staging.example.com
// browser=chrome
// timeout=10

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;
    private static final String CONFIG_PATH = "src/test/resources/config.properties";

    // Singleton — chỉ đọc file 1 lần
    public static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_PATH)) {
                properties.load(fis);
            } catch (IOException e) {
                throw new RuntimeException("Không đọc được config file: " + CONFIG_PATH, e);
            }
        }
        return properties;
    }

    public static String get(String key) {
        String value = getProperties().getProperty(key);
        if (value == null) {
            throw new RuntimeException("Không tìm thấy key trong config: " + key);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }
}

// Dùng:
String baseUrl = ConfigReader.get("base.url");
String browser = ConfigReader.get("browser", "chrome"); // default là chrome
int timeout = Integer.parseInt(ConfigReader.get("timeout", "10"));
```

### Đọc file CSV (test data)

```java
// testdata/users.csv
// email,password,role
// admin@test.com,Admin@123,admin
// user@test.com,User@123,user

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CsvReader {
    public static List<String[]> readCsv(String filePath) {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // bỏ qua dòng header
                    continue;
                }
                data.add(line.split(","));
            }
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được CSV: " + filePath, e);
        }
        return data;
    }
}

// Dùng với @DataProvider trong TestNG
@DataProvider(name = "loginData")
public Object[][] getLoginData() {
    List<String[]> csvData = CsvReader.readCsv("src/test/resources/testdata/users.csv");
    Object[][] data = new Object[csvData.size()][3];
    for (int i = 0; i < csvData.size(); i++) {
        data[i][0] = csvData.get(i)[0]; // email
        data[i][1] = csvData.get(i)[1]; // password
        data[i][2] = csvData.get(i)[2]; // role
    }
    return data;
}

@Test(dataProvider = "loginData")
public void testLogin(String email, String password, String role) {
    loginPage.login(email, password);
    // assert...
}
```

### Đọc file JSON (test data phức tạp)

```java
// testdata/users.json
// [
//   {"email": "admin@test.com", "password": "Admin@123", "role": "admin"},
//   {"email": "user@test.com", "password": "User@123", "role": "user"}
// ]

// Dùng Jackson (thêm dependency vào pom.xml)
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;

public class JsonReader {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T> T readJson(String filePath, Class<T> clazz) {
        try {
            return mapper.readValue(new File(filePath), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Không đọc được JSON: " + filePath, e);
        }
    }
}

// POJO class
public class UserData {
    public String email;
    public String password;
    public String role;
}

// Dùng:
List<UserData> users = JsonReader.readJson(
    "src/test/resources/testdata/users.json",
    mapper.getTypeFactory().constructCollectionType(List.class, UserData.class)
);
```

### try-with-resources — Tự động đóng file

```java
// Cách cũ — phải đóng thủ công trong finally
FileInputStream fis = null;
try {
    fis = new FileInputStream("file.txt");
    // đọc file
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (fis != null) {
        try { fis.close(); } catch (IOException e) { }
    }
}

// Cách mới (Java 7+) — try-with-resources, tự đóng
try (FileInputStream fis = new FileInputStream("file.txt")) {
    // đọc file
    // fis tự động đóng khi ra khỏi block, dù có exception hay không
} catch (IOException e) {
    e.printStackTrace();
}
```

---

## 4. Đọc file từ classpath (resources folder)

```java
// Đọc file trong src/test/resources/ — cách đúng trong Maven project
public class ResourceReader {
    public static Properties loadProperties(String fileName) {
        Properties props = new Properties();
        // getResourceAsStream tìm file trong classpath
        try (InputStream is = ResourceReader.class
                .getClassLoader()
                .getResourceAsStream(fileName)) {
            if (is == null) {
                throw new RuntimeException("Không tìm thấy file: " + fileName);
            }
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file: " + fileName, e);
        }
        return props;
    }
}

// Dùng:
Properties config = ResourceReader.loadProperties("config.properties");
// Tìm file tại: src/test/resources/config.properties
```

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: Tại sao String immutable trong Java?**

```
Gợi nhớ: 3 lý do — "An toàn, Tiết kiệm, Bảo mật"

1. Thread-safe: nhiều thread dùng cùng String không cần synchronize
2. String Pool: tiết kiệm bộ nhớ, tái sử dụng object
3. Security: URL, password không bị thay đổi ngoài ý muốn

Hệ quả: mọi method của String (toUpperCase, replace...) đều trả về String mới,
không thay đổi String gốc.
```

**Q2: String vs StringBuilder vs StringBuffer?**

```
String: immutable, thread-safe, dùng khi ít thay đổi
StringBuilder: mutable, KHÔNG thread-safe, nhanh, dùng khi nối nhiều String
StringBuffer: mutable, thread-safe, chậm hơn StringBuilder

Trong automation: 95% dùng String, 5% dùng StringBuilder khi build message/xpath dài
```

**Q3: == vs .equals() với String?**

```java
String s1 = "hello";
String s2 = "hello";
String s3 = new String("hello");

s1 == s2      // true  — cùng reference trong String Pool
s1 == s3      // false — s3 là object mới trong Heap
s1.equals(s3) // true  — cùng nội dung

Quy tắc: LUÔN dùng .equals() để so sánh nội dung String
         == chỉ so sánh reference (địa chỉ bộ nhớ)
```

**Q4: try-with-resources là gì? Tại sao dùng?**

```
try-with-resources (Java 7+): tự động gọi .close() khi ra khỏi block
Áp dụng cho: bất kỳ class implement AutoCloseable (File, DB connection, Stream...)

Lợi ích:
1. Không quên đóng resource
2. Code ngắn gọn hơn
3. Đóng đúng thứ tự khi có nhiều resource

Trong automation: đọc file config, test data
```

**Q5: Làm thế nào để đọc file config trong Maven project?**

```
Đặt file trong src/test/resources/
Đọc bằng getClass().getClassLoader().getResourceAsStream("config.properties")

Tại sao không dùng đường dẫn tuyệt đối?
- Đường dẫn tuyệt đối khác nhau trên mỗi máy
- getResourceAsStream tìm trong classpath → hoạt động mọi nơi
- CI/CD server cũng tìm được
```

---

**Tiếp theo:** [05 — Java 8 Features](./05-java8-features.md)
