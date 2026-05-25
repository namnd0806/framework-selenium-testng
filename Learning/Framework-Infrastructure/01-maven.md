# Maven

## 1. Maven là gì? Tại sao dùng?

Maven là build tool và dependency management cho Java. Giải quyết:
- **Dependency management:** Tự động download JAR từ Maven Central
- **Build lifecycle:** Chuẩn hóa quy trình compile → test → package
- **Project structure:** Convention over configuration
- **Plugin ecosystem:** Surefire, Failsafe, Allure, Docker...

---

## 2. POM.xml Structure đầy đủ

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- Project coordinates -->
    <groupId>com.example</groupId>
    <artifactId>selenium-testng-framework</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <!-- Properties - định nghĩa version tập trung -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <!-- Versions -->
        <selenium.version>4.18.1</selenium.version>
        <testng.version>7.9.0</testng.version>
        <rest-assured.version>5.4.0</rest-assured.version>
        <allure.version>2.25.0</allure.version>
        <webdrivermanager.version>5.8.0</webdrivermanager.version>
    </properties>

    <dependencies>
        <!-- Selenium -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>

        <!-- TestNG -->
        <dependency>
            <groupId>org.testng</groupId>
            <artifactId>testng</artifactId>
            <version>${testng.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- REST Assured -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <version>${rest-assured.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Allure TestNG -->
        <dependency>
            <groupId>io.qameta.allure</groupId>
            <artifactId>allure-testng</artifactId>
            <version>${allure.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- AssertJ -->
        <dependency>
            <groupId>org.assertj</groupId>
            <artifactId>assertj-core</artifactId>
            <version>3.25.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Maven Surefire - chạy unit/integration tests -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
                <configuration>
                    <suiteXmlFiles>
                        <suiteXmlFile>src/test/resources/testng.xml</suiteXmlFile>
                    </suiteXmlFiles>
                    <!-- System properties truyền vào test -->
                    <systemPropertyVariables>
                        <browser>${browser}</browser>
                        <env>${env}</env>
                        <headless>${headless}</headless>
                    </systemPropertyVariables>
                    <!-- Allure AspectJ weaver -->
                    <argLine>
                        -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/1.9.21/aspectjweaver-1.9.21.jar"
                    </argLine>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 3. Maven Lifecycle

```
validate   → Kiểm tra POM hợp lệ
compile    → Compile source code
test       → Chạy unit tests (Surefire)
package    → Đóng gói thành JAR/WAR
verify     → Chạy integration tests (Failsafe)
install    → Cài vào local repository (~/.m2)
deploy     → Upload lên remote repository
```

```bash
# Chạy từng phase
mvn compile
mvn test
mvn package
mvn verify

# Skip tests
mvn package -DskipTests
mvn package -Dmaven.test.skip=true  # Skip cả compile test

# Chạy test cụ thể
mvn test -Dtest=LoginTest
mvn test -Dtest=LoginTest#testValidLogin
mvn test -Dtest="Login*,Checkout*"  # Wildcard
```

---

## 4. Dependency Scope

| Scope | Compile | Test | Runtime | Đóng gói |
|-------|---------|------|---------|---------|
| compile (default) | ✅ | ✅ | ✅ | ✅ |
| test | ❌ | ✅ | ❌ | ❌ |
| provided | ✅ | ✅ | ❌ | ❌ |
| runtime | ❌ | ✅ | ✅ | ✅ |

```xml
<!-- test: chỉ dùng trong test code -->
<dependency>
    <groupId>org.testng</groupId>
    <artifactId>testng</artifactId>
    <scope>test</scope>
</dependency>

<!-- provided: server cung cấp (Servlet API) -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <scope>provided</scope>
</dependency>
```

---

## 5. Maven Profiles

```xml
<profiles>
    <!-- Profile cho môi trường staging -->
    <profile>
        <id>staging</id>
        <properties>
            <env>staging</env>
            <base.url>https://staging.example.com</base.url>
        </properties>
    </profile>

    <!-- Profile cho CI/CD -->
    <profile>
        <id>ci</id>
        <properties>
            <headless>true</headless>
            <browser>chrome</browser>
        </properties>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>
                    <configuration>
                        <parallel>methods</parallel>
                        <threadCount>4</threadCount>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

```bash
# Kích hoạt profile
mvn test -Pstaging
mvn test -Pci
mvn test -Pstaging,ci  # Nhiều profiles
```

---

## 6. Maven Wrapper

```bash
# Tạo Maven Wrapper
mvn wrapper:wrapper

# Dùng mvnw thay vì mvn
./mvnw test          # Linux/Mac
mvnw.cmd test        # Windows
```

**Tại sao dùng Maven Wrapper trong CI?**
- Đảm bảo tất cả developer và CI dùng cùng version Maven
- Không cần cài Maven trên CI server
- Version được commit vào repo → reproducible builds

---

## 7. Câu hỏi phỏng vấn

**Q1: Sự khác nhau giữa Maven Surefire và Failsafe plugin?**
> **Trả lời:** Surefire chạy unit tests (phase `test`), fail ngay nếu test fail. Failsafe chạy integration tests (phase `verify`), đảm bảo server được stop dù test fail hay pass. Tên class: *Test.java cho Surefire, *IT.java cho Failsafe.
>
> **Gợi nhớ:** Surefire = unit test, Failsafe = integration test (an toàn hơn)

**Q2: Dependency scope "test" có ý nghĩa gì?**
> **Trả lời:** Dependency chỉ available trong test code (src/test/java), không được compile vào production JAR. TestNG, REST Assured, AssertJ đều nên để scope test.
>
> **Gợi nhớ:** scope test = chỉ dùng trong phòng test, không ra ngoài production

**Q3: Maven profiles dùng để làm gì trong automation?**
> **Trả lời:** Cấu hình khác nhau cho từng môi trường (dev/staging/prod) hoặc từng loại chạy (local/CI). Ví dụ: profile CI bật headless, tăng thread count; profile staging dùng staging URL.
>
> **Gợi nhớ:** Profile = bộ cài đặt theo ngữ cảnh, như profile Facebook vs LinkedIn

**Q4: ${property} trong pom.xml dùng như thế nào?**
> **Trả lời:** Định nghĩa trong `<properties>`, dùng `${tên}` để tham chiếu. Giúp quản lý version tập trung — đổi version 1 chỗ, áp dụng cho tất cả dependencies dùng property đó.
>
> **Gợi nhớ:** Properties = biến toàn cục của pom.xml, đổi 1 chỗ áp dụng khắp nơi

---

[Tiếp theo: 02-git.md](./02-git.md) | [Quay lại README](./README.md)
