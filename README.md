# Simple Spring Boot Agent

A minimal, production-ready AI agent built with **Spring Boot**, **Spring AI SDK**, and **Spring AI AgentCore**. This project demonstrates how to build a tool-augmented AI agent — backed by AWS Bedrock — with zero custom controller boilerplate.

---

## Overview

This project shows the simplest possible AgentCore-compatible Spring AI agent. There are no custom REST controllers, no manual protocol handling, and no hand-rolled health endpoints. The AgentCore runtime handles all of that automatically, leaving you to focus purely on agent logic.

The agent is backed by **AWS Bedrock Converse** and ships with a `MathematicalTools` implementation to demonstrate how Spring AI's function-calling mechanism works in practice.

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 3.5.13 | Application framework |
| Spring AI | 1.1.4 | LLM abstraction & tool calling |
| Spring AI AgentCore | 1.0.0 | Agent runtime, routing & protocol |
| AWS Bedrock Converse | — | LLM provider |
| SpringDoc OpenAPI | 2.8.16 | API documentation (Swagger UI) |
| Spring Boot Actuator | — | Health & metrics |

---

## Project Structure

```
simple-spring-boot-agent/
├── src/
│   └── main/
│       ├── java/com/bsmlabs/springai/
│       │   ├── agents/
│       │   │   └── SampleChatAgent.java       # Core agent — @AgentCoreInvocation handler
│       │   ├── models/
│       │   │   └── PromptRequest.java          # Input model (Java record)
│       │   └── tools/
│       │       └── MathematicalTools.java      # Tool definitions for the LLM
│       └── resources/
│           └── application.properties          # AWS Bedrock config
├── test-sample-request.http                    # Ready-to-run HTTP test requests
└── pom.xml
```

---

## How It Works

```
User Request  ──►  POST /invocations
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

The **AgentCore runtime** auto-exposes:
- `POST /invocations` — main agent entry point (JSON & SSE streaming)
- `GET /ping` — health/liveness probe

No `@RestController` required.

---

## Prerequisites

- **Java 21+**
- **Maven 3.8+**
- **AWS account** with Bedrock access enabled
- AWS credentials configured locally (`~/.aws/credentials` or environment variables)

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/bsmahi/simple-spring-boot-agent.git
cd simple-spring-boot-agent
```

### 2. Configure AWS Credentials

Ensure your AWS credentials are available via one of the standard methods:

```bash
# Option A: Environment variables
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_REGION=us-east-1

# Option B: AWS CLI
aws configure
```

### 3. Configure the Application

Edit `src/main/resources/application.properties` to set your preferred Bedrock model:

```properties
spring.ai.bedrock.converse.chat.options.model=anthropic.claude-3-5-sonnet-20241022-v2:0
spring.ai.bedrock.aws.region=us-east-1
```

### 4. Build and Run

```bash
./mvnw spring-boot:run
```

The application starts on `http://localhost:8080`.

---

## API Usage

### Standard Invocation (JSON)

```http
POST http://localhost:8080/invocations
Content-Type: application/json

{ "prompt": "What is Spring AI?" }
```

### SSE Streaming Invocation

```http
POST http://localhost:8080/invocations
Content-Type: application/json
Accept: text/event-stream

{ "prompt": "Tell me about Spring AI in detail." }
```

### Health / Ping

```http
GET http://localhost:8080/ping
```

> All sample requests are available in [`test-sample-request.http`](./test-sample-request.http) and can be run directly from IntelliJ IDEA or VS Code REST Client.

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI spec (JSON):

```
http://localhost:8080/v3/api-docs
```

---

## Key Concepts

### `@AgentCoreInvocation`
Marks a method as the agent's entry point. The AgentCore runtime intercepts this method to inject context, handle HTTP routing, manage protocol compliance, and expose endpoints — no `@RestController` needed.

### `AgentCoreContext`
Provides access to agent runtime metadata such as session IDs, trace headers, and routing information. Used for logging, multi-turn tracking, and distributed tracing.

### `MathematicalTools`
A Spring AI tool class whose methods are registered with the `ChatClient`. When the LLM determines it needs to compute something (e.g., `√144`), Spring AI invokes the matching tool method, feeds the result back to the model, and returns the final answer automatically.

---

## Optional AgentCore Modules

The `pom.xml` includes commented-out dependencies for additional AgentCore capabilities you can enable as needed:

```xml
<!-- Conversational memory across sessions -->
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-agentcore-memory</artifactId>
</dependency>

<!-- Browser/web interaction capability -->
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
