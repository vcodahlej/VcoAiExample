# Spring AI Chat App

Multi-module Maven project with:

- `backend`: Spring Boot WebSocket backend using Spring AI `1.0.0`
- `chuckMcp`: Spring Boot MCP server over STDIO that exposes a Chuck Norris fact tool
- `frontend`: React + TypeScript chat client managed as a Maven module

## Versions

- Spring Boot `3.4.4`
- Spring AI `1.0.0`
- Java `21`

## Spring AI documentation

- This project is pinned to Spring AI `1.0.0`
- Use the Spring AI `1.0` reference docs for examples and configuration details: https://docs.spring.io/spring-ai/reference/1.0/

## Run

Backend:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home
mvn -pl chuckMcp -DskipTests compile dependency:copy-dependencies -DincludeScope=runtime
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

## Getting API keys

OpenAI API key:

1. Create or sign in to your OpenAI Platform account.
2. Open the API key page: https://platform.openai.com/api-keys
3. Create a new secret key and export it as `SPRING_AI_OPENAI_API_KEY`.

Brave Search API key:

1. Create or sign in to your Brave Search API account.
2. Start from the Brave Search API page: https://brave.com/search/api/
3. Create an API key from the Brave dashboard and export it as `BRAVE_API_KEY`.

Shell example:

```bash
export SPRING_AI_OPENAI_API_KEY=your_openai_api_key
export BRAVE_API_KEY=your_brave_search_api_key
```

Notes:

- Keep both keys out of source control
- Do not put API keys in the frontend
- For OpenAI key safety guidance, see: https://help.openai.com/en/articles/4936850-where-do-i-find-my-openai-api-key and https://help.openai.com/articles/5112595-best-practices-for-api-key-safety

Optional overrides:

```bash
export SPRING_AI_OPENAI_CHAT_OPTIONS_MODEL=gpt-4o-mini
export BRAVE_MCP_NPX_COMMAND=/opt/homebrew/bin/npx
export CHUCK_MCP_COMMAND=/bin/zsh
export CHUCK_MCP_JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home
export APP_ALLOWED_ORIGIN=http://localhost:5173
```

## MCP integration

- The backend is configured with two STDIO MCP client connections: `brave-search` and `chuck-norris`
- `chuckMcp` is launched by the backend from `application.yml` using `java -cp ... com.vco.chuckmcp.ChuckMcpApplication`
- Before starting the backend, `chuckMcp` must be compiled and its runtime jars copied into `chuckMcp/target/dependency`
- The `chuckMcp` server exposes one tool: `randomChuckNorrisFact`
- The tool uses `javafaker` to generate a random Chuck Norris fact for tool calls made through the backend chat flow

## Troubleshooting

- If the backend still uses old MCP settings after editing `backend/src/main/resources/application.yml`, IntelliJ may be launching with the stale copied file in `backend/target/classes/application.yml`
- If that happens, rebuild the backend module or copy the updated resource into `backend/target/classes`
- If `chuckMcp` fails with SnakeYAML classpath errors, stale jars may still be present in `chuckMcp/target/dependency`
- Regenerate the runtime dependency folder with:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-21.jdk/Contents/Home
mvn -pl chuckMcp -DskipTests compile dependency:copy-dependencies -DincludeScope=runtime
```

- If needed, remove stale jars from `chuckMcp/target/dependency` before regenerating dependencies

## WebSocket flow

- Frontend connects to `ws://localhost:8080/wss/api/v1/chat`
- Frontend sends JSON: `{"type":"user","content":"Hello"}`
- Backend returns assistant/system/error messages as JSON
