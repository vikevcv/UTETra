package com.ute.trasua.service;

import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * DispatchNotifier đảm bảo các thông báo WebSocket STOMP CHỈ phát sau khi Transaction commit thành công.
 * Ngăn chặn tuyệt đối "thông báo ma" khi database rollback (Pre-Mortem #3).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchNotifier {

    private final OrderWebSocketPublisher orderWebSocketPublisher;

    public void notifyOrderDispatched(Order order, Driver driver) {
        dispatchAfterCommit(() -> orderWebSocketPublisher.broadcastOrderDispatched(order, driver));
    }

    public void notifyDriverIncident(Order order, Driver driver, String reason, String note) {
        dispatchAfterCommit(() -> orderWebSocketPublisher.broadcastDriverIncident(order, driver, reason, note));
    }

    public void notifyOrderCancelled(Order order, String reason) {
        dispatchAfterCommit(() -> orderWebSocketPublisher.broadcastOrderCancelled(order, reason));
    }

    public void notifyQueueUnassigned(Long branchId, int unassignedCount, List<String> orderCodes) {
        dispatchAfterCommit(() -> orderWebSocketPublisher.broadcastQueueUnassigned(branchId, unassignedCount, orderCodes));
    }

    private void dispatchAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        action.run();
                    } catch (Exception e) {
                        log.warn("Lỗi khi phát WebSocket sau commit: {}", e.getMessage());
                    }
                }
            });
        } else {
            action.run();
        }
    }
}
