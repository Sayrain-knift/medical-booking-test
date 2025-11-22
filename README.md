# 医疗预约系统

一个基于Spring Boot的智能医疗预约系统，提供在线挂号、医生科室查询、AI问诊等功能。

## 技术栈

### 后端技术栈
- **核心框架**：Spring Boot 3.x
- **安全认证**：Spring Security + JWT
- **数据访问**：Spring Data JPA
- **数据库**：H2内存数据库（开发环境）
- **缓存**：Redis
- **服务治理**：Nacos（可选）
- **消息队列**：RabbitMQ（可选）
- **流量控制**：Sentinel
- **AI功能**：Spring AI + Ollama
- **API文档**：SpringDoc OpenAPI (Swagger)
- **日志**：SLF4J + Logback

### 前端技术栈
- **基础技术**：HTML5, CSS3, JavaScript
- **UI框架**：Bootstrap 5
- **图标库**：Bootstrap Icons
- **AJAX请求**：原生Fetch API
- **页面模板**：Thymeleaf（服务端渲染）

## 功能模块

1. **用户管理**：注册、登录、个人信息管理
2. **科室管理**：科室列表查询、详情展示
3. **医生管理**：医生信息查询、排班查看
4. **预约挂号**：在线预约、取消预约、预约记录查询
5. **AI问诊**：智能问答、医疗咨询
6. **流量控制**：接口限流、防止滥用

## 本地部署步骤

### 1. 环境准备

- JDK 17 或更高版本
- Maven 3.6+ 或 Gradle 7.0+
- Git
- Redis (可选，用于缓存)
- Ollama (可选，用于AI问诊功能)
- Sentinel Dashboard (可选，用于流量监控)

### 2. 获取代码

```bash
git clone https://github.com/Sayrain-knift/medical-booking-test.git
cd medical-booking-test
```

### 3. 配置修改

根据需要修改以下配置文件：

#### 3.1 数据源配置（application-dev.yml）

默认使用H2内存数据库，可以保持默认配置。如需切换到其他数据库，请修改：

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:medical_booking;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: 
    driver-class-name: org.h2.Driver
```

#### 3.2 Redis配置（application-dev.yml）

如需启用Redis缓存，请修改：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password: 
      database: 0
  cache:
    type: redis  # 将none改为redis启用缓存
```

#### 3.3 AI功能配置（application-dev.yml）

如需使用AI问诊功能，请确保Ollama已安装并运行：

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: tinyllama
        options:
          temperature: 0.8
          top-p: 0.9
          num-predict: 128
```

#### 3.4 JWT配置（application-dev.yml）

建议修改JWT密钥以增强安全性：

```yaml
jwt:
  secret: 自定义密钥，建议使用强随机字符串
  expiration: 86400000 # 24小时
```

### 4. 构建项目

使用Maven构建项目：

```bash
mvn clean package
```

### 5. 启动应用

方式一：使用Maven直接运行

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

方式二：运行打包后的jar文件

```bash
java -jar target/medical-booking-system-1.0.0.jar --spring.profiles.active=dev
```

### 6. 访问应用

- 应用地址：http://localhost:8080
- API文档：http://localhost:8080/swagger-ui.html
- H2数据库控制台：http://localhost:8080/h2-console (用户名: sa, 密码: 空)

## 配置修改要点

### 1. 环境切换

项目支持多环境配置，通过`spring.profiles.active`参数切换：
- dev: 开发环境（默认）
- minimal: 最小化环境配置

### 2. 核心配置项

#### 2.1 数据库配置
- 开发环境默认使用H2内存数据库，无需额外安装
- 生产环境建议切换到MySQL或PostgreSQL等持久化数据库

#### 2.2 缓存配置
- 默认禁用Redis缓存（`cache.type: none`）
- 启用时需确保Redis服务正常运行

#### 2.3 服务治理配置
- Nacos和Sentinel默认禁用
- 需要时可修改`spring.cloud.nacos.discovery.enabled`和`spring.cloud.sentinel.enabled`为true

#### 2.4 文件上传配置
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
```

#### 2.5 服务器配置
```yaml
server:
  port: 8080
  tomcat:
    threads:
      max: 200
      min-spare: 10
```

## 项目更新内容

### 最新更新

1. **AI问诊功能增强**
   - 集成Spring AI框架
   - 支持基于Ollama的本地大模型调用
   - 添加聊天历史记录功能

2. **流量控制优化**
   - 集成Sentinel实现接口限流
   - 为关键接口（登录、注册、预约）添加流控保护
   - 优化流控规则配置

3. **安全增强**
   - 升级JWT工具类，使用更安全的密钥生成方式
   - 修复认证相关潜在安全问题
   - 增强密码加密处理

4. **性能优化**
   - 优化数据库查询
   - 改进Redis缓存策略
   - 调整连接池配置

5. **功能完善**
   - 完善用户角色权限管理
   - 优化预约流程
   - 添加更多测试用例

### 配置文件更新

- 更新了`application-dev.yml`，添加AI配置和流控配置
- 添加`application-minimal.yml`，提供最小化部署配置
- 优化日志配置，调整日志级别和文件输出

## 测试脚本

项目包含多个测试脚本，帮助验证功能：

1. **预约接口流控测试**：`./appointment-flow-demo.sh`
2. **Sentinel功能演示**：`./test-sentinel-demo.sh`
3. **完整功能测试**：`./complete-test.sh`

运行前请确保应用已启动，并给予脚本执行权限：

```bash
chmod +x *.sh
./complete-test.sh
```

## 开发说明

### 代码结构

- `src/main/java/com/sayrain/medicalbooking/` - 主代码目录
  - `controller/` - REST控制器
  - `service/` - 业务逻辑层
  - `repository/` - 数据访问层
  - `model/` - 数据模型
  - `dto/` - 数据传输对象
  - `config/` - 配置类
  - `util/` - 工具类
- `src/main/resources/` - 资源文件
  - `static/` - 静态资源（HTML, CSS, JS）
  - `application*.yml` - 配置文件

### 开发建议

1. 使用开发环境配置进行本地开发：`-Dspring.profiles.active=dev`
2. 充分利用H2数据库的控制台功能进行调试
3. 通过Swagger文档页面测试API接口
4. 开发新功能前先运行现有测试确保兼容性

这次更新完善了main的主要模块功能，依旧由于时间问题，此项目前后端无分离，会在下次更新时采用分离
