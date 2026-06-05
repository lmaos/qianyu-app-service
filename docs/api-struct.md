@ApiController 对象返回结构 
```json
{
  "status": 0,
  "state": "OK",
  "content": {}, 
  "message": "OK"
}
```

| 字段 | 说明 |
| --- | --- |
| requestId | 请求 ID，唯一标识一次请求 |
| status | 状态码，0 表示成功，非 0 表示失败 |
| state | 状态文本，通常与 status 对应 |
| content | 响应内容，通常为业务数据 |
| message | 响应消息，通常为状态文本的补充说明 |

---

举例说明。

```java

@ApiController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/profile")
    public UserProfile profile() {
        return new UserProfile();
    }
}

public class UserProfile {
    private String username;
    private String email;
    // 省略 getter/setter
}

```

应答结构: 

```json
{
  "requestId": "xxx",
  "status": 0,
  "state": "OK",
  "content": {
    "username": "john_doe",
    "email": ""
  },
  "message": "OK"
}
```

---

HTTP 状态码与其他通用状态的说明： 

```X
public static final int A_ERROR_STATUS = 403; // 登录之后，403，权限异常 需要退出登录
public static final int U_ERROR_STATUS = 401; // 登陆之前，登陆失败异常的状态。
public static final int P_ERROR_STATUS = 400; // 参数错误
public static final int R_ERROR_STATUS = 422; // 结果错误 - 业务状态码, 业务执行逻辑中触发的。
public static final int S_ERROR_STATUS = 500; // 系统错误状态
public static final int L_ERROR_STATUS = 429; // 访问限流，限制访问
public static final int F_ERROR_STATUS = 503; // 下游服务异常
```


```X
	OK(200, 0, "OK", "一个成功的请求"),

	NOT_FOUND(404, 404, "Not Found", "页面不存在"),
	SYSTEM_ERROR(HttpStatusValue.S_ERROR_STATUS, 500, "system error", "系统发生不确定异常需要管理员查看"),
	ILLEGAL_STATE(HttpStatusValue.S_ERROR_STATUS, 500, "Illegal State", "非法状态"),

	// 权限相关
    /** 403 无权限访问， 登录之后， 权限不足，需要退出登录 */
	AUTH_NO_PERMISSION(HttpStatusValue.A_ERROR_STATUS, 100001, "权限验证失败"),
    /** 401 登录的时候，登录失败 */
	AUTH_LOGIN_FAIL(HttpStatusValue.U_ERROR_STATUS, 100002, "登录失败", "多用于登录的时候"),
    /** 403 TOKEN失效，退出登录 */
	AUTH_TOKEN_INVALID(HttpStatusValue.A_ERROR_STATUS, 100003, "token 失效"),


    /** 被拒绝访问 */
    A_ACCESS_DENIED(HttpStatusValue.A_ERROR_STATUS, 100005, "access denied", "访问被拒绝, 没有权限访问"),
    /** 限制访问， 限流 */
    L_ACCESS_LIMITED(HttpStatusValue.L_ERROR_STATUS, 100006, "access limited", "访问受限, 请求过于频繁被限制访问"),

    // 参数相关

	P_ERROR(HttpStatusValue.P_ERROR_STATUS, 300001, ""),

	P_NOTNULL(HttpStatusValue.P_ERROR_STATUS, 300002, "", "请求时携带参数值不可以是空"),

	P_NOTZERO(HttpStatusValue.P_ERROR_STATUS, 300003, "", "请求时携带参数值必须大于0的值"),

	P_VALUE_ERROR(HttpStatusValue.P_ERROR_STATUS, 300004, "", "请求时携带的参数值验证过程值内容不通过"),

	// 结果相关

	R_ERROR(HttpStatusValue.R_ERROR_STATUS, 400001, "", "结果异常段 400000 < status < 500000"),

	R_OPERATION_FAIL(HttpStatusValue.R_ERROR_STATUS, 400002, "operation failure", "通用的操作失败返回结果"),

	R_NOEXIST_DATA(HttpStatusValue.R_ERROR_STATUS, 400003, "data does not exist", "数据不存在, 查询更新数据时候, 没有数据可以返回这个错"),

	R_EXIST_DATA(HttpStatusValue.R_ERROR_STATUS, 400004, "data already exists", "数据已存在, 新建,插入,保存数据时,已经存在同样内容时的错"),
    /** 访问频繁 */
	L_FREQUENT_ACCESS(HttpStatusValue.L_ERROR_STATUS, 400005, "frequently accessed", "操作过于频繁, 或请求太频繁, 一般使用: 请求限制一段时间内只能请求一次,却请求了更多次"),
	/** IP限制 */
	L_IP_LIMIT(HttpStatusValue.L_ERROR_STATUS, 400006, "IP limit", "接口请求时 限制ip访问次数, 或限制ip不允许访问时使用这个状态"),

	/** 活动暂未开始 */
	R_NOT_START(HttpStatusValue.R_ERROR_STATUS, 400007, "not started", "没有启动, 没有开始, 或者开始时间未到"),
	/** 活动已经开始 */
	R_ALREADY_START(HttpStatusValue.R_ERROR_STATUS, 400008, "already started", "已经开始, 依然触发启动时发生错误"),
	/** 活动已经结束 */
	R_ALREADY_OVER(HttpStatusValue.R_ERROR_STATUS, 400009, "over.", "已经结束, 或者已经超过使用时间,超过活动时间, 超过业务进行时间"),
	/** 数据已经失效 */
	R_ALREADY_EXPIRED(HttpStatusValue.R_ERROR_STATUS, 400010, "expired.", "数据已经失效, 缓存已经超时, 状态已经超时, 或者无效了"),
	/** 状态标记隐藏 */
	R_STATUS_HIDE(HttpStatusValue.R_ERROR_STATUS, 400011, "", "功能隐藏, 状态标记隐藏"),
	/** 状态标记关闭 */
	R_STATUS_CLOSE(HttpStatusValue.R_ERROR_STATUS, 400012, "", "功能隐藏, 状态标记隐藏"),
	/** 非法的内容 */
	R_ILLEGALITY_CONTENT(HttpStatusValue.R_ERROR_STATUS, 400013, "illegality content", "非法内容"),
	/** 非法的内容 */
	R_ILLEGALITY_TEXT(HttpStatusValue.R_ERROR_STATUS, 400014, "illegality text", "非法内容"),

    // 登陆后，操作业务的时候出现的业务异常。
    /** 账户有什么未知毛病， 使用某个业务的时候出错  */
	R_ACCOUNT_ERROR(HttpStatusValue.R_ERROR_STATUS, 400015, "account error", "账户错误"),
    /** 余额不足了 */
	R_ACCOUNT_LESS_MONEY(HttpStatusValue.R_ERROR_STATUS, 400016, "余额不足", "账户余额不足"),
    /** 账户使用某个功能， 这个功能需要额外签名， 签名失败或者签名无效了。 比如进入某个页面，这个页面需要额外签名. */
	R_ACCOUNT_SIGN_ERROR(HttpStatusValue.R_ERROR_STATUS, 400017, "签名错误", "签名失败"),
    /** 账户不存在， 可能比如查询某个账户， 或者电商账户，或者主播账户，或者非法调用接口的时候， 提醒不存在 */
	R_ACCOUNT_NOT_EXIST(HttpStatusValue.R_ERROR_STATUS, 400018, "账户不存在", "账户不存在"),

    /** 不支持的操作 */
    R_NOT_SUPPORTED(HttpStatusValue.R_ERROR_STATUS, 400020, "not supported", "不支持的操作"),
    /** 被对方拉黑，查看对方数据的时候提醒 */
	R_BLACKLIST(HttpStatusValue.R_ERROR_STATUS, 400021, "账户被拉黑", "请求查看用户信息或其他对当前用户操作行为时，被这个用户拉黑了则无法使用这个业务"),

    // 用户相关， 多用于登录之前， 参数校验， 错误 401；
	U_INFO_ERROR(HttpStatusValue.U_ERROR_STATUS, 200001, "用户信息错误"),

	U_REGISTER_FAIL(HttpStatusValue.U_ERROR_STATUS, 200002, "用户注册失败", "通用的注册失败"),

	U_EXIST_ACCOUNT(HttpStatusValue.U_ERROR_STATUS, 200003, "已存在账户"),

    /** 多用于用户登录时， 用户已经被系统冻结了，401 */
	U_FREEZE(HttpStatusValue.U_ERROR_STATUS, 200005, "账户被冻结", "账户发生违规行为被官方标记冻结状态"),

    /** 多用于登录之后被冻结，用户已经登录中被系统冻结了，无权限操作了。 错误 403 */
    A_FREEZE(HttpStatusValue.A_ERROR_STATUS, 200005, "账户已被冻结", "账户发生违规行为被官方标记冻结状态"),

    /** 下游服务故障，异常 */
    F_SERVICE_UNAVAILABLE(HttpStatusValue.F_ERROR_STATUS, 500003, "服务不可用", "调用的某个服务不可用"),
    /** 请求某个业务，或者使用某个RPC业务发生异常。 */
    F_BUSINESS_REQUEST_FAIL(503, 500004, "业务请求失败", "业务请求失败"),
	;
```