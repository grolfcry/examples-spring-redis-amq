package com.tasks.cache.service

import com.tasks.cache.Cache
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.concurrent.TimeUnit


@Component
@Primary
@ConditionalOnProperty(prefix = "", name = ["redis.host"])
class RedisCache(private val redissonClient: RedissonClient) : Cache<Serializable> {
    //TODO LT?? - may be switch to NEAR (local) cache
    private val msgSuffix = "messages:"
    private val msgExpSuffix = "messages_exp:" //diff space for possibility of getting the value
    var logger = LoggerFactory.getLogger(RedisCache::class.java)!!
    override fun get(key: String): Serializable? {
        logger.debug("get with key $key")
        return redissonClient.getBucket<Serializable>("$msgSuffix$key").get()
    }

    override fun put(key: String, value: Serializable, expiry:Long) {
        logger.debug("put with key $key and expiry=$expiry")
        redissonClient.getBucket<Serializable>("$msgSuffix$key").set(value)
        if (expiry>0) {
            redissonClient.getBucket<String>("$msgExpSuffix$key").set("_", expiry,TimeUnit.SECONDS)
        }
    }

    override fun delete(key: String): Serializable? {
        logger.debug("delete with key $key")
        val bucket = redissonClient.getBucket<Serializable>("$msgSuffix$key")
        val result = bucket.get()
        bucket.delete()
        redissonClient.getBucket<String>("$msgExpSuffix:$key").delete()
        return result
    }


}