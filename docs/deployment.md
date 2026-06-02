# 云服务部署说明

## 部署平台

本项目使用 Railway 部署。

选择原因：

- 支持 GitHub 自动部署。
- 支持 Dockerfile 构建。
- 支持 MySQL 数据库服务。
- 适合前后端分离项目。

## 服务结构

- frontend：静态前端服务，目录为 `frontend`。
- backend：Spring Boot 后端服务，目录为 `backend`。
- MySQL：Railway 数据库服务。

## 部署配置

项目根目录提供 `railway.toml`，前端和后端分别使用各自目录下的 Dockerfile 构建。

Railway 服务配置：

- backend 服务 Root Directory：`backend`
- frontend 服务 Root Directory：`frontend`
- 部署分支：`main`

## 环境变量

后端服务需要配置：

```env
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}
SERVER_PORT=9099
NODE_ENV=production
API_KEY=not-used
```

说明：作业要求中的 `DATABASE_URL` 在本项目中对应 Spring Boot 使用的 `SPRING_DATASOURCE_URL`。

## 前端接口配置

后端部署完成后，将 `frontend/config.js` 中的 `API_BASE` 修改为后端线上地址：

```js
window.APP_CONFIG = {
  API_BASE: "https://your-backend.up.railway.app"
};
```

修改后提交并推送到 `main` 分支，Railway 会自动重新部署前端。

## 自动部署

Railway 连接 GitHub 仓库后，将部署分支设置为 `main`。每次向 `main` 分支推送代码后，Railway 会自动触发构建和部署。

## 数据库初始化

使用 Railway MySQL 的连接信息导入初始化脚本：

```powershell
mysql -h MYSQLHOST -P MYSQLPORT -u MYSQLUSER -p MYSQLDATABASE < backend/init_reservation_demo.sql
```

其中 `MYSQLHOST`、`MYSQLPORT`、`MYSQLUSER`、`MYSQLDATABASE` 替换为 Railway MySQL 服务 Variables 页面中的实际值。

## 在线地址

- 前端地址：https://your-frontend.up.railway.app
- 后端地址：https://studyroom-booking-production.up.railway.app
- 后端健康检查：https://studyroom-booking-production.up.railway.app/health

## 部署步骤

1. 在 Railway 新建项目，并连接 GitHub 仓库。
2. 添加 MySQL 数据库服务。
3. 添加 backend 服务，Root Directory 设置为 `backend`。
4. 在 backend 服务中配置数据库和生产环境变量。
5. 导入 `backend/init_reservation_demo.sql` 初始化数据库。
6. 为 backend 服务生成 Public Domain。
7. 修改 `frontend/config.js` 中的 `API_BASE` 为后端线上地址。
8. 添加 frontend 服务，Root Directory 设置为 `frontend`。
9. 为 frontend 服务生成 Public Domain。
10. 访问前端线上地址，验证页面和接口请求是否正常。
