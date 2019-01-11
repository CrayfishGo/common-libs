package common.redis.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Evan
 */
@Component()
@DependsOn("redisTemplate")
public class CustomizedRedisTemplate {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 默认过期时间为10天
     */
    private final int defailtExpireTime = 10 * 24 * 60 * 60;

    private final Logger logger = LoggerFactory.getLogger(CustomizedRedisTemplate.class);


    /**
     * @param key
     * @return
     */
    public boolean isContinueKey(final String key) {
        long count = redisTemplate.opsForValue().increment(key, 1);
        boolean isContinue = false;
        if (count <= 1) {
            setExpire(key, 60);
            isContinue = true;
        }
        return isContinue;
    }


    /**
     * @param lockName
     * @return
     * @throws Exception
     */
    public boolean getLock(String lockName) throws Exception {
        String redisKey = lockName;
        long expire = 30;// 单位:秒
        long timeout = 5000;// 单位:毫秒
        long redisValue = System.currentTimeMillis() + timeout + 1;
        // 通过SETNX试图获取一个lock
        if (setNX(redisKey, redisValue, expire)) {// SETNX成功，则成功获取一个锁
            return true;
        }
        // SETNX失败，说明锁仍然被其他对象保持，检查其是否已经超时
        else {
            long oldValue = Long.valueOf(get(redisKey).toString());
            // 超时
            if (oldValue < System.currentTimeMillis()) {
                String getValue = getAndSet(redisKey, redisValue);
                // 获取锁成功
                if (getValue.equals(oldValue)) {
                    return true;
                }
                // 已被其他进程捷足先登了
                else {
                    return false;
                }
            } else {//未超时，则直接返回失败
                return false;
            }
        }
    }

    /**
     * @param lockName
     * @throws Exception
     */
    public void unLock(String lockName) throws Exception {
        delete(lockName);
    }

    private boolean setNX(final String key, final Object value, final long expire) {
        return redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            try {
                byte[] keyBytes = redisTemplate.getStringSerializer().serialize(key);
                boolean locked = connection.setNX(keyBytes, objectMapper.writeValueAsBytes(value));
                if (locked) {
                    connection.expire(keyBytes, expire);
                }
                return locked;
            } catch (Exception e) {
                logger.error("====> CustomizedRedisTemplate.setNX(): " + e.getMessage());
                return false;
            }
        });
    }

    private String getAndSet(final String key, final Object value) {
        return redisTemplate.execute((RedisCallback<String>) connection -> {
            try {
                byte[] result = connection.getSet(key.getBytes(), objectMapper.writeValueAsBytes(value));
                if (result != null) {
                    return new String(result);
                }
            } catch (Exception e) {
                logger.error("====> CustomizedRedisTemplate.getAndSet(): " + e.getMessage());
            }
            return null;
        });
    }

    /**
     * @param key
     * @param value
     * @param seconds
     * @param forever
     */
    public void put(final String key, final Object value, final int seconds, final boolean forever) {

        redisTemplate.execute((RedisCallback<Long>) connection -> {
            try {
                connection.set(key.getBytes(), objectMapper.writeValueAsBytes(value));
            } catch (JsonProcessingException e) {
                logger.error("====> CustomizedRedisTemplate.put(): " + e.getMessage(), e);
            }
            if (!forever) {
                connection.expire(redisTemplate.getStringSerializer().serialize(key), seconds > 0 ? seconds : defailtExpireTime);
            }
            return 1L;
        });
    }

    /**
     * @param key
     * @param value
     */
    public void put(final String key, final Object value) {
        this.put(key, value, defailtExpireTime, false);
    }

    /**
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(final String key, final Class<T> clazz) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redisTemplate.execute((RedisCallback<T>) connection -> {
            if (connection.exists(key.getBytes())) {
                byte[] value = connection.get(key.getBytes());
                try {
                    return objectMapper.readValue(value, clazz);
                } catch (IOException e) {
                    logger.error("====> CustomizedRedisTemplate.get(): " + e.getMessage(), e);
                }
            }
            return null;
        });
    }

    /**
     * @param key
     * @return
     */
    public String get(final String key) {
        return get(key, String.class);
    }

    /**
     * @param keys
     */
    public void delete(final String... keys) {
        redisTemplate.execute((RedisCallback<Long>) connection -> {
            long result = 0;
            for (String key : keys) {
                result = connection.del(key.getBytes());
            }
            return result;
        });
    }

    /**
     * @param key
     * @param t
     * @param seconds
     * @param <T>
     */
    public <T> void setOpsForValue(final String key, T t, final int seconds) {
        if (seconds < 0) {
            redisTemplate.opsForValue().set(key, t);
        } else {
            redisTemplate.opsForValue().set(key, t, seconds > 0 ? seconds : defailtExpireTime, TimeUnit.SECONDS);
        }
    }

    /**
     * @param key
     * @return
     */
    public Object getOpsForValue(final String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        return redisTemplate.opsForValue().get(key);
    }


    /**
     * @param key
     */
    public void setExpire(final String key, int timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }

}
