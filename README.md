# Simple Spring Boot Agent

A minimal, production-ready AI agent built with **Spring Boot**, **Spring AI**, and **Spring AI AgentCore**. This project demonstrates how to build a tool-augmented AI agent — backed by **AWS Bedrock Converse** — with zero custom controller boilerplate, now extended with **Short-Term Conversational Memory** via AgentCore Memory.

---

## Overview

This project contains two agent capabilities:

1. **`SampleChatAgent`** — A zero-boilerplate AgentCore-compatible agent. No custom controllers, no protocol handling, no health check implementation. The AgentCore runtime exposes and manages everything automatically.
2. **`ShortTermMemoryController`** — A conversational memory agent that maintains chat history within a session using `AgentCoreMemory` and Spring AI's `ChatMemory`, with REST endpoints to query and clear history.

---

## Tech Stack

| Technology           | Version | Purpose                                  |
|----------------------|---------|------------------------------------------|
| Java                 | 21      | Language                                 |
| Spring Boot          | 3.5.13  | Application framework                    |
| Spring AI            | 1.1.4   | LLM abstraction & tool calling           |
| Spring AI AgentCore  | 1.0.0   | Agent runtime, routing, memory & protocol|
| AWS Bedrock Converse | —       | LLM provider                             |
| SpringDoc OpenAPI    | 2.8.16  | API documentation (Swagger UI)           |
| Spring Boot Actuator | —       | Health & metrics                         |

---

## Project Structure

```
simple-spring-boot-agent/
├── src/
│   └── main/
│       ├── java/com/bsmlabs/springai/
│       │   ├── agents/
│       │   │   ├── SampleChatAgent.java              # AgentCore agent — zero boilerplate
│       │   │   └── ShortTermMemoryController.java    # Conversational memory agent
│       │   ├── models/
│       │   │   ├── PromptRequest.java                # Input model for SampleChatAgent
│       │   │   ├── ChatRequest.java                  # Input model for memory agent
│       │   │   └── ChatResponse.java                 # Response model for memory agent
│       │   └── tools/
│       │       └── MathematicalTools.java            # Tool definitions exposed to the LLM
│       └── resources/
│           └── application.properties               # AWS Bedrock configuration
├── test-sample-request.http                         # Ready-to-run HTTP test requests
├── pom.xml
├── mvnw / mvnw.cmd
└── .gitignore
```

---

## How It Works

### SampleChatAgent — Zero Boilerplate Agent

```
User Request  ──►  POST /invocations  (auto-exposed by AgentCore)
                        │
                        ▼
              @AgentCoreInvocation
              agentCoreHandler(...)
                        │
                        ├──► Logs Session ID from AgentCoreContext
                        │
                        ▼
              chatClient.prompt()
                        │
                        ├──► LLM reasons about the prompt
                        │         │
                        │         └── Needs math? ──► MathematicalTools ──► result fed back
                        │
                        ▼
              Final response returned as String
```

The **AgentCore runtime** automatically exposes:

| Endpoint            | Description                         |
|---------------------|-------------------------------------|
| `POST /invocations` | Main agent entry point (JSON & SSE) |
| `GET /ping`         | Health / liveness probe             |

No `@RestController` required.

---

### ShortTermMemoryController — Conversational Memory Agent

```
User Request  ──►  POST /api/short
                        │
                        ▼
              chatClient.prompt()
                        │
                        ├──► .advisors(agentCoreMemory.advisors)     # inject memory advisors
                        ├──► .advisors(ChatMemory.CONVERSATION_ID)   # bind to session
                        │
                        ▼
              LLM receives full conversation history
                        │
                        ▼
              Response + history stored in ChatMemory
```

Memory management REST endpoints:

| Endpoint             | Method   | Description                            |
|----------------------|----------|----------------------------------------|
| `POST /api/short`    | POST     | Chat with short-term memory            |
| `GET /api/history`   | GET      | Retrieve full conversation history     |
| `DELETE /api/history`| DELETE   | Clear conversation history             |

---

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **AWS account** with Bedrock access enabled
- AWS credentials configured via `~/.aws/credentials` or environment variables

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/bsmahi/simple-spring-boot-agent.git
cd simple-spring-boot-agent
```

### 2. Configure AWS Credentials

```bash
# Option A: Environment variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1

# Option B: AWS CLI
aws configure
```

### 3. Configure the Application

Edit `src/main/resources/application.properties`:

```properties
spring.ai.bedrock.converse.chat.options.model=anthropic.claude-3-5-sonnet-20241022-v2:0
spring.ai.bedrock.aws.region=us-east-1
```

### 4. Enable AgentCore Memory Dependency

Uncomment the memory dependency in `pom.xml`:

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-agentcore-memory</artifactId>
</dependency>
```

### 5. Build and Run

```bash
./mvnw spring-boot:run
```

The application starts at `http://localhost:8080`.

---

## API Usage

All sample requests are available in [`test-sample-request.http`](./test-sample-request.http) and can be run directly from **IntelliJ IDEA** or **VS Code REST Client**.

### SampleChatAgent Endpoints

**Invoke the Agent (JSON)**

```http
POST http://localhost:8080/invocations
Content-Type: application/json

{ "prompt": "What is Spring AI?" }
```

**Invoke the Agent (SSE Streaming)**

```http
POST http://localhost:8080/invocations
Content-Type: application/json
Accept: text/event-stream

{ "prompt": "Tell me about Spring AI in detail." }
```

**Health / Ping**

```http
GET http://localhost:8080/ping
```

---

### Short-Term Memory Endpoints

**Chat with Memory**

```http
POST http://localhost:8080/api/short
Content-Type: application/json

{ "message": "My name is Mahendra. What is Spring AI?" }
```

```http
POST http://localhost:8080/api/short
Content-Type: application/json

{ "message": "What was my name again?" }
```

> The agent remembers the conversation — the second request will correctly recall `Mahendra` from the session history.

**Retrieve Conversation History**

```http
GET http://localhost:8080/api/history
```

**Clear Conversation History**

```http
DELETE http://localhost:8080/api/history
```

---

## API Documentation

Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI Spec (JSON):

```
http://localhost:8080/v3/api-docs
```

---

## Key Concepts

### `@AgentCoreInvocation`
Marks a method as the AgentCore agent entry point. The runtime handles HTTP routing, protocol compliance, context injection, and endpoint exposure — no `@RestController` needed.

### `AgentCoreContext`
Provides access to agent runtime metadata such as session IDs, trace headers, and routing info, used for logging and distributed tracing.

### `AgentCoreMemory`
The AgentCore memory abstraction that provides pre-configured **chat advisors**. Injected into `ChatClient.prompt()` via `.advisors(agentCoreMemory.advisors)` to automatically load and save conversation history for the given `CONVERSATION_ID`.

### `AgentCoreShortTermMemoryRepository`
The repository backing short-term (in-session) memory storage. Can be used to explicitly delete conversation history by ID (e.g., `deleteByConversationId(...)`).

### `ChatMemory`
Spring AI's conversation memory store. Used directly to retrieve (`get`) and clear (`clear`) message history by conversation ID, exposed via the `/api/history` endpoints.

### `MathematicalTools`
A Spring AI tool class registered with `ChatClient`. When the LLM needs to compute something, Spring AI invokes the matching tool, feeds the result back, and returns the final answer automatically.

---

## Other AgentCore Modules

The `pom.xml` includes commented-out dependencies for additional capabilities:

```xml
<!-- Conversational memory across sessions — used by ShortTermMemoryController -->
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-agentcore-memory</artifactId>
</dependency>

<!-- Browser / web interaction capability -->
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-agentcore-browser</artifactId>
</dependency>

<!-- Code execution capability -->
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-agentcore-code-interpreter</artifactId>
</dependency>
```

---

## Running Tests

```bash
./mvnw test
```

---

## Author

**Mahendra Rao B** — [bs.mahi@gmail.com](mailto:bs.mahi@gmail.com)

---

## License

This project is open source. See the [repository](https://github.com/bsmahi/simple-spring-boot-agent) for license details.
