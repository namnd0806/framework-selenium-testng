# Requirements Document

## Introduction

Framework tự động hóa kiểm thử Selenium được xây dựng theo mindset SDET (Software Development Engineer in Test) chuyên nghiệp. Framework cung cấp nền tảng vững chắc để viết, tổ chức và thực thi các bài kiểm thử UI tự động trên nhiều trình duyệt khác nhau, hỗ trợ chạy song song nhằm tối ưu thời gian thực thi, đồng thời đảm bảo khả năng bảo trì và mở rộng cao theo các nguyên tắc kỹ thuật phần mềm.

---

## Glossary

- **Framework**: Hệ thống tự động hóa kiểm thử Selenium được mô tả trong tài liệu này.
- **Test_Runner**: Thành phần chịu trách nhiệm phát hiện, lên lịch và thực thi các test case.
- **WebDriver_Manager**: Thành phần quản lý vòng đời của WebDriver instance (khởi tạo, cấu hình, hủy).
- **Page_Object**: Lớp đại diện cho một trang hoặc thành phần UI, đóng gói các locator và hành động tương tác.
- **Test_Case**: Một kịch bản kiểm thử độc lập được viết bởi SDET.
- **Test_Suite**: Tập hợp các Test_Case được nhóm lại theo mục đích hoặc phạm vi kiểm thử.
- **Config_Manager**: Thành phần đọc và cung cấp cấu hình runtime cho toàn bộ Framework.
- **Report_Engine**: Thành phần tổng hợp kết quả kiểm thử và tạo báo cáo.
- **Driver_Factory**: Thành phần tạo và cung cấp WebDriver instance phù hợp với trình duyệt được chỉ định.
- **Base_Page**: Lớp cha trừu tượng mà mọi Page_Object phải kế thừa, cung cấp các phương thức tương tác chung.
- **Base_Test**: Lớp cha trừu tượng mà mọi Test_Case phải kế thừa, quản lý setup/teardown.
- **Fixture**: Dữ liệu hoặc trạng thái được chuẩn bị trước khi thực thi Test_Case.
- **Parallel_Executor**: Thành phần điều phối việc chạy đồng thời nhiều Test_Case hoặc Test_Suite.
- **CI_Pipeline**: Hệ thống tích hợp liên tục (ví dụ: Jenkins, GitHub Actions, GitLab CI).
- **Locator**: Chiến lược xác định phần tử UI (ID, CSS Selector, XPath, v.v.).
- **Explicit_Wait**: Cơ chế chờ có điều kiện của Selenium WebDriver.
- **Screenshot_Capturer**: Thành phần chụp ảnh màn hình khi test thất bại.
- **Log_Manager**: Thành phần ghi nhật ký hoạt động trong quá trình thực thi.
- **Data_Provider**: Thành phần cung cấp dữ liệu đầu vào cho Data-Driven Testing.
- **Retry_Analyzer**: Thành phần phát hiện và thực thi lại các test thất bại do lỗi không ổn định (flaky test).

---

## Requirements

---

### Requirement 1: Cấu Trúc Dự Án Theo Chuẩn SDET

**User Story:** Là một SDET, tôi muốn framework có cấu trúc thư mục và phân lớp rõ ràng theo chuẩn kỹ thuật phần mềm, để tôi có thể bảo trì, mở rộng và onboard thành viên mới một cách dễ dàng.

#### Acceptance Criteria

1. THE Framework SHALL tổ chức mã nguồn theo cấu trúc phân lớp bao gồm: `src/main` (core framework), `src/test` (test cases), `src/resources` (cấu hình và dữ liệu kiểm thử).
2. THE Framework SHALL tách biệt hoàn toàn logic kiểm thử khỏi logic điều khiển trình duyệt thông qua lớp Page_Object và Base_Page.
3. THE Framework SHALL cung cấp Base_Test làm lớp cha cho tất cả Test_Case, đảm bảo setup và teardown nhất quán.
4. THE Framework SHALL cung cấp Base_Page làm lớp cha cho tất cả Page_Object, đóng gói các phương thức tương tác WebDriver dùng chung.
5. WHEN một SDET tạo Page_Object mới, THE Framework SHALL yêu cầu Page_Object đó kế thừa Base_Page để đảm bảo tính nhất quán.
6. THE Framework SHALL sử dụng hệ thống quản lý dependency (Maven hoặc Gradle) để quản lý toàn bộ thư viện phụ thuộc.
7. THE Framework SHALL cung cấp tài liệu README mô tả cấu trúc dự án, cách cài đặt và cách chạy test.

---

### Requirement 2: Quản Lý WebDriver và Hỗ Trợ Đa Trình Duyệt

**User Story:** Là một SDET, tôi muốn framework tự động quản lý WebDriver và hỗ trợ nhiều trình duyệt, để tôi có thể thực thi kiểm thử trên Chrome, Firefox, Edge và Safari mà không cần thay đổi mã kiểm thử.

#### Acceptance Criteria

1. THE Driver_Factory SHALL hỗ trợ khởi tạo WebDriver cho các trình duyệt: Chrome, Firefox, Microsoft Edge và Safari.
2. WHEN một Test_Case được khởi chạy, THE Driver_Factory SHALL tạo WebDriver instance tương ứng với giá trị `browser` được chỉ định trong cấu hình hoặc tham số dòng lệnh.
3. THE WebDriver_Manager SHALL sử dụng WebDriverManager (io.github.bonigarcia) hoặc cơ chế tương đương để tự động tải và cấu hình driver binary phù hợp với phiên bản trình duyệt đang cài đặt.
4. WHEN một Test_Case kết thúc (dù thành công hay thất bại), THE WebDriver_Manager SHALL đóng và giải phóng WebDriver instance tương ứng.
5. THE Driver_Factory SHALL hỗ trợ chế độ headless cho Chrome và Firefox để thực thi trong môi trường CI_Pipeline không có giao diện đồ họa.
6. IF trình duyệt được chỉ định không được hỗ trợ, THEN THE Driver_Factory SHALL ném ra ngoại lệ có thông báo rõ ràng chỉ định tên trình duyệt không hợp lệ.
7. THE WebDriver_Manager SHALL lưu trữ WebDriver instance theo từng luồng thực thi (thread-local) để đảm bảo an toàn khi chạy song song.

---

### Requirement 3: Thực Thi Song Song (Parallel Execution)

**User Story:** Là một SDET, tôi muốn framework hỗ trợ chạy nhiều test đồng thời, để giảm tổng thời gian thực thi của Test_Suite và tăng hiệu quả phản hồi trong CI_Pipeline.

#### Acceptance Criteria

1. THE Parallel_Executor SHALL hỗ trợ thực thi đồng thời tối thiểu 4 Test_Case trên các luồng độc lập.
2. THE Framework SHALL sử dụng TestNG hoặc JUnit 5 với cấu hình parallel để điều phối Parallel_Executor.
3. WHEN nhiều Test_Case chạy đồng thời, THE WebDriver_Manager SHALL đảm bảo mỗi luồng sử dụng WebDriver instance riêng biệt, không chia sẻ trạng thái.
4. THE Config_Manager SHALL cho phép cấu hình số luồng song song tối đa thông qua tham số `thread.count` trong file cấu hình.
5. WHEN một Test_Case trong luồng song song thất bại, THE Parallel_Executor SHALL tiếp tục thực thi các Test_Case còn lại mà không bị gián đoạn.
6. THE Framework SHALL hỗ trợ cấu hình parallel ở cả cấp độ Test_Suite (suite-level) và cấp độ Test_Case (method-level).
7. WHILE nhiều Test_Case đang chạy song song, THE Log_Manager SHALL ghi nhật ký có nhãn định danh luồng (thread ID) để phân biệt output của từng Test_Case.

---

### Requirement 4: Mô Hình Page Object (Page Object Model)

**User Story:** Là một SDET, tôi muốn framework áp dụng mô hình Page Object, để tôi có thể tái sử dụng logic tương tác UI và giảm thiểu công sức bảo trì khi giao diện thay đổi.

#### Acceptance Criteria

1. THE Base_Page SHALL cung cấp phương thức `findElement(Locator)` với Explicit_Wait tích hợp sẵn, chờ tối đa thời gian được cấu hình trong `explicit.wait.timeout` (tính bằng giây).
2. THE Base_Page SHALL cung cấp các phương thức tương tác cơ bản: click, sendKeys, getText, isDisplayed, selectFromDropdown.
3. THE Base_Page SHALL cung cấp phương thức `waitForPageLoad()` chờ cho đến khi `document.readyState` của trang bằng `"complete"`.
4. WHEN một phần tử không được tìm thấy trong thời gian `explicit.wait.timeout`, THE Base_Page SHALL ném ra ngoại lệ có thông báo bao gồm tên Locator và thời gian chờ đã vượt quá.
5. THE Page_Object SHALL chỉ chứa các Locator và phương thức tương tác với trang tương ứng, không chứa logic xác nhận (assertion).
6. THE Base_Page SHALL hỗ trợ khởi tạo Page_Object thông qua PageFactory của Selenium để quản lý Locator theo chuẩn.
7. WHERE JavaScript execution được yêu cầu, THE Base_Page SHALL cung cấp phương thức `executeScript(script, args)` để thực thi JavaScript trực tiếp trên trình duyệt.

---

### Requirement 5: Quản Lý Cấu Hình

**User Story:** Là một SDET, tôi muốn framework có hệ thống quản lý cấu hình tập trung và linh hoạt, để tôi có thể thay đổi môi trường, trình duyệt và các tham số thực thi mà không cần sửa mã nguồn.

#### Acceptance Criteria

1. THE Config_Manager SHALL đọc cấu hình từ file `config.properties` hoặc `config.yaml` đặt trong thư mục `src/resources`.
2. THE Config_Manager SHALL hỗ trợ các tham số cấu hình tối thiểu: `base.url`, `browser`, `headless`, `implicit.wait.timeout`, `explicit.wait.timeout`, `thread.count`, `screenshot.on.failure`.
3. WHEN một tham số được truyền qua dòng lệnh (system property), THE Config_Manager SHALL ưu tiên giá trị từ dòng lệnh hơn giá trị trong file cấu hình.
4. THE Config_Manager SHALL hỗ trợ nhiều profile môi trường (ví dụ: `dev`, `staging`, `production`) được kích hoạt thông qua tham số `env`.
5. IF một tham số bắt buộc bị thiếu trong cấu hình, THEN THE Config_Manager SHALL ném ra ngoại lệ có thông báo chỉ định tên tham số bị thiếu trước khi bất kỳ Test_Case nào được thực thi.
6. THE Config_Manager SHALL cung cấp phương thức truy cập cấu hình theo kiểu type-safe (String, Integer, Boolean) để tránh lỗi ép kiểu tại runtime.
7. THE Config_Manager SHALL được triển khai theo mẫu Singleton để đảm bảo chỉ có một instance duy nhất trong suốt quá trình thực thi.

---

### Requirement 6: Báo Cáo Kết Quả Kiểm Thử

**User Story:** Là một SDET, tôi muốn framework tạo báo cáo kiểm thử chi tiết và trực quan, để tôi và các bên liên quan có thể nhanh chóng đánh giá kết quả và xác định nguyên nhân thất bại.

#### Acceptance Criteria

1. THE Report_Engine SHALL tạo báo cáo HTML sau mỗi lần thực thi Test_Suite, bao gồm: tổng số test, số test thành công, số test thất bại, số test bị bỏ qua và thời gian thực thi.
2. THE Report_Engine SHALL tích hợp với Allure Report hoặc ExtentReports để cung cấp báo cáo tương tác với khả năng lọc và tìm kiếm.
3. WHEN một Test_Case thất bại và `screenshot.on.failure` được đặt là `true`, THE Screenshot_Capturer SHALL chụp ảnh màn hình và đính kèm vào báo cáo của Test_Case đó.
4. THE Report_Engine SHALL ghi lại thông tin chi tiết cho mỗi Test_Case bao gồm: tên test, trình duyệt sử dụng, thời gian bắt đầu, thời gian kết thúc, thông báo lỗi (nếu có) và stack trace (nếu có).
5. THE Log_Manager SHALL ghi nhật ký ở các cấp độ: DEBUG, INFO, WARN, ERROR sử dụng Log4j2 hoặc SLF4J.
6. WHEN một Test_Case thất bại, THE Log_Manager SHALL ghi log ở cấp độ ERROR bao gồm tên Test_Case, thông báo lỗi và tên luồng thực thi.
7. THE Report_Engine SHALL xuất kết quả kiểm thử theo định dạng XML tương thích JUnit để tích hợp với CI_Pipeline.

---

### Requirement 7: Kiểm Thử Dựa Trên Dữ Liệu (Data-Driven Testing)

**User Story:** Là một SDET, tôi muốn framework hỗ trợ Data-Driven Testing, để tôi có thể thực thi cùng một kịch bản kiểm thử với nhiều bộ dữ liệu đầu vào khác nhau mà không cần nhân bản mã kiểm thử.

#### Acceptance Criteria

1. THE Data_Provider SHALL hỗ trợ đọc dữ liệu kiểm thử từ các định dạng: Excel (.xlsx), CSV và JSON.
2. WHEN một Test_Case được chú thích với annotation Data-Driven, THE Test_Runner SHALL thực thi Test_Case đó một lần cho mỗi hàng dữ liệu được cung cấp bởi Data_Provider.
3. THE Data_Provider SHALL hỗ trợ lọc dữ liệu theo tên sheet (Excel) hoặc tên file (CSV/JSON) thông qua tham số cấu hình.
4. IF file dữ liệu không tồn tại tại đường dẫn được chỉ định, THEN THE Data_Provider SHALL ném ra ngoại lệ có thông báo bao gồm đường dẫn file không tìm thấy.
5. THE Data_Provider SHALL hỗ trợ kiểu dữ liệu: String, Integer, Boolean, Double khi đọc từ nguồn dữ liệu.
6. THE Framework SHALL cho phép đặt file dữ liệu kiểm thử trong thư mục `src/resources/testdata` và tham chiếu bằng đường dẫn tương đối.

---

### Requirement 8: Xử Lý Lỗi và Cơ Chế Retry

**User Story:** Là một SDET, tôi muốn framework có cơ chế xử lý lỗi thông minh và khả năng tự động thử lại, để giảm thiểu tác động của các flaky test do sự cố mạng hoặc trình duyệt không ổn định.

#### Acceptance Criteria

1. THE Retry_Analyzer SHALL tự động thực thi lại Test_Case thất bại tối đa số lần được cấu hình trong tham số `retry.count` (mặc định là 1).
2. WHEN một Test_Case được thực thi lại bởi Retry_Analyzer, THE Log_Manager SHALL ghi log ở cấp độ WARN bao gồm tên Test_Case và số lần thử lại hiện tại.
3. THE Base_Page SHALL bắt ngoại lệ `StaleElementReferenceException` và tự động thử lại thao tác tương tác tối đa 3 lần trước khi ném lại ngoại lệ.
4. WHEN một Test_Case thất bại sau tất cả các lần retry, THE Report_Engine SHALL đánh dấu Test_Case đó là FAILED và ghi lại số lần đã thử trong báo cáo.
5. THE Framework SHALL phân biệt lỗi do assertion thất bại (không retry) và lỗi do ngoại lệ kỹ thuật (có thể retry) thông qua cấu hình loại ngoại lệ được phép retry.

---

### Requirement 9: Tích Hợp CI/CD

**User Story:** Là một SDET, tôi muốn framework tích hợp liền mạch với CI_Pipeline, để các bài kiểm thử có thể được kích hoạt tự động trong quy trình phát triển phần mềm.

#### Acceptance Criteria

1. THE Framework SHALL hỗ trợ thực thi toàn bộ Test_Suite thông qua lệnh Maven (`mvn test`) hoặc Gradle (`gradle test`) mà không yêu cầu cấu hình bổ sung trên máy CI.
2. THE Framework SHALL cung cấp file cấu hình mẫu cho GitHub Actions, Jenkins và GitLab CI trong thư mục `.ci/`.
3. WHEN thực thi trong CI_Pipeline, THE Framework SHALL tự động kích hoạt chế độ headless nếu tham số `ci.mode` được đặt là `true`.
4. THE Framework SHALL trả về exit code khác 0 khi có ít nhất một Test_Case thất bại, để CI_Pipeline có thể phát hiện và xử lý kết quả kiểm thử.
5. THE Report_Engine SHALL lưu báo cáo và ảnh chụp màn hình vào thư mục `target/reports` để CI_Pipeline có thể thu thập artifact sau khi thực thi.
6. THE Framework SHALL hỗ trợ nhận tham số cấu hình qua biến môi trường (environment variables) để tương thích với cơ chế secret management của CI_Pipeline.

---

### Requirement 10: Tiện Ích và Công Cụ Hỗ Trợ SDET

**User Story:** Là một SDET, tôi muốn framework cung cấp các tiện ích và helper class sẵn có, để tôi có thể tập trung vào viết logic kiểm thử thay vì xây dựng lại các chức năng cơ bản.

#### Acceptance Criteria

1. THE Framework SHALL cung cấp lớp `WaitUtils` với các phương thức chờ tường minh: `waitForElementVisible`, `waitForElementClickable`, `waitForElementInvisible`, `waitForTextPresent`.
2. THE Framework SHALL cung cấp lớp `JavaScriptUtils` với các phương thức: cuộn trang đến phần tử, highlight phần tử, lấy giá trị thuộc tính qua JavaScript.
3. THE Framework SHALL cung cấp lớp `FileUtils` với các phương thức: đọc file, ghi file, kiểm tra sự tồn tại của file trong thư mục `target`.
4. THE Framework SHALL cung cấp lớp `AssertionUtils` bọc các assertion của TestNG/JUnit với thông báo lỗi mô tả rõ ràng hơn.
5. THE Framework SHALL cung cấp lớp `BrowserUtils` với các phương thức: chuyển tab, xử lý alert, chuyển sang iframe, chụp ảnh màn hình theo vùng.
6. WHEN một phương thức trong các lớp tiện ích ném ra ngoại lệ không mong đợi, THE Log_Manager SHALL ghi log ở cấp độ ERROR trước khi ngoại lệ được lan truyền lên Test_Case.
```
