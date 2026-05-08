package ksh.tryptobackend.common.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class StompSessionMetrics {

    private static final String ACTIVE_GAUGE = "stomp.sessions.active";
    private static final String CONNECT_COUNTER = "stomp.sessions.connect.total";
    private static final String DISCONNECT_COUNTER = "stomp.sessions.disconnect.total";

    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final Counter connectCounter;
    private final Counter disconnectCounter;

    public StompSessionMetrics(MeterRegistry meterRegistry) {
        Gauge.builder(ACTIVE_GAUGE, activeSessions, AtomicInteger::get)
                .description("현재 활성 STOMP 세션 수")
                .register(meterRegistry);
        this.connectCounter =
                Counter.builder(CONNECT_COUNTER)
                        .description("STOMP 세션 연결 누계")
                        .register(meterRegistry);
        this.disconnectCounter =
                Counter.builder(DISCONNECT_COUNTER)
                        .description("STOMP 세션 종료 누계")
                        .register(meterRegistry);
    }

    @EventListener
    public void onSessionConnected(SessionConnectedEvent event) {
        activeSessions.incrementAndGet();
        connectCounter.increment();
    }

    @EventListener
    public void onSessionDisconnected(SessionDisconnectEvent event) {
        activeSessions.decrementAndGet();
        disconnectCounter.increment();
    }
}
