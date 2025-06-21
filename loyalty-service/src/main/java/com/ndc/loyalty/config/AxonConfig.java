package com.ndc.loyalty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.extensions.kafka.eventhandling.KafkaMessageConverter;
import org.axonframework.extensions.kafka.eventhandling.DefaultKafkaMessageConverter;
import org.axonframework.messaging.interceptors.BeanValidationInterceptor;
import org.axonframework.modelling.command.Repository;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.DefaultQueryGateway;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.persistence.EntityManagerFactory;

/**
 * Axon Framework Configuration
 * 
 * Configures:
 * - Event Store với PostgreSQL JPA backend
 * - Command Bus với validation interceptors
 * - Query Bus cho CQRS read side
 * - Kafka integration cho event streaming
 * - Serialization với Jackson
 * - Token Store cho event tracking
 * - Caching cho performance
 * 
 * @author NDC Team
 */
@Configuration
public class AxonConfig {

    /**
     * Jackson Serializer cho events và messages
     */
    @Bean
    @Primary
    public Serializer jacksonSerializer(ObjectMapper objectMapper) {
        return JacksonSerializer.builder()
                .objectMapper(objectMapper)
                .build();
    }

    /**
     * Event Storage Engine với JPA backend
     */
    @Bean
    public JpaEventStorageEngine eventStorageEngine(Serializer serializer,
                                                   EntityManagerFactory entityManagerFactory) {
        return JpaEventStorageEngine.builder()
                .entityManagerProvider(() -> entityManagerFactory.createEntityManager())
                .eventSerializer(serializer)
                .snapshotSerializer(serializer)
                .build();
    }

    /**
     * Token Store cho Event Tracking Processors
     */
    @Bean
    public TokenStore tokenStore(Serializer serializer, EntityManagerFactory entityManagerFactory) {
        return JpaTokenStore.builder()
                .entityManagerProvider(() -> entityManagerFactory.createEntityManager())
                .serializer(serializer)
                .build();
    }

    /**
     * Command Bus với validation interceptor
     */
    @Bean
    public CommandBus commandBus() {
        SimpleCommandBus commandBus = SimpleCommandBus.builder().build();
        
        // Add validation interceptor
        commandBus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
        
        return commandBus;
    }

    /**
     * Command Gateway
     */
    @Bean
    public CommandGateway commandGateway(CommandBus commandBus) {
        return DefaultCommandGateway.builder()
                .commandBus(commandBus)
                .build();
    }

    /**
     * Query Bus
     */
    @Bean
    public QueryBus queryBus() {
        return SimpleQueryBus.builder().build();
    }

    /**
     * Query Gateway
     */
    @Bean
    public QueryGateway queryGateway(QueryBus queryBus) {
        return DefaultQueryGateway.builder()
                .queryBus(queryBus)
                .build();
    }

    /**
     * Event Bus
     */
    @Bean
    public EventBus eventBus() {
        return SimpleEventBus.builder().build();
    }

    /**
     * Cache cho Aggregate performance
     */
    @Bean
    public Cache cache() {
        return new WeakReferenceCache();
    }

    /**
     * Kafka Message Converter
     */
    @Bean
    public KafkaMessageConverter<String, byte[]> kafkaMessageConverter(Serializer serializer) {
        return DefaultKafkaMessageConverter.builder()
                .serializer(serializer)
                .build();
    }

    /**
     * Member Aggregate Repository
     */
    @Bean
    @Qualifier("memberRepository")
    public Repository<com.ndc.loyalty.domain.member.aggregate.MemberAggregate> memberRepository(
            EventStore eventStore, Cache cache) {
        return EventSourcingRepository.builder(com.ndc.loyalty.domain.member.aggregate.MemberAggregate.class)
                .eventStore(eventStore)
                .cache(cache)
                .build();
    }

    /**
     * Wallet Aggregate Repository
     */
    @Bean
    @Qualifier("walletRepository")
    public Repository<com.ndc.loyalty.domain.wallet.aggregate.WalletAggregate> walletRepository(
            EventStore eventStore, Cache cache) {
        return EventSourcingRepository.builder(com.ndc.loyalty.domain.wallet.aggregate.WalletAggregate.class)
                .eventStore(eventStore)
                .cache(cache)
                .build();
    }

    /**
     * Tier Aggregate Repository
     */
    @Bean
    @Qualifier("tierRepository")
    public Repository<com.ndc.loyalty.domain.tier.aggregate.TierAggregate> tierRepository(
            EventStore eventStore, Cache cache) {
        return EventSourcingRepository.builder(com.ndc.loyalty.domain.tier.aggregate.TierAggregate.class)
                .eventStore(eventStore)
                .cache(cache)
                .build();
    }

    /**
     * Transaction Aggregate Repository
     */
    @Bean
    @Qualifier("transactionRepository")
    public Repository<com.ndc.loyalty.domain.transaction.aggregate.TransactionAggregate> transactionRepository(
            EventStore eventStore, Cache cache) {
        return EventSourcingRepository.builder(com.ndc.loyalty.domain.transaction.aggregate.TransactionAggregate.class)
                .eventStore(eventStore)
                .cache(cache)
                .build();
    }
}