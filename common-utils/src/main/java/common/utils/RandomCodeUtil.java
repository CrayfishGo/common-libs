package common.utils;

/**
 * Created by Evan on 2017-08-15.
 */
public class RandomCodeUtil {

    private static final int lenth = 6;

    /**
     * 返回随机码
     *
     * @return
     */
    public static String randomCode() {
        String randomcode = "";
        // 用字符数组的方式随机
        String model = "23456789abcdefghjkmnpqrstuvwxyz";
        char[] m = model.toCharArray();
        for (int j = 0; j < lenth; j++) {
            char c = m[(int) (Math.random() * model.length())];
            // 保证六位随机数之间没有重复的
            if (randomcode.contains(String.valueOf(c))) {
                j--;
                continue;
            }
            randomcode = randomcode + c;
        }
        return randomcode;
    }

}
