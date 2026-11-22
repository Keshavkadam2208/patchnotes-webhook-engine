package com.patchnotes.ingestion_service.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "patchnotes.exchange";
    public static final String DELIVERY_QUEUE = "webhook.delivery.queue";
    public static final String ROUTING_KEY_PATTERN = "webhook.event.#";

    //1. Durabe Queue: RabbitMQ restart hone pr bhi message safe rahenge
    @Bean
    public Queue deliveryQueue() {
        return  QueueBuilder.durable(DELIVERY_QUEUE).build();
    }

    //2. Topic Exchange: Event type ke hisab se flexible routing ke liye
    @Bean
    public TopicExchange patchnotesExchange()
    {
        return new TopicExchange(EXCHANGE_NAME);
    }

    //3. Binding: Exchange aur Queue ko jodna
    @Bean
    public Binding deliveryBinding(Queue deleveryQueue,
                                   TopicExchange patchnotesExchange)
    {
        return BindingBuilder.bind(deleveryQueue)
                .to(patchnotesExchange)
                .with(ROUTING_KEY_PATTERN);
    }

    //4. JSON Converter: Messages ko readable JSON format me bhejne k liye
    @Bean
    public MessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
