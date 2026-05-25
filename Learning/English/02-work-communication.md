# Work Communication - Giao tiếp trong công việc

## 1. Daily Standup

**Template chuẩn:**
```
Yesterday I [completed/worked on/finished]...
Today I plan to [work on/start/continue]...
I'm blocked by / I have no blockers.
```

**Ví dụ thực tế:**
```
"Yesterday I finished writing test cases for the login feature
 and fixed 2 flaky tests in the regression suite.
 Today I plan to start automating the checkout flow.
 I'm blocked by the staging environment being down —
 I've already notified the DevOps team."

"Yesterday I reviewed the PR for the payment tests.
 Today I'll continue working on the API test framework.
 No blockers."
```

---

## 2. Hỏi Clarification

```
Hỏi rõ hơn:
"Could you clarify what you mean by [term]?"
"What do you mean by 'the test should pass'?"
"Could you give me an example of the expected behavior?"
"Just to confirm — you want me to test [X], right?"
"I want to make sure I understand correctly. Are you saying [restate]?"

Khi không hiểu requirement:
"The acceptance criteria is a bit ambiguous. Could we discuss it?"
"I'm not sure about the expected behavior when [edge case]. Could you clarify?"
"Is there any documentation or mockup I can refer to?"

Khi cần thêm thời gian:
"Let me look into this and get back to you."
"I'll need some time to investigate. Can I update you by [time]?"
"I'm not sure about this — let me check with [person] first."
```

---

## 3. Báo cáo vấn đề

```
Báo bug:
"I found an issue with the [feature]. When I [action], the expected result
 is [expected], but the actual result is [actual]."

"The test is failing because [reason]. Here's the error message: [error]"

"I've identified a potential issue in [area]. It might affect [impact].
 I'll investigate further and update you."

Báo tiến độ:
"I've completed [X] out of [Y] test cases."
"The automation for [feature] is 80% done. I expect to finish by [date]."
"I'm slightly behind schedule due to [reason]. I'll catch up by [date]."

Escalate vấn đề:
"I've been blocked by [issue] for [time]. I've tried [solutions] but
 haven't been able to resolve it. Could you help or escalate this?"
```

---

## 4. Email Templates

### Hỏi hỗ trợ
```
Subject: Help needed - [brief description]

Hi [Name],

I'm working on [task] and I've run into an issue.

Problem: [describe the problem clearly]
What I've tried: [list what you've already tried]
Expected: [what should happen]
Actual: [what is happening]

Could you help me with this? I'm available for a quick call if needed.

Thanks,
[Your name]
```

### Báo cáo tiến độ
```
Subject: Test Progress Update - Sprint [X]

Hi team,

Here's the testing progress for this sprint:

Completed:
- Login feature: 15/15 test cases automated ✅
- Checkout flow: 8/12 test cases automated (in progress)

Blocked:
- Payment tests: waiting for staging environment fix (ETA: tomorrow)

Next steps:
- Complete checkout automation by Wednesday
- Start regression suite update

Let me know if you have any questions.

[Your name]
```

### Báo cáo bug nghiêm trọng
```
Subject: [CRITICAL] Bug found in production - Payment flow

Hi [Manager/Team],

I've found a critical bug that affects the payment flow in production.

Summary: Users cannot complete payment when using Visa cards.
Impact: All Visa card transactions are failing (estimated 30% of users).
Steps to reproduce:
  1. Add item to cart
  2. Proceed to checkout
  3. Enter Visa card details
  4. Click "Pay Now"
  5. Error: "Payment gateway timeout"

I've already notified the DevOps team and created Jira ticket PROJ-456.

Recommended action: Consider rolling back to v2.3.1 until fixed.

[Your name]
```

---

## 5. Slack/Teams Communication

```
Informal nhưng professional:
"Hey, quick question about [topic] — do you have 5 mins?"
"FYI — the staging env is down. I've notified DevOps."
"Heads up — I found a bug in [feature]. Creating a Jira ticket now."
"LGTM on the PR! Just one minor comment."
"Thanks for the quick fix! 🙏"

Reactions:
👍 = agree / acknowledged
✅ = done / completed
👀 = looking into it
🔥 = urgent / on fire
❓ = question / unclear

Thread etiquette:
- Reply in thread, not in main channel
- Use @mention when you need someone's attention
- Keep messages concise
```

---

## 6. Code Review Comments

```
Suggest improvement (không phải lệnh):
"Consider using ExpectedConditions.elementToBeClickable() here
 instead of Thread.sleep() for better reliability."

"This might cause a NullPointerException if the element is not found.
 Consider adding a null check."

"Could we extract this into a helper method to avoid duplication?"

Positive feedback:
"LGTM (Looks Good To Me) ✅"
"Nice approach! This is much cleaner than the previous implementation."
"Good catch on the edge case!"

Minor issues (nit):
"nit: variable name could be more descriptive — maybe 'loginButton' instead of 'btn'?"
"nit: missing Javadoc comment for this public method."

Blocking issues:
"This will cause a race condition in parallel execution.
 We need to use ThreadLocal here. Blocking until fixed."

"The locator By.xpath('//div[1]/span[2]') is fragile.
 Please use a more stable locator like data-testid."
```

---

## 7. Meeting Phrases

```
Agree:
"That makes sense."
"I agree with that approach."
"Good point. Let's go with that."
"That works for me."

Disagree politely:
"I see your point, but I'm concerned about [reason]."
"That's a valid approach, but have we considered [alternative]?"
"I'm not sure that's the best solution because [reason]. What about [alternative]?"

Ask for time:
"Could we table this for now and revisit after the meeting?"
"I need more time to think about this. Can we discuss tomorrow?"

Summarize:
"So to summarize, we agreed to [action] by [date], right?"
"Let me recap: [person] will [action], and I'll [action]."

Ask for clarification in meeting:
"Sorry, could you repeat that?"
"I didn't quite catch that — could you say it again?"
"Could you elaborate on [point]?"
```

---

## 8. Phỏng vấn - Giới thiệu bản thân

```
Template:
"I'm [name], a [role] with [X] years of experience in [area].
 Currently, I'm working at [company] where I [main responsibilities].
 I specialize in [skills] and I'm passionate about [interest].
 I'm looking for [what you want in new role]."

Ví dụ:
"I'm Nam, a Software Development Engineer in Test with 2 years of experience
 in automation testing. Currently at ABC Company, I'm responsible for building
 and maintaining the Selenium TestNG framework for web UI testing and REST Assured
 for API testing. I specialize in Java-based automation and CI/CD integration
 with GitHub Actions. I'm looking for a role where I can work on more complex
 test architecture and contribute to improving test quality at scale."
```

### Mô tả kinh nghiệm (STAR method)
```
Situation: "In my current project, we had a test suite with 40% flaky tests..."
Task: "My task was to investigate and fix the root causes..."
Action: "I analyzed the failures, identified timing issues, replaced Thread.sleep()
         with explicit waits, and implemented a retry mechanism..."
Result: "As a result, flaky tests dropped from 40% to under 5%, and the CI
         pipeline became much more reliable."
```

### Trả lời câu hỏi kỹ thuật
```
Khi biết câu trả lời:
"[Direct answer]. For example, in my project, I used this when [context]."

Khi không chắc:
"I'm not 100% sure about the exact details, but my understanding is [answer].
 I would verify this by [how you'd find out]."

Khi không biết:
"I haven't worked with [technology] directly, but I'm familiar with [related thing]
 and I'm confident I can pick it up quickly. I learn best by [how you learn]."
```

---

## 9. Từ viết tắt phổ biến

| Viết tắt | Đầy đủ | Nghĩa |
|----------|--------|-------|
| LGTM | Looks Good To Me | Trông ổn rồi |
| WIP | Work In Progress | Đang làm |
| FYI | For Your Information | Để bạn biết |
| ASAP | As Soon As Possible | Càng sớm càng tốt |
| ETA | Estimated Time of Arrival | Thời gian dự kiến xong |
| TBD | To Be Determined | Chưa quyết định |
| TBH | To Be Honest | Thành thật mà nói |
| IMO | In My Opinion | Theo ý kiến tôi |
| PR | Pull Request | Yêu cầu gộp code |
| CR | Code Review | Xem xét code |
| DoD | Definition of Done | Định nghĩa hoàn thành |
| AC | Acceptance Criteria | Tiêu chí chấp nhận |
| POC | Proof of Concept | Thử nghiệm ý tưởng |
| MVP | Minimum Viable Product | Sản phẩm tối thiểu khả dụng |

---

[Quay lại README](./README.md) | [Quay lại ROADMAP](../ROADMAP.md)
