# Docker for Testing

## 1. Docker Concepts

```
Image  → Blueprint (như class trong Java)
Container → Instance đang chạy (như object)
Dockerfile → Script để build image
Docker Hub → Registry chứa images
docker-compose → Quản lý nhiều containers
```

```bash
# Lệnh cơ bản
docker pull selenium/node-chrome:4.18.1   # Download image
docker images                              # Xem images
docker ps                                  # Xem containers đang chạy
docker ps -a                               # Xem tất cả containers
docker stop <container_id>
docker rm <container_id>
docker rmi <image_id>                      # Xóa image
```

---

## 2. Tại sao dùng Docker trong Testing?

| Vấn đề không có Docker | Giải pháp với Docker |
|------------------------|---------------------|
| "Works on my machine" | Môi trường nhất quán |
| Cài Chrome/Firefox thủ công | Image đã có sẵn browser |
| Version browser khác nhau | Pin version trong image |
| Khó scale (thêm node) | docker-compose scale |
| Cleanup sau test | docker-compose down |

---

## 3. Selenium Grid với Docker

```yaml
# docker-compose.yml
version: "3.8"

services:
  # Hub - điều phối
  selenium-hub:
    image: selenium/hub:4.18.1
    container_name: selenium-hub
    ports:
      - "4442:4442"   # Event bus publish
      - "4443:4443"   # Event bus subscribe
      - "4444:4444"   # Grid UI và WebDriver endpoint
    environment:
      - SE_SESSION_REQUEST_TIMEOUT=300
      - SE_SESSION_RETRY_INTERVAL=5

  # Chrome Node
  chrome-node:
    image: selenium/node-chrome:4.18.1
    container_name: chrome-node
    shm_size: 2gb     # Shared memory - Chrome cần nhiều
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
      - SE_NODE_MAX_SESSIONS=3
      - SE_NODE_SESSION_TIMEOUT=300
      - SE_VNC_NO_PASSWORD=1
    ports:
      - "7900:7900"   # noVNC - xem browser qua web
    deploy:
      replicas: 2     # 2 Chrome nodes

  # Firefox Node
  firefox-node:
    image: selenium/node-firefox:4.18.1
    container_name: firefox-node
    shm_size: 2gb
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
      - SE_NODE_MAX_SESSIONS=2
    ports:
      - "7901:7900"

  # Edge Node (optional)
  edge-node:
    image: selenium/node-edge:4.18.1
    shm_size: 2gb
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
```

```bash
# Khởi động Grid
docker-compose up -d

# Xem Grid UI
# http://localhost:4444

# Xem browser qua noVNC (debug)
# http://localhost:7900 (Chrome)
# http://localhost:7901 (Firefox)

# Scale thêm Chrome nodes
docker-compose up -d --scale chrome-node=5

# Dừng và cleanup
docker-compose down
docker-compose down -v  # Xóa cả volumes
```

---

## 4. Chạy test trên Selenium Grid

```java
// DriverFactory.java - hỗ trợ cả local và Grid
public class DriverFactory {

    public static WebDriver createDriver(String browser, boolean useGrid) {
        if (useGrid) {
            return createRemoteDriver(browser);
        }
        return createLocalDriver(browser);
    }

    private static WebDriver createRemoteDriver(String browser) {
        String gridUrl = ConfigReader.get("grid.url"); // http://localhost:4444

        DesiredCapabilities caps;
        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage");
                return new RemoteWebDriver(new URL(gridUrl), chromeOptions);
            case "firefox":
                return new RemoteWebDriver(new URL(gridUrl), new FirefoxOptions());
            default:
                throw new IllegalArgumentException("Unsupported: " + browser);
        }
    }
}
```

```properties
# config/ci.properties
grid.url=http://localhost:4444
use.grid=true
browser=chrome
headless=false  # Grid có browser thật, không cần headless
```

---

## 5. Dockerfile cho Test Project

```dockerfile
# Dockerfile
FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app

# Copy pom.xml trước để cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Chạy test khi container start
CMD ["mvn", "test", "-Pci", "-Dheadless=true"]
```

```bash
# Build image
docker build -t my-selenium-tests:latest .

# Chạy test trong container
docker run --rm \
  --network selenium-grid_default \
  -e GRID_URL=http://selenium-hub:4444 \
  -e ENV=staging \
  -v $(pwd)/target:/app/target \
  my-selenium-tests:latest

# Lấy test results từ volume mount
ls target/allure-results/
```

---

## 6. Docker trong GitHub Actions

```yaml
# .github/workflows/test-with-docker.yml
name: Tests with Docker Grid

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      # Service containers - tự động start/stop
      selenium-hub:
        image: selenium/hub:4.18.1
        ports:
          - 4444:4444

      chrome-node:
        image: selenium/node-chrome:4.18.1
        options: --shm-size=2gb
        env:
          SE_EVENT_BUS_HOST: selenium-hub
          SE_EVENT_BUS_PUBLISH_PORT: 4442
          SE_EVENT_BUS_SUBSCRIBE_PORT: 4443

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Wait for Grid to be ready
        run: |
          timeout 60 bash -c 'until curl -s http://localhost:4444/status | grep -q "ready"; do sleep 2; done'

      - name: Run Tests on Grid
        run: mvn test -Duse.grid=true -Dgrid.url=http://localhost:4444

      - name: Upload Results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: target/allure-results
```

---

## 7. Câu hỏi phỏng vấn

**Q1: Tại sao cần shm_size: 2gb cho Selenium Chrome container?**
> **Trả lời:** Chrome dùng /dev/shm (shared memory) để render. Docker mặc định chỉ cấp 64MB → Chrome crash khi render trang phức tạp. Tăng lên 2GB giải quyết vấn đề này.
>
> **Gợi nhớ:** Chrome cần bộ nhớ chia sẻ, Docker mặc định quá ít → tăng shm_size

**Q2: noVNC trong Selenium Docker dùng để làm gì?**
> **Trả lời:** noVNC cho phép xem browser đang chạy trong container qua web browser (port 7900). Hữu ích để debug test fail — xem trực tiếp browser đang làm gì mà không cần cài VNC client.
>
> **Gợi nhớ:** noVNC = cửa sổ nhìn vào container, debug không cần cài thêm gì

**Q3: Sự khác nhau giữa docker-compose up và docker run?**
> **Trả lời:** docker run chạy 1 container đơn lẻ. docker-compose up chạy nhiều containers được định nghĩa trong docker-compose.yml, tự động tạo network giữa chúng, quản lý dependencies (depends_on).
>
> **Gợi nhớ:** docker run = 1 nhạc cụ, docker-compose = cả dàn nhạc

**Q4: Tại sao copy pom.xml trước rồi mới copy source code trong Dockerfile?**
> **Trả lời:** Docker cache layer theo thứ tự. Nếu pom.xml không thay đổi, layer `mvn dependency:go-offline` được cache → không cần download lại dependencies. Chỉ khi pom.xml thay đổi mới download lại.
>
> **Gợi nhớ:** pom.xml ít thay đổi hơn source code → cache dependencies riêng = build nhanh hơn

---

[Quay lại README](./README.md) | [Quay lại ROADMAP](../ROADMAP.md)
