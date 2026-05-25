# Git

## 1. Git Concepts

```
Working Directory  →  Staging Area  →  Local Repo  →  Remote Repo
    (edit files)      (git add)        (git commit)    (git push)
```

- **Repository:** Thư mục chứa toàn bộ lịch sử code
- **Commit:** Snapshot của code tại 1 thời điểm
- **Branch:** Nhánh phát triển độc lập
- **Merge:** Gộp 2 nhánh lại
- **Remote:** Repository trên server (GitHub, GitLab)

---

## 2. Workflow cơ bản

```bash
# Clone repo
git clone https://github.com/org/repo.git
cd repo

# Tạo branch mới từ main
git checkout main
git pull origin main
git checkout -b feature/add-login-tests

# Làm việc, add và commit
git add src/test/java/LoginTest.java
git add src/test/resources/features/login.feature
git commit -m "feat(auth): add login test scenarios"

# Push lên remote
git push -u origin feature/add-login-tests

# Tạo Pull Request trên GitHub/GitLab
# Sau khi merge, cleanup
git checkout main
git pull origin main
git branch -d feature/add-login-tests
```

---

## 3. Branching Strategies

### Git Flow
```
main          ──────────────────────────────────────► (production)
              ↑                                  ↑
develop   ────┼──────────────────────────────────┤
              ↑          ↑                       ↑
feature/x ───►           │                       │
                  release/1.0 ──────────────────►│
                                    hotfix/bug ──►│
```
- **Phù hợp:** Release theo schedule, nhiều version song song
- **Nhược điểm:** Phức tạp, nhiều branch

### GitHub Flow (Đơn giản hơn)
```
main ──────────────────────────────────────────────►
         ↑              ↑              ↑
feature/a ──►    feature/b ──►   fix/bug ──►
```
- **Phù hợp:** Deploy liên tục (continuous deployment)
- **Quy tắc:** main luôn deployable, feature branch → PR → merge

### Trunk-based Development
```
main (trunk) ──────────────────────────────────────►
    ↑    ↑    ↑    ↑    ↑    ↑
  short-lived feature branches (< 1 ngày)
```
- **Phù hợp:** CI/CD mạnh, team có kinh nghiệm
- **Yêu cầu:** Feature flags, automated testing tốt

---

## 4. Merge vs Rebase

```bash
# Merge - tạo merge commit, giữ nguyên lịch sử
git checkout feature/login
git merge main
# Kết quả: có thêm merge commit "Merge branch 'main' into feature/login"

# Rebase - viết lại lịch sử, linear history
git checkout feature/login
git rebase main
# Kết quả: commits của feature/login được "replay" lên đầu main
```

```
Trước:
main:    A - B - C
feature:     D - E

Sau merge:
main:    A - B - C - M (merge commit)
                ↗
feature:     D - E

Sau rebase:
main:    A - B - C
feature:         D' - E' (commits được viết lại)
```

**Khi nào dùng:**
- **Merge:** Khi muốn giữ lịch sử đầy đủ, feature branch public
- **Rebase:** Khi muốn lịch sử linear, clean; chỉ dùng cho branch local/private

**KHÔNG rebase branch đã push lên remote** → gây conflict cho người khác

---

## 5. Git Commands quan trọng

```bash
# Stash - lưu tạm thay đổi chưa commit
git stash                    # Lưu tạm
git stash pop                # Lấy lại
git stash list               # Xem danh sách
git stash apply stash@{1}    # Áp dụng stash cụ thể

# Cherry-pick - lấy 1 commit từ branch khác
git cherry-pick abc1234      # Lấy commit có hash abc1234

# Reset - quay về commit trước
git reset --soft HEAD~1      # Undo commit, giữ changes trong staging
git reset --mixed HEAD~1     # Undo commit, giữ changes trong working dir
git reset --hard HEAD~1      # Undo commit, XÓA changes (nguy hiểm!)

# Revert - tạo commit mới để undo (an toàn hơn reset)
git revert abc1234           # Tạo commit đảo ngược commit abc1234

# Reflog - lịch sử tất cả thao tác (cứu nguy khi lỡ reset --hard)
git reflog
git reset --hard HEAD@{2}    # Quay về trạng thái 2 bước trước

# Interactive rebase - sửa lịch sử commit
git rebase -i HEAD~3         # Sửa 3 commit gần nhất
# pick, squash, reword, drop
```

---

## 6. .gitignore cho Automation Project

```gitignore
# Build output
target/
build/
out/

# IDE
.idea/
*.iml
.vscode/
*.classpath
*.project

# Test reports
allure-results/
allure-report/
test-output/
cucumber-reports/

# Logs
*.log
logs/

# Screenshots (optional - có thể muốn giữ)
target/screenshots/

# Config với secrets
config/local.properties
.env
secrets.properties

# OS
.DS_Store
Thumbs.db

# Maven wrapper (nên commit)
# !.mvn/
# !mvnw
# !mvnw.cmd
```

---

## 7. Commit Message Convention (Conventional Commits)

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

```bash
# Types:
feat:     Tính năng mới
fix:      Bug fix
test:     Thêm/sửa test
refactor: Refactor code
docs:     Cập nhật tài liệu
chore:    Maintenance (update deps, config)
ci:       CI/CD changes
perf:     Performance improvement

# Ví dụ:
git commit -m "feat(auth): add login test with valid credentials"
git commit -m "fix(checkout): fix flaky test due to timing issue"
git commit -m "test(api): add user CRUD API tests"
git commit -m "refactor(pages): extract common methods to BasePage"
git commit -m "chore(deps): update selenium to 4.18.1"
```

---

## 8. Pull Request Best Practices

```markdown
## PR Title (< 70 chars)
feat(auth): add login and logout test scenarios

## Description
### What changed
- Added LoginTest with 5 test scenarios
- Added LogoutTest with 2 test scenarios
- Updated LoginPage with new locators

### Why
Covers acceptance criteria AC-123 and AC-124

### Testing
- All tests pass locally
- Ran smoke suite: 15/15 passed
- Tested on Chrome 121 and Firefox 122

### Screenshots (if UI changes)
[attach screenshots]

### Checklist
- [ ] Tests pass locally
- [ ] No hardcoded test data
- [ ] Follows POM pattern
- [ ] No Thread.sleep()
```

---

## 9. Câu hỏi phỏng vấn

**Q1: Sự khác nhau giữa git reset và git revert?**
> **Trả lời:** git reset xóa commit khỏi lịch sử (nguy hiểm nếu đã push). git revert tạo commit mới để undo — an toàn hơn vì không xóa lịch sử. Luôn dùng revert cho branch đã push.
>
> **Gợi nhớ:** reset = xóa lịch sử (nguy hiểm), revert = thêm lịch sử mới (an toàn)

**Q2: Khi nào dùng git stash?**
> **Trả lời:** Khi đang làm dở feature nhưng cần switch sang branch khác để fix urgent bug. Stash lưu tạm thay đổi chưa commit, cho phép switch branch, sau đó stash pop để lấy lại.
>
> **Gợi nhớ:** Stash = ngăn kéo tạm, cất đồ vào khi cần làm việc khác

**Q3: Merge vs Rebase - khi nào dùng cái nào?**
> **Trả lời:** Merge khi muốn giữ lịch sử đầy đủ và branch đã public. Rebase khi muốn lịch sử linear, clean và branch chỉ local. Không rebase branch đã push vì sẽ gây conflict cho người khác.
>
> **Gợi nhớ:** Merge = giữ nguyên lịch sử, Rebase = viết lại lịch sử (chỉ local)

**Q4: Conventional Commits có lợi ích gì?**
> **Trả lời:** Commit message có cấu trúc chuẩn → dễ đọc lịch sử, tự động generate CHANGELOG, trigger CI/CD theo type (feat → minor version, fix → patch version). Cả team hiểu commit làm gì chỉ qua title.
>
> **Gợi nhớ:** Conventional Commits = ngôn ngữ chung cho team, máy đọc được

---

[Tiếp theo: 03-allure-report.md](./03-allure-report.md) | [Quay lại README](./README.md)
