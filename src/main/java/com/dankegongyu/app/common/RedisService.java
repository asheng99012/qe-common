package com.dankegongyu.app.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by ashen on 2017-2-14.
 */
@Service
public class RedisService {

    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RedisSerializer stringSerializer = new StringRedisSerializer();
        // 使用Jackson2JsonRedisSerialize 替换默认序列化
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer1=new GenericJackson2JsonRedisSerializer();

        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer1);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer1);
        this.redisTemplate = redisTemplate;
    }

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
