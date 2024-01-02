package com.middleware.order.event.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

@Component
public class EventProducer {

	@Value("${spring.rabbitmq.host}")
    private String host;
	
	@Value("${spring.rabbitmq.port}")
    private int port;
	
	@Value("${spring.rabbitmq.username}")
    private String username;
	
	@Value("${spring.rabbitmq.password}")
    private String password;
	
	public void publishEvent(String exchange, String exchangeType, String routingKey, String message) {
		System.out.println("publishEvent method start");
		try {
			ConnectionFactory connectionFactory = getConnectionFactory();
			try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
				channel.exchangeDeclare(exchange, exchangeType);
				channel.basicPublish(exchange, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
				System.out.println("Sent '" + message + "'");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("publishEvent method start");
	}
	
	private ConnectionFactory getConnectionFactory() {
		ConnectionFactory connectionFactory = new ConnectionFactory();
		connectionFactory.setHost(host);
		connectionFactory.setPort(port);
		connectionFactory.setUsername(username);
		connectionFactory.setPassword(password);
		return connectionFactory;
	}
}
