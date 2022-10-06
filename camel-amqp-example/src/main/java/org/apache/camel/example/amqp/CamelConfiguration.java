package org.apache.camel.example.amqp;

import javax.jms.DeliveryMode;
import javax.jms.Session;
import org.apache.camel.LoggingLevel;
import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.jms.ConsumerType;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultRedeliveryPolicy;
import org.springframework.jms.connection.CachingConnectionFactory;

public class CamelConfiguration {
    public static AMQPComponent esbBroker(String clientId) {
        return new AMQPComponent(jmsConfiguration("ESB_SUBSCRIBER_" + clientId + ":", Session.CLIENT_ACKNOWLEDGE));
    }
 
    public static AMQPComponent psbBroker(String clientId) {
        return new AMQPComponent(jmsConfiguration("PSB_PUBLISHER_" + clientId + ":", Session.AUTO_ACKNOWLEDGE));
    }

    public static JmsConfiguration jmsConfiguration(String clientId, int acknowledgementMode) {
        JmsConnectionFactory jmsConnectionFactory = jmsConnectionFactory("default", "default", toFailover("amqp://localhost:5672"), clientId);
        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        jmsConfiguration.setAcceptMessagesWhileStopping(false);
        jmsConfiguration.setAcknowledgementMode(acknowledgementMode);
        jmsConfiguration.setCacheLevelName("CACHE_CONSUMER");
        jmsConfiguration.setConsumerType(ConsumerType.Default);
        jmsConfiguration.setAutoStartup(true);
        jmsConfiguration.setExplicitQosEnabled(true);
        jmsConfiguration.setPreserveMessageQos(false);
        jmsConfiguration.setDeliveryPersistent(true);
        jmsConfiguration.setDeliveryMode(DeliveryMode.PERSISTENT);
        jmsConfiguration.setErrorHandlerLoggingLevel(LoggingLevel.WARN);
        jmsConfiguration.setErrorHandlerLogStackTrace(false);
        CachingConnectionFactory cachingConnectionFactory = cachingConnectionFactory(jmsConnectionFactory);
        jmsConfiguration.setConnectionFactory(cachingConnectionFactory);
        return jmsConfiguration;
    }
 
    public static String toFailover(String amqpConnectionString) {
        if (!amqpConnectionString.startsWith("failover")) {
            return "failover:(" + amqpConnectionString + ")";
        }
        return amqpConnectionString;
    }
 
    public static JmsConnectionFactory jmsConnectionFactory(String username, String password, String remoteUri, String clientId) {
        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory(username, password, remoteUri);
        jmsConnectionFactory.setAwaitClientID(false);
        jmsConnectionFactory.setClientIDPrefix(clientId);
        jmsConnectionFactory.setUseDaemonThread(false);
        jmsConnectionFactory.setRedeliveryPolicy(jmsRedeliveryPolicy());
        return jmsConnectionFactory;
    }
 
    public static JmsDefaultRedeliveryPolicy jmsRedeliveryPolicy() {
        JmsDefaultRedeliveryPolicy jmsRedeliveryPolicy = new JmsDefaultRedeliveryPolicy();
        jmsRedeliveryPolicy.setMaxRedeliveries(-1); // Always try to redeliver
        jmsRedeliveryPolicy.setOutcome("MODIFIED_FAILED_UNDELIVERABLE");
        return jmsRedeliveryPolicy;
    }
 
    public static CachingConnectionFactory cachingConnectionFactory(JmsConnectionFactory connectionFactory) {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setSessionCacheSize(1000);
        cachingConnectionFactory.setCacheProducers(true);
        cachingConnectionFactory.setCacheConsumers(true);
        cachingConnectionFactory.setTargetConnectionFactory(connectionFactory);
        cachingConnectionFactory.setReconnectOnException(true);
        return cachingConnectionFactory;
    }
}