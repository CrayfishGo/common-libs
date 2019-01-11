package common.utils;

/**
 * @auther Yang peng <yangpeng01@credit.com>
 * @date 2016/4/30
 * <p>
 * 正则表达式
 */
public final class RegExpression {

    /**
     * 手机号码校验正则
     */
    public static final String REG_MOBILE = "^(13[0-9]|14[0-9]|15[0-9]|17[0-9]|18[0-9])\\d{8}$";

    /**
     * 邮箱校验正则
     */
    public static final String REG_EMAIL = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})$";

    /**
     * 中文姓名校验正则
     */
    public static final String REG_CHINESE_NAME = "^[\\u4e00-\\u9fa5]+(·[\\u4e00-\\u9fa5]+)*$";

}
