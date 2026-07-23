# GitHub 上传与阿里云 ECS 部署命令复盘

> 这份文档记录本项目上传 GitHub、构建、发布到阿里云 ECS、重启验证时用到的核心命令。敏感信息如数据库密码、私钥路径已省略。

## 1. 本地检查与 GitHub 上传

```bash
# 查看当前仓库状态
git status

# 查看远程仓库
git remote -v

# 如远程未配置，则绑定 GitHub 仓库
git remote add origin https://github.com/1054077314/wqproject-java.git

# 暂存代码
git add .

# 提交代码
git commit -m "Harden campus trading workflow"

# 推送到 GitHub main 分支
git push -u origin main
```

后续每轮功能修复和部署资产补充也是同样流程：

```bash
git status
git add .
git commit -m "Add ECS deployment assets"
git push origin main
```

## 2. 本地构建后端与前端

```bash
# 后端：运行测试
mvn clean test

# 后端：打包 Spring Boot jar
mvn -DskipTests package

# 前端：安装依赖
cd frontend
npm install

# 前端：构建生产 dist
npm run build
```

项目最终部署产物：

```text
target/campus-share-1.0.0.jar
frontend/dist/
```

## 3. ECS 初次目录准备

```bash
# 登录 ECS
ssh root@120.26.174.97

# 创建应用目录
mkdir -p /opt/campus-share/app
mkdir -p /opt/campus-share/frontend
mkdir -p /opt/campus-share/logs
mkdir -p /opt/campus-share/media
mkdir -p /etc/campus-share
```

服务器上的核心路径：

```text
/opt/campus-share/app/campus-share-1.0.0.jar
/opt/campus-share/frontend/dist/
/opt/campus-share/media/
/opt/campus-share/logs/app.log
/etc/campus-share/campus-share.env
/etc/systemd/system/campus-share.service
/etc/nginx/sites-available/campus-share
```

## 4. 上传部署文件到 ECS

```bash
# 上传后端 jar
scp target/campus-share-1.0.0.jar root@120.26.174.97:/tmp/campus-share-1.0.0.jar

# 打包并上传前端 dist
tar -czf target/frontend-dist.tgz -C frontend/dist .
scp target/frontend-dist.tgz root@120.26.174.97:/tmp/campus-share-frontend-dist.tgz
```

在服务器替换产物：

```bash
ssh root@120.26.174.97

install -m 0644 /tmp/campus-share-1.0.0.jar /opt/campus-share/app/campus-share-1.0.0.jar

rm -rf /opt/campus-share/frontend/dist.new
mkdir -p /opt/campus-share/frontend/dist.new
tar -xzf /tmp/campus-share-frontend-dist.tgz -C /opt/campus-share/frontend/dist.new
rm -rf /opt/campus-share/frontend/dist
mv /opt/campus-share/frontend/dist.new /opt/campus-share/frontend/dist
```

现在仓库里已经封装了同等流程，后续可以直接执行：

```bash
DEPLOY_HOST=root@120.26.174.97 bash deploy/deploy.sh
```

## 5. systemd 服务配置与重启

```bash
# 复制示例服务文件到 systemd
cp deploy/campus-share.service.example /etc/systemd/system/campus-share.service

# 重新加载 systemd
systemctl daemon-reload

# 设置开机自启
systemctl enable campus-share

# 启动或重启后端服务
systemctl restart campus-share

# 查看服务状态
systemctl status campus-share

# 查看日志
journalctl -u campus-share -n 100 --no-pager
tail -n 100 /opt/campus-share/logs/app.log
```

## 6. Nginx 配置与静态资源代理

```bash
# 复制 Nginx 配置
cp deploy/nginx-campus-share.conf.example /etc/nginx/sites-available/campus-share

# 启用站点
ln -s /etc/nginx/sites-available/campus-share /etc/nginx/sites-enabled/campus-share

# 检查配置
nginx -t

# 重载 Nginx
systemctl reload nginx
```

Nginx 负责：

```text
/        -> React 前端 dist
/api/    -> Spring Boot 8085
/media/  -> /opt/campus-share/media
```

## 7. MySQL 与 Flyway 迁移

首次部署时需要准备数据库和账号：

```sql
CREATE DATABASE campus_share DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'campus_share'@'localhost' IDENTIFIED BY '******';
GRANT ALL PRIVILEGES ON campus_share.* TO 'campus_share'@'localhost';
FLUSH PRIVILEGES;
```

后续发布 V5/V6 种子数据和图片修复前，先备份线上库：

```bash
mysqldump --no-tablespaces -u campus_share -p campus_share > /opt/campus-share/backup-campus-share-20260723163755.sql
```

启动 Spring Boot 后，Flyway 自动执行：

```text
V1__init.sql
V2__seed_data.sql
V3__indexes.sql
V4__audit_logs.sql
V5__enrich_demo_data.sql
V6__fix_demo_images.sql
```

## 8. 部署后验证

```bash
# 检查服务是否存活
systemctl is-active campus-share
systemctl is-active nginx
systemctl is-active mysql

# 后端本机健康检查
curl http://127.0.0.1:8085/actuator/health

# 分类接口
curl http://127.0.0.1:8085/api/categories/

# 公网首页
curl http://120.26.174.97/

# 公网商品分页接口
curl "http://120.26.174.97/api/products/?page=1&page_size=5"
```

线上最终访问地址：

```text
校园二手交易平台：http://120.26.174.97/
后台管理入口：http://120.26.174.97/admin
足球项目：http://120.26.174.97/zuqiujulebguanli/front/index.html
```

## 9. 本轮关键提交记录

```bash
git log --oneline -8
```

对应提交：

```text
aa40ab9 Add admin audit log page
9af5635 Fix enriched demo product images
6cca7f4 Preserve pagination totals after VO mapping
fcc6b9f Enrich demo data and operations support
9c3eee4 Add ECS deployment assets
bc2285c Harden campus trading workflow
96afbc9 Initial commit
```

## 10. 简历面试可讲法

这次部署不是“把代码扔服务器上跑”，而是按 Java 项目生产发布流程做了闭环：

- GitHub 管理代码版本，按功能提交并推送 `main` 分支；
- 本地 Maven 构建 Spring Boot jar，Node 构建 React 静态资源；
- ECS 上用 MySQL + Flyway 管理数据库版本，用 systemd 托管 Java 服务；
- Nginx 统一承载前端、反向代理 API、转发媒体文件；
- 发布后通过 systemctl、日志、curl、页面访问验证服务状态。
