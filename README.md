# Genesys Integration System

A comprehensive Spring Boot application that integrates with Genesys contact center solutions, providing database operations, Redis caching, JMeter testing, and a modern web interface.

## Features

### 🔧 **Core Functionality**
- **Database Integration**: Support for PostgreSQL, Oracle, and Microsoft SQL Server
- **Redis Caching**: High-performance caching for customer and interaction data
- **Genesys SDK Integration**: GCTI service integration and Composer workflow execution
- **Real-time Monitoring**: Kazimir-style logging and performance metrics
- **RESTful APIs**: Complete CRUD operations for customers and interactions

### 🌐 **Frontend Interface**
- **Responsive Dashboard**: Modern Bootstrap-based interface
- **Real-time Updates**: Live metrics and system status
- **Interactive Controls**: Agent management, call controls, and workflow execution

### 🧪 **Testing & Monitoring**
- **JMeter Load Testing**: Comprehensive performance testing scripts
- **Logging System**: Detailed interaction and system logging
- **Performance Metrics**: Response time tracking and system monitoring

## Technology Stack

### Backend
- **Java 21**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **Spring Security**
- **Spring Data Redis**
- **Flowable Workflow Engine**

### Database Support
- **PostgreSQL** (Primary)
- **Oracle Database**
- **Microsoft SQL Server**
- **Redis** (Caching)

### Frontend
- **HTML5, CSS3, JavaScript**
- **Bootstrap 5**
- **Font Awesome Icons**
- **RESTful API Integration**

### Testing
- **JMeter 5.4+**
- **JUnit 5**
- **Spring Boot Test**

## Quick Start

### Prerequisites

1. **Java Development Kit (JDK) 21**
2. **Maven 3.8+**
3. **MySQL 8.0+** (Primary database) or PostgreSQL, Oracle, SQL Server
4. **Redis Server** (optional, for production caching)
5. **Apache JMeter 5.4+** (for testing)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Genesys_Example
   ```

2. **Configure Database**
   - Create database using one of the provided schema scripts:
     - `database-scripts/schema-postgresql.sql`
     - `database-scripts/schema-oracle.sql`
     - `database-scripts/schema-mssql.sql`
   - Update `src/main/resources/application.yaml` with your database credentials

3. **Configure Redis**
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
   ```

4. **Build the application**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

6. **Access the application**
   - Web Interface: http://localhost:8060
   - API Documentation: http://localhost:8060/swagger-ui.html (if enabled)

## API Endpoints

### Customer Management
- `POST /api/customers` - Create new customer
- `GET /api/customers/{customerId}` - Get customer by ID
- `GET /api/customers/search` - Search customers
- `DELETE /api/customers/{customerId}` - Delete customer
- `GET /api/customers/inactive` - Get inactive customers

### Genesys Integration
- `POST /api/genesys/gcti/agent/{agentId}/connect` - Connect agent
- `POST /api/genesys/gcti/agent/{agentId}/call` - Make outbound call
- `POST /api/genesys/gcti/call/{callId}/transfer` - Transfer call
- `GET /api/genesys/gcti/agent/{agentId}/status` - Get agent status
- `GET /api/genesys/gcti/queue/{queueName}/stats` - Get queue statistics
- `POST /api/genesys/composer/workflow/{workflowName}/execute` - Execute workflow
- `POST /api/genesys/composer/flow/{flowName}/create` - Create call flow
- `GET /api/genesys/composer/customer/{customerId}/history` - Get interaction history

## Database Schema

### Key Tables
- **customers**: Customer information and contact details
- **interactions**: Customer interaction records (calls, chats, emails)
- **agent_sessions**: Agent login/logout tracking
- **call_records**: Detailed call records with recordings
- **queue_statistics**: Real-time queue performance metrics

### Relationships
- One-to-Many: Customers → Interactions
- One-to-Many: Customers → Call Records
- Many-to-One: Interactions → Agents

## Redis Caching Strategy

### Cache Keys and TTL
- **Customer Data**: `customer:{customerId}` (30 minutes)
- **Interaction Data**: `interaction:{interactionId}` (15 minutes)
- **Agent Status**: `agent:status:{agentId}` (2 hours)
- **Queue Statistics**: `queue:stats:{queueName}` (5 minutes)

### Cache Operations
- Automatic cache population on database queries
- Cache invalidation on data updates
- Fallback to database on cache misses

## Genesys Integration

### GCTI Service
- **Agent Connection Management**: Connect/disconnect agents
- **Call Control**: Initiate, transfer, and monitor calls
- **Queue Management**: Real-time queue statistics
- **Agent Status Monitoring**: Track agent availability and performance

### Composer Service
- **Workflow Execution**: Run Genesys Composer workflows
- **Call Flow Creation**: Define and execute call routing logic
- **Customer Validation**: Validate customer data through workflows
- **Interaction History**: Retrieve customer interaction history

## Testing

### JMeter Load Testing
1. **Install JMeter 5.4+**
2. **Run the test script**:
   ```bash
   cd jmeter-tests
   run-jmeter-tests.bat
   ```
3. **View results** in `jmeter-tests/results/dashboard`

### Test Scenarios
- **Customer API Load Test**: 50 concurrent users, 10-minute duration
- **Genesys Integration Test**: 20 concurrent users, 5-minute duration
- **Performance Metrics**: Response times, throughput, error rates

## Logging and Monitoring

### Kazimir Logger
- **Interaction Logging**: Detailed interaction tracking
- **Agent Activity**: Agent status and activity monitoring
- **System Events**: Component-level system events
- **Performance Metrics**: Operation timing and throughput
- **Error Tracking**: Comprehensive error logging with stack traces

### Log Files
- `logs/kazimir/interactions_YYYY-MM-DD.log`
- `logs/kazimir/agents_YYYY-MM-DD.log`
- `logs/kazimir/system_YYYY-MM-DD.log`
- `logs/kazimir/errors_YYYY-MM-DD.log`
- `logs/kazimir/performance_YYYY-MM-DD.log`

## Configuration

### Application Properties
Key configuration options in `application.yaml`:

```yaml
# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/genesys_db
    username: genesys_user
    password: genesys_pass

# Redis Configuration
spring:
  data:
    redis:
      host: localhost
      port: 6379

# Genesys Configuration
genesys:
  gcti:
    server-url: ws://localhost:8060/gcti
    username: admin
    password: password
```

## Development

### Project Structure
```
src/main/java/com/gfo/demo/
├── config/          # Configuration classes
├── controller/      # REST API controllers
├── entity/          # JPA entities
├── genesys/         # Genesys integration services
├── repository/      # Data access layer
└── service/         # Business logic services

src/main/resources/
├── static/          # Web interface (HTML, CSS, JS)
└── application.yaml # Configuration

database-scripts/    # Database schema scripts
jmeter-tests/       # Load testing scripts
logs/              # Application logs
```

### Building and Running
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Run tests
mvn test

# Package
mvn package
```

## Deployment

### Production Considerations
1. **Database Connection Pooling**: Configure optimal pool sizes
2. **Redis High Availability**: Set up Redis cluster for production
3. **Load Balancing**: Deploy behind a load balancer
4. **Monitoring**: Integrate with APM tools
5. **Security**: Enable HTTPS and configure Spring Security

### Docker Deployment
```dockerfile
FROM openjdk:21-jre-slim
COPY target/genesys-example.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Contributing

1. **Fork the repository**
2. **Create a feature branch**
3. **Make your changes**
4. **Add tests**
5. **Submit a pull request**

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the repository
- Check the documentation in the `/docs` folder
- Review the API examples in the codebase
- Read the [Chinese Documentation](CHINESE_DOCUMENTATION.md) for detailed technical specifications

## Roadmap

- [ ] WebSocket integration for real-time updates
- [ ] Advanced reporting and analytics
- [ ] Multi-tenant support
- [ ] Enhanced security features
- [ ] Mobile application interface
- [ ] Integration with additional Genesys products