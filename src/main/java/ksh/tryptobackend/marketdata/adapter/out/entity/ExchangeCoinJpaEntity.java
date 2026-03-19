package ksh.tryptobackend.marketdata.adapter.out.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import ksh.tryptobackend.marketdata.domain.model.ExchangeCoin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exchange_coin")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeCoinJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_coin_id")
    private Long id;

    @Column(name = "exchange_id", nullable = false)
    private Long exchangeId;

    @Column(name = "coin_id", nullable = false)
    private Long coinId;

    public ExchangeCoinJpaEntity(Long exchangeId, Long coinId) {
        this.exchangeId = exchangeId;
        this.coinId = coinId;
    }

    public ExchangeCoin toDomain() {
        return new ExchangeCoin(id, exchangeId, coinId);
    }
}
