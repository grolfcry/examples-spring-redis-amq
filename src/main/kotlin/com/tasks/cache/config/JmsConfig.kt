package com.tasks.cache.config

import org.springframework.context.annotation.Configuration
import org.springframework.jms.config.DefaultJmsListenerContainerFactory
import org.springframework.jms.core.JmsTemplate
import org.apache.activemq.ActiveMQConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean


@Configuration
class JmsConfig {
    @Value("#{environment['amq.url']}")
    lateinit var brokerUrl:String
    @Value("#{environment['amq.username']}")
    lateinit var brokerUserName:String

    @Value("#{environment['amq.password']}")
    lateinit var brokerPassword:String

    @Bean
    fun connectionFactory(): ActiveMQConnectionFactory {
        val connectionFactory = ActiveMQConnectionFactory()
        connectionFactory.brokerURL = brokerUrl
        connectionFactory.password = brokerUserName
        connectionFactory.userName = brokerPassword
        return connectionFactory
    }

    @Bean
    fun jmsTemplate(): JmsTemplate {
        val template = JmsTemplate()
        template.connectionFactory = connectionFactory()
        return template
    }

    @Bean
    fun jmsListenerContainerFactory(): DefaultJmsListenerContainerFactory {
        val factory = DefaultJmsListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory())
        factory.setConcurrency("1-1")
        return factory
    }
}