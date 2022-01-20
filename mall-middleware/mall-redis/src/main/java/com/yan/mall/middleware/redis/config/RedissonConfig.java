package com.yan.mall.middleware.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.IOException;
import java.time.Duration;

/**
 * Created by huyan on 2021/12/1.
 * TIME: 21:22
 * DESC:
 */
@Configuration
@Slf4j
public class RedissonConfig {
    /**
     * 单节点模式
     * @return
     * @throws IOException
     */
    @Bean
    public RedissonClient redissonClient() throws IOException {
        log.info("初始化加载redisson.yml文件配置...");
        Config config = Config.fromYAML(new ClassPathResource("redisson.yml").getInputStream());
        log.info("加载redisson.yml文件配置完成");
        return Redisson.create(config);
    }

    /**
     * useSentinelServers
     * 多节点模式，可参考官网配置
     */
    /*@Bean
    public RedissonClient redissonClient() throws IOException {
        List<String> sentinelSslAddressWithList = Arrays.stream(sentinelAddress.split(","))
                .map(s -> REDIS_PROXY + s)
                .collect(Collectors.toList());

        Config config = Config.fromYAML(new ClassPathResource("redisson.yml").getInputStream());
        config.useSentinelServers()
                .setPassword(password)
                .setMasterName(masterName)
                .setReadMode(ReadMode.MASTER)
                .setCheckSentinelsList(false)
                // use "rediss://" for SSL connection
                .addSentinelAddress(sentinelSslAddressWithList.toArray(new String[sentinelAddress.split(",").length]));
        return Redisson.create(config);
    }*/


    /**
     * 使用redistemplate设置的序列化器
     * @param redisTemplate
     * @return
     */
    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate redisTemplate) {
        RedisCacheWriter redisCacheWriter = RedisCacheWriter.nonLockingRedisCacheWriter(redisTemplate.getConnectionFactory());

        //从redistemplate从取序列化构造器 (注意RedisCacheConfiguration返回的是新的对象，因此这里直接设置链式调用)
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisTemplate.getValueSerializer()))
                .entryTtl(Duration.ofSeconds(600L));//60s过期时间

        return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
    }

    /**
     * 重新实现RedisTemplate：解决序列化问题
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        //key值序列化指定为String
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        //value值序列化指定为jackson
        Jackson2JsonRedisSerializer valueSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        // 设置任何字段可见
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 设置不是final的属性可以转换
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        log.info("objectMapper: {}", om);
        valueSerializer.setObjectMapper(om);
        // key采用String的序列化方式
        template.setKeySerializer(keySerializer);
        // hash的key采用String的序列化方式
        template.setHashKeySerializer(keySerializer);
        // value序列化方式采用jackson序列化方式
        template.setValueSerializer(valueSerializer);
        // hash的value序列化方式采用jackson序列化方式
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        template.setEnableTransactionSupport(true);
        return template;
    }

    /**
     * 重新实现StringRedisTmeplate：键值都是String的的数据
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        //key值序列化指定为String
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        //value值序列化指定为jackson
        Jackson2JsonRedisSerializer valueSerializer = new Jackson2JsonRedisSerializer(Object.class);

        // key采用String的序列化方式
        template.setKeySerializer(keySerializer);
        // hash的key采用String的序列化方式
        template.setHashKeySerializer(keySerializer);
        // value序列化方式采用jackson序列化方式
        template.setValueSerializer(valueSerializer);
        // hash的value序列化方式采用jackson序列化方式
        template.setHashValueSerializer(valueSerializer);
        return template;
    }
}
