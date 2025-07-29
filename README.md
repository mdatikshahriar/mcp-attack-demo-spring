# MCP Security Demonstration Project

⚠️ **SECURITY RESEARCH AND EDUCATIONAL PROJECT** ⚠️

This project demonstrates critical security vulnerabilities that can arise when MCP (Model Context Protocol) server URLs
are shared with clients. It consists of two components: a vulnerable MCP server and a demonstration client that exploits
common security weaknesses.

**This is for educational and security research purposes only.**

## 🎯 Project Goals

This demonstration project aims to:

1. **Educate developers** about MCP security risks and best practices
2. **Demonstrate real-world attack vectors** against MCP implementations
3. **Provide practical examples** of vulnerability exploitation
4. **Promote secure coding practices** in MCP server development
5. **Foster security awareness** in the AI/MCP development community

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                 MCP Security Demo Project                    │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────┐         ┌─────────────────────────┐ │
│  │   MCP Client        │◄────────┤   MCP Server            │ │
│  │   (Port 8234)       │         │   (Port 8080)           │ │
│  │                     │   MCP   │                         │ │
│  │ • Chat Interface    │ Protocol│ • Calculator Tools      │ │
│  │ • Security Tools    │   SSE   │ • Weather Service       │ │
│  │ • Attack Simulation │         │ • Quiz Management       │ │
│  │ • Reconnaissance    │         │ • Vulnerable APIs       │ │
│  └─────────────────────┘         └─────────────────────────┘ │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 🚨 Security Vulnerabilities Demonstrated

### Critical Issues Shown:

1. **URL Information Disclosure** - MCP server URLs reveal infrastructure details
2. **Unauthorized API Access** - Hidden endpoints accessible without authentication
3. **Network Reconnaissance** - Port scanning and service discovery
4. **Data Extraction** - Sensitive information accessible to any client
5. **Privilege Escalation** - Administrative functions exposed
6. **Lateral Movement** - Using MCP access to discover other services

### Business Impact:

- **Confidentiality**: COMPLETELY COMPROMISED
- **Integrity**: AT RISK
- **Availability**: POTENTIALLY COMPROMISED
- Data breach costs: $50,000 - $500,000+
- Regulatory fines: Up to 4% of annual revenue
- Reputation damage: SEVERE

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker & Docker Compose (optional)
- OpenAI API key or Azure OpenAI credentials

### Option 1: Docker Compose (Recommended)

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd mcp-security-demo
   ```

2. **Set environment variables:**
   ```bash
   # For OpenAI
   export AZURE_OPENAI_API_KEY="your-api-key"
   export AZURE_OPENAI_ENDPOINT="your-endpoint"
   
   # Or create .env file
   echo "AZURE_OPENAI_API_KEY=your-api-key" > .env
   echo "AZURE_OPENAI_ENDPOINT=your-endpoint" >> .env
   ```

3. **Start both services:**
   ```bash
   docker-compose up --build
   ```

4. **Access the applications:**
    - Client Interface: http://localhost:8234
    - Security Demo: http://localhost:8234/demo/exploit-mcp-server
    - Server API: http://localhost:8080 (internal)

### Option 2: Manual Setup

#### Start the MCP Server

1. **Navigate to server directory:**
   ```bash
   cd server
   ```

2. **Set server port:**
   ```bash
   export SERVER_PORT=8080
   ```

3. **Run the server:**
   ```bash
   # Using Maven wrapper
   ./mvnw spring-boot:run
   
   # Or using provided scripts
   # Windows:
   mvn17.bat spring-boot:run
   
   # Unix/Linux/macOS:  
   ./mvn17.sh spring-boot:run
   ```

#### Start the Attack Client

1. **Open new terminal and navigate to client directory:**
   ```bash
   cd client
   ```

2. **Configure MCP connection:**
   ```yaml
   # Edit src/main/resources/application.yml
   spring:
     ai:
       mcp:
         sse:
           connections:
             server1:
               url: "http://localhost:8080"
               sse-endpoint: "/mcp"
   ```

3. **Set API credentials:**
   ```bash
   export AZURE_OPENAI_API_KEY="your-api-key"
   export AZURE_OPENAI_ENDPOINT="your-endpoint"
   export SERVER_PORT=8234
   ```

4. **Run the client:**
   ```bash
   ./mvnw spring-boot:run
   ```

### Option 3: Individual Docker Containers

#### Build and run server:

```bash
cd server
docker build -t mcp-server .
docker run -p 8080:8080 -e SERVER_PORT=8080 mcp-server
```

#### Build and run client:

```bash
cd client  
docker build -t mcp-client .
docker run -p 8234:8234 \
  -e SERVER_PORT=8234 \
  -e MCP_SERVER_URL=http://host.docker.internal:8080/mcp/message \
  -e AZURE_OPENAI_API_KEY=your-key \
  -e AZURE_OPENAI_ENDPOINT=your-endpoint \
  mcp-client
```

## 🛡️ MCP Server Features

### 🧮 Mathematical Tools

- Basic arithmetic (add, subtract, multiply, divide)
- Advanced functions (power, square root, factorial)
- Trigonometric functions (sin, cos, tan)
- Logarithmic functions and conversions
- Geometric calculations (area, circumference)

### 🌤️ Weather Service

- Real-time weather data using Open-Meteo API
- Temperature forecasting by coordinates
- RESTful weather endpoints

### 📝 Quiz Management API

- Complete CRUD operations for quizzes
- Search functionality
- **⚠️ VULNERABLE: No authentication required**

| Method | Endpoint                            | Description             |
|--------|-------------------------------------|-------------------------|
| GET    | `/api/quizzes`                      | Get all quizzes         |
| GET    | `/api/quizzes/{id}`                 | Get quiz by ID          |
| POST   | `/api/quizzes`                      | Create new quiz         |
| PUT    | `/api/quizzes/{id}`                 | Update quiz             |
| DELETE | `/api/quizzes/{id}`                 | Delete quiz             |
| GET    | `/api/quizzes/search?title={title}` | Search quizzes by title |
| GET    | `/api/quizzes/{id}/questions`       | Get quiz questions      |

## 🔍 Attack Client Capabilities

### Automated Security Assessment

The client performs comprehensive reconnaissance:

```bash
# Trigger full security assessment
POST http://localhost:8234/demo/exploit-mcp-server
```

### Attack Modules

1. **Network Reconnaissance**
    - Port scanning (80, 443, 8080, 8081, 3000, 9090)
    - Service fingerprinting
    - Infrastructure mapping

2. **API Discovery & Exploitation**
    - Path enumeration (`/api/quizzes`, `/admin`, `/actuator`)
    - HTTP method testing
    - Unauthorized data access

3. **Data Exfiltration**
    - Quiz content extraction
    - Question and answer harvesting
    - Sensitive information gathering

4. **Stealth Operations**
    - Randomized User-Agent strings
    - Request timing randomization
    - Header manipulation

### Interactive Features

- **Chat Interface**: http://localhost:8234/chat
- **Real-time Attack Demo**: WebSocket-based demonstration
- **Impact Assessment**: Detailed security reports

## 📊 Sample Attack Sequence

1. **Information Gathering**
   ```
   MCP URL: http://localhost:8080/mcp/message
   → Extract: hostname, port, technology stack
   ```

2. **Network Discovery**
   ```
   Port Scan: 80, 443, 8080, 8081, 3000, 9090
   → Discover: Additional services and endpoints
   ```

3. **API Enumeration**
   ```
   Test Paths: /api/quizzes, /admin, /actuator, /swagger-ui
   → Find: Unprotected Quiz API
   ```

4. **Data Extraction**
   ```
   GET /api/quizzes → All quiz metadata
   GET /api/quizzes/1/questions → Sensitive quiz content
   → Result: Complete database extraction
   ```

## 🛡️ Security Recommendations

### Immediate Actions

- [ ] Audit all MCP URL sharing practices
- [ ] Implement API authentication/authorization
- [ ] Add network segmentation
- [ ] Monitor access logs for reconnaissance patterns

### Long-term Solutions

- [ ] Deploy API Gateway with authentication
- [ ] Implement Zero-Trust architecture
- [ ] Add comprehensive logging and monitoring
- [ ] Regular security assessments
- [ ] Developer security training

## 🔧 Configuration

### Environment Variables

**Server:**

- `SERVER_PORT` - Server port (required, e.g., 8080)

**Client:**

- `SERVER_PORT` - Client port (required, e.g., 8234)
- `MCP_SERVER_URL` - MCP server endpoint
- `AZURE_OPENAI_API_KEY` - OpenAI API key
- `AZURE_OPENAI_ENDPOINT` - OpenAI endpoint
- `SPRING_PROFILES_ACTIVE` - Active profile (docker/default)

### Docker Compose Configuration

```yaml
services:
  server:
    build: ./server
    expose: [ "8080" ]
    environment:
      - SERVER_PORT=8080

  client:
    build: ./client
    ports: [ "8234:8234" ]
    environment:
      - SERVER_PORT=8234
      - MCP_SERVER_URL=http://server:8080/mcp/message
      - AZURE_OPENAI_API_KEY=${AZURE_OPENAI_API_KEY}
    depends_on: [ server ]
```

## 📚 Educational Value

This project teaches:

- **API Security Fundamentals** - Authentication, authorization, input validation
- **Network Security Principles** - Segmentation, monitoring, access control
- **Secure Development Practices** - Threat modeling, secure coding
- **Incident Response** - Detection, analysis, mitigation
- **Risk Assessment** - Impact analysis, vulnerability prioritization

## ⚖️ Legal and Ethical Guidelines

### ✅ Authorized Use Only

- Test only systems you own or have explicit permission to test
- Educational and authorized security research purposes only
- Follow responsible disclosure practices

### ❌ Prohibited Activities

- Testing systems without authorization
- Accessing data you're not authorized to access
- Causing damage or disruption to services
- Using for malicious purposes

### 📝 Responsible Disclosure

- Report vulnerabilities through proper channels
- Allow reasonable time for fixes
- Follow coordinated vulnerability disclosure

## 🤝 Contributing

### Security Research

- Submit new attack vectors via pull requests
- Document mitigation strategies
- Share lessons learned from testing

### Code Quality

- Follow secure coding practices
- Add comprehensive logging
- Include proper error handling

## 📞 Support

### Security Issues

- Report vulnerabilities privately to maintainers
- Use encrypted communication for sensitive reports
- Follow responsible disclosure guidelines

### General Questions

- Open GitHub issues for feature requests
- Join security research discussions
- Share defensive strategies

## 📄 License

This project is licensed under dual licenses:

- **Server**: Apache License 2.0
- **Client**: MIT License

See respective LICENSE files for details.

## ⚠️ Disclaimer

**This software is provided for educational and authorized testing purposes only. The authors are not responsible for
any misuse or damage caused by this program. Users must ensure they have proper authorization before testing any
systems.**

**Use these tools to make systems more secure, not to cause harm.**

---

## 🔍 Technical Details

### Project Structure

```
mcp-security-demo/
├── client/                    # Attack demonstration client
│   ├── src/main/java/
│   │   └── com/example/client/
│   │       ├── attack/        # Security testing modules
│   │       ├── service/       # MCP and reconnaissance services  
│   │       └── controller/    # Web and demo endpoints
│   ├── Dockerfile
│   └── README.md
├── server/                    # Vulnerable MCP server
│   ├── src/main/java/
│   │   └── com/example/server/
│   │       ├── controller/    # Quiz and MCP endpoints
│   │       ├── service/       # Business logic
│   │       ├── model/         # Data models
│   │       └── tools/         # MCP tool implementations
│   ├── Dockerfile  
│   └── README.md
├── docker-compose.yml         # Container orchestration
└── README.md                  # This file
```

### Dependencies

- **Spring Boot** 3.4.5
- **Spring AI** 1.1.0-SNAPSHOT (MCP support)
- **Java** 17+
- **Maven** 3.8+
- **Docker** & Docker Compose

---

**🔒 Remember: The best defense is a good understanding of the attack. Use this knowledge responsibly.**
