package org.ft.wallet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ft_wallet", schema = "wallet")
public class Wallet {

    @Id
    private Long customerId;

    private BigDecimal balance;

    private String currency;
}
