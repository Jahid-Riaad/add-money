package org.ft.wallet.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic transferInitiated() {
        return TopicBuilder.name("transfer.initiated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic bankDebitSuccess() {
        return TopicBuilder.name("bank.debit.success")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic walletCreditSuccess() {
        return TopicBuilder.name("wallet.credit.success")
                .partitions(3)
                .replicas(1)
                .build();
    }
}