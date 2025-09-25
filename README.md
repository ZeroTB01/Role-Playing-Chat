
# 1. 项目概述（最终目标）

开发一个 **AI 角色扮演 Web 应用 MVP**：

- 用户无需注册，直接进入页面选择角色（预置若干），与角色进行：
    
    - **文字对话**（上下文连续）
        
    - **语音对话**（用户语音 → ASR → LLM → TTS → 播放）
        
- 最小化：只做角色选择 + 文本/语音对话 + （可选）聊天记录保存/查看
    

---

# 2. 最终架构（简图）

```
[Vue Frontend (A)]
  - UI: 角色选择, 聊天窗, 录音/播放
  - 调用: /api/character/list, /api/chat/text, /api/chat/voice

       |
       v (REST JSON)
[Spring Boot API (A)]  <--->  MySQL (A)
  - 角色数据 (character)
  - 聊天记录 (chat_record) (可选存储)
  - 上下文拼接：取最近 N 条记录 -> 发给 FastAPI
  - 转发到 FastAPI (text/audio)

       |
       v (REST JSON)
[FastAPI AI Service (B)]
  - /chat (文本对话) -> 调用 LLM（OpenAI GPT）
  - /process_audio (语音对话) -> Whisper (ASR) -> LLM -> TTS
  - 返回: reply/text/audioBase64
```

---

# 3. 技术栈（最终确认）

- 前端：Vue 3 + Element Plus + Axios + Web Audio API (MediaRecorder)
    
- 主后端：Java Spring Boot 3.x + MyBatis-Plus + MySQL 8.0
    
- AI 服务：Python 3.x + FastAPI + Uvicorn + OpenAI API (Whisper/GPT/TTS) 或等价云服务
    
- 容器化/部署：Docker, Nginx (前端静态反代), 阿里云 ECS（已有）
    
- 可选：Redis（临时会话缓存）、OSS/S3（长期音频对象存储）
    

---

# 4. 功能/开发内容清单（按优先级）

## 高优先级（MVP 必做）

1. 角色选择（预置若干角色） — Spring Boot + 前端
    
2. 文字聊天（上下文） — Spring Boot + FastAPI + LLM
    
3. 语音聊天（ASR + LLM + TTS） — FastAPI (核心) + 前端 (录音/播放)
    
4. 上下文管理（Spring Boot 从 DB 取最近 N 条拼接）
    

## 中优先级（如果时间允许）

1. 保存聊天记录到 MySQL 并在前端展示历史
    
2. 不同角色的 Prompt 模板可配置化
    
3. 基本异常处理与重试策略（AI 接口超时）
    

## 低优先级（后续迭代）

1. 个性化记忆（长期偏好）
    
2. 多语音角色音色选择
    
3. 本地/自训练模型接入（需 Python 扩展）
    

---

# 5. API 联调文档（最终格式）

> 说明：前端只与 Spring Boot 交互；Spring Boot 与 FastAPI 按下列内部接口对接。

## 5.1 前端 ↔ Spring Boot（对前端统一格式）

所有响应对前端均采用：

```json
{
  "code": 200,    // 200 成功, 400/500 错误
  "msg": "success",
  "data": { ... }
}
```

### GET /api/character/list

- 功能：获取预置角色列表
    
- 请求：无
    
- 响应 data：
    

```json
[
  {"id":1,"name":"哈利波特","description":"魔法学校学生"},
  {"id":2,"name":"苏格拉底","description":"古希腊哲学家"}
]
```

### POST /api/chat/text

- 功能：文本对话（前端发送用户新消息）
    
- 请求 body：
    

```json
{
  "characterId": 2,
  "message": "你怎么看待勇气？"
}
```

- Spring Boot 行为：
    
    1. 从 DB 查 `character.name`（如 "苏格拉底"）
        
    2. 从 `chat_record` 取最近 N 条（例如 N=6）构建上下文（见下文上下文格式）
        
    3. 调用 FastAPI `/chat`（传 `character`, `context`）
        
    4. 保存用户消息 + AI 回复到 `chat_record`（可选）
        
- Spring Boot 返回给前端：
    

```json
{
  "code": 200,
  "msg": "success",
  "data": {"reply": "勇气不是没有恐惧，而是能够在恐惧中行动。"}
}
```

### POST /api/chat/voice

- 功能：语音对话（前端上传 base64）
    
- 请求 body：
    

```json
{
  "characterId": 1,
  "audioBase64": "...."   // 用户录音 base64 (wav 或 mp3)
}
```

- Spring Boot 行为：
    
    1. 下行调用 FastAPI `/process_audio`：传 `character` 与 `audioBase64`（或 multipart file）
        
    2. FastAPI 处理 (ASR -> text, LLM -> reply, TTS -> audioBase64)
        
    3. 保存（可选）
        
- Spring Boot 返回：
    

```json
{
  "code":200, "msg":"success",
  "data": {
    "text":"（ASR 转写）...",
    "reply":"（AI 回复文本）...",
    "audioBase64":"(AI 语音 base64, e.g. wav/mp3)"
  }
}
```

## 5.2 Spring Boot ↔ FastAPI（内部契约）

### POST /chat (FastAPI)

- 请求：
    

```json
{
  "character": "苏格拉底",
  "context": [
     {"role":"user","content":"你好"},
     {"role":"ai","content":"你好，我是苏格拉底。"},
     {"role":"user","content":"你怎么看待勇气？"}
  ]
}
```

- 响应：
    

```json
{
  "reply": "勇气不是没有恐惧，而是能够在恐惧中行动。"
}
```

### POST /process_audio (FastAPI)

- 请求：
    

```json
{
  "character": "哈利波特",
  "audioBase64": "..."   // base64 用户音频（wav 优先）
}
```

- 响应：
    

```json
{
  "text": "ASR 转写文本",
  "reply": "角色风格的回复文本",
  "audioBase64": "..."   // AI 合成语音 base64 (wav/mp3)
}
```

> 备注：内部调用可以采用 multipart/form-data 上传文件，或 JSON 传 base64。开发期间建议先用 JSON base64 以简化实现。

---

# 6. 数据库设计（核心表）

## 表：character（角色表）

```sql
CREATE TABLE character (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  prompt_template TEXT, -- 可选：角色Prompt模板
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 表：chat_record（聊天记录，可选）

```sql
CREATE TABLE chat_record (
  id INT AUTO_INCREMENT PRIMARY KEY,
  character_id INT NOT NULL,
  role ENUM('user','ai') NOT NULL,
  content TEXT NOT NULL,
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_character_time (character_id, create_time)
);
```

> 注：`chat_record` 用于上下文拼接。MVP 可选做：初期可只保留在内存（Redis）或暂不存储。

---

# 7. 上下文管理（推荐实现细节）

**策略（Spring Boot 负责）**：

- 每次用户发起请求（text/voice），Spring Boot：
    
    1. 查询 chat_record，取最近 M 条消息（M = 6~10），按时间排序（老 -> 新）；
        
    2. 将这些消息转成 FastAPI 需要的 `context` 格式（role: user/ai, content）；
        
    3. 把 user 的最新 message 追加到 context，然后调用 FastAPI；
        
    4. 将 user message 和 AI reply 分别写入 chat_record（可选）。
        
- **裁剪**：如果聊天长度超限，优先保留最近 M 条或合并较早消息为摘要（后期优化）。
    
- **示例拼接（Java 伪码）**：
    

```java
List<ChatRecord> recs = chatRecordRepo.findLatestByCharacter(characterId, M);
List<Map<String,String>> context = recs.stream()
    .map(r -> Map.of("role", r.getRole(), "content", r.getContent()))
    .collect(Collectors.toList());
context.add(Map.of("role","user","content", userMessage));
callFastApi(characterName, context);
```

---

# 8. FastAPI 服务实现要点（B 参考模板）

- Endpoints：`/chat`, `/process_audio`
    
- `/chat`：接收 `character` + `context`，将 context 转为 LLM 消息数组（system + user + assistant），执行 LLM 调用（OpenAI Chat API），返回 `reply`。
    
- `/process_audio`：
    
    1. 把 base64 解码为音频文件（wav/mp3）
        
    2. 调用 Whisper API（或云 ASR）得到 `text`
        
    3. 根据 `character` + `context` 给 LLM 生成 `reply`
        
    4. 调用 TTS API 把 `reply` 转为音频 base64 返回
        
- 注意：为降低延迟，建议使用流式或并行调用（例如 ASR 完成后并发请求 LLM/TTS），但 MVP 可顺序实现。
    

---

# 9. 音频格式与前端处理

- 推荐音频格式：**wav (16-bit PCM)** 或 **mp3**。wav 在本地录音兼容性最好，TTS 多支持 wav/mp3 输出。
    
- 前端录音：MediaRecorder 录成 webm/ogg 或 wav，后端需确保转为 FastAPI / OpenAI 可识别格式。为减少复杂性，前端直接上传 base64 的 wav（或上传 blob 给 Spring Boot，Spring Boot 可转存并转 base64）。
    
- 前端播放 AI 音频示例：
    

```js
const audio = new Audio("data:audio/wav;base64," + data.audioBase64);
audio.play();
```


# 10. 分工

**A（负责 Spring Boot + Vue + MySQL）**

- 初始化项目、数据库、角色管理
    
- 实现前端 UI（聊天框、录音、播放）
    
- 实现 `/api/chat/text`, `/api/chat/voice`，上下文拼接与保存
    
- 测试与部署 Spring Boot、前端
    

**B（负责 FastAPI + AI 集成）**

- 初始化 FastAPI，实现 `/chat` 和 `/process_audio`
    
- 调用 OpenAI (Whisper, ChatCompletion/Chat API, TTS)
    
- 设计并维护角色 prompt 模板
    
- 测试 AI 输出、优化 Prompt、返回音频 base64
    
- 部署 FastAPI 容器
    

两人工作量目标平衡：A 负责 UI + 核心业务，B 负责 AI 能力与 Prompt，联调靠接口契约。
