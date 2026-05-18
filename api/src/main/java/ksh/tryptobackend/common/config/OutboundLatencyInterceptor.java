package ksh.tryptobackend.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.concurrent.TimeUnit;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.ExecutorChannelInterceptor;

/**
 * STOMP clientOutbound 채널 핸들러 1건당 처리 시간(소켓 write 포함) 을 기록한다.
 *
 * <p>시세는 SSE 로 분리됐고 이 인터셉터에 남는 STOMP 트래픽은 체결 통지(/user/{userId}/queue/events) 뿐이다. 시세 e2e 지연은 {@link
 * ksh.tryptobackend.marketdata.adapter.in.SseTickerEmitterRegistry} 에서 측정한다.
 */
public class OutboundLatencyInterceptor implements ExecutorChannelInterceptor {

    private static final String HANDLE_TIMER_NAME = "stomp.clientOutbound.handle.duration";
    private static final ThreadLocal<Long> START_NANOS = new ThreadLocal<>();

    private final Timer handleTimer;

    public OutboundLatencyInterceptor(MeterRegistry registry) {
        this.handleTimer =
                Timer.builder(HANDLE_TIMER_NAME)
                        .description("clientOutbound 채널 핸들러가 메시지 1건을 처리(소켓 write 포함)하는 데 걸린 시간")
                        .publishPercentileHistogram()
                        .register(registry);
    }

    @Override
    public Message<?> beforeHandle(
            Message<?> message, MessageChannel channel, MessageHandler handler) {
        START_NANOS.set(System.nanoTime());
        return message;
    }

    @Override
    public void afterMessageHandled(
            Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        Long start = START_NANOS.get();
        if (start == null) {
            return;
        }
        START_NANOS.remove();
        handleTimer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
    }
}
