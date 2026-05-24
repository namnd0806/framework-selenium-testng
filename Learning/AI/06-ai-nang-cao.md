# Phần 6 — AI Nâng cao

> Phần này dành cho người đã dùng AI hàng ngày (Phần 1-5) và muốn hiểu sâu hơn.
> Nếu bạn mới bắt đầu, hãy đọc Phần 1-5 trước — phần này không cần thiết ngay.

---

## Phần này nói về gì?

Khi dùng Kiro, Cursor, Claude Code hàng ngày — bạn đang dùng **AI có tích hợp sẵn Agent**.
Phần 6 giải thích **bên trong nó hoạt động thế nào**, và cách tự build nếu cần.

Liên hệ với những gì bạn đã biết:
```
Bạn đã dùng:                    Phần 6 giải thích bên trong:
─────────────────────────────────────────────────────────
Kiro Autopilot mode          →  Agent + ReAct loop
Kiro đọc nhiều file cùng lúc →  Tool use / Function calling
Cursor @codebase             →  RAG + Embedding
Claude Code CLI              →  Agent chạy qua terminal
```

---

## 6.1 Agent — Bên trong hoạt động thế nào

### Bạn đã biết Agent là gì (từ Phần 2)

Agent = AI tự lên kế hoạch và thực thi nhiều bước.
Kiro Autopilot mode chính là Agent.

### Vòng lặp ReAct — Cơ chế bên trong

Khi bạn giao việc cho Kiro/Cursor, bên trong nó chạy vòng lặp này:

```
[Bạn giao việc: "Fix tất cả flaky test"]
        ↓
Thought: Cần tìm hiểu test nào đang fail trước
        ↓
Action:  Đọc file test report
        ↓
Observe: Thấy 5 test fail vì StaleElementException
        ↓
Thought: Nguyên nhân là Thread.sleep, cần thay bằng WebDriverWait
        ↓
Action:  Sửa file LoginTest.java
        ↓
Observe: File đã sửa
        ↓
Thought: Chạy test để verify
        ↓
Action:  Chạy mvn test
        ↓
Observe: 5 test pass
        ↓
Answer:  Đã fix 5 flaky test, thay Thread.sleep bằng WebDriverWait
```

Đây là lý do Kiro đôi khi mất vài phút — nó đang chạy nhiều vòng lặp như trên.

### Tool Use — AI dùng công cụ

Để Agent làm được việc, AI được trao các "công cụ" (tool):

| Tool | AI làm được gì |
|---|---|
| read_file | Đọc file trong project |
| write_file | Tạo/sửa file |
| run_command | Chạy lệnh terminal (mvn test, npm run...) |
| web_search | Tìm kiếm internet |
| browse_web | Mở trình duyệt, tương tác trang web |

Kiro, Cursor đã tích hợp sẵn các tool này — bạn không cần setup gì thêm.

---

## 6.2 Multi-Agent — Nhiều AI phối hợp

### Đây là gì?

Thay vì 1 AI làm tất cả, **nhiều AI chuyên biệt phối hợp** như một team:

```
Orchestrator (điều phối)
├── Analyst Agent    → đọc requirement, phân tích
├── Coder Agent      → viết code
├── Tester Agent     → viết và chạy test
└── Reviewer Agent   → review code quality
```

### Khi nào cần Multi-Agent?

Hiện tại (2025-2026), Multi-Agent chủ yếu dùng trong:
- Công ty lớn build AI pipeline tự động
- Research, thử nghiệm
- Tự động hóa quy trình phức tạp

**Với tester/SDET hàng ngày:** chưa cần thiết. Kiro/Cursor đã đủ dùng.
Biết khái niệm này để hiểu xu hướng, không cần học sâu ngay.

### Các Framework (nếu muốn tìm hiểu thêm)

**LangChain** — phổ biến nhất, dùng Python:
```python
# Ví dụ đơn giản: agent tự chạy test và báo cáo
from langchain_anthropic import ChatAnthropic
from langchain.agents import create_react_agent

agent = create_react_agent(
    llm=ChatAnthropic(model="claude-3-5-sonnet"),
    tools=[run_test_tool, read_file_tool]
)
agent.run("Chạy regression test và tóm tắt kết quả")
```

**CrewAI** — dễ setup hơn cho multi-agent:
```python
from crewai import Agent, Task, Crew

qa = Agent(role="QA Engineer", goal="Viết test case")
dev = Agent(role="Developer", goal="Fix bug")
crew = Crew(agents=[qa, dev], tasks=[write_tests, fix_bugs])
crew.kickoff()
```

**AutoGen (Microsoft)** — agents tự trò chuyện với nhau để giải quyết vấn đề.

---

## 6.3 Chạy AI Local với Ollama

### Tại sao chạy local?

| Lý do | Chi tiết |
|---|---|
| **Bảo mật** | Code không rời khỏi máy bạn |
| **Chi phí** | Miễn phí sau khi cài |
| **Offline** | Không cần internet |
| **Không giới hạn** | Không bị rate limit |

### Cài đặt và dùng Ollama

```bash
# Tải Ollama tại: ollama.ai
# Sau khi cài, mở terminal:

ollama run llama3.1          # model đa năng
ollama run codellama         # chuyên cho code
ollama run deepseek-coder    # mạnh về coding
ollama run qwen2.5-coder     # tốt cho code, hỗ trợ tiếng Việt

# Chat ngay trong terminal:
>>> Viết hàm validate email trong Java
```

### Yêu cầu phần cứng

| Model size | RAM cần | Tốc độ |
|---|---|---|
| 7B params | 8GB RAM | Vừa phải |
| 13B params | 16GB RAM | Chậm hơn |
| 70B params | 48GB+ | Cần GPU mạnh |

> Máy 16GB RAM: chạy tốt model 7B-13B — đủ dùng cho coding hàng ngày.

### Tích hợp với IDE

**Dùng với Continue extension (VS Code) — miễn phí:**
```json
// .continue/config.json
{
  "models": [{
    "title": "CodeLlama Local",
    "provider": "ollama",
    "model": "codellama"
  }]
}
```

**Dùng với Aider (CLI):**
```bash
aider --model ollama/codellama LoginTest.java
```

### LM Studio — Giao diện đồ họa

Nếu không quen terminal, LM Studio có UI đẹp như ChatGPT nhưng chạy local:
- Tải tại: lmstudio.ai
- Tải model, chat, expose API local

### Các model local tốt cho dev/tester

| Model | RAM cần | Mạnh về |
|---|---|---|
| CodeLlama 7B | 4GB | Code completion |
| DeepSeek Coder 6.7B | 4GB | Viết code, debug |
| Llama 3.1 8B | 5GB | Đa năng |
| Qwen2.5 Coder 7B | 5GB | Code + tiếng Việt |
| Mistral 7B | 4GB | Nhanh, đa năng |

---

## Tóm lại Phần 6

| Khái niệm | Liên hệ thực tế | Cần học ngay? |
|---|---|---|
| ReAct loop | Cơ chế bên trong Kiro Autopilot | Biết để hiểu, không cần làm |
| Tool use | Lý do Kiro đọc/sửa file được | Biết để hiểu |
| Multi-agent | Nhiều AI phối hợp, LangChain, CrewAI | Chưa cần, tìm hiểu sau |
| Ollama | Chạy AI local, miễn phí, bảo mật | Nên thử nếu có nhu cầu |

---

**Tiếp theo:** [Phần 7 — Xu hướng & Tương lai](./07-xu-huong-tuong-lai.md)
