package dev.example.visa.config;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.rabbitmq.connect.ChannelPool;
import io.micronaut.rabbitmq.connect.RabbitConnectionFactory;
import io.micronaut.rabbitmq.connect.RabbitConnectionFactoryConfig;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Mock configuration for RabbitMQ components in test environment.
 * This avoids the need for an actual RabbitMQ server during tests.
 */
@Factory
public class RabbitMQTestConfiguration {

    @Bean
    @Singleton
    @Primary
    @Replaces(bean = Connection.class)
    public Connection mockRabbitConnection() throws Exception {
        Connection connection = mock(Connection.class);
        Channel channel = mock(Channel.class);
        when(connection.createChannel()).thenReturn(channel);
        return connection;
    }

    @Bean
    @Singleton
    @Primary
    @Replaces(bean = RabbitConnectionFactoryConfig.class)
    public RabbitConnectionFactoryConfig mockRabbitConfig() {
        RabbitConnectionFactoryConfig config = mock(RabbitConnectionFactoryConfig.class);

        // Set host and port directly instead of URI
        when(config.getHost()).thenReturn("localhost");
        when(config.getPort()).thenReturn(5672);
        when(config.getUsername()).thenReturn("guest");
        when(config.getPassword()).thenReturn("guest");
        when(config.getName()).thenReturn("default");

        // Mock RPC configuration
        RabbitConnectionFactoryConfig.RpcConfiguration rpcConfig =
                new RabbitConnectionFactoryConfig.RpcConfiguration();
        rpcConfig.setTimeout(java.time.Duration.ofSeconds(5));
        when(config.getRpc()).thenReturn(rpcConfig);

        // Mock channel pool configuration
        RabbitConnectionFactoryConfig.ChannelPoolConfiguration channelPoolConfig =
                mock(RabbitConnectionFactoryConfig.ChannelPoolConfiguration.class);
        when(channelPoolConfig.getMaxIdleChannels()).thenReturn(Optional.of(5));
        when(config.getChannelPool()).thenReturn(channelPoolConfig);

        return config;
    }

    @Bean
    @Singleton
    @Primary
    @Named("default")
    public ChannelPool mockDefaultChannelPool() throws Exception {
        ChannelPool channelPool = mock(ChannelPool.class);
        Channel channel = mock(Channel.class);
        when(channelPool.getChannel()).thenReturn(channel);
        return channelPool;
    }

    @Bean
    @Singleton
    @Primary
    @Replaces(bean = RabbitConnectionFactory.class)
    public RabbitConnectionFactory mockConnectionFactory() {
        return mock(RabbitConnectionFactory.class);
    }
}