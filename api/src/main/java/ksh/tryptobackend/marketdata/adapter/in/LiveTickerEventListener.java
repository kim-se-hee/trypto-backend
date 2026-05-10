package ksh.tryptobackend.marketdata.adapter.in;

import java.util.Map;
import ksh.tryptobackend.common.config.RabbitMqConfig;
import ksh.tryptobackend.common.dto.messages.TickerMessage;
import ksh.tryptobackend.marketdata.adapter.in.dto.response.TickerResponse;
import ksh.tryptobackend.marketdata.application.port.in.ResolveLiveTickerUseCase;
import ksh.tryptobackend.marketdata.application.port.in.dto.result.LiveTickerResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveTickerEventListener {

    private static final String TOPIC_PREFIX = "/topic/tickers.";

    private final ResolveLiveTickerUseCase resolveLiveTickerUseCase;
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(
            queues = "#{tickerMarketdataQueue.name}",
            autoStartup = "false",
            id = RabbitMqConfig.TICKER_MARKETDATA_LISTENER_ID)
    public void onTickerEvent(TickerMessage message) {
        try {
            resolveLiveTickerUseCase
                    .resolve(
                            message.exchange(),
                            message.symbol(),
                            message.currentPrice(),
                            message.changeRate(),
                            message.quoteTurnover(),
                            message.timestamp())
                    .ifPresent(this::broadcast);
        } catch (Exception e) {
            log.error(
                    "시세 브로드캐스트 실패: exchange={}, symbol={}",
                    message.exchange(),
                    message.symbol(),
                    e);
        }
    }

    private void broadcast(LiveTickerResult result) {
        TickerResponse response =
                new TickerResponse(
                        result.coinId(),
                        result.symbol(),
                        result.price(),
                        result.changeRate(),
                        result.quoteTurnover(),
                        result.timestamp());
        // collector publish 시각을 헤더에 실어 보낸다.
        // OutboundLatencyInterceptor 가 socket write 직전에 이 헤더를 읽어
        // "수집기 publish → api outbound 직전" e2e 시간을 Timer 로 기록한다.
        Map<String, Object> headers = Map.of(PUBLISHED_AT_MS_HEADER, result.timestamp());
        messagingTemplate.convertAndSend(
                TOPIC_PREFIX + result.exchangeId(), response, headers);
    }

    public static final String PUBLISHED_AT_MS_HEADER = "publishedAtMs";
}
