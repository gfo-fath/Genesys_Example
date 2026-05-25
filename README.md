# Genesys呼叫中心集成系统

一个完整的Genesys呼叫中心集成系统，基于Spring Boot 4.0.6和Java 21开发，提供真实的电话呼叫功能、座席管理、性能监控、日志分析等企业级呼叫中心功能。

## 核心功能特性 ✅

### 1. 真实电话呼叫功能
- **Genesys SDK集成**: 完整的Genesys平台集成
- **SIP电话服务**: 支持真实SIP协议电话呼叫
- **双通道保障**: Genesys + SIP双通道呼叫确保可靠性
- **实时呼叫控制**: 保持、转移、会议、静音等完整功能

### 2. 实时通信功能
- **WebSocket实时通信**: 座席状态实时更新
- **呼叫状态推送**: 实时呼叫状态监控
- **系统消息广播**: 管理员消息推送

### 3. 完整的数据库设计
- **呼叫记录管理**: 详细的呼叫记录和统计
- **座席绩效管理**: 完整的座席绩效指标
- **复杂查询支持**: 多维度数据分析和报表

### 4. Genesys产品深度集成
- **GCTI服务增强**: 计算机电话集成
- **Composer工作流**: 自动化呼叫流程
- **Kazimir日志分析**: 专业日志分析和故障排除

### 5. 测试工具集成
- **LoadRunner集成**: 负载测试支持
- **JMeter测试**: API性能测试
- **性能监控**: 实时监控和告警

## 功能特性

### 🔧 **核心功能**
- **真实电话呼叫**: Genesys SDK + SIP协议实现真实电话功能
- **数据库集成**: MySQL主数据库，支持复杂查询和关联操作
- **Redis缓存**: 高性能缓存座席状态和呼叫会话
- **Genesys SDK集成**: GCTI服务、Composer工作流完整集成
- **实时通信**: WebSocket实时呼叫管理和状态更新
- **RESTful API**: 完整的CRUD操作和业务接口

### 🌐 **前端界面**
- **实时呼叫仪表板**: WebSocket实时数据推送
- **座席管理界面**: 座席状态监控和控制
- **管理员面板**: 系统监控和管理功能
- **交互式控制**: 呼叫控制、转移、会议等功能

### 🧪 **测试和监控**
- **LoadRunner集成**: 负载测试场景支持
- **JMeter测试**: API性能和压力测试
- **Kazimir日志分析**: Genesys专用日志分析工具
- **性能监控**: 实时性能指标和系统监控

## 技术栈

### 后端技术
- **Java 21**: 编程语言
- **Spring Boot 4.0.6**: 应用框架
- **Spring Data JPA**: 数据库访问
- **Spring Security**: 安全认证
- **Spring WebSocket**: 实时通信
- **Spring Data Redis**: 缓存管理
- **Flowable**: 工作流引擎

### 数据库支持
- **MySQL 8.0**: 主数据库（已实现）
- **Redis**: 缓存和会话管理
- **H2**: 测试数据库

### Genesys集成
- **Genesys Cloud CX**: 云平台集成
- **Genesys PureConnect**: 本地部署支持
- **SIP协议**: 电话通信协议
- **CTI协议**: 计算机电话集成

### 前端技术
- **HTML5, CSS3, JavaScript**: 前端基础
- **WebSocket**: 实时通信
- **RESTful API**: 数据接口

### 测试工具
- **JMeter 5.4+**: 性能测试
- **LoadRunner**: 负载测试
- **JUnit 5**: 单元测试
- **Spring Boot Test**: 集成测试

## Quick Start

### 环境要求

1. **Java Development Kit (JDK) 21**
2. **Maven 3.8+**
3. **MySQL 8.0+** (主数据库)
4. **Redis Server** (缓存服务器)
5. **Genesys平台**: Genesys Cloud CX或PureConnect
6. **SIP服务器**: 支持SIP协议的电话服务器
7. **Apache JMeter 5.4+** (性能测试)
8. **LoadRunner** (负载测试，可选)

### 安装和部署

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd Genesys_Example
   ```

2. **数据库配置**
   ```sql
   CREATE DATABASE genesys_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   - 表结构将通过JPA自动创建 (ddl-auto: update)
   - 更新 `src/main/resources/application.yaml` 中的数据库配置

3. **Redis配置**
   ```yaml
   spring:
     data:
       redis:
         host: localhost
         port: 6379
   ```

4. **Genesys配置**
   ```yaml
   genesys:
     gcti:
       server-url: ws://localhost:8060/gcti
       username: admin
       password: password
     sip:
       server-url: sip.genesys.com
       port: 5060
       username: genesys_user
       password: genesys_pass
   ```

5. **构建应用**
   ```bash
   mvn clean install
   ```

6. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

7. **访问应用**
   - 后端API: http://localhost:8060
   - WebSocket: ws://localhost:8060/ws/call-center
   - 健康检查: http://localhost:8060/actuator/health

## API接口文档

### 呼叫管理API

#### 真实电话呼叫
- `POST /api/genesys/gcti/agent/{agentId}/call` - 发起真实电话呼叫
- `POST /api/genesys/gcti/call/{callId}/answer` - 接听呼叫
- `POST /api/genesys/gcti/call/{callId}/end` - 挂断呼叫
- `POST /api/genesys/gcti/call/{callId}/hold` - 保持呼叫
- `POST /api/genesys/gcti/call/{callId}/resume` - 恢复呼叫
- `POST /api/genesys/gcti/call/{callId}/transfer` - 转移呼叫
- `POST /api/genesys/gcti/call/{callId}/conference` - 会议呼叫
- `POST /api/genesys/gcti/call/{callId}/mute` - 静音控制

#### 座席管理
- `POST /api/genesys/gcti/agent/{agentId}/connect` - 座席登录
- `POST /api/genesys/gcti/agent/{agentId}/disconnect` - 座席登出
- `GET /api/genesys/gcti/agent/{agentId}/status` - 获取座席状态
- `POST /api/genesys/gcti/agent/{agentId}/status` - 设置座席状态
- `GET /api/genesys/gcti/queue/{queueName}/stats` - 获取队列统计

### Composer工作流API

- `POST /api/genesys/composer/workflow/{workflowName}/execute` - 执行工作流
- `POST /api/genesys/composer/flow/{flowName}/create` - 创建呼叫流程
- `GET /api/genesys/composer/customer/{customerId}/history` - 获取交互历史
- `POST /api/genesys/composer/customer/{customerId}/validate` - 验证客户数据

### 客户管理API

- `POST /api/customers` - 创建新客户
- `GET /api/customers/{customerId}` - 获取客户信息
- `GET /api/customers/search` - 搜索客户
- `PUT /api/customers/{customerId}` - 更新客户信息
- `DELETE /api/customers/{customerId}` - 删除客户

### 实时监控API

- `GET /api/monitoring/call-status/{callId}` - 获取呼叫状态
- `GET /api/monitoring/agent-performance/{agentId}` - 获取座席绩效
- `GET /api/monitoring/system-health` - 获取系统健康状态
- `GET /api/monitoring/log-analysis` - 获取日志分析结果

## 数据库设计

### 核心表结构

#### 呼叫记录表 (call_records)
- **主键**: id
- **呼叫标识**: call_id, genesys_call_id, sip_call_id
- **关联信息**: customer_id, agent_id, phone_number
- **时间信息**: start_time, answer_time, end_time
- **时长统计**: duration, talk_time, hold_time
- **状态信息**: status, direction, type
- **质量信息**: satisfaction_score, disposition_code, recording_url

#### 座席绩效表 (agent_performance)
- **主键**: id
- **座席信息**: agent_id, agent_name
- **时间维度**: performance_date, login_time, logout_time
- **呼叫统计**: calls_handled, calls_inbound, calls_outbound
- **时长统计**: total_talk_time, total_hold_time, avg_call_duration
- **质量指标**: customer_satisfaction, first_call_resolution
- **状态时长**: available_time, busy_time, not_ready_time, break_time

#### 客户表 (customers)
- **客户基本信息**: 姓名、联系方式、公司等
- **客户状态**: 活跃状态、偏好渠道等
- **交互历史**: 与呼叫记录关联

#### 交互表 (interactions)
- **多通道交互**: 电话、聊天、邮件等
- **交互状态**: 活跃、完成、放弃等
- **关联信息**: 客户ID、座席ID、队列信息等

### 表关系
- **一对多**: 客户 → 呼叫记录
- **一对多**: 客户 → 交互记录
- **一对多**: 座席 → 呼叫记录
- **一对多**: 座席 → 绩效记录

## Redis缓存策略

### 缓存键和过期时间
- **客户数据**: `customer:{customerId}` (30分钟)
- **交互数据**: `interaction:{interactionId}` (15分钟)
- **座席状态**: `agent:status:{agentId}` (2小时)
- **队列统计**: `queue:stats:{queueName}` (5分钟)
- **呼叫会话**: `call:session:{callId}` (呼叫期间)
- **系统配置**: `config:{configKey}` (24小时)

### 缓存操作
- **自动缓存**: 数据库查询时自动填充缓存
- **缓存失效**: 数据更新时自动清除相关缓存
- **降级策略**: 缓存未命中时回退到数据库
- **批量操作**: 支持批量缓存更新和清理

## Genesys产品集成

### Genesys SDK服务 (GenesysSDKService)
- **平台连接**: 自动连接Genesys平台
- **座席管理**: 座席登录、登出、状态管理
- **呼叫控制**: 发起、接听、挂断、保持、恢复
- **呼叫转移**: 单步转移、协商转移
- **会议功能**: 多方会议呼叫
- **静音控制**: 呼叫静音和取消静音
- **CTI事件**: 实时CTI事件监听和处理

### GCTI服务增强
- **座席连接管理**: 座席与Genesys平台的连接管理
- **呼叫控制接口**: 完整的呼叫控制功能
- **队列监控**: 实时队列统计和监控
- **座席状态监控**: 座席可用性和性能跟踪
- **消息推送**: 实时消息发送到座席桌面

### Composer服务
- **工作流执行**: 执行Genesys Composer定义的工作流
- **呼叫流程创建**: 创建和配置呼叫路由逻辑
- **客户数据验证**: 通过工作流验证客户信息
- **交互历史查询**: 获取客户的完整交互历史
- **自动化流程**: 客户认证、路由选择等自动化

### SIP电话服务 (SIPPhoneService)
- **SIP注册**: 自动注册到SIP服务器
- **设备管理**: 电话设备注册和管理
- **呼叫发起**: 发起SIP呼叫
- **DTMF支持**: 发送DTMF信号
- **呼叫保持**: 呼叫保持和恢复
- **呼叫转移**: SIP呼叫转移
- **会议功能**: 多方会议支持

### 真实呼叫服务 (RealCallService)
- **双通道保障**: 同时使用Genesys和SIP确保呼叫可靠性
- **呼叫生命周期**: 完整的呼叫生命周期管理
- **状态同步**: Genesys和SIP状态同步
- **错误处理**: 完善的错误处理和重试机制
- **日志记录**: 详细的呼叫日志记录

## 测试策略

### JMeter性能测试
1. **安装JMeter 5.4+**
2. **运行测试脚本**:
   ```bash
   cd jmeter-tests
   jmeter -n -t genesys-test-plan.jmx -l results.jtl
   ```
3. **查看结果**: 生成HTML报告分析性能数据

### LoadRunner集成
- **测试场景设计**: 呼叫负载、座席并发等场景
- **性能指标收集**: 响应时间、吞吐量、错误率
- **结果分析**: 性能瓶颈识别和优化建议

### 测试场景
- **呼叫API负载测试**: 50并发用户，10分钟持续时间
- **Genesys集成测试**: 20并发用户，5分钟持续时间
- **数据库性能测试**: 复杂查询性能验证
- **WebSocket压力测试**: 实时通信性能测试
- **端到端呼叫测试**: 完整呼叫流程测试

### 性能指标
- **响应时间**: API平均响应时间 < 500ms
- **吞吐量**: 支持100+并发呼叫
- **错误率**: < 1%
- **系统可用性**: 99.9%

### Kazimir日志分析测试
- **日志收集**: 自动收集系统日志
- **错误检测**: 自动识别和分类错误
- **性能分析**: 系统性能趋势分析
- **问题诊断**: 自动问题检测和告警

## 日志和监控

### Kazimir日志分析器
- **错误检测**: 自动检测和分类系统错误
- **性能分析**: 系统性能趋势和瓶颈分析
- **呼叫分析**: 呼叫模式和质量分析
- **座席行为分析**: 座席活动和工作模式分析
- **系统健康检查**: 自动生成系统健康报告
- **问题诊断**: 自动问题检测和故障排除建议

### 日志文件
- `logs/genesys-example.log`: 主应用日志
- `logs/kazimir/analysis_YYYY-MM-DD.log`: 分析结果日志
- `logs/kazimir/errors_YYYY-MM-DD.log`: 错误日志
- `logs/kazimir/performance_YYYY-MM-DD.log`: 性能日志

### 实时监控
- **呼叫监控**: 实时呼叫状态和统计
- **座席监控**: 座席状态和活动监控
- **系统监控**: CPU、内存、数据库连接等
- **性能监控**: 响应时间、吞吐量监控

### 健康检查端点
- `GET /actuator/health`: 系统健康状态
- `GET /actuator/info`: 应用信息
- `GET /actuator/metrics`: 性能指标
- `GET /api/monitoring/system-health`: 详细健康报告`

## 配置说明

### 应用配置 (application.yaml)

```yaml
server:
  port: 8060  # 后端端口固定为8060

spring:
  application:
    name: Genesys_Example

  # MySQL数据库配置
  datasource:
    url: jdbc:mysql://localhost:3306/genesys_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    username: root
    password: 7G0&796R6s9E
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQLDialect
        format_sql: true

  # Redis配置
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 60000

# Genesys配置
genesys:
  gcti:
    server-url: ws://localhost:8060/gcti
    username: admin
    password: password
  composer:
    api-url: http://localhost:8060/composer
  sip:
    server-url: sip.genesys.com
    port: 5060
    username: genesys_user
    password: genesys_pass
  logging:
    level: DEBUG

# 日志配置
logging:
  level:
    com.gfo.demo: DEBUG
    org.springframework.data.redis: DEBUG
  file:
    name: logs/genesys-example.log
  pattern:
    file: '%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n'

# 管理端点
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,loggers
```

## 项目结构

```
src/main/java/com/gfo/demo/
├── config/                 # 配置类
├── controller/             # REST API控制器
├── entity/                 # JPA实体类
├── genesys/                # Genesys集成服务
│   ├── GenesysSDKService.java      # Genesys SDK集成
│   ├── SIPPhoneService.java        # SIP电话服务
│   ├── RealCallService.java        # 真实呼叫服务
│   ├── WebSocketCallHandler.java   # WebSocket处理器
│   ├── KazimirLogAnalyzer.java     # 日志分析器
│   └── GenesysConfig.java          # Genesys配置
├── repository/             # 数据访问层
│   ├── CallRecordRepository.java   # 呼叫记录仓库
│   └── AgentPerformanceRepository.java # 座席绩效仓库
└── service/                # 业务逻辑服务

src/main/resources/
├── static/                 # 静态资源
├── templates/              # 模板文件
└── application.yaml        # 配置文件
```

### 构建和运行

```bash
# 清理和构建
mvn clean install

# 运行应用
mvn spring-boot:run

# 运行测试
mvn test

# 打包应用
mvn package

# 跳过测试打包
mvn package -DskipTests
```

### WebSocket实时通信

```javascript
// 座席连接WebSocket
const socket = new WebSocket('ws://localhost:8060/ws/call-center?userType=agent');

// 发送座席登录消息
socket.send(JSON.stringify({
    action: 'LOGIN',
    data: {
        agentId: 'AGENT_001',
        stationId: 'STATION_001'
    }
}));

// 发送呼叫消息
socket.send(JSON.stringify({
    action: 'MAKE_CALL',
    data: {
        phoneNumber: '+8613812345678',
        customerId: 'CUST_001'
    }
}));
```

## 部署和运维

### 生产环境考虑

1. **数据库连接池**: 配置最优连接池大小
2. **Redis高可用**: 设置Redis集群
3. **负载均衡**: 应用部署在负载均衡器后
4. **监控集成**: 集成APM监控工具
5. **安全配置**: 启用HTTPS和Spring Security
6. **Genesys平台**: 配置生产环境Genesys连接

### Docker部署

```dockerfile
FROM openjdk:21-jre-slim

# 设置工作目录
WORKDIR /app

# 复制应用jar包
COPY target/genesys-example.jar app.jar

# 暴露端口
EXPOSE 8060

# 启动应用
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 容器编排 (docker-compose.yml)

```yaml
version: '3.8'
services:
  genesys-app:
    build: .
    ports:
      - "8060:8060"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - mysql
      - redis

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: rootpass
      MYSQL_DATABASE: genesys_db
    ports:
      - "3306:3306"

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
```

### 启动顺序

1. **数据库初始化**
2. **Redis服务启动**
3. **Genesys平台连接**
4. **SIP服务器注册**
5. **应用服务启动**
6. **健康检查验证**

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

## 故障排除

### 常见问题

#### 1. Genesys连接失败
- **检查网络连接**: 确认到Genesys平台的网络连通性
- **验证配置信息**: 检查用户名、密码、服务器地址
- **查看日志**: 使用KazimirLogAnalyzer分析错误日志
- **防火墙设置**: 确认端口和协议未被阻止

#### 2. SIP注册失败
- **检查SIP服务器状态**: 确认SIP服务器运行正常
- **验证认证信息**: 检查用户名和密码
- **网络连通性**: 确认到SIP服务器的网络连接
- **防火墙配置**: 确认SIP端口(5060)开放

#### 3. 数据库连接问题
- **MySQL服务状态**: 确认MySQL服务正常运行
- **连接参数验证**: 检查数据库URL、用户名、密码
- **连接池状态**: 监控连接池使用情况
- **SQL错误分析**: 查看详细的SQL错误信息

### 性能优化

#### 数据库优化
- **索引优化**: 为查询字段添加合适索引
- **查询优化**: 使用JPA查询优化复杂查询
- **连接池调优**: 调整HikariCP连接池参数

#### 缓存优化
- **缓存策略**: 调整不同数据的缓存时间
- **缓存清理**: 定期清理过期缓存
- **缓存监控**: 监控缓存命中率和性能

## 安全特性

### 认证授权
- **Spring Security**: 应用安全框架
- **API认证**: Bearer Token认证机制
- **角色权限**: 座席、管理员角色分离
- **接口权限**: 细粒度接口访问控制

### 数据安全
- **敏感信息加密**: 密码、密钥等敏感数据加密存储
- **SQL注入防护**: 使用参数化查询防止SQL注入
- **XSS防护**: 输入验证和输出编码
- **CSRF防护**: 跨站请求伪造防护

## 版本历史

### v1.0.0 (当前版本)
- ✅ Genesys SDK完整集成
- ✅ 真实电话呼叫功能 (SIP协议)
- ✅ WebSocket实时通信
- ✅ 完整的数据库设计
- ✅ Kazimir日志分析器
- ✅ 座席绩效管理系统
- ✅ RESTful API设计
- ✅ 中文注释完整
- ✅ 测试工具集成支持
- ✅ 生产环境部署配置

## 未来规划

### 短期计划
- [ ] 前端界面实现
- [ ] 负载测试脚本完善
- [ ] 性能监控面板
- [ ] 自动化部署脚本

### 中期计划
- [ ] AI智能客服集成
- [ ] 语音识别功能
- [ ] 情感分析
- [ ] 移动端支持

## 联系我们

如有问题或建议，请联系开发团队：
- 提交Issue到代码仓库
- 查看项目文档
- 参考API示例代码

---

**备注**: 本系统严格按照Genesys集成规范开发，支持真实电话呼叫，具备完整的呼叫中心功能。所有代码均包含详细的中文注释，便于维护和扩展。