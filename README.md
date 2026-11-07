# AppPush

多厂商移动端消息推送服务。对外提供统一的 HTTP 接口，内部根据 `manufacturer` 字段路由到 Apple APNs、华为 HMS、小米、OPPO、vivo 等厂商的推送通道。

单体 Spring Boot 应用，不依赖注册中心与配置中心。

---

## 技术栈

| 组件 | 版本 |
|---|---|
| Java | 8 |
| Spring Boot | 2.5.6 |
| 构建工具 | Maven 3.8+ |
| HTTP 客户端 | OkHttp 3.12.13 |
| JSON | fastjson 1.2.83 / Jackson |
| JWT | jjwt 0.9.1（APNs ES256） |
| 其他 | Lombok、Hibernate Validator、springfox |

---

## 目录结构

```
AppPush/
├── pom.xml
├── conf/
│   ├── AuthKey_SN7KFQH94H.dev.p8  # APNs 私钥（dev）
│   └── application-local.yml      # 本地密钥，已 gitignore，需自行创建
├── src/main/java/top/wprop/app/push/
│   ├── AppPushApplication.java
│   ├── annotation/                # 自定义参数校验注解
│   ├── common/AjaxResult.java     # 统一响应体
│   ├── constant/HttpCode.java     # 业务状态码
│   ├── controller/PushController.java
│   ├── exception/                 # 全局异常处理
│   ├── factory/PushServiceFactory.java
│   ├── model/dto/MessageDTO.java
│   └── service/impl/              # 各厂商实现
└── src/main/resources/
    ├── application.yml            # 主配置
    └── logback.xml
```

---

## 快速开始

### 1. 环境要求

- **JDK 8**（必须。项目使用 Lombok 1.18.18，在 JDK 17 下会编译失败）
- Maven 3.8+

```bash
export JAVA_HOME=/path/to/jdk8
mvn -v
```

### 2. 配置密钥

密钥不写入 `application.yml`，通过以下两种方式之一提供：

**方式 A：本地配置文件（推荐用于开发）**

创建 `conf/application-local.yml`：

```yaml
push:
  huawei:
    clientSecret: <your-huawei-client-secret>
  xiaomi:
    appSecret: <your-xiaomi-app-secret>
  oppo:
    appKey: <your-oppo-app-key>
    masterSecret: <your-oppo-master-secret>
  vivo:
    appId: <your-vivo-app-id>
    appKey: <your-vivo-app-key>
    appSecret: <your-vivo-app-secret>
```

该文件已加入 `.gitignore`，通过 `spring.config.import` 自动加载。

**方式 B：环境变量**

```bash
export HUAWEI_CLIENT_SECRET=...
export XIAOMI_APP_SECRET=...
export OPPO_APP_KEY=...
export OPPO_MASTER_SECRET=...
export VIVO_APP_ID=...
export VIVO_APP_KEY=...
export VIVO_APP_SECRET=...
```

未提供上述任一值时，应用启动会因占位符无法解析而失败（fail-fast）。

### 3. 构建

```bash
mvn clean package -DskipTests
```

产物：`target/AppPush-0.0.1-SNAPSHOT.jar`

### 4. 启动

```bash
# 开发环境（默认 dev，APNs 走 sandbox）
java -jar target/AppPush-0.0.1-SNAPSHOT.jar

# 生产环境（APNs 走正式地址）
java -jar target/AppPush-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

**注意**：必须在项目根目录（或 jar 所在目录）启动，因为 APNs 私钥使用相对路径 `./conf/...`。

服务端口：`6668`

---

## 配置说明

| 配置项 | 说明 |
|---|---|
| `server.port` | 服务端口，默认 6668 |
| `spring.profiles.active` | 默认 `dev`；`push.apple.*` 的地址与私钥路径随 profile 切换 |
| `local.push.apple.dev.*` | dev 的 APNs 私钥路径与推送地址（sandbox） |
| `local.push.apple.prod.*` | prod 的 APNs 私钥路径与推送地址（正式） |
| `push.apple.keyId` / `teamId` | APNs 密钥标识 |
| `push.apple.appPackageName` | iOS App 的 Bundle ID，即 APNs `apns-topic` |
| `push.huawei.clientId` | 华为应用 Client ID |
| `push.xiaomi.appPackageName` | 小米 `restricted_package_name` |
| `push.xiaomi.channelIdIm` / `channelIdVoip` | 小米 IM / VOIP 通道 ID |
| `push.oppo.appKey` / `masterSecret` | OPPO 应用凭据 |
| `push.vivo.appId` / `appKey` / `appSecret` | vivo 应用凭据 |

---

## API

### POST /push

推送一条消息到指定设备。

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `manufacturer` | string | 是 | `apple` / `huawei` / `xiaomi` / `oppo` / `vivo` |
| `deviceToken` | string | 是 | 设备推送 Token / Registration ID |
| `category` | string | 是 | `im`（文本消息）/ `voip`（音视频通话） |
| `title` | string | 是 | 通知标题 |
| `body` | string | 是 | 通知内容 |
| `data` | object | 否 | 业务自定义数据 |

**示例**

```bash
curl -X POST http://localhost:6668/push \
  -H "Content-Type: application/json" \
  -d '{
    "manufacturer": "xiaomi",
    "deviceToken": "abc123",
    "category": "im",
    "title": "新消息",
    "body": "你有一条新消息",
    "data": {"conversationId": "1001"}
  }'
```

**响应**

```json
{"code": 0, "msg": "操作成功"}
```

```json
{"code": 500, "msg": "manufacturer 不能为空"}
```

> HTTP 状态码恒为 200，业务结果以 `code` 为准：`0` 成功，其余为失败。

---

## 厂商支持

| 厂商 | manufacturer | 实现类 | 状态 |
|---|---|---|---|
| 小米 | `xiaomi` | `XiaomiPushServiceImpl` | 可用 |
| 华为 | `huawei` | `HuaweiPushServiceImpl` | 可用（见已知问题） |
| Apple | `apple` | `ApplePushServiceImpl` | 可用（需 `.p8` 私钥） |
| OPPO | `oppo` | `OppoPushServiceImpl` | 配置已修复，需真实凭据 |
| vivo | `vivo` | `VivoPushServiceImpl` | **未注册到工厂，调用会返回「该厂商暂未开发」** |

---

## 部署

1. 将 `AppPush-0.0.1-SNAPSHOT.jar` 与 `conf/` 目录放在同一工作目录下
2. `conf/` 至少包含 APNs 私钥；生产环境需提供 `AuthKey_SN7KFQH94H.prod.p8`（当前仓库中缺失）
3. 通过环境变量或 `conf/application-local.yml` 注入厂商凭据
4. 启动：

```bash
java -jar AppPush-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

日志输出到 `logs/app-push/`（info / debug / error 三个文件，保留 3 天）。

---

## 已知问题 / TODO

- vivo 实现类未注册到 `PushServiceFactory`
- 华为：token 刷新后首次请求缺少 `Bearer ` 前缀；`data` 使用 Map.toString 而非 JSON
- 小米：`data` 为空时 NPE；payload 被 URL 编码两次
- Apple：自定义数据放在 `aps.alert` 内，应按规范放在根对象
- OPPO：通知分类硬编码为 `IM`
- 各实现未关闭 OkHttp `Response` 连接
- `PushController` 自行捕获异常，导致业务错误码被统一成 500
- springfox 依赖存在但未启用 Swagger
- 无单元测试

---

## 版本

`0.0.1-SNAPSHOT`
