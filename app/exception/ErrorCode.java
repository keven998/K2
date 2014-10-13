package exception;

/**
 * 错误代码
 *
 * @author Zephyre
 */
public class ErrorCode {
    public static int NORMAL = 0;
    public static int INVALID_ARGUMENT = 100;
    public static int INVALID_OBJECTID = 110;
    public static int DONOTNEED_UPDATE = 120;
    public static int INVALID_CONFARG = 130;
    public static int DATABASE_ERROR = 200;
    public static int UNSUPPORTED_OP = 800;
    public static int UNKOWN_ERROR = 900;
    public static int AUTHENTICATE_ERROR = 300;

    /**
     * 数据已存在的错误
     */
    public static int DATA_EXIST = 401;
    public static int DATA_NOT_EXIST = 402;

    /**
     * 注册时验证码错误
     */
    public static int CAPTCHA_ERROR = 403;


}
