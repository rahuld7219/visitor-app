package io.bootify.visitor_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean // To communicate to the redis server via the connection made, we need RedisTemplate
    public RedisTemplate<String,Object> getRedisTemplate(RedisConnectionFactory redisConnectionFactory){

        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.setKeySerializer(new StringRedisSerializer()); // we use StringRedisSerializer() for key,
                                                                        // so keys on redis shown as string
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer()); // we use JdkSerializationRedisSerializer for value,
                                                                                    // so values on redis will be shown in
                                                                                    // some hexa unreadable form,
                                                                                    // we can use StringRedisSerializer also
                                                                                    // if we want values to be shown as string on redis,
                                                                                    // there are other options also for serialization
        return redisTemplate;
    }

}
