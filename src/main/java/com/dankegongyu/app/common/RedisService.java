package com.dankegongyu.app.common;

import com.google.common.base.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by ashen on 2017-2-14.
 */
@Service()
public class RedisService {

    @Autowired(required = false)
    private RedisTemplate redisTemplate;

//    @Value("${redis.disable:false}")
//    private Boolean disable;

    public RedisTemplate getRedisTemplate() {
        return redisTemplate;
    }

    public void set(String key, Object object) {
//        if (disable) return;
        redisTemplate.opsForValue().set(key, object);
    }

    public void set(String key, Object object, int minute) {
//        if (disable) return;
        redisTemplate.opsForValue().set(key, object, minute, TimeUnit.MINUTES);
    }


    public void del(String key) {
        redisTemplate.delete(key);
    }


    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public <T> T get(String key, Function<String, T> function) {
        return (T) get(key, function, null);
    }

    public <T> T get(String key, Function<String, T> function, Integer minute) {
        ValueOperations<String, T> ops = redisTemplate.opsForValue();
        T val = ops.get(key);
        if (val == null) {
            val = (T) function.apply(key);
            if (val != null) {
                if (minute != null && minute > 0)
                    ops.set(key, val, minute, TimeUnit.MINUTES);
                else ops.set(key, val);
            }
        }
        return val;
    }

}
