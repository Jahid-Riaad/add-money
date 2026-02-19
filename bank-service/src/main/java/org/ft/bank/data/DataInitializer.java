package org.ft.bank.data;

import org.ft.bank.entity.Account;
import org.ft.bank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    @Value("${bank.default-currency}")
    private String defaultCurrency;

    @Bean
    CommandLineRunner initDatabase(AccountRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                List<Account> demoAccounts = List.of(
                    createAccount("CUST-001", "1000.00"),
                    createAccount("CUST-002", "500.50"),
                    createAccount("CUST-003", "0.00"),
                    createAccount("CUST-004", "5000.00"),
                    createAccount("CUST-005", "10.00")
                );
                
                repository.saveAll(demoAccounts);
                System.out.println(">>> 5 Demo Accounts inserted into ft_accounts");
            }
        };
    }

    private Account createAccount(String customerId, String balance) {
        Account acc = new Account();
        acc.setCustomerId(customerId);
        acc.setCurrency(defaultCurrency);
        acc.setBalance(new BigDecimal(balance));
        return acc;
    }
}