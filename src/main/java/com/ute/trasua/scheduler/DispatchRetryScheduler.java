package com.ute.trasua.scheduler;

import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.DriverDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Vòng quét nền tự động gán đơn chờ cho tài xế (Module 9 - UC12).
 *
 * <p>Mục đích: đơn ĐÃ ĐƯỢC NHÂN VIÊN DÁN TEM BÀN GIAO nhưng chưa có tài xế (hoặc tài xế vừa báo sự cố
 * trả đơn về quán) sẽ KHÔNG nằm chết chờ nhân viên bấm tay nữa. Cứ 15 giây quét lại, tài xế nào vừa
 * bật ca ONLINE là đơn tự chảy sang ngay.</p>
 *
 * <p>Đơn vừa pha chế xong mà nhân viên CHƯA bấm nút bàn giao thì không có mốc dán tem, nên tuyệt đối
 * không bị vòng quét này đụng vào.</p>
 *
 * <p>Token/DB cost: khi không có đơn chờ, chỉ chạy đúng 1 câu truy vấn rỗng trên index
 * idx_orders_branch_status_driver rồi thoát, không quét từng chi nhánh, không đụng bảng tài xế.</p>
 *
 * <p>Có thể chỉnh chu kỳ bằng property {@code dispatch.retry-interval-ms} (mặc định 15000ms).</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DispatchRetryScheduler {

    private final DriverDispatchService driverDispatchService;
    private final OrderRepository orderRepository;

    @Scheduled(
            fixedDelayString = "${dispatch.retry-interval-ms:15000}",
            initialDelayString = "${dispatch.retry-initial-delay-ms:20000}"
    )
    public void retryPendingDispatch() {
        List<Long> branchIds;
        try {
            branchIds = orderRepository.findBranchIdsWithSealedDelivery();
        } catch (Exception e) {
            log.warn(">>> [DISPATCH RETRY] Không truy vấn được hàng đợi đơn chờ: {}", e.getMessage());
            return;
        }

        for (Long branchId : branchIds) {
            try {
                int assigned = driverDispatchService.autoDispatchReadyOrders(branchId);
                if (assigned > 0) {
                    log.info(">>> [DISPATCH RETRY] Chi nhánh {} đã tự động gán {} đơn chờ cho tài xế trực ca",
                            branchId, assigned);
                }
            } catch (Exception e) {
                // Một chi nhánh lỗi không được làm chết cả vòng quét của các chi nhánh khác
                log.warn(">>> [DISPATCH RETRY] Bỏ qua chi nhánh {}: {}", branchId, e.getMessage());
            }
        }
    }
}
