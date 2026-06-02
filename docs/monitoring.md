# 监控配置说明

## 监控目标

本项目为 Spring Boot 后端配置了基础可观测性能力，覆盖结构化日志、健康检查和基础请求指标，便于部署后快速判断服务是否可用、请求是否异常以及接口响应是否变慢。

## 日志管理

后端使用 `backend/src/main/resources/logback-spring.xml` 配置日志输出：

- 控制台输出 JSON 日志，方便 Railway、Docker 或终端直接采集。
- 文件输出到 `logs/app.log`，并按日期和大小滚动。
- 默认日志级别为 `INFO`，可通过环境变量 `LOG_LEVEL` 调整。

日志字段包括：

```json
{
  "time": "2026-06-02T10:00:00Z",
  "level": "INFO",
  "message": "request completed method=GET path=/health status=200 durationMs=12",
  "logger": "com.mango.monitoring.MonitoringFilter",
  "thread": "http-nio-9099-exec-1",
  "module": "MonitoringFilter"
}
```

## 健康检查端点

健康检查端点为：

```text
GET /health
```

返回示例：

```json
{
  "status": "healthy",
  "timestamp": "2026-06-02T10:00:00+08:00",
  "version": "1.0.0",
  "requests": 10
}
```

部署平台可使用该端点判断服务是否正常运行。生产环境后端示例地址：

```text
https://studyroom-booking-production.up.railway.app/health
```

## 基础指标收集

项目新增 `MonitoringFilter` 和 `MonitoringMetrics` 收集基础请求指标：

- `requestCount`：请求总数
- `errorCount`：HTTP 状态码大于等于 400 的错误请求数
- `errorRate`：错误率
- `averageResponseTimeMs`：平均响应时间
- `maxResponseTimeMs`：最大响应时间
- `activeRequests`：当前活跃请求数

指标查看端点：

```text
GET /metrics
```

返回示例：

```json
{
  "requestCount": 10,
  "errorCount": 1,
  "errorRate": 0.1,
  "averageResponseTimeMs": 18.6,
  "maxResponseTimeMs": 55,
  "activeRequests": 0
}
```

## 错误追踪

当前版本未接入外部错误追踪服务。后续可以接入 Sentry，并通过环境变量配置 DSN：

```text
SENTRY_DSN=your-dsn
```

## 告警建议

可以在部署平台或监控平台配置以下基础告警：

- 服务不可用：连续 3 次访问 `/health` 失败或返回非 2xx 状态码。
- 错误率过高：5 分钟内 `/metrics` 中 `errorRate` 大于 0.05。
- 响应变慢：5 分钟内 `averageResponseTimeMs` 大于 1000。

## 验证方法

本地启动后端后执行：

```powershell
curl http://localhost:9099/health
curl http://localhost:9099/metrics
```

查看终端或 `backend/logs/app.log`，确认日志为 JSON 格式。
