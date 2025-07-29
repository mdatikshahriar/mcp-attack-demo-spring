# MCP Security Demonstration Client

⚠️ **SECURITY RESEARCH PROJECT** ⚠️

This project demonstrates critical security vulnerabilities that can arise when MCP (Model Context Protocol) server URLs
are shared with clients. **This is for educational and security research purposes only.**

## 🚨 What This Demonstrates

This client application shows how a malicious or compromised MCP client can exploit the server URL to:

1. **Discover Hidden APIs** - Find unprotected endpoints like `/api/quizzes`
2. **Extract Sensitive Data** - Download quiz content, questions, and answers
3. **Perform Network Reconnaissance** - Scan ports and map infrastructure
4. **Attempt Unauthorized Access** - Try to create, modify, or delete data
5. **Conduct Lateral Movement** - Discover subdomains and admin interfaces

## 🎯 Key Security Issues Demonstrated

### Problem 1: URL Information Disclosure

- MCP server URLs reveal hostname, port, and infrastructure details
- Clients can extract this information for reconnaissance

### Problem 2: Network Discovery

- Port scanning to find other services on the same server
- Technology stack fingerprinting
- Infrastructure mapping

### Problem 3: API Enumeration

- Path enumeration to discover hidden endpoints
- HTTP method testing (GET, POST, PUT, DELETE)
- Content type manipulation

### Problem 4: Unauthorized Data Access

- Access to Quiz API without authentication
- Extraction of sensitive quiz content
- Download of questions and correct answers

### Problem 5: Data Modification Attempts

- Attempts to create unauthorized content
- Testing PUT/PATCH operations for data modification
- Injection of malicious data

## 🏗️ Architecture

```
MCP Client
├── 💬 Chat Interface (WebSocket)
├── 🔧 MCP Tool Integration
├── 🔍 Security Reconnaissance Services
│   ├── NetworkReconnaissanceClient
│   ├── PathEnumerationClient  
│   ├── SubdomainEnumerationClient
│   └── TimingAttackClient
├── 📊 Data Extraction
└── 📈 Impact Assessment
```

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.8+
- MCP Server URL (configured in application.yml)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd mcp-attack-demo-client
   ```

2. **Configure MCP Connection**
   ```yaml
   # src/main/resources/application.yml
   spring:
     ai:
       mcp:
         sse:
           connections:
             server1:
               url: "http://your-server.com:8080"
               sse-endpoint: "/mcp"
   ```

3. **Build and Run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Access the Application**
    - Chat Interface: http://localhost:8081
    - Security Demo: http://localhost:8081/demo/exploit-mcp-server

## 🛡️ Security Demonstration Features

### Automated Reconnaissance

```bash
POST /demo/exploit-mcp-server
```

Runs a comprehensive security assessment including:

- Port scanning
- Path enumeration
- Subdomain discovery
- Timing analysis
- Data extraction attempts

### Interactive Chat Interface

- WebSocket-based chat at http://localhost:8081/chat
- Supports both legitimate MCP tool usage and security testing
- Real-time demonstration of attack progression

### Stealth Mode

- Randomized User-Agent strings
- Request timing randomization
- Header manipulation to avoid detection

## 📋 Key Components

### Core Services

- **`McpConnectionService`** - Extracts server URLs from MCP configuration
- **`ComprehensiveReconnaissanceService`** - Orchestrates multi-phase attacks
- **`RealWorldExploitationSimulator`** - Simulates actual API exploitation

### Attack Clients

- **`NetworkReconnaissanceClient`** - Port scanning and fingerprinting
- **`PathEnumerationClient`** - Hidden API discovery and exploitation
- **`SubdomainEnumerationClient`** - Subdomain enumeration
- **`TimingAttackClient`** - Response time analysis

### Security Assessment

- **`SecurityImpactAssessment`** - Generates detailed impact reports
- **`DataExtractionClient`** - Simulates data exfiltration
- **`StealthReconnaissanceClient`** - Evasion techniques

## 🎯 Demonstrated Attack Vectors

### 1. Information Gathering

```java
// Extract server details from MCP URL
String serverUrl = mcpConnectionService.getServer1Url();
// Result: http://your-server.com:8080
```

### 2. Port Scanning

```java
// Scan common ports
int[] ports = {80, 443, 8080, 8081, 3000, 9090};
// Discover additional services
```

### 3. API Discovery

```java
// Test common paths
String[] paths = {"/api/quizzes",    // Hidden Quiz API
                "/admin",          // Admin interface
                "/actuator",       // Spring Boot actuator
                "/swagger-ui"      // API documentation
        };
```

### 4. Data Extraction

```java
// Access Quiz API without authorization
GET /api/quizzes GET /api/quizzes/{id}/questions
// Extract sensitive quiz content
```

## 📊 Security Impact

### Technical Impact

- **Confidentiality**: COMPLETELY COMPROMISED
- **Integrity**: AT RISK
- **Availability**: POTENTIALLY COMPROMISED
- **Authentication**: BYPASSED
- **Authorization**: FAILED

### Business Impact

- Data breach costs: $50,000 - $500,000+
- Regulatory fines (GDPR): Up to 4% of annual revenue
- Reputation damage: SEVERE
- Legal liability: Class action lawsuits possible

## 🔧 Configuration

### Application Properties

```yaml
server:
  port: 8081

spring:
  ai:
    mcp:
      sse:
        connections:
          server1:
            url: "http://your-server.com:8080"
            sse-endpoint: "/mcp"
    openai:
      api-key: "your-openai-api-key"

logging:
  level:
    com.example.client.attack: ERROR  # Show attack logs prominently
```

### Security Testing Configuration

```java
// Customize attack parameters
private final int[] commonPorts = {80, 443, 8080, 8081, 8082, 3000};
private final String[] commonPaths = {"/api/quizzes", "/admin", "/actuator"};
private final String[] userAgents = {"Mozilla/5.0...", "PostmanRuntime/7.29.0"};
```

## 🛡️ Mitigation Strategies

### Immediate Actions

1. **Audit MCP URL Sharing** - Review all shared URLs
2. **Implement API Gateway** - Add authentication layer
3. **Network Segmentation** - Isolate MCP servers
4. **Monitor Access Logs** - Watch for reconnaissance patterns

### Long-term Solutions

1. **Zero-Trust Architecture** - Authenticate every request
2. **API Security** - Implement proper authorization
3. **Network Security** - Use firewalls and VPNs
4. **Continuous Monitoring** - Deploy SIEM solutions

## 📚 Educational Value

This project teaches:

- **API Security Best Practices**
- **Network Security Fundamentals**
- **Threat Modeling Techniques**
- **Security Assessment Methods**
- **Defense-in-Depth Strategies**

## ⚖️ Legal and Ethical Considerations

### Authorized Testing Only

- Only use against systems you own or have explicit permission to test
- This tool is for educational and authorized security research only
- Unauthorized access to computer systems is illegal

### Responsible Disclosure

- Report vulnerabilities through proper channels
- Allow reasonable time for fixes before public disclosure
- Follow coordinated vulnerability disclosure practices

## 🤝 Contributing

### Security Research

- Submit new attack vectors via pull requests
- Document mitigation strategies for discovered issues
- Share lessons learned from testing

### Code Quality

- Follow secure coding practices
- Add comprehensive logging for security events
- Include error handling for robust testing

## 📞 Support

### Security Issues

- Report security vulnerabilities privately
- Use encrypted communication for sensitive reports
- Follow responsible disclosure guidelines

### General Questions

- Open GitHub issues for feature requests
- Join security research discussions
- Share defensive strategies and improvements

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## ⚠️ Disclaimer

**This software is provided for educational and authorized testing purposes only. The authors are not responsible for
any misuse or damage caused by this program. Users must ensure they have proper authorization before testing any
systems.**

---

**🔒 Remember: With great power comes great responsibility. Use these tools to make systems more secure, not to cause
harm.**
