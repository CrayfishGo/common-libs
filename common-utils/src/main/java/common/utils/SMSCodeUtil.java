package common.utils;

import java.util.Random;

/**
 * @date 2016年4月15日
 */

public class SMSCodeUtil {

    public static String generateSMSCode() {
        return getSix();
    }

    private static String getSix() {
        Random rad = new Random();
        String result = rad.nextInt(1000000) + "";
        if (result.length() != 6) {
            return getSix();
        }
        return result;
    }

}
