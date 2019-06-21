package com.tasks.cache.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RedisSpringConfig {
    @Bean
    fun redissonConfig(@Value("#{environment['redis.url']}") url: String): Config {
        var config = Config()
        config.useSingleServer().setAddress("redis://$url")
        //useClusterSetvers.addNodeAddress("redis://$url")
        return config
    }

    @Bean
    fun redissonClient(config: Config): RedissonClient {
        return Redisson.create(config)
    }
}