package org.ft.bank.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name="accounts")
@Data
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    private String customerId;
    private String currency;
    private BigDecimal balance;
}
