package io.j1st.utils.http.entity;


/**
 * Error Code
 */
@SuppressWarnings("unused")
public class ErrorCode {

    // authorize
    public static final Integer UNAUTHORIZED = 401;   // 未授权
    public static final Integer FORBIDDEN = 403;    //禁止访问

    // validate
    public static final Integer CONFLICT = 409;
    public static final Integer INVALID = 422; //   验证失败

    // other
    public static final Integer INTERNAL = 500; //500=服务器内部错误


    //公用错误码
    public static final Integer PARAMS_VALID = 1001; //1001=参数类型或格式错误
    public static final Integer UNAUTHORIZED_VALID = 1002; //1002=授权失效，请重新登录
    public static final Integer AUTH_VALID = 1003; // 1003=无操作权限


    //用户相关
    public static final Integer USERNAME_VALID = 2001;//    2001=用户名格式不正确
    public static final Integer MOBILE_VALID = 2002;//    2002=手机号格式错误
    public static final Integer EMAIL_VALID = 2003;//    2003=邮箱格式错误
    public static final Integer PASSWORD_VALID = 2004;//    2004=密码格式不正确
    public static final Integer USERNAME_IS_EXISTS = 2005;//    2005=注册失败，用户名已被注册
    public static final Integer MOBILE_IS_EXISTS = 2006;//    2006=注册失败，手机号已存在
    public static final Integer EMAIL_IS_EXISTS = 2007;//    2007=注册失败，邮箱已存在
    public static final Integer VALID_CODE_ERROR = 2009;//    2009=验证码错误
    public static final Integer USER_NOT_EXISTS = 2010;//    2010=登录失败，用户不存在
    public static final Integer PASSWORD_ERROR = 2011;//    2011=登录失败，密码错误
    public static final Integer OLD_PASS_ERROR = 2012;//    2012=用户旧密码错误
    public static final Integer PASSWORD_SAME = 2013;//    2013=新密码与旧密码相同
    public static final Integer SMS_CODE_SEND_FAIL = 2014;//    2014=短信验证码发送失败
    public static final Integer VALID_TIMEOUT = 2015;//    2015=验证超时
    public static final Integer LOGIN_OR_PASSWORD_VALID=2016; //    登录名或密码错误


    //操作设备相关
    public static final Integer AGENT_NOT_EXISTS = 3001;//    3001=绑定设备失败，设备不存在
    public static final Integer USER_BIND_AGENT = 3002;//    3002=绑定设备失败，不能重复绑定
    public static final Integer AGENT_IN_BIND = 3003;//    3003=绑定设备失败，该设备已被其他用户绑定
    public static final Integer USER_NOT_BIND_AGENT = 3004;//    3004=用户未绑定当前设备
    public static final Integer PRODUCT_NOT_EXISTS = 3005;//    3005=你查询的产品不存在
    public static final Integer DEVICE_NOT_EXISTS = 3006;//    3006=设备信息不存在
    public static final Integer PRODUCT_NOT_FNX = 3007;//    3007=产品未配置fnx
    public static final Integer PRODUCT_NAME_EXISTS = 3008;//    3008=产品名称已存在
    public static final Integer PRODUCT_STATUS_SUSPEND = 3009;//    3009 产品为暂停服务状态
    public static final Integer AGENT_DISCONNECTED = 3010;//    设备未连接或已断开
    public static final Integer NAME_IS_NULL = 3011;//    名称不能为null
    public static final Integer AGENT_OFF_LINE = 3012;//    设备已离线



    //operator相关
    public static final Integer OPERATOR_SYSTEM_NAME_EXISTS = 4001;//    4001=system名称已存在
    public static final Integer SYSTEM_WIDGET_NAME_EXISTS = 4002;//    4002=system widget 名称已存在
    public static final Integer SYSTEM_NOT_EXISTS = 4003;//    4003=system Id不存在
    public static final Integer SYSTEM_WIDGET_TYPE_EXISTS = 4004;//    4003=Widget类型已存在
    public static final Integer SYSTEM_AGENT_BIND = 4005;  // agent已存在
    public static final Integer SYSTEM_DATA_EXISTS = 4006;  // system暂无相关数据

    //pong Game相关错误码
    public static final Integer GAME_STATUS_STARTED = 10001;//    10001 游戏进行中
    public static final Integer GAME_IS_FULL = 10002;//    10002 游戏满员
    public static final Integer GAME_NOT_USER = 10003;//    10003 用户未参加游戏



    private ErrorCode() {
    }

}