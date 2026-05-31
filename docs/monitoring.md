# 监控配置说明
## 1. 日志配置
- 结构化JSON日志
- 字段：time、level、message、module
- 输出：控制台

## 2. 健康检查
- 端点：/health
- 返回：status、timestamp、version

## 3. 指标收集
- 实现请求计数、响应时间统计