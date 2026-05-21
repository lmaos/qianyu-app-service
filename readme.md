# QIANYU 服务器

## 项目结构: 

```X
qianyu-api: 对外暴露的接口
qianyu-core: 公共层
qianyu-service: 业务模块
qianyu-boot: 启动模块

```

### 依赖关系:

```X

启动依赖: qianyu-boot -> qianyu-service -> qianyu-core

调用依赖: qianyu-service -> qianyu-api

```

### 启动模块说明

```
qianyu-boot: 负责启动应用.

qianyu-single-boot: 单体应用启动模块. 依赖全部Service模块， 全部启动.

qianyu-{module}-boot: 模块化启动模块. 依赖单个Service模块， 启动单个模块.


```