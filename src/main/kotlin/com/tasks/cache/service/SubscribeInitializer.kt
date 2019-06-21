package com.tasks.cache.service

import org.redisson.api.RedissonClient
import org.redisson.client.codec.StringCodec
import org.springframework.stereotype.Service


@Service
class SubscribeInitializer(redissonClient: RedissonClient, expireMessageListener: ExpireMessageListener) {

    init {
        redissonClient.getTopic("__keyevent@0__:expired", StringCodec.INSTANCE)
                .addListener(String::class.java, expireMessageListener)
    }

}