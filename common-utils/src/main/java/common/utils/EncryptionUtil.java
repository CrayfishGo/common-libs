package common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * Date: 2016/5/18
 * <p>
 * 数据加密工具类 --- 采用AES
 */
public final class EncryptionUtil {

    private static final String GENERATOR = "AES";
    private static final String CHARSET = "UTF-8";
    private static final String DEFAULT_PASSWORD = "P@ssw0rd";


    private static Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @return
     */
    public static String encrypt(String content) {
        return encrypt(content, DEFAULT_PASSWORD);
    }


    /**
     * 加密
     *
     * @param content  需要加密的内容
     * @param password 加密密码
     * @return
     */
    public static String encrypt(String content, String password) {
        try {

            if (StringUtils.isEmpty(password)) {
                password = DEFAULT_PASSWORD;
            }

            KeyGenerator kgen = KeyGenerator.getInstance(GENERATOR);
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, GENERATOR);
            Cipher cipher = Cipher.getInstance(GENERATOR);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byteContent = content.getBytes(CHARSET);
            byte[] result = cipher.doFinal(byteContent);
            return parseByte2HexStr(result);
        } catch (Exception e) {
            logger.error("====> 数据加密出错： " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @return
     */
    public static String decrypt(String content) {
        return decrypt(content, DEFAULT_PASSWORD);
    }

    /**
     * 解密
     *
     * @param content  待解密内容
     * @param password 解密密钥
     * @return
     */
    public static String decrypt(String content, String password) {
        try {

            if (StringUtils.isEmpty(password)) {
                password = DEFAULT_PASSWORD;
            }

            byte[] decryptResult = parseHexStr2Byte(content);
            KeyGenerator kgen = KeyGenerator.getInstance(GENERATOR);
            kgen.init(128, new SecureRandom(password.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, GENERATOR);
            Cipher cipher = Cipher.getInstance(GENERATOR);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(decryptResult);
            return new String(result, CHARSET);
        } catch (Exception e) {
            logger.error("====> 数据解密出错： " + e.getMessage(), e);
        }
        return null;
    }


    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    private static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    private static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1) {
            return null;
        }
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * @param bytes
     * @return
     */
    public static String getMD5(byte[] bytes) {
        String s = null;
        char hexDigits[] = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
                'e', 'f'};
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            s = new String(str);
        } catch (Exception e) {
            logger.error("====> getMD5 string error： " + e.getMessage(), e);
        }
        return s;
    }

}
