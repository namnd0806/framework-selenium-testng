# IT Vocabulary - Từ vựng IT/Testing

## 1. Testing Terms (Thuật ngữ Testing)

| Từ | Nghĩa | Ví dụ câu |
|----|-------|-----------|
| **test case** | ca kiểm thử | "I wrote 20 test cases for the login feature." |
| **test suite** | bộ test | "The regression test suite takes 2 hours to run." |
| **regression** | kiểm thử hồi quy | "We run regression tests before every release." |
| **smoke test** | kiểm thử khói (cơ bản) | "Smoke tests verify the app starts and core features work." |
| **sanity test** | kiểm thử tỉnh táo | "After a hotfix, we do a quick sanity check." |
| **exploratory testing** | kiểm thử khám phá | "I spent 2 hours on exploratory testing of the new feature." |
| **edge case** | trường hợp biên | "What's the edge case when the input is empty?" |
| **boundary value** | giá trị biên | "Test boundary values: 0, 1, max-1, max." |
| **equivalence partition** | phân vùng tương đương | "Use equivalence partitioning to reduce test cases." |
| **flaky test** | test không ổn định | "This test is flaky — it fails randomly due to timing." |
| **blocker** | lỗi chặn | "Found a blocker bug — can't proceed with testing." |
| **severity** | mức độ nghiêm trọng | "The severity is critical — data loss possible." |
| **priority** | độ ưu tiên | "High priority — fix before the release." |
| **defect / bug / issue** | lỗi | "I logged a defect in Jira for this issue." |
| **test coverage** | độ phủ test | "We have 80% test coverage for the API layer." |
| **false positive** | kết quả dương tính giả | "This is a false positive — the test fails but the feature works." |
| **false negative** | kết quả âm tính giả | "False negative — test passes but there's actually a bug." |
| **assertion** | khẳng định/kiểm tra | "The assertion failed because the title was wrong." |
| **locator** | bộ định vị | "Use a stable locator like data-testid." |
| **selector** | bộ chọn | "The CSS selector targets the submit button." |

---

## 2. Development Terms (Thuật ngữ Phát triển)

| Từ | Nghĩa | Ví dụ câu |
|----|-------|-----------|
| **sprint** | chu kỳ phát triển (1-4 tuần) | "We're in sprint 12, ending Friday." |
| **backlog** | danh sách công việc chờ | "The product backlog has 50 items." |
| **story / user story** | câu chuyện người dùng | "This story covers the checkout flow." |
| **epic** | nhóm stories lớn | "The payment epic includes 5 stories." |
| **acceptance criteria** | tiêu chí chấp nhận | "The acceptance criteria says the page loads in 3s." |
| **definition of done** | định nghĩa hoàn thành | "Our DoD requires unit tests and code review." |
| **technical debt** | nợ kỹ thuật | "We have technical debt in the test framework." |
| **refactor** | tái cấu trúc | "I refactored the BasePage to reduce duplication." |
| **deployment** | triển khai | "The deployment to staging failed." |
| **release** | phát hành | "The release is scheduled for next Monday." |
| **hotfix** | sửa lỗi khẩn cấp | "We need a hotfix for the payment bug in production." |
| **rollback** | quay lại phiên bản cũ | "We had to rollback the deployment due to errors." |
| **merge** | gộp code | "Merge your branch after the PR is approved." |
| **branch** | nhánh | "Create a feature branch for each story." |
| **commit** | lưu thay đổi | "Commit your changes with a meaningful message." |
| **pull request / PR** | yêu cầu gộp code | "Open a PR when your feature is ready for review." |
| **code review** | xem xét code | "The code review found 3 issues." |
| **dependency** | phụ thuộc | "Add the REST Assured dependency to pom.xml." |

---

## 3. Infrastructure Terms (Thuật ngữ Hạ tầng)

| Từ | Nghĩa | Ví dụ câu |
|----|-------|-----------|
| **environment** | môi trường | "Tests pass on staging but fail on production." |
| **staging** | môi trường kiểm thử | "Deploy to staging before production." |
| **production / prod** | môi trường thực tế | "Never run destructive tests on production." |
| **pipeline** | quy trình tự động | "The CI pipeline runs tests on every push." |
| **CI/CD** | tích hợp/triển khai liên tục | "We use GitHub Actions for CI/CD." |
| **container** | hộp chứa ứng dụng | "Run Selenium Grid in Docker containers." |
| **instance** | phiên bản đang chạy | "Spin up a new EC2 instance for testing." |
| **endpoint** | điểm cuối API | "The /api/users endpoint returns a list of users." |
| **API** | giao diện lập trình | "Test the REST API with REST Assured." |
| **microservice** | dịch vụ nhỏ | "The payment microservice handles transactions." |
| **latency** | độ trễ | "High latency causes flaky tests." |
| **throughput** | thông lượng | "The API handles 1000 requests per second." |
| **uptime** | thời gian hoạt động | "The SLA requires 99.9% uptime." |
| **load balancer** | cân bằng tải | "Requests are distributed via a load balancer." |
| **timeout** | hết thời gian chờ | "Increase the timeout for slow API calls." |

---

## 4. Agile Terms (Thuật ngữ Agile)

| Từ | Nghĩa | Ví dụ câu |
|----|-------|-----------|
| **scrum** | framework agile | "We follow Scrum with 2-week sprints." |
| **kanban** | bảng công việc | "Use a Kanban board to track test progress." |
| **velocity** | tốc độ team | "Our velocity is 40 story points per sprint." |
| **burndown** | biểu đồ tiến độ | "The burndown chart shows we're on track." |
| **retrospective / retro** | họp nhìn lại | "In the retro, we discussed flaky tests." |
| **standup / daily** | họp hàng ngày | "In standup, I mentioned the blocking issue." |
| **planning poker** | ước tính story points | "We used planning poker to estimate the story." |
| **story point** | điểm ước tính | "This story is 5 story points." |
| **stakeholder** | bên liên quan | "The stakeholder approved the test plan." |
| **iteration** | vòng lặp | "Each iteration delivers working software." |

---

## 5. Phát âm hay sai

| Từ | Phát âm đúng | Hay sai |
|----|-------------|---------|
| **cache** | /kæʃ/ (kash) | /keɪʃ/ (kaysh) |
| **schema** | /ˈskiːmə/ (skee-ma) | /ˈʃiːmə/ |
| **queue** | /kjuː/ (kyoo) | /kwee/ |
| **facade** | /fəˈsɑːd/ (fa-sahd) | /ˈfeɪsɪd/ |
| **deprecated** | /ˈdeprəkeɪtɪd/ | /dɪˈpriːkeɪtɪd/ |
| **boolean** | /ˈbuːliən/ (boo-lee-an) | /ˈbɒliən/ |
| **null** | /nʌl/ (nul) | /njuːl/ |
| **regex** | /ˈriːdʒeks/ (ree-jex) | /ˈreɪdʒeks/ |
| **Maven** | /ˈmeɪvən/ (may-ven) | /ˈmɑːvən/ |
| **Ubuntu** | /ʊˈbʊntuː/ (oo-boon-too) | /juːˈbʌntuː/ |
| **SQL** | /ˈsiːkwəl/ (see-kwel) | /ˈɛskjuːˈɛl/ (cả 2 đều chấp nhận) |
| **API** | /ˌeɪpiːˈaɪ/ (ay-pee-eye) | /ˈæpi/ |

---

## 6. Ví dụ câu dùng trong công việc

```
Testing:
"I found a critical bug in the payment flow."
"The test is failing intermittently — might be a timing issue."
"I'll add a test case for this edge case."
"The acceptance criteria is not clear — let me clarify with the BA."

Code:
"I refactored the login page to use the BasePage pattern."
"This method is deprecated — use the new API instead."
"I added a null check to prevent NullPointerException."
"The test is flaky because of a race condition."

CI/CD:
"The pipeline failed at the test stage."
"I'll investigate why the build is broken."
"Tests are passing locally but failing in CI."
"Let me check the logs to find the root cause."
```

---

[Tiếp theo: 02-work-communication.md](./02-work-communication.md) | [Quay lại README](./README.md)
