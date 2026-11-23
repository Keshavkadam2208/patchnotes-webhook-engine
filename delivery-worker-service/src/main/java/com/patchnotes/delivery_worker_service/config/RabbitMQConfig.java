package com.patchnotes.delivery_worker_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.Query;
import java.security.PublicKey;
import java.sql.Connection;


@Configuration
public class RabbitMQConfig {

    //Main Exchange aur main delivery queue(jo ingestion_service se match karti hai)
    public static final String MAIN_EXCHANGE = "patchnotes.exchange";
    public static final String DELIVERY_QUEUE = "webhook.delivery.queue";
    public static final String DELIVERY_ROUTING_KEY = "webhook.event.retry";

    //Retry queus ke naam aur unki routing keys
    public static final String RETRY_10S_QUEUE = "webhook.retry.10s.queue";
    public static final String RETRY_1M_QUEUE = "webhook.retry.1m.queue";
    public static final String RETRY_5M_QUEUE = "webhook.retry.5m.queue";

    public static final String RETRY_10S_KEY = "retry.10s";
    public static final String RETRY_1M_KEY = "retry.1m";
    public  static final String RETRY_5M_KEY = "retry.5m";

    //final dead letter queue(jb saare retries fail ho jaayein)
    public static final String FINAL_DLQ = "webhook.dlq";
    public static final String FINAL_DLQ_KEY = "dlq.final";

    @Bean
    public TopicExchange patchnotesExchange(){
        return new TopicExchange(MAIN_EXCHANGE);
    }

    //1. main delivery queue(jaha se hamara worker message padhega)
    @Bean
    public Queue deliveryQueue(){
        return  QueueBuilder.durable(DELIVERY_QUEUE).build();
    }

    //jab retry queue se messages expire hokar wapas aayenge to is routing key se main key me girega
    @Bean
    public Binding deliveryRetryBinding(Queue deliveryQueue, TopicExchange patchnotesExchange){
        return BindingBuilder.bind(deliveryQueue)
                .to(patchnotesExchange)
                .with(DELIVERY_ROUTING_KEY);

    }

    //2. Retry queue 1(10sec delay)
    //ttl(10_000) = 10s later msg will expire
    //deadletterexchange = expire hone k baad wapas main_exchange ko bhej do
    //deadletterRoutingKey = aur DELIVERY_ROUTING_KEY lagakr wapas deliveryqueue me daal do
    @Bean
    public Queue retry10sQueue(){
        return QueueBuilder.durable(RETRY_10S_QUEUE)
                .ttl(10_000)
                .deadLetterExchange(MAIN_EXCHANGE)
                .deadLetterRoutingKey(DELIVERY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding retry10sBinding(Queue retry10sQueue, TopicExchange patchnotesExchange){
        return BindingBuilder.bind(retry10sQueue)
                .to(patchnotesExchange)
                .with(RETRY_10S_KEY);
    }

    //3. Retry queue 2 (1minute= 60000 ms delay)
    @Bean
    public Queue retry1mQueue(){
        return QueueBuilder.durable(RETRY_1M_QUEUE)
                .ttl(60_000)
                .deadLetterExchange(MAIN_EXCHANGE)
                .deadLetterRoutingKey(DELIVERY_ROUTING_KEY)
                .build();
    }
    @Bean
    public Binding retry1mBinding(Queue retry1mQueue, TopicExchange patchnotesExchange){
        return BindingBuilder.bind(retry1mQueue)
                .to(patchnotesExchange)
                .with(RETRY_1M_KEY);
    }

    //4. Retry queue 3(5minutes = 300,000 ms delay)
    @Bean
    public Queue retry5mQueue(){
        return QueueBuilder.durable(RETRY_5M_QUEUE)
                .ttl(300_000)
                .deadLetterExchange(MAIN_EXCHANGE)
                .deadLetterRoutingKey(DELIVERY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding retry5mBinding(Queue retry5mQueue, TopicExchange patchnotesExchange){
        return BindingBuilder.bind(retry5mQueue)
                .to(patchnotesExchange)
                .with(RETRY_5M_KEY);
    }

    //5. final deadletterqueue(yaha msgs hamesha k liye safe rahenge)
    @Bean
    public Queue finalDlq(){
        return QueueBuilder.durable(FINAL_DLQ).build();
    }

    @Bean
    public Binding finalDlqBinding(Queue finalDlq, TopicExchange patchnotesExchange){
        return BindingBuilder.bind(finalDlq)
                .to(patchnotesExchange)
                .with(FINAL_DLQ_KEY);
    }

    //6. JSON msg converter(so DTOs seedhe JSON me serialize/deserialize ho)
    @Bean
    public MessageConverter jsonMessageConverter() {

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());

        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonmessageConverter)
    {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
