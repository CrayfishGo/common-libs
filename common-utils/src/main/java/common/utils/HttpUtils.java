package common.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Created by Evan on 2017-08-11.
 */
public final class HttpUtils {

    /**
     * @param accessToken
     * @param clientType
     * @return
     */
    public static HttpHeaders getHttpHeaders(String accessToken, String clientType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("accessToken", accessToken);
        headers.set("clientType", clientType);
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        return headers;
    }

    /**
     * @param request
     * @return
     */
    public static String getClientIPAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = ip.indexOf(",");
            if (index != -1) {
                return ip.substring(0, index);
            } else {
                return ip;
            }
        }
        ip = request.getHeader("X-Real-IP");
        if (!StringUtils.isEmpty(ip) && !"unKnown".equalsIgnoreCase(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * 生成accessToken
     *
     * @return
     */
    public static String generateAccessToken() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

}
