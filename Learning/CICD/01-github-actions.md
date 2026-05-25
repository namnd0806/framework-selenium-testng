# GitHub Actions

## 1. Concepts

```
Workflow (.github/workflows/*.yml)
└── Job (chạy trên 1 runner)
    └── Step (1 action hoặc 1 command)
        ├── uses: actions/checkout@v4  (dùng action có sẵn)
        └── run: mvn test              (chạy command)
```

**Triggers:**
```yaml
on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]
  schedule:
    - cron: '0 2 * * *'    # Chạy lúc 2AM mỗi ngày
  workflow_dispatch:         # Trigger thủ công từ GitHub UI
    inputs:
      browser:
        description: 'Browser to test'
        default: 'chrome'
        required: true
```

---

## 2. Workflow YAML đầy đủ

```yaml
name: Selenium TestNG CI

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]
  workflow_dispatch:

jobs:
  test:
    name: Run Automation Tests
    runs-on: ubuntu-latest

    steps:
      # 1. Checkout code
      - name: Checkout repository
        uses: actions/checkout@v4

      # 2. Setup Java
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      # 3. Cache Maven dependencies
      - name: Cache Maven packages
        uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      # 4. Run tests
      - name: Run Selenium Tests
        run: mvn test -Pci -Dheadless=true -Dbrowser=chrome
        env:
          BASE_URL: ${{ secrets.STAGING_URL }}
          API_KEY: ${{ secrets.API_KEY }}

      # 5. Upload Allure results (dù pass hay fail)
      - name: Upload Allure Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: allure-results-${{ github.run_number }}
          path: target/allure-results
          retention-days: 30

      # 6. Upload test reports
      - name: Upload Surefire Reports
        uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: surefire-reports
          path: target/surefire-reports
```

---

## 3. Matrix Strategy

```yaml
jobs:
  test:
    strategy:
      fail-fast: false  # Tiếp tục chạy dù 1 matrix fail
      matrix:
        browser: [chrome, firefox]
        java: [17, 21]
        os: [ubuntu-latest, windows-latest]
        # Loại trừ combination không cần
        exclude:
          - os: windows-latest
            browser: firefox

    runs-on: ${{ matrix.os }}
    name: Test on ${{ matrix.browser }} / Java ${{ matrix.java }}

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: ${{ matrix.java }}
          distribution: 'temurin'
      - name: Run tests
        run: mvn test -Dbrowser=${{ matrix.browser }} -Dheadless=true
```

---

## 4. Secrets

```yaml
# Dùng secrets trong workflow
- name: Run API Tests
  run: mvn test -Dapi.base.url=${{ secrets.API_BASE_URL }}
  env:
    DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
    AUTH_TOKEN: ${{ secrets.AUTH_TOKEN }}
```

**Cách thêm secrets:**
1. GitHub repo → Settings → Secrets and variables → Actions
2. New repository secret
3. Dùng `${{ secrets.SECRET_NAME }}` trong workflow

**Không bao giờ hardcode secrets trong workflow file!**

---

## 5. Publish Allure Report lên GitHub Pages

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Run Tests
        run: mvn test -Dheadless=true

      - name: Get Allure History
        uses: actions/checkout@v4
        if: always()
        continue-on-error: true
        with:
          ref: gh-pages
          path: gh-pages

      - name: Generate Allure Report
        uses: simple-elf/allure-report-action@master
        if: always()
        with:
          allure_results: target/allure-results
          allure_history: allure-history
          keep_reports: 20  # Giữ 20 lần chạy gần nhất

      - name: Deploy to GitHub Pages
        if: always()
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_branch: gh-pages
          publish_dir: allure-history
```

---

## 6. Ví dụ workflow hoàn chỉnh - Selenium + TestNG + Allure

```yaml
name: Full Automation Suite

on:
  schedule:
    - cron: '0 1 * * 1-5'  # Thứ 2-6 lúc 1AM
  workflow_dispatch:
    inputs:
      tags:
        description: 'TestNG groups to run'
        default: 'smoke'
        required: false

jobs:
  smoke-test:
    name: Smoke Tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - uses: actions/cache@v4
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

      - name: Run Smoke Tests
        run: |
          mvn test \
            -Dgroups="${{ github.event.inputs.tags || 'smoke' }}" \
            -Dheadless=true \
            -Dbrowser=chrome \
            -Denv=staging
        env:
          STAGING_URL: ${{ secrets.STAGING_URL }}

      - name: Upload Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: allure-results
          path: target/allure-results

      - name: Notify on Failure
        if: failure()
        uses: 8398a7/action-slack@v3
        with:
          status: failure
          text: 'Smoke tests FAILED! Check: ${{ github.server_url }}/${{ github.repository }}/actions/runs/${{ github.run_id }}'
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK }}
```

---

## 7. Câu hỏi phỏng vấn

**Q1: GitHub Actions workflow, job, step khác nhau thế nào?**
> **Trả lời:** Workflow = file YAML định nghĩa toàn bộ CI pipeline. Job = tập hợp steps chạy trên 1 runner (máy ảo). Step = 1 action hoặc 1 command trong job. Nhiều jobs có thể chạy song song.
>
> **Gợi nhớ:** Workflow > Job > Step (từ lớn đến nhỏ)

**Q2: Tại sao cần cache Maven dependencies?**
> **Trả lời:** Mỗi lần chạy CI, runner là máy mới → phải download tất cả dependencies từ đầu (mất 2-5 phút). Cache lưu ~/.m2 giữa các runs → chỉ download khi pom.xml thay đổi → tiết kiệm thời gian.
>
> **Gợi nhớ:** Cache = tủ lạnh, không cần ra chợ mua mỗi ngày

**Q3: if: always() trong upload artifact dùng để làm gì?**
> **Trả lời:** Mặc định, step sau không chạy nếu step trước fail. if: always() đảm bảo step upload artifacts luôn chạy dù test pass hay fail → luôn có report để debug.
>
> **Gợi nhớ:** always() = "dù sống hay chết cũng phải upload report"

**Q4: Matrix strategy giải quyết vấn đề gì?**
> **Trả lời:** Chạy cùng 1 test suite trên nhiều combination (browser × OS × Java version) song song. Thay vì viết nhiều jobs giống nhau, dùng matrix để tự động tạo combinations.
>
> **Gợi nhớ:** Matrix = bảng nhân, 2 browsers × 2 OS = 4 jobs tự động

---

[Tiếp theo: 02-docker-for-testing.md](./02-docker-for-testing.md) | [Quay lại README](./README.md)
