package ksh.tryptoengine.dbwriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ksh.tryptoengine.matching.OrderDetail;
import ksh.tryptoengine.dbwriter.FillCommand;
import ksh.tryptoengine.outbox.OrderFilledEvent;
import ksh.tryptoengine.outbox.OutboxPublisher;
import ksh.tryptoengine.dbwriter.HoldingIncrementalUpdater;
import ksh.tryptoengine.metrics.EngineMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class FillTransactionExecutor {

    private static final String ORDER_FILLED = "ORDER_FILLED";

    private final JdbcTemplate jdbc;
    private final HoldingIncrementalUpdater holdingUpdater;
    private final ObjectMapper objectMapper;
    private final EngineMetrics metrics;
    private final OutboxPublisher outboxPublisher;

    public FillTransactionExecutor(
        JdbcTemplate jdbc,
        HoldingIncrementalUpdater holdingUpdater,
        @Qualifier("engineObjectMapper") ObjectMapper objectMapper,
        EngineMetrics metrics,
        OutboxPublisher outboxPublisher
    ) {
        this.jdbc = jdbc;
        this.holdingUpdater = holdingUpdater;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
        this.outboxPublisher = outboxPublisher;
    }

    @Transactional
    public void executeBatch(List<FillCommand> cmds) {
        if (cmds.isEmpty()) return;

        int[] updated = jdbc.batchUpdate(
            "UPDATE orders SET status='FILLED', filled_price=?, filled_at=? " +
                "WHERE order_id=? AND status='PENDING'",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    FillCommand cmd = cmds.get(i);
                    ps.setBigDecimal(1, cmd.executedPrice());
                    ps.setTimestamp(2, Timestamp.valueOf(cmd.executedAt()));
                    ps.setLong(3, cmd.order().orderId());
                }

                @Override
                public int getBatchSize() {
                    return cmds.size();
                }
            }
        );

        List<FillCommand> succeeded = new ArrayList<>(cmds.size());
        for (int i = 0; i < cmds.size(); i++) {
            if (updated[i] > 0) {
                succeeded.add(cmds.get(i));
            } else {
                log.debug("fill skipped orderId={} already non-pending", cmds.get(i).order().orderId());
            }
        }
        if (succeeded.isEmpty()) return;

        jdbc.batchUpdate(
            "UPDATE wallet_balance SET locked = locked - ? WHERE wallet_id = ? AND coin_id = ?",
            new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    OrderDetail o = succeeded.get(i).order();
                    ps.setBigDecimal(1, o.lockedAmount());
                    ps.setLong(2, o.walletId());
                    ps.setLong(3, o.lockedCoinId());
                }

                @Override
                public int getBatchSize() {
                    return succeeded.size();
                }
            }
        );

        List<OrderFilledEvent> events = new ArrayList<>(succeeded.size());
        List<String> payloads = new ArrayList<>(succeeded.size());
        for (FillCommand cmd : succeeded) {
            OrderDetail o = cmd.order();
            OrderFilledEvent event = new OrderFilledEvent(
                o.orderId(), o.userId(), cmd.executedPrice(), o.quantity(), cmd.executedAt(), cmd.matchedAt()
            );
            events.add(event);
            try {
                payloads.add(objectMapper.writeValueAsString(event));
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("outbox payload serialization failed", e);
            }
        }

        Timestamp createdAt = Timestamp.valueOf(LocalDateTime.now());
        List<Long> outboxIds = jdbc.execute((java.sql.Connection conn) -> {
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO outbox (event_type, payload, created_at, matched_at) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            )) {
                for (int i = 0; i < payloads.size(); i++) {
                    ps.setString(1, ORDER_FILLED);
                    ps.setString(2, payloads.get(i));
                    ps.setTimestamp(3, createdAt);
                    ps.setTimestamp(4, Timestamp.valueOf(succeeded.get(i).matchedAt()));
                    ps.addBatch();
                }
                ps.executeBatch();
                List<Long> ids = new ArrayList<>(payloads.size());
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    while (rs.next()) {
                        ids.add(rs.getLong(1));
                    }
                }
                return ids;
            }
        });

        holdingUpdater.apply(succeeded);

        metrics.matches().increment(succeeded.size());

        if (outboxIds != null && outboxIds.size() == events.size()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    outboxPublisher.publishAsync(outboxIds, events);
                }
            });
        } else {
            log.warn(
                "outbox generated key count mismatch ids={} events={}; polling will pick up",
                outboxIds == null ? -1 : outboxIds.size(),
                events.size()
            );
        }
    }
}
