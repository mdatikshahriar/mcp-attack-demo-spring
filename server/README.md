# MCP Calculator & Quiz Server

A Spring Boot-based Model Context Protocol (MCP) server that provides calculator/math tools, weather services, and quiz
management capabilities. This server demonstrates how to integrate MCP with Spring AI to create intelligent tools that
can be consumed by AI assistants.

## Features

### 🧮 Math Tools

- Basic arithmetic operations (add, subtract, multiply, divide)
- Advanced mathematical functions (power, square root, factorial)
- Trigonometric functions (sin, cos, tan)
- Logarithmic functions (natural log, base-10 log)
- Geometric calculations (circle area/circumference, rectangle area, triangle area)
- Utility functions (absolute value, modulo, rounding, min/max)
- Unit conversions (degrees/radians)

### 🌤️ Weather Service

- Real-time weather data using Open-Meteo API
- Temperature forecasting by latitude/longitude coordinates
- RESTful weather endpoints

### 📝 Quiz Management

- Complete CRUD operations for quizzes and questions
- In-memory storage with mock data
- Search functionality
- RESTful API endpoints

### 🔄 MCP Integration

- Synchronous MCP server implementation
- Server-Sent Events (SSE) transport
- Tool change notifications
- Dynamic tool updates

## Quick Start

### Prerequisites

- Java 17 or later
- Maven 3.6+
- Docker (optional)

### Running Locally

1. **Clone and navigate to the server directory:**
   ```bash
   cd server
   ```

2. **Set the server port (required):**
   ```bash
   export SERVER_PORT=8080
   ```

3. **Run with Maven:**
   ```bash
   ./mvnw spring-boot:run
   ```

   Or using the provided scripts:
   ```bash
   # Windows
   mvn17.bat spring-boot:run
   
   # Unix/Linux/macOS
   ./mvn17.sh spring-boot:run
   ```

4. **Access the server:**
    - Server will start on `http://localhost:8080`
    - MCP endpoint: `http://localhost:8080/mcp/message`

### Running with Docker

1. **Build the Docker image:**
   ```bash
   docker build -t mcp-server .
   ```

2. **Run the container:**
   ```bash
   docker run -p 8080:8080 -e SERVER_PORT=8080 mcp-server
   ```

## API Endpoints

### Quiz Management API

| Method | Endpoint                            | Description             |
|--------|-------------------------------------|-------------------------|
| GET    | `/api/quizzes`                      | Get all quizzes         |
| GET    | `/api/quizzes/{id}`                 | Get quiz by ID          |
| POST   | `/api/quizzes`                      | Create new quiz         |
| PUT    | `/api/quizzes/{id}`                 | Update quiz             |
| DELETE | `/api/quizzes/{id}`                 | Delete quiz             |
| GET    | `/api/quizzes/search?title={title}` | Search quizzes by title |
| GET    | `/api/quizzes/{id}/questions`       | Get quiz questions      |

### MCP Management

| Method | Endpoint       | Description                |
|--------|----------------|----------------------------|
| GET    | `/updateTools` | Trigger tool update signal |
| POST   | `/mcp/message` | MCP message endpoint (SSE) |

## MCP Tools Available

### Math Tools

- `sumNumbers(int, int)` - Add two numbers
- `multiplyNumbers(int, int)` - Multiply two numbers
- `divideNumbers(double, double)` - Divide two numbers
- `subtractNumbers(int, int)` - Subtract numbers
- `powerOf(double, double)` - Calculate power
- `squareRoot(double)` - Calculate square root
- `factorial(int)` - Calculate factorial
- `sine(double)` - Sine function (radians)
- `cosine(double)` - Cosine function (radians)
- `tangent(double)` - Tangent function (radians)
- `degreesToRadians(double)` - Convert degrees to radians
- `radiansToDegrees(double)` - Convert radians to degrees
- `circleArea(double)` - Calculate circle area
- `circleCircumference(double)` - Calculate circle circumference
- `rectangleArea(double, double)` - Calculate rectangle area
- `triangleArea(double, double)` - Calculate triangle area
- And more...

### Weather Tools

- `weatherForecast(double latitude, double longitude)` - Get current temperature

## Configuration

The server can be configured through `application.properties`:

```properties
# Server Configuration
spring.ai.mcp.server.name=calculator-server
spring.ai.mcp.server.version=0.0.1
server.port=${SERVER_PORT}
# MCP Configuration
spring.ai.mcp.server.type=SYNC
spring.ai.mcp.server.stdio=false
spring.ai.mcp.server.sse-message-endpoint=/mcp/message
# Notifications
spring.ai.mcp.server.resource-change-notification=true
spring.ai.mcp.server.tool-change-notification=true
spring.ai.mcp.server.prompt-change-notification=true
# Logging
logging.file.name=./target/calculator-server.log
logging.level.org.springframework.ai.mcp=DEBUG
```

## Project Structure

```
server/
├── src/main/java/com/example/server/
│   ├── McpAttackDemoServer.java          # Main application class
│   ├── config/
│   │   └── McpConfiguration.java         # MCP and tool configuration
│   ├── controller/
│   │   ├── McpController.java            # MCP management endpoints
│   │   └── QuizController.java           # Quiz CRUD endpoints
│   ├── model/
│   │   ├── Question.java                 # Question entity
│   │   └── Quiz.java                     # Quiz entity
│   ├── service/
│   │   ├── QuizService.java              # Quiz business logic
│   │   ├── UpdateSignalService.java      # Update signal handling
│   │   └── WeatherService.java           # Weather API integration
│   └── tools/
│       ├── MathTools.java                # Mathematical tool implementations
│       └── McpCommandLineRunner.java     # MCP server initialization
├── src/main/resources/
│   └── application.properties            # Application configuration
├── Dockerfile                            # Container configuration
├── pom.xml                              # Maven dependencies
└── README.md                            # This file
```

## Sample Data

The server comes with pre-loaded sample quizzes:

1. **Java Programming Basics** - Test your knowledge of Java fundamentals
2. **General Knowledge** - Test your general knowledge

Each quiz contains multiple-choice questions with correct answers.

## Dependencies

Key dependencies include:

- Spring Boot 3.4.5
- Spring AI 1.1.0-SNAPSHOT (MCP support)
- Lombok (code generation)
- Jackson (JSON processing)

## Development

### Building from Source

```bash
# Clean and package
./mvnw clean package

# Skip tests during build
./mvnw clean package -DskipTests

# Run tests only
./mvnw test
```

### Environment Variables

- `SERVER_PORT` - Required. Port number for the server (e.g., 8080)

### Logging

Logs are written to `./target/calculator-server.log` with DEBUG level for MCP components.

## Integration with AI Assistants

This MCP server can be integrated with AI assistants that support the Model Context Protocol. The tools provided by this
server can be called by AI assistants to perform calculations, get weather information, and manage quizzes.

Example MCP client configuration:

```json
{
  "name": "calculator-server",
  "type": "sse",
  "url": "http://localhost:8080/mcp/message"
}
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Support

For questions and support, please check the application logs in `./target/calculator-server.log` or review the Spring AI
MCP documentation.
