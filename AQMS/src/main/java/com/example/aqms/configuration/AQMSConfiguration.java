package com.example.aqms.configuration;

import com.example.aqms.listeners.OracleAQListeners;
import com.example.aqms.publisher.MessagePushService;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import oracle.jakarta.jms.AQjmsFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.sql.DataSource;

@Configuration
public class AQMSConfiguration {

    public static final String ORACLE_1 = "oracle-1";
    public static final String ORACLE_2 = "oracle-2";

    @Bean(name = "oracle1DataSource")
    public DataSource oracle1DataSource(AQMSProperties aqmsProperties) {
        DataSourceProperties dataSourceProperties = aqmsProperties.getDatasource().get(ORACLE_1);
        return DataSourceBuilder.create()
                .driverClassName(dataSourceProperties.getDriverClassName())
                .url(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .build();
    }

    @Bean(name = "oracle2DataSource")
    public DataSource oracle2DataSource(AQMSProperties aqmsProperties) {
        DataSourceProperties dataSourceProperties = aqmsProperties.getDatasource().get(ORACLE_2);
        return DataSourceBuilder.create()
                .driverClassName(dataSourceProperties.getDriverClassName())
                .url(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .build();
    }

    @Bean(name = "oracle1ConnectionFactory")
    public ConnectionFactory oracle1ConnectionFactory(DataSource oracle1DataSource) throws JMSException {
        return AQjmsFactory.getQueueConnectionFactory(oracle1DataSource);
    }

    @Bean(name = "oracle2ConnectionFactory")
    public ConnectionFactory oracle2ConnectionFactory(DataSource oracle2DataSource) throws JMSException {
        return AQjmsFactory.getQueueConnectionFactory(oracle2DataSource);
    }

    @Bean(name = "oracle1JmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory oracle1JmsListenerContainerFactory(ConnectionFactory oracle1ConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(oracle1ConnectionFactory);
        // Parallelism settings for database 1
        factory.setConcurrency("1-1");
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return factory;
    }

    @Bean(name = "oracle2JmsListenerContainerFactory")
    public DefaultJmsListenerContainerFactory oracle2JmsListenerContainerFactory(ConnectionFactory oracle2ConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(oracle2ConnectionFactory);
        factory.setConcurrency("1-2");
        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        return factory;
    }

    // JmsTemplate for Database 1
    @Bean(name = "oracle1JmsTemplate")
    public JmsTemplate oracle1JmsTemplate(ConnectionFactory oracle1ConnectionFactory) {
        return new JmsTemplate(oracle1ConnectionFactory);
    }

    // JmsTemplate for Database 2
    @Bean(name = "oracle2JmsTemplate")
    public JmsTemplate oracle2JmsTemplate(ConnectionFactory oracle2ConnectionFactory) {
        return new JmsTemplate(oracle2ConnectionFactory);
    }
    @Bean
    public OracleAQListeners oracleAQListeners() {
        return new OracleAQListeners();
    }


    @Bean
    public MessagePushService messagePushService(JmsTemplate oracle1JmsTemplate, JmsTemplate oracle2JmsTemplate) {
        return new MessagePushService(oracle1JmsTemplate, oracle2JmsTemplate);
    }

}
