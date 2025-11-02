这本来是预定是一个springboot+vue的前后端分离医疗预约项目，但是由于不可抗力因素，目前只完成了后端
后端技术栈：
  该项目使用了以下技术栈：

## 后端框架与库

- **Spring Boot**: 主要的应用框架
- **Spring Security**: 用于安全认证和授权
- **Spring Data JPA**: 数据持久层框架
- **Spring Web MVC**: Web 层框架
- **Lombok**: 用于简化 Java 代码

## 数据库相关

- **MySQL**: 主数据库（生产环境）
- **H2 Database**: 测试环境内存数据库
- **Redis**: 缓存存储（用于科室和医生数据缓存）

## API 文档

- **SpringDoc OpenAPI**: 用于生成 API 文档（Swagger 替代方案）

## 测试框架

- **JUnit 5**: 单元测试框架
- **Mockito**: Mock 框架，用于单元测试中的模拟对象

## 安全与认证

- **JWT (JSON Web Token)**: 用于用户身份验证
- **JJWT**: JWT 的 Java 实现库

## 其他工具

- **Maven**: 项目构建工具（从 pom.xml 文件推断）
- **Logback**: 日志框架（通过 Spring Boot 默认配置）
- **Hibernate**: ORM 框架（通过 Spring Data JPA）

此项目还加入了springai，具体可参考yml文件中的ai配置，lz用的是ollama下载的小模型。

如果有大佬可以完善下前端的话感激不尽


