package core.user;

import java.util.Map;

/**
 * 生成注册时的验证码内容。
 *
 * @author Zephyre
 */
public class SignupValFormatter implements ValFormatter {

    @Override
    public String format(int countryCode, String tel, String valCode, long expireMs, Map<String, Object> misc) {
        return String.format("%s（桃子旅行注册验证码），请在%d分钟内完成注册。如非本人操作，请忽略。【桃子旅行】", valCode,
                expireMs / 60000);
    }
}