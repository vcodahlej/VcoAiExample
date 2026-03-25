# Spring AI Chat App

Multi-module Maven project with:

- `backend`: Spring Boot WebSocket backend using Spring AI `1.0.0`
- `frontend`: React + TypeScript chat client managed as a Maven module

## Versions

- Spring Boot `3.4.4`
- Spring AI `1.0.0`
- Java `21`

## Run

Backend:

```bash
cd backend
mvn spring-boot:run
```

Chuck MCP server:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home
mvn -pl chuckMcp -DskipTests compile dependency:copy-dependencies -DincludeScope=runtime
```

Frontend:

```bash
cd frontend
npm install
npm run dev
```

## Environment

The backend is configured for the Spring AI OpenAI starter. Set:

```bash
export SPRING_AI_OPENAI_API_KEY=your_api_key
export BRAVE_API_KEY=your_brave_search_api_key
```

Optional overrides:

```bash
export SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=gpt-4o-mini
export BRAVE_MCP_NPX_COMMAND=/opt/homebrew/bin/npx
export CHUCK_MCP_COMMAND=/bin/zsh
export CHUCK_MCP_JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home
export APP_ALLOWED_ORIGIN=http://localhost:5173
```

## WebSocket flow

- Frontend connects to `ws://localhost:8080/wss/api/v1/chat`
- Frontend sends JSON: `{"type":"user","content":"Hello"}`
- Backend returns assistant/system/error messages as JSON
