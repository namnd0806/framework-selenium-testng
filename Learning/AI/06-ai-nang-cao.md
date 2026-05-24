# Phần 6 — AI Nâng cao

> Phần này dành cho người đã dùng AI thành thạo và muốn hiểu sâu hơn về cách AI hoạt động.

---

## 6.1 Agentic AI — Đi sâu hơn

### Vòng lặp ReAct chi tiết

Agent hoạt động theo pattern **ReAct = Reasoning + Acting**:

```
[Nhận nhiệm vụ]
        ↓
Thought: Phân tích nhiệm vụ, lên kế hoạch
        ↓
Action: Thực hiện 1 bước (đọc file, chạy lệnh, tìm kiếm...)
        ↓
Observation: Xem kết quả của action
        ↓
Thought: Đánh giá kết quả, quyết định bước tiếp theo
        ↓
[Lặp lại cho đến khi xong hoặc gặp lỗi]
        ↓
Answer: Báo cáo kết quả
```

Ví dụ thực tế — Agent fix flaky test:
```
Thought: Cần hiểu test đang làm gì trước
Action: read_file("tests/CheckoutTest.java")
Observe: [code test]

Thought: Test dùng Thread.sleep(3000) — đây là nguyên nhân flaky
Action: read_file("pages/CheckoutPage.java")
Observe: [code page]

Thought: Cần thay Thread.sleep bằng WebDriverWait
Action: write_file("pages/CheckoutPage.java", [code đã fix])
Observe: File updated

Thought: Chạy test để verify fix
Action: run_command("mvn test -Dtest=CheckoutTest")
Observe: Tests passed (3/3)

Answer: Đã fix flaky test bằng cách thay Thread.sleep(3000)
        bằng WebDriverWait với ExpectedConditions.
```

### Tool Use / Function Calling

Để agent làm được việc, AI cần được trao **công cụ**. Mỗi tool là 1 function AI có thể gọi:

```json
// Ví dụ tool definition (OpenAI format)
{
  "name": "run_test",
  "description": "Chạy test và trả về kết quả",
  "parameters": {
    "test_class": "Tên class test cần chạy",
    "profile": "maven profile (optional)"
  }
}
```

AI quyết định khi nào gọi tool nào dựa vào nhiệm vụ.

### Khi nào dùng Agent, khi nào dùng Chat?

| Tình huống | Dùng gì | Lý do |
|---|---|---|
| Hỏi nhanh, câu trả lời đơn giản | Chat | Nhanh hơn, không cần overhead |
| Sửa 1 file cụ thể | Chat + copy | Đơn giản, kiểm soát được |
| Refactor nhiều file | Agent | AI tự đọc, sửa, verify |
| Thêm feature mới | Agent | Nhiều bước, nhiều file |
| Debug phức tạp | Agent | AI tự chạy test, xem log |
| Tìm hiểu codebase lạ | Agent | AI tự explore, tóm tắt |

---

## 6.2 Multi-Agent — Nhiều AI phối hợp

### Tại sao cần Multi-Agent?

1 agent làm tất cả có vấn đề:
- Context window bị giới hạn khi task quá lớn
- Không thể chuyên sâu nhiều lĩnh vực cùng lúc
- Không có cơ chế review chéo

Multi-agent giải quyết bằng cách **chia nhỏ và chuyên biệt hóa**:

```
Orchestrator (điều phối)
├── Analyst Agent    → đọc requirement, phân tích
├── Coder Agent      → viết code
├── Tester Agent     → viết và chạy test
├── Reviewer Agent   → review code quality
└── Reporter Agent   → tổng hợp kết quả
```

### LangChain — Framework phổ biến nhất

LangChain là framework Python/JS để build AI application:

```python
from langchain_anthropic import ChatAnthropic
from langchain.agents import create_react_agent, AgentExecutor
from langchain.tools import tool

# Định nghĩa tool
@tool
def run_selenium_test(test_class: str) -> str:
    """Chạy Selenium test class và trả về kết quả"""
    result = subprocess.run(
        ["mvn", "test", f"-Dtest={test_class}"],
        capture_output=True, text=True
    )
    return result.stdout

# Tạo agent
llm = ChatAnthropic(model="claude-3-5-sonnet-20241022")
agent = create_react_agent(llm, tools=[run_selenium_test, read_file, write_file])
executor = AgentExecutor(agent=agent, tools=[...])

# Chạy
result = executor.invoke({"input": "Fix tất cả failing test trong project"})
```

### CrewAI — Dễ setup hơn cho Multi-Agent

```python
from crewai import Agent, Task, Crew

# Định nghĩa agents
qa_analyst = Agent(
    role="QA Analyst",
    goal="Phân tích requirement và viết test case",
    backstory="Senior QA với 10 năm kinh nghiệm testing",
    llm=claude
)

automation_engineer = Agent(
    role="Automation Engineer",
    goal="Viết automation test từ test case",
    backstory="SDET chuyên Selenium Java",
    llm=claude
)

# Định nghĩa tasks
analyze_task = Task(
    description="Phân tích user story và viết test case",
    agent=qa_analyst
)

automate_task = Task(
    description="Viết Selenium test từ test case đã có",
    agent=automation_engineer
)

# Chạy crew
crew = Crew(agents=[qa_analyst, automation_engineer],
            tasks=[analyze_task, automate_task])
result = crew.kickoff()
```

### AutoGen (Microsoft) — Agents tự trò chuyện

```python
import autogen

# Agents tự chat với nhau để giải quyết vấn đề
tester = autogen.AssistantAgent(
    name="Tester",
    system_message="Bạn là QA engineer, chuyên tìm bug và viết test"
)

developer = autogen.AssistantAgent(
    name="Developer",
    system_message="Bạn là developer, fix bug mà Tester tìm ra"
)

user_proxy = autogen.UserProxyAgent(name="User")

# Bắt đầu conversation
user_proxy.initiate_chat(
    tester,
    message="Review code này và tìm potential bugs: [paste code]"
)
# Tester tìm bug → Developer fix → Tester verify → lặp lại
```

---

## 6.3 Chạy AI Local với Ollama

### Tại sao chạy local?

| Lý do | Chi tiết |
|---|---|
| **Bảo mật** | Code không rời khỏi máy bạn |
| **Chi phí** | Miễn phí sau khi cài |
| **Offline** | Không cần internet |
| **Không giới hạn** | Không bị rate limit |
| **Privacy** | Dữ liệu nhạy cảm không gửi lên cloud |

### Cài đặt Ollama

```bash
# Windows: tải installer từ ollama.ai
# Mac:
brew install ollama

# Chạy model
ollama run llama3.1          # model đa năng, 8B params
ollama run codellama         # chuyên cho code
ollama run deepseek-coder    # mạnh về coding
ollama run qwen2.5-coder     # tốt cho code, hỗ trợ tiếng Việt
ollama run mistral           # nhẹ, nhanh

# Xem danh sách model đã cài
ollama list

# Xóa model
ollama rm codellama
```

### Yêu cầu phần cứng

| Model | RAM cần | Tốc độ (CPU) |
|---|---|---|
| 3B params | 4GB | Nhanh |
| 7B params | 8GB | Vừa |
| 13B params | 16GB | Chậm hơn |
| 70B params | 48GB+ | Rất chậm nếu không có GPU |

> Máy 16GB RAM: chạy tốt model 7B-13B. Đủ dùng cho coding hàng ngày.

### Tích hợp Ollama với IDE

**Dùng với Continue extension (VS Code):**
```json
// .continue/config.json
{
  "models": [{
    "title": "CodeLlama Local",
    "provider": "ollama",
    "model": "codellama",
    "apiBase": "http://localhost:11434"
  }]
}
```

**Dùng với Aider:**
```bash
aider --model ollama/codellama LoginTest.java
```

**Dùng qua API (tích hợp vào script):**
```python
import requests

response = requests.post("http://localhost:11434/api/generate", json={
    "model": "codellama",
    "prompt": "Viết hàm validate email trong Java",
    "stream": False
})
print(response.json()["response"])
```

### Các model local tốt cho dev/tester

| Model | Size | Mạnh về | Phù hợp |
|---|---|---|---|
| CodeLlama 7B | 4GB | Code completion | Inline suggestion |
| DeepSeek Coder 6.7B | 4GB | Viết code, debug | Coding hàng ngày |
| Llama 3.1 8B | 5GB | Đa năng | Chat + code |
| Qwen2.5 Coder 7B | 5GB | Code + tiếng Việt | Nếu hay dùng tiếng Việt |
| Mistral 7B | 4GB | Nhanh, đa năng | Khi cần tốc độ |

### LM Studio — Giao diện đồ họa

Nếu không quen terminal, LM Studio có UI đẹp:
- Tải model từ HuggingFace qua giao diện
- Chat như ChatGPT nhưng chạy local
- Expose API local để tích hợp với tool khác
- Download tại: lmstudio.ai

---

## Tóm lại Phần 6

- Agent = AI tự lên kế hoạch, thực thi nhiều bước theo vòng lặp ReAct
- Multi-agent = nhiều AI chuyên biệt phối hợp, phù hợp task phức tạp
- LangChain, CrewAI, AutoGen là framework phổ biến để build multi-agent
- Ollama cho phép chạy AI local: miễn phí, bảo mật, không cần internet
- Model 7B chạy được trên máy 8GB RAM — đủ dùng cho coding hàng ngày

---

**Tiếp theo:** [Phần 7 — Xu hướng & Tương lai](./07-xu-huong-tuong-lai.md)
