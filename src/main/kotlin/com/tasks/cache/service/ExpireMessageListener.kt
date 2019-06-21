package com.tasks.cache.service

import org.redisson.api.RedissonClient
import org.redisson.api.listener.MessageListener
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.jms.UncategorizedJmsException
import org.springframework.jms.core.JmsTemplate
import org.springframework.stereotype.Service
import java.io.Serializable
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import javax.jms.JMSException


@Service
class ExpireMessageListener(private val redissonClient: RedissonClient, private val jmsTemplate: JmsTemplate) : MessageListener<String> {
    @Value("#{environment['amq.queue']}")
    lateinit var queue: String
    private val logger = LoggerFactory.getLogger(ExpireMessageListener::class.java)!!
    private val expiryTime = 5L
    override fun onMessage(channel: CharSequence?, msg: String?) {
        logger.debug("onMessage $channel  - $msg")
        val expiryKey: String = msg!!
        if (channel != null && channel.endsWith("expired")) {
            logger.debug("Expired key - $expiryKey")
            val messageKey = "messages:" + expiryKey.substring(expiryKey.indexOf(":") + 1) //change key "scope"
            processExpired(messageKey, expiryKey)
        }
    }

    private fun processExpired(messageKey: String, expiryKey: String) {
        val fairLock = redissonClient.getFairLock("LOCK-$expiryKey")
        logger.debug("try lock - $expiryKey")
        val locked = fairLock.tryLock(6, 5, TimeUnit.SECONDS)
        //TODO vs infinity try??
        //  while (!fairLock.tryLock(6, 5, TimeUnit.SECONDS)) {
        //  }
        if (!locked) logger.warn("can`t lock expired key") else {
            logger.debug("locked - $messageKey")
            val value = redissonClient.getBucket<Serializable>(messageKey).get()
            if (value != null) {
                logger.debug("send $messageKey to AMQ  - $queue")
                try {
                    jmsTemplate.send(queue) { session ->
                        session.createObjectMessage(value)
                    }
                    logger.debug("delete - $messageKey")
                    redissonClient.getBucket<String>(messageKey).delete()
                } catch (e: RuntimeException) {
                    when (e) {
                        is UncategorizedJmsException, is JMSException -> {
                            //schedule retry send to AMQ
                            logger.error(e.message, e)
                            logger.warn("schedule reprocess expiryKey after $expiryTime seconds")
                            redissonClient.getBucket<Serializable>(expiryKey).set("", expiryTime, TimeUnit.SECONDS)
                        }
                        else -> throw e
                    }
                } finally {
                    fairLock.unlock()
                }
            } else {
                logger.debug("$messageKey not found, maybe already processed")
            }

        }
    }
}