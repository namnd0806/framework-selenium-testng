# Collections — Quản lý dữ liệu trong Java

> Collections dùng khắp nơi trong automation: lưu danh sách element, test data, config...
> Biết chọn đúng collection = code nhanh hơn, ít bug hơn.

---

## 1. Tổng quan Collections Framework

```
Collection (interface)
├── List — có thứ tự, cho phép trùng
│   ├── ArrayList   ← dùng nhiều nhất
│   └── LinkedList
├── Set — không có thứ tự, không trùng
│   ├── HashSet     ← dùng nhiều nhất
│   └── TreeSet     ← tự sắp xếp
└── Queue — hàng đợi FIFO
    └── LinkedList

Map (interface) — cặp key-value, key không trùng
├── HashMap         ← dùng nhiều nhất
├── LinkedHashMap   ← giữ thứ tự insert
└── TreeMap         ← tự sắp xếp theo key
```

---

## 2. List — Danh sách có thứ tự

### ArrayList — Dùng nhiều nhất

```java
// Tạo list
List<String> browsers = new ArrayList<>();
List<String> browsers = new ArrayList<>(Arrays.asList("Chrome", "Firefox", "Edge"));

// Thêm
browsers.add("Safari");
browsers.add(0, "Chrome");        // thêm vào vị trí 0

// Lấy
String first = browsers.get(0);   // lấy theo index
int size = browsers.size();

// Xóa
browsers.remove("Safari");        // xóa theo value
browsers.remove(0);               // xóa theo index

// Kiểm tra
boolean has = browsers.contains("Chrome");
boolean empty = browsers.isEmpty();

// Duyệt
for (String browser : browsers) {
    System.out.println(browser);
}

// Dùng trong automation — lưu danh sách element
List<WebElement> rows = driver.findElements(By.cssSelector("table tr"));
System.out.println("Số dòng: " + rows.size());
for (WebElement row : rows) {
    System.out.println(row.getText());
}
```

### ArrayList vs LinkedList

| | ArrayList | LinkedList |
|---|---|---|
| Truy cập theo index | Nhanh O(1) | Chậm O(n) |
| Thêm/xóa ở giữa | Chậm O(n) | Nhanh O(1) |
| Bộ nhớ | Ít hơn | Nhiều hơn |
| **Dùng khi** | Đọc nhiều, ít thêm/xóa | Thêm/xóa nhiều ở đầu/giữa |

**Trong automation:** Hầu hết dùng ArrayList — lưu list element, test data, kết quả.

---

## 3. Map — Cặp key-value

### HashMap — Dùng nhiều nhất

```java
// Tạo map
Map<String, String> testData = new HashMap<>();
Map<String, String> config = new HashMap<>();

// Thêm
testData.put("email", "test@gmail.com");
testData.put("password", "Test@123");

// Lấy
String email = testData.get("email");           // "test@gmail.com"
String phone = testData.get("phone");           // null nếu không có
String phone2 = testData.getOrDefault("phone", "N/A"); // "N/A" nếu không có

// Kiểm tra
boolean hasEmail = testData.containsKey("email");
boolean hasValue = testData.containsValue("test@gmail.com");

// Xóa
testData.remove("password");

// Duyệt
for (Map.Entry<String, String> entry : testData.entrySet()) {
    System.out.println(entry.getKey() + " = " + entry.getValue());
}

// Dùng trong automation — lưu test data
Map<String, String> loginData = new HashMap<>();
loginData.put("validEmail", "user@test.com");
loginData.put("validPassword", "Pass@123");
loginData.put("invalidEmail", "notanemail");

// Dùng trong automation — lưu config theo môi trường
Map<String, String> envConfig = new HashMap<>();
envConfig.put("baseUrl", "https://staging.example.com");
envConfig.put("apiUrl", "https://api.staging.example.com");
```

### LinkedHashMap — Giữ thứ tự insert

```java
// Khi cần duyệt theo thứ tự đã thêm vào
Map<String, String> orderedSteps = new LinkedHashMap<>();
orderedSteps.put("step1", "Open login page");
orderedSteps.put("step2", "Enter email");
orderedSteps.put("step3", "Enter password");
orderedSteps.put("step4", "Click login");
// Duyệt sẽ ra đúng thứ tự step1 → step4
```

---

## 4. Set — Không trùng lặp

```java
// HashSet — không có thứ tự, không trùng
Set<String> uniqueUrls = new HashSet<>();
uniqueUrls.add("https://example.com/page1");
uniqueUrls.add("https://example.com/page2");
uniqueUrls.add("https://example.com/page1"); // bị bỏ qua, đã có rồi
System.out.println(uniqueUrls.size()); // 2

// Dùng trong automation — tìm duplicate
List<String> allTexts = new ArrayList<>(Arrays.asList("A", "B", "A", "C"));
Set<String> uniqueTexts = new HashSet<>(allTexts);
if (allTexts.size() != uniqueTexts.size()) {
    System.out.println("Có duplicate!");
}

// TreeSet — tự sắp xếp
Set<String> sortedBrowsers = new TreeSet<>();
sortedBrowsers.add("Firefox");
sortedBrowsers.add("Chrome");
sortedBrowsers.add("Edge");
// Tự sắp xếp: [Chrome, Edge, Firefox]
```

---

## 5. Các thao tác hay dùng

### Chuyển đổi giữa các collection

```java
// Array → List
String[] arr = {"Chrome", "Firefox"};
List<String> list = new ArrayList<>(Arrays.asList(arr));
List<String> list2 = Arrays.asList(arr); // fixed-size, không add/remove được

// List → Array
String[] arr2 = list.toArray(new String[0]);

// List → Set (loại bỏ duplicate)
List<String> withDup = Arrays.asList("A", "B", "A");
Set<String> noDup = new HashSet<>(withDup);

// Set → List
List<String> fromSet = new ArrayList<>(noDup);

// Map values → List
Map<String, String> map = new HashMap<>();
map.put("k1", "v1");
map.put("k2", "v2");
List<String> values = new ArrayList<>(map.values());
List<String> keys = new ArrayList<>(map.keySet());
```

### Collections utility class

```java
import java.util.Collections;

List<Integer> numbers = new ArrayList<>(Arrays.asList(3, 1, 4, 1, 5));

Collections.sort(numbers);           // sắp xếp tăng dần: [1, 1, 3, 4, 5]
Collections.reverse(numbers);        // đảo ngược: [5, 4, 3, 1, 1]
Collections.shuffle(numbers);        // xáo trộn ngẫu nhiên
int max = Collections.max(numbers);  // tìm max
int min = Collections.min(numbers);  // tìm min
Collections.frequency(numbers, 1);  // đếm số lần xuất hiện của 1
```

### Ví dụ thực tế trong automation

```java
// Lấy text của tất cả option trong dropdown
Select dropdown = new Select(driver.findElement(By.id("country")));
List<WebElement> options = dropdown.getOptions();
List<String> optionTexts = new ArrayList<>();
for (WebElement option : options) {
    optionTexts.add(option.getText());
}
// Verify có đủ option không
assertTrue(optionTexts.contains("Vietnam"));
assertTrue(optionTexts.contains("USA"));

// Verify không có duplicate trong danh sách sản phẩm
List<WebElement> productNames = driver.findElements(By.cssSelector(".product-name"));
List<String> names = new ArrayList<>();
for (WebElement el : productNames) {
    names.add(el.getText());
}
Set<String> uniqueNames = new HashSet<>(names);
assertEquals(names.size(), uniqueNames.size(), "Có sản phẩm bị duplicate!");

// Lưu test data dạng Map để dùng với @DataProvider
Map<String, String> validUser = new HashMap<>();
validUser.put("email", "valid@test.com");
validUser.put("password", "Valid@123");
validUser.put("expectedUrl", "/dashboard");
```

---

## 6. Generics — Kiểu dữ liệu an toàn

```java
// Không dùng Generics — unsafe, dễ lỗi runtime
List list = new ArrayList();
list.add("hello");
list.add(123);           // OK lúc compile, lỗi lúc runtime
String s = (String) list.get(1); // ClassCastException!

// Dùng Generics — safe, lỗi được phát hiện lúc compile
List<String> safeList = new ArrayList<>();
safeList.add("hello");
safeList.add(123);       // Lỗi compile ngay! Không thể add Integer vào List<String>
String s = safeList.get(0); // Không cần cast

// Generic method — dùng trong utility class
public <T> List<T> filterNotNull(List<T> list) {
    List<T> result = new ArrayList<>();
    for (T item : list) {
        if (item != null) result.add(item);
    }
    return result;
}

// Dùng:
List<String> texts = filterNotNull(Arrays.asList("a", null, "b")); // ["a", "b"]
List<WebElement> elements = filterNotNull(someElements);
```

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: ArrayList vs LinkedList — khi nào dùng cái nào?**

```
Gợi nhớ: ArrayList = "mảng động", LinkedList = "chuỗi xích"

ArrayList:
- Truy cập theo index nhanh (O1) — như mảng
- Thêm/xóa ở giữa chậm (On) — phải dịch chuyển phần tử
→ Dùng khi: đọc nhiều, ít thêm/xóa ở giữa
→ Trong automation: lưu list WebElement, test data

LinkedList:
- Truy cập theo index chậm (On) — phải duyệt từ đầu
- Thêm/xóa ở đầu/giữa nhanh (O1) — chỉ đổi pointer
→ Dùng khi: thêm/xóa nhiều ở đầu/giữa
→ Trong automation: ít dùng
```

**Q2: HashMap hoạt động thế nào? Điều gì xảy ra khi 2 key có cùng hashCode?**

```
Gợi nhớ: HashMap = "tủ hồ sơ có ngăn kéo đánh số"

Cách hoạt động:
1. Gọi key.hashCode() → tính ra số ngăn kéo (bucket index)
2. Lưu cặp key-value vào ngăn kéo đó

Khi 2 key cùng hashCode (collision):
- Cả 2 vào cùng 1 ngăn kéo
- Lưu dạng LinkedList trong ngăn kéo đó
- Khi get() → tìm đúng key bằng .equals()

Tại sao cần override cả hashCode() và equals()?
- equals() trả về true → hashCode() phải bằng nhau
- Nếu chỉ override equals() → HashMap hoạt động sai
```

**Q3: HashSet vs TreeSet — khác nhau thế nào?**

```
HashSet:
- Không có thứ tự
- Nhanh hơn (O1 cho add/remove/contains)
- Dùng khi: chỉ cần loại bỏ duplicate, không cần thứ tự

TreeSet:
- Tự sắp xếp (theo natural order hoặc Comparator)
- Chậm hơn (O log n)
- Dùng khi: cần duyệt theo thứ tự đã sắp xếp
```

**Q4: Khi nào dùng Map thay vì List?**

```
List: khi chỉ cần lưu danh sách, truy cập theo index
Map: khi cần tra cứu theo key (như dictionary)

Ví dụ thực tế:
- Lưu danh sách URL cần test → List<String>
- Lưu config theo tên (baseUrl, apiUrl) → Map<String, String>
- Lưu test data (email, password, expected) → Map<String, String>
- Lưu kết quả test (testName → pass/fail) → Map<String, Boolean>
```

**Q5: Làm thế nào để loại bỏ duplicate trong List?**

```java
// Cách 1: Dùng Set
List<String> withDup = Arrays.asList("A", "B", "A", "C");
List<String> noDup = new ArrayList<>(new HashSet<>(withDup));
// Lưu ý: mất thứ tự

// Cách 2: Dùng LinkedHashSet (giữ thứ tự)
List<String> noDupOrdered = new ArrayList<>(new LinkedHashSet<>(withDup));

// Cách 3: Stream (Java 8+)
List<String> noDupStream = withDup.stream().distinct().collect(Collectors.toList());
```

---

**Tiếp theo:** [03 — Exception Handling](./03-exception-handling.md)
