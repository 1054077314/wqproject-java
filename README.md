# 校园二手交易平台（campus-share）

前后端分离的校园二手商品交易平台，支持商品发布与审核、分类浏览、收藏、预约看货、评论，以及管理员后台（用户管理、商品审核、分类管理、数据统计）。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | React 19 + TypeScript + Vite 6 + Tailwind CSS 4 + React Router 7 |
| 后端 | Spring Boot 3.3.5 + MyBatis + Spring Security + PageHelper |
| 数据库 | MySQL（默认）/ H2（可选 profile） |
| 数据库脚本 | Flyway（自动建表 + 初始化数据） |
| 图片存储 | 本地文件系统（`media/`） |
| 认证 | 自定义 Bearer Token（7 天有效期） |
| 包管理 | 后端 Maven，前端 npm |

## 功能概览

**用户端：**
- 注册 / 登录 / 登出
- 商品列表（分类筛选、分页）
- 商品详情（图片轮播、评论列表）
- 发布商品（多图上传、分类选择）
- 收藏 / 取消收藏（Toggle 设计）
- 预约看货（买家申请/取消 → 卖家确认即成交售出 / 拒绝；取消或拒绝后可再次预约）
- 个人中心（我的商品、我的收藏、我的预约）
- 登录/注册按 IP 限流（可配置）

**管理员后台：**
- 数据统计看板（Chart.js 图表）
- 用户管理（启用/禁用账户）
- 商品审核（通过/驳回 + 驳回原因）
- 分类管理（CRUD、排序）

## 项目结构

```
campus-share/
├── pom.xml                         # Maven / Spring Boot API
├── src/
│   ├── main/
│   │   ├── java/com/campus/
│   │   │   ├── CampusShareApplication.java
│   │   │   ├── user/               # 用户模块（User、Token、认证/管理接口）
│   │   │   ├── product/            # 商品模块（含图片、审核）
│   │   │   ├── category/           # 分类模块
│   │   │   ├── favorite/           # 收藏模块
│   │   │   ├── appointment/        # 预约模块
│   │   │   ├── comment/            # 评论模块
│   │   │   ├── common/             # 统一响应、异常、分页
│   │   │   ├── config/             # Security、MVC、AppProperties
│   │   │   └── security/           # Token 过滤器
│   │   └── resources/
│   │       ├── application.yml     # 主配置（端口 8085，默认 mysql）
│   │       ├── application-mysql.yml
│   │       ├── application-h2.yml
│   │       ├── db/migration/       # Flyway 建表 V1 + 索引 V3
│   │       ├── db/data/            # Flyway 初始化数据 V2
│   │       └── mapper/             # MyBatis XML
│   └── test/                       # API 测试
├── frontend/                       # React SPA（唯一前端）
│   ├── src/
│   │   ├── view/                   # 页面（home/login/products/profile/admin）
│   │   ├── components/             # 公共组件（ProtectedRoute、Layout）
│   │   ├── context/                # AuthContext、ToastContext
│   │   ├── utils/request.ts        # Axios 封装（拦截器自动带 Token）
│   │   └── types/index.ts
│   ├── package.json
│   └── vite.config.ts              # 代理 /api、/media → 8085
├── media/                          # 商品图片上传目录
├── data/                           # 本地开发数据（如 H2 文件）
└── docs/                           # 文档
```

## 快速开始

### 环境要求

- JDK >= 17
- Maven >= 3.9
- Node.js >= 20
- npm >= 10
- MySQL 8（默认 profile）

### 数据库（MySQL）

连接信息通过环境变量或 `application-mysql.yml` 配置，**不要把账号密码写进文档或提交到仓库**。

| 变量 | 说明 |
|------|------|
| `DB_HOST` | 主机（默认本机） |
| `DB_PORT` | 端口（默认 `3306`） |
| `DB_NAME` | 库名（默认 `campus_share`） |
| `DB_USER` | 用户名 |
| `DB_PASSWORD` | 密码 |

首次启动由 Flyway 自动建表并写入初始化数据。

### 后端

```bash
cd campus-share

# 安装依赖并启动（默认 profile=mysql，端口 8085）
mvn spring-boot:run

# 可选：改用 H2
# mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

后端运行在 `http://localhost:8085`，API 前缀 `/api/`。

### 前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器（端口 5173，自动代理 /api 和 /media 到后端 8085）
npm run dev
```

前端运行在 `http://localhost:5173`。

### 生产构建

```bash
# 前端
cd frontend
npm run build   # 输出到 frontend/dist/

# 后端
cd ..
mvn -DskipTests package   # 输出 target/campus-share-1.0.0.jar
```

## API 接口

基础地址：`/api/`  
认证方式：Bearer Token（Header `Authorization: Bearer <token>`）  
统一响应格式：`{"code": 200, "message": "success", "data": ...}`
分页接口的 `data` 为：`{"count": N, "next": "...", "previous": "...", "results": [...]}`

### 用户

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/register` | 无 | 注册 |
| POST | `/api/login` | 无 | 登录，返回 Token + 用户信息 |
| POST | `/api/logout` | 需要 | 删除当前 Token |
| GET | `/api/profile` | 需要 | 获取当前用户信息 |

### 商品

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/products/` | 无 | 商品列表（支持 `?category_id=` 筛选） |
| POST | `/api/products/` | 需要 | 发布商品（JSON 或 multipart） |
| GET | `/api/products/{id}/` | 无 | 商品详情 |
| PUT | `/api/products/{id}/` | 需要（仅卖家） | 修改商品 |
| DELETE | `/api/products/{id}/` | 需要（仅卖家） | 删除商品（软删除） |
| GET | `/api/my-products/` | 需要 | 我的商品列表 |

### 分类

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| GET | `/api/categories/` | 无 | 分类列表 |

### 收藏

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/favorites/` | 需要 | 收藏/取消收藏（Toggle） |
| GET | `/api/my-favorites/` | 需要 | 我的收藏列表 |

### 预约

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/appointments/` | 需要 | 预约看货（取消/拒绝后可重开） |
| PATCH | `/api/appointments/{id}/` | 需要 | 卖家 `confirm`（成交→sold）/`reject`；买家 `cancel`（仅 pending） |
| GET | `/api/my-appointments/as-buyer/` | 需要 | 我作为买家的预约 |
| GET | `/api/my-appointments/as-seller/` | 需要 | 我作为卖家的预约 |

### 评论

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/comments/` | 需要 | 发表评论 |
| GET | `/api/products/{id}/comments/` | 无 | 商品评论列表 |

### 管理员接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/users/` | 用户列表（分页） |
| PUT | `/api/admin/users/{id}/` | 启用/禁用用户 |
| GET | `/api/admin/statistics/` | 数据统计 |
| POST | `/api/admin/categories/` | 创建分类 |
| PUT/DELETE | `/api/admin/categories/{id}/` | 修改/删除分类 |
| GET | `/api/admin/pending-products/` | 待审核商品列表 |
| POST | `/api/admin/products/{id}/review/` | 审核商品（通过/驳回） |

## 数据库设计

```
users (用户)
├── id, username, password, is_active, is_staff, is_superuser, last_login, created_at
│
├── tokens (令牌)
│   └── key(PK), user_id(FK), created_at, expires_at
│
├── products (商品)          ← soft delete (is_deleted)
│   ├── id, title, description, price, contact_info
│   ├── category_id(FK→categories), seller_id(FK→users)
│   ├── status (pending/active/rejected/offline)
│   ├── reject_reason, created_at, updated_at, is_deleted
│   │
│   └── product_images (商品图片)
│       └── id, product_id(FK→products), image, is_deleted, created_at, updated_at
│
├── favorites (收藏)         ← unique(user, product)
│   └── id, user_id(FK), product_id(FK), created_at
│
├── appointments (预约)      ← unique(buyer, product)
│   └── id, buyer_id(FK), product_id(FK), status(pending/confirmed/rejected/cancelled), created_at, updated_at
│
└── comments (评论)
    └── id, user_id(FK), product_id(FK), content, created_at

categories (分类)
└── id, name, sort_order, created_at
```

**关键设计：**
- 商品表 `products` 有 `(status, created_at)` 复合索引，优化列表查询
- 商品、商品图片支持软删除（`is_deleted` 标记，不物理删除）
- 收藏和预约均有唯一约束，防止重复操作；预约取消/拒绝后可重开同一商品
- 商品状态流转：`pending` → `active` → `sold`（确认预约成交）/ `offline`（下架）；审核可 `rejected`
- 预约状态：`pending` → `confirmed`（同时商品 sold、其余 pending 自动 rejected）/ `rejected` / `cancelled`；`confirmed` 后不可取消
- 写操作使用 CAS（`UPDATE … WHERE status=期望值`），冲突返回 409
- 登录/注册接口按 IP 限流（`app.rate-limit-login-per-minute` / `app.rate-limit-register-per-minute`）
- 图片软删时同步清理磁盘文件；收藏列表仅展示在售商品

## 生产部署

可通过 Nginx 反向代理 Spring Boot + 前端静态资源：

```nginx
server {
    listen 80;

    # Spring Boot API
    location /api/ {
        proxy_pass http://127.0.0.1:8085;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 静态媒体文件
    location /media/ {
        proxy_pass http://127.0.0.1:8085;
        # 或直接: alias /opt/campus-share/media/;
    }

    # React SPA 兜底
    location / {
        root /opt/campus-share/frontend/dist;
        try_files $uri $uri/ /index.html;
    }
}
```

后端启动命令：

```bash
cd /opt/campus-share
nohup java -jar target/campus-share-1.0.0.jar \
  --spring.profiles.active=mysql \
  > /opt/campus-share/app.log 2>&1 &
```

## 许可证

MIT
