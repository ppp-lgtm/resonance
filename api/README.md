# Resonance API — 个人数字简历后端

面向个人数字简历 / 作品集展示的 **Spring Boot 3.3** 后端项目。配合前台 Vue 3 + Tailwind 杂志风前端、后台管理页面使用。

## 技术栈

| 模块 | 选型 |
| --- | --- |
| 语言 | Java 17 |
| 框架 | Spring Boot 3.3.x（Spring Web、Spring Security） |
| 持久化 | Spring Data JPA + Hibernate 6 |
| 数据库 | **H2（默认本地即可跑）** / MySQL 8（生产切换） |
| 认证 | JWT（无状态）+ BCrypt 密码哈希 |
| JSON | Jackson（JavaTime + pretty + NULL 跳过） |
| 跨域 | 全局 CORS 放行 `http://localhost:5173`（可在 `application.yml` 改） |
| 上传 | 本地文件系统兜底，URL 前缀可配（预留后续 MinIO / OSS 替换接口） |

## 快速启动（本地 0 依赖）

默认使用 **H2 内存数据库**，无需安装任何数据库即可跑：

```bash
# 1. 确认 JDK 17 与 Maven 3.9+ 已安装
java -version        # 需要 17
mvn -version         # 需要 3.9+

# 2. 构建 & 启动
cd api
mvn clean spring-boot:run -DskipTests
```

服务启动在 `http://localhost:8080`，首次启动会自动注入样例数据：

- **默认登录账号**：`admin` / `admin123`
- 登录示例：
  ```bash
  curl -X POST http://localhost:8080/api/auth/login \
       -H 'Content-Type: application/json' \
       -d '{"username":"admin","password":"admin123"}'
  ```
  返回：
  ```json
  {"code":0,"msg":"OK","data":{"token":"eyJhbGciOiJI...","expireSeconds":86400,"adminId":1,"displayName":"张明远"}}
  ```
  之后调用管理接口在请求头带：`Authorization: Bearer <token>`

- 首屏聚合接口：`GET http://localhost:8080/api/public/overview`（前台首页直接调即可）

## 切换到 MySQL 8（生产）

1. 先建库 + 执行 DDL（脚本在 `docs/schema.sql`，也可以让 Hibernate 自动 `update` 建表）：
   ```sql
   CREATE DATABASE resonance DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   USE resonance;
   SOURCE docs/schema.sql;   -- 建表 + 默认样例数据（admin/admin123）
   ```
2. 在 `application.yml` 中激活 `mysql` profile（或在启动时加 `--spring.profiles.active=mysql`）
3. 修改：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://127.0.0.1:3306/resonance?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
       username: root
       password: <your_pwd>
   ```
4. 把 `spring.jpa.hibernate.ddl-auto` 从 `create-drop` 改成 `update`（生产别用 create-drop！）

## 主要目录

```
api/
├── pom.xml
└── src/main/
    ├── java/com/zhangmy/resonance/
    │   ├── ResonanceApplication.java
    │   ├── bootstrap/          # 启动初始化（默认账号+样例）
    │   ├── common/             # R、BizException、错误码、JWT 工具
    │   ├── config/             # Security、CORS、Jackson、Storage
    │   └── modules/
    │       ├── auth/           # 登录/登出
    │       ├── entity/         # 8 个 JPA Entity
    │       ├── repo/           # 8 个 Spring Data JPA Repository
    │       ├── profile/        # 个人简介 + 联系方式
    │       ├── skill/          # 技能 CRUD
    │       ├── project/        # 项目作品 CRUD（多图、标签筛选、封面）
    │       ├── timeline/       # 奖项 + 教育经历
    │       ├── pub/            # /api/public/overview 聚合接口
    │       ├── dashboard/      # /api/admin/dashboard/stats
    │       └── upload/         # 图片/PDF 上传（本地存储）
    └── resources/
        └── application.yml
```

## 接口总览

| 大类 | 方法 | 路径 | 鉴权 |
| --- | --- | --- | --- |
| 认证 | POST | `/api/auth/login` | 否 |
| 认证 | POST | `/api/auth/logout` | 是 |
| 认证 | GET  | `/api/auth/me` | 是 |
| 公开 | GET  | `/api/public/overview` | 否（首屏聚合） |
| 公开 | GET  | `/api/public/profile` | 否 |
| 公开 | GET  | `/api/public/skills` | 否 |
| 公开 | GET  | `/api/public/projects` | 否（支持 `?tag=`） |
| 公开 | GET  | `/api/public/projects/{id}` | 否 |
| 公开 | GET  | `/api/public/awards` | 否 |
| 公开 | GET  | `/api/public/education` | 否 |
| 管理 | GET  | `/api/admin/dashboard/stats` | 是 |
| 管理 | GET/POST/PUT/DELETE | `/api/admin/profile`、`/api/admin/contact/*` | 是 |
| 管理 | GET/POST/PUT/DELETE | `/api/admin/skills/*` | 是 |
| 管理 | GET/POST/PUT/DELETE | `/api/admin/projects/*` | 是 |
| 管理 | GET/POST/PUT/DELETE | `/api/admin/awards/*` | 是 |
| 管理 | GET/POST/PUT/DELETE | `/api/admin/education/*` | 是 |
| 上传 | POST | `/api/admin/upload/image` | 是（multipart/form-data） |
| 上传 | POST | `/api/admin/upload/file` | 是（PDF 等） |

上传文件会落到项目根目录下 `./upload/yyyy/MM/`，对外 URL 前缀由 `app.storage.public-url-prefix` 决定（默认 `/static/`，生产用反代到 CDN 或 OSS 绑定的域名）。

## 默认样例数据（H2 自动创建）

- 管理员：`admin` / `admin123`（建议启动后立即去 `/api/admin/profile` 改密码 — 改密码接口稍后补充，可先改数据库直接更新 `password_hash`）
- Profile、联系方式、技能、3 个项目、3 个奖项、2 条教育经历

## 常见问题

1. **启动失败，端口被占用？**
   `application.yml` 中修改 `server.port: 8080` → 其他端口。

2. **JWT 过期了？**
   默认 `24h`，在 `app.jwt.expire-seconds: 86400` 修改。

3. **上传图片后前台看不到？**
   `WebMvcConfig` 已经把 `/static/**` 映射到本地 `./upload/` 了。如果部署到 Nginx/CDN，请把 `app.storage.public-url-prefix` 改成公网可访问的前缀（例如 `https://cdn.yourdomain.com/`），上传接口返回的 `url` 字段会自动带该前缀。

4. **生产需要删除默认样例初始化？**
   把 `SampleDataInitializer` 类的 `@Component` 注释掉即可，或者加上 `@Profile("!prod")` 配合启动 profile。

## 与前台对接要点

- 前台首屏：只需要发 **1 个** 请求 `GET /api/public/overview`，里面聚合了 `profile / skills / projects / awards / education`，减少到首屏请求数、提升 LCP。
- 项目筛选：`GET /api/public/projects?tag=Vue%203`（tag 精确匹配）。
- 管理端所有 `/api/admin/**` 请求都必须携带 `Authorization: Bearer <token>`。
