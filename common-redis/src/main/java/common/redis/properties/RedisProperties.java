package common.redis.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by Evan
 */
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {

    private String host;
    private String password;
    private int port;
    private int expire;
    private int timeout = 0;

    public int getExpire() {
        return expire;
    }

    public void setExpire(int expire) {
        this.expire = expire;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
