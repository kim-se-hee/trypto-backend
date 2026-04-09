package ksh.tryptobackend.trading.adapter.out;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.marketdata.adapter.out.entity.CoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeCoinJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.entity.ExchangeJpaEntity;
import ksh.tryptobackend.marketdata.adapter.out.repository.CoinJpaRepository;
import ksh.tryptobackend.marketdata.adapter.out.repository.ExchangeCoinJpaRepository;
import ksh.tryptobackend.marketdata.adapter.out.repository.ExchangeJpaRepository;
import ksh.tryptobackend.trading.domain.vo.PendingOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class PendingOrderRedisCommandAdapter {

    private static final String KEY_PREFIX = "pending:orders:";

    private final StringRedisTemplate redisTemplate;
    private final ExchangeCoinJpaRepository exchangeCoinRepository;
    private final ExchangeJpaRepository exchangeRepository;
    private final CoinJpaRepository coinRepository;

    private final ConcurrentMap<Long, String> keyPrefixCache = new ConcurrentHashMap<>();

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void add(PendingOrder pendingOrder) {
        String key = buildKey(pendingOrder.exchangeCoinId(), pendingOrder.side().name());
        redisTemplate.opsForZSet().add(
                key,
                pendingOrder.orderId().toString(),
                pendingOrder.price().doubleValue());
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void remove(Long exchangeCoinId, Long orderId) {
        String keyPrefix = resolveKeyPrefix(exchangeCoinId);
        redisTemplate.opsForZSet().remove(keyPrefix + ":BUY", orderId.toString());
        redisTemplate.opsForZSet().remove(keyPrefix + ":SELL", orderId.toString());
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void addAll(List<PendingOrder> pendingOrders) {
        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (PendingOrder order : pendingOrders) {
                String key = buildKey(order.exchangeCoinId(), order.side().name());
                connection.zSetCommands().zAdd(
                        key.getBytes(),
                        order.price().doubleValue(),
                        order.orderId().toString().getBytes());
            }
            return null;
        });
    }

    private String buildKey(Long exchangeCoinId, String side) {
        return resolveKeyPrefix(exchangeCoinId) + ":" + side;
    }

    private String resolveKeyPrefix(Long exchangeCoinId) {
        return keyPrefixCache.computeIfAbsent(exchangeCoinId, this::buildKeyPrefix);
    }

    private String buildKeyPrefix(Long exchangeCoinId) {
        ExchangeCoinJpaEntity exchangeCoin = exchangeCoinRepository.findById(exchangeCoinId)
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_COIN_NOT_FOUND));

        ExchangeJpaEntity exchange = exchangeRepository.findById(exchangeCoin.getExchangeId())
                .orElseThrow(() -> new CustomException(ErrorCode.EXCHANGE_NOT_FOUND));

        String baseSymbol = findCoinSymbol(exchangeCoin.getCoinId());
        String quoteSymbol = findCoinSymbol(exchange.getBaseCurrencyCoinId());

        return KEY_PREFIX + exchange.getName() + ":" + baseSymbol + "/" + quoteSymbol;
    }

    private String findCoinSymbol(Long coinId) {
        return coinRepository.findById(coinId)
                .map(CoinJpaEntity::getSymbol)
                .orElseThrow(() -> new CustomException(ErrorCode.COIN_NOT_FOUND));
    }
}
