# Java 8+ Features — Lambda, Stream, Optional

> Java 8 thay đổi cách viết code hoàn toàn. Framework hiện đại dùng Lambda và Stream rất nhiều.
> Biết Java 8 = code ngắn hơn, đọc dễ hơn, ít bug hơn.

---

## 1. Lambda Expression

### Lambda là gì?

Lambda = cách viết ngắn gọn cho **anonymous function** (hàm không tên).

```java
// Trước Java 8 — anonymous class, dài dòng
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// Java 8 — Lambda, ngắn gọn
Runnable r = () -> System.out.println("Hello");

// Cú pháp: (parameters) -> { body }
// Nếu body 1 dòng: bỏ {} và return
// Nếu không có parameter: ()
// Nếu 1 parameter: có thể bỏ ()
```

### Cú pháp Lambda

```java
// Không có parameter
() -> System.out.println("Hello")

// 1 parameter (có thể bỏ ())
x -> x * 2
(x) -> x * 2

// Nhiều parameter
(x, y) -> x + y

// Body nhiều dòng
(x, y) -> {
    int sum = x + y;
    return sum * 2;
}
```

### Lambda trong automation

```java
// WebDriverWait với Lambda (Java 8+)
// Trước:
wait.until(new ExpectedCondition<Boolean>() {
    @Override
    public Boolean apply(WebDriver driver) {
        return driver.findElements(By.id("loading")).isEmpty();
    }
});

// Sau — Lambda:
wait.until(driver -> driver.findElements(By.id("loading")).isEmpty());

// Sort list element theo text
List<WebElement> items = driver.findElements(By.cssSelector(".item"));
items.sort((a, b) -> a.getText().compareTo(b.getText()));

// Chạy action với retry
public void retryAction(Runnable action, int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        try {
            action.run();
            return;
        } catch (Exception e) {
            if (i == maxRetries - 1) throw e;
        }
    }
}
// Dùng:
retryAction(() -> driver.findElement(By.id("btn")).click(), 3);
```

---

## 2. Functional Interfaces

Lambda chỉ dùng được với **Functional Interface** — interface có đúng 1 abstract method.

```java
// Các Functional Interface phổ biến trong java.util.function

// Predicate<T> — nhận T, trả về boolean
Predicate<String> isNotEmpty = s -> !s.isEmpty();
isNotEmpty.test("hello"); // true
isNotEmpty.test("");      // false

// Function<T, R> — nhận T, trả về R
Function<String, Integer> strToInt = s -> Integer.parseInt(s);
strToInt.apply("123"); // 123

// Consumer<T> — nhận T, không trả về gì
Consumer<String> printer = s -> System.out.println(s);
printer.accept("Hello"); // in ra "Hello"

// Supplier<T> — không nhận gì, trả về T
Supplier<WebDriver> driverSupplier = () -> new ChromeDriver();
WebDriver driver = driverSupplier.get();

// Ví dụ thực tế trong framework
public List<WebElement> filterElements(List<WebElement> elements,
                                        Predicate<WebElement> condition) {
    return elements.stream()
                   .filter(condition)
                   .collect(Collectors.toList());
}

// Dùng:
List<WebElement> visibleElements = filterElements(
    driver.findElements(By.cssSelector(".item")),
    el -> el.isDisplayed()
);
```

---

## 3. Stream API

### Stream là gì?

Stream = pipeline xử lý collection theo chuỗi thao tác.

```
Collection → Stream → [filter] → [map] → [collect] → Kết quả
```

**Gợi nhớ:** Như dây chuyền sản xuất — nguyên liệu vào, qua từng công đoạn, ra thành phẩm.

### Các thao tác Stream

```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Anna", "Brian");

// filter — lọc theo điều kiện
List<String> aNames = names.stream()
    .filter(name -> name.startsWith("A"))
    .collect(Collectors.toList());
// ["Alice", "Anna"]

// map — biến đổi từng phần tử
List<String> upperNames = names.stream()
    .map(String::toUpperCase)  // method reference
    .collect(Collectors.toList());
// ["ALICE", "BOB", "CHARLIE", "ANNA", "BRIAN"]

// sorted — sắp xếp
List<String> sorted = names.stream()
    .sorted()
    .collect(Collectors.toList());
// ["Alice", "Anna", "Bob", "Brian", "Charlie"]

// distinct — loại bỏ duplicate
List<String> withDup = Arrays.asList("A", "B", "A", "C");
List<String> unique = withDup.stream()
    .distinct()
    .collect(Collectors.toList());
// ["A", "B", "C"]

// limit / skip
List<String> first3 = names.stream().limit(3).collect(Collectors.toList());
List<String> skip2 = names.stream().skip(2).collect(Collectors.toList());

// forEach — duyệt (terminal, không trả về Stream)
names.stream().forEach(name -> System.out.println(name));
names.forEach(System.out::println); // ngắn hơn

// count — đếm
long count = names.stream().filter(n -> n.length() > 4).count(); // 2

// anyMatch / allMatch / noneMatch
boolean anyA = names.stream().anyMatch(n -> n.startsWith("A")); // true
boolean allShort = names.stream().allMatch(n -> n.length() < 10); // true
boolean noneZ = names.stream().noneMatch(n -> n.startsWith("Z")); // true

// findFirst — tìm phần tử đầu tiên
Optional<String> first = names.stream()
    .filter(n -> n.startsWith("B"))
    .findFirst();
first.ifPresent(System.out::println); // "Bob"

// reduce — gộp thành 1 giá trị
int totalLength = names.stream()
    .mapToInt(String::length)
    .sum(); // tổng độ dài tất cả tên

// Kết hợp nhiều thao tác
List<String> result = names.stream()
    .filter(n -> n.length() > 3)      // lọc tên dài hơn 3 ký tự
    .map(String::toLowerCase)          // chuyển thành lowercase
    .sorted()                          // sắp xếp
    .collect(Collectors.toList());
```

### Stream trong automation — Ví dụ thực tế

```java
// Lấy text của tất cả element
List<WebElement> items = driver.findElements(By.cssSelector(".product-name"));
List<String> productNames = items.stream()
    .map(WebElement::getText)
    .collect(Collectors.toList());

// Lọc element đang hiển thị
List<WebElement> visibleItems = items.stream()
    .filter(WebElement::isDisplayed)
    .collect(Collectors.toList());

// Kiểm tra có element nào chứa text không
boolean hasProduct = items.stream()
    .anyMatch(el -> el.getText().contains("iPhone"));

// Tìm element theo text
Optional<WebElement> iphone = items.stream()
    .filter(el -> el.getText().equals("iPhone 15"))
    .findFirst();
iphone.ifPresent(el -> el.click());

// Đếm element theo điều kiện
long enabledButtons = driver.findElements(By.tagName("button"))
    .stream()
    .filter(WebElement::isEnabled)
    .count();

// Lấy tất cả href từ link
List<String> allLinks = driver.findElements(By.tagName("a"))
    .stream()
    .map(el -> el.getAttribute("href"))
    .filter(href -> href != null && !href.isEmpty())
    .distinct()
    .collect(Collectors.toList());
```

---

## 4. Optional — Tránh NullPointerException

### Optional là gì?

Optional = container có thể chứa hoặc không chứa giá trị. Thay thế cho việc trả về null.

```java
// Không dùng Optional — dễ NPE
public String getUserEmail(int userId) {
    User user = database.findUser(userId);
    return user.getEmail(); // NPE nếu user = null!
}

// Dùng Optional — an toàn hơn
public Optional<String> getUserEmail(int userId) {
    User user = database.findUser(userId);
    if (user == null) return Optional.empty();
    return Optional.of(user.getEmail());
}

// Dùng:
Optional<String> email = getUserEmail(123);
email.ifPresent(e -> System.out.println("Email: " + e));
String e = email.orElse("no-email@default.com");
String e2 = email.orElseThrow(() -> new RuntimeException("User không tồn tại"));
```

### Các method Optional hay dùng

```java
Optional<String> opt = Optional.of("hello");
Optional<String> empty = Optional.empty();
Optional<String> nullable = Optional.ofNullable(null); // không throw NPE

// Kiểm tra
opt.isPresent();   // true
empty.isPresent(); // false
opt.isEmpty();     // false (Java 11+)

// Lấy giá trị
opt.get();                          // "hello" (throw nếu empty)
opt.orElse("default");              // "hello"
empty.orElse("default");            // "default"
empty.orElseGet(() -> "computed");  // "computed" (lazy)
empty.orElseThrow(() -> new RuntimeException("Không có giá trị"));

// Biến đổi
opt.map(String::toUpperCase);       // Optional["HELLO"]
opt.filter(s -> s.length() > 3);   // Optional["hello"]

// Thực thi nếu có giá trị
opt.ifPresent(s -> System.out.println(s));
opt.ifPresentOrElse(                // Java 9+
    s -> System.out.println("Có: " + s),
    () -> System.out.println("Không có")
);
```

### Optional trong automation

```java
// Tìm element, không throw exception nếu không có
public Optional<WebElement> findElementSafely(By locator) {
    List<WebElement> elements = driver.findElements(locator);
    return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(0));
}

// Dùng:
findElementSafely(By.id("popup-close"))
    .ifPresent(WebElement::click); // click nếu có, bỏ qua nếu không

// Lấy attribute, trả về default nếu null
public String getAttributeSafely(WebElement element, String attr) {
    return Optional.ofNullable(element.getAttribute(attr))
                   .orElse("");
}
```

---

## 5. Method Reference

Cách viết ngắn hơn Lambda khi chỉ gọi 1 method:

```java
// Lambda → Method Reference
list.forEach(s -> System.out.println(s));  // Lambda
list.forEach(System.out::println);          // Method Reference

// Các loại Method Reference
// 1. Static method: ClassName::staticMethod
Function<String, Integer> parse = Integer::parseInt;

// 2. Instance method của object cụ thể: object::method
String prefix = "Hello";
Predicate<String> startsWith = prefix::startsWith; // không đúng, chỉ ví dụ

// 3. Instance method của type bất kỳ: ClassName::instanceMethod
Function<String, String> upper = String::toUpperCase;
List<String> uppers = names.stream().map(String::toUpperCase).collect(Collectors.toList());

// 4. Constructor: ClassName::new
Supplier<ArrayList> listFactory = ArrayList::new;
ArrayList list = listFactory.get();

// Trong automation
List<WebElement> elements = driver.findElements(By.cssSelector(".item"));
List<String> texts = elements.stream()
    .map(WebElement::getText)      // method reference thay vì el -> el.getText()
    .collect(Collectors.toList());

List<WebElement> visible = elements.stream()
    .filter(WebElement::isDisplayed) // thay vì el -> el.isDisplayed()
    .collect(Collectors.toList());
```

---

## Câu hỏi phỏng vấn & Gợi ý trả lời

**Q1: Lambda là gì? Cho ví dụ thực tế trong automation.**

```
Lambda = anonymous function, viết ngắn gọn hơn anonymous class.
Chỉ dùng được với Functional Interface (interface có 1 abstract method).

Ví dụ trong automation:
1. WebDriverWait: wait.until(driver -> driver.findElements(By.id("loading")).isEmpty())
2. Sort elements: items.sort((a, b) -> a.getText().compareTo(b.getText()))
3. Filter elements: elements.stream().filter(el -> el.isDisplayed())
```

**Q2: Stream API — filter, map, collect làm gì?**

```
Gợi nhớ: "Lọc → Biến đổi → Thu thập"

filter(predicate) — giữ lại phần tử thỏa điều kiện
map(function)     — biến đổi từng phần tử thành dạng khác
collect(collector) — thu thập kết quả vào collection

Ví dụ thực tế:
List<String> productNames = driver.findElements(By.cssSelector(".product"))
    .stream()
    .filter(WebElement::isDisplayed)    // chỉ lấy element đang hiển thị
    .map(WebElement::getText)           // lấy text
    .collect(Collectors.toList());      // thu thập vào List
```

**Q3: Optional là gì? Tại sao dùng?**

```
Optional = container có thể có hoặc không có giá trị.
Thay thế cho việc trả về null.

Tại sao dùng:
1. Tránh NullPointerException
2. Code rõ ý định hơn — "method này có thể không có kết quả"
3. Buộc caller phải xử lý trường hợp không có giá trị

Trong automation:
Optional<WebElement> popup = findElementSafely(By.id("popup"));
popup.ifPresent(WebElement::click); // click nếu có, bỏ qua nếu không
```

**Q4: Sự khác nhau giữa map() và flatMap() trong Stream?**

```java
// map() — biến đổi 1-1
List<String> names = Arrays.asList("Alice", "Bob");
List<Integer> lengths = names.stream()
    .map(String::length)  // "Alice"→5, "Bob"→3
    .collect(Collectors.toList()); // [5, 3]

// flatMap() — biến đổi 1-nhiều, rồi flatten
List<List<String>> nested = Arrays.asList(
    Arrays.asList("a", "b"),
    Arrays.asList("c", "d")
);
List<String> flat = nested.stream()
    .flatMap(List::stream)  // [[a,b],[c,d]] → [a,b,c,d]
    .collect(Collectors.toList());

// Trong automation: ít dùng flatMap, chủ yếu dùng map
```

**Q5: Khi nào dùng Stream, khi nào dùng for loop?**

```
Dùng Stream khi:
- Cần filter, map, collect — code ngắn và rõ hơn
- Cần anyMatch, allMatch, count
- Cần chaining nhiều thao tác

Dùng for loop khi:
- Cần break/continue giữa chừng
- Logic phức tạp, nhiều điều kiện
- Performance critical (Stream có overhead nhỏ)
- Người đọc code chưa quen Stream

Trong automation: cả 2 đều dùng, Stream cho xử lý list element
```

---

**Tiếp theo:** [06 — Generics & Annotations](./06-generics-annotations.md)
