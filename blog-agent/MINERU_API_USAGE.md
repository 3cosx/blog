# MinerU API 集成说明

## 概述

本项目集成了两种 MinerU API 调用方式：

1. **同步文件解析 API** (`/file_parse`) - 默认方式
2. **异步任务 API** (`/api/v4/extract/task`) - 新增方式

## 配置说明

在 `application.yml` 中配置 MinerU 相关参数：

```yaml
mineru:
  api-key: your-api-key-here  # MinerU API Key
  api-url: https://mineru.net  # API 基础 URL
  model-version: vlm  # 模型版本：vlm 或 pipeline
  use-async-api: false  # 设置为 true 使用异步任务 API，false 使用同步文件解析 API
  connect-timeout: 60000  # 连接超时（毫秒）
  response-timeout: 300000  # 响应超时（毫秒）
```

## API 调用方式对比

### 1. 同步文件解析 API（默认）

**优点：**
- 简单直接，一次请求完成
- 适合小文件和快速处理场景

**缺点：**
- 长时间占用连接
- 大文件可能超时

**使用方式：**
```yaml
mineru:
  use-async-api: false
```

### 2. 异步任务 API（新增）

**优点：**
- 不会长时间占用连接
- 适合大文件和长时间处理
- 可以轮询任务状态

**缺点：**
- 实现相对复杂
- 需要先将文件上传到可访问的 URL

**使用方式：**
```yaml
mineru:
  use-async-api: true
```

## 异步任务 API 工作流程

1. **上传文件到 MinIO** - 获取可访问的文件 URL
2. **提交提取任务** - 调用 `/api/v4/extract/task` 创建任务
3. **轮询任务结果** - 定期调用 `/api/v4/extract/result/{task_id}` 检查状态
4. **下载 ZIP 文件** - 任务完成后下载结果
5. **清理临时文件** - 异步删除 MinIO 上的临时文件

## API 响应格式

### 提交任务响应
```json
{
  "code": 0,
  "data": {
    "task_id": "a90e6ab6-44f3-4554-b4***"
  },
  "msg": "ok",
  "trace_id": "c876cd60b202f2396de1f9e39a1b0172"
}
```

### 轮询结果响应
```json
{
  "code": 0,
  "data": {
    "status": "completed",  // completed, failed, processing
    "result_url": "https://...",
    "error_msg": null
  },
  "msg": "ok",
  "trace_id": "..."
}
```

## 使用示例

### curl 示例（异步任务 API）

```bash
# 1. 提交提取任务
curl --location --request POST 'https://mineru.net/api/v4/extract/task' \
--header 'Authorization: Bearer YOUR_API_KEY' \
--header 'Content-Type: application/json' \
--header 'Accept: */*' \
--data-raw '{
    "url": "https://your-domain.com/path/to/file.pdf",
    "model_version": "vlm"
}'

# 2. 轮询任务结果
curl --location --request POST 'https://mineru.net/api/v4/extract/result/TASK_ID' \
--header 'Authorization: Bearer YOUR_API_KEY' \
--header 'Content-Type: application/json' \
--header 'Accept: */*'
```

## 注意事项

1. **文件 URL 可访问性** - 使用异步 API 时，MinerU 需要能够访问文件 URL
2. **MinIO 公共访问** - 确保 MinIO bucket 设置为公共读，或者使用预签名 URL
3. **超时配置** - 根据文件大小和处理时间调整超时参数
4. **轮询间隔** - 默认 10 秒轮询一次，最多轮询 60 次（10 分钟）
5. **临时文件清理** - 异步 API 会自动清理 MinIO 上的临时文件

## 切换 API 方式

只需修改配置文件中的 `use-async-api` 参数即可切换：

```yaml
# 使用同步 API
mineru:
  use-async-api: false

# 使用异步 API
mineru:
  use-async-api: true
```

无需修改业务代码，系统会自动根据配置选择对应的 API 调用方式。
