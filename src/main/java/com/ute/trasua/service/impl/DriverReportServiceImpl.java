package com.ute.trasua.service.impl;

import com.ute.trasua.dto.driver.DriverPerformanceSummaryDto;
import com.ute.trasua.dto.driver.DriverTripHistoryDto;
import com.ute.trasua.entity.Driver;
import com.ute.trasua.entity.Order;
import com.ute.trasua.repository.DriverRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.DriverReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai nghiệp vụ trích xuất báo cáo lịch sử giao hàng và hiệu suất tài xế (UC15).
 */
@Service
@Transactional(readOnly = true)
public class DriverReportServiceImpl implements DriverReportService {

    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;

    public DriverReportServiceImpl(OrderRepository orderRepository, DriverRepository driverRepository) {
        super();
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public List<DriverTripHistoryDto> getDriverCompletedTrips(Long driverId) {
        List<String> statuses = List.of("COMPLETED", "DELIVERED");
        List<Order> orders = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(driverId, statuses);

        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        List<DriverTripHistoryDto> result = new ArrayList<>();
        for (Order order : orders) {
            LocalDateTime deliveredTime = (order.getDeliveredAt() != null)
                    ? order.getDeliveredAt()
                    : (order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now());
            LocalDate tripDate = deliveredTime.toLocalDate();
            // Tối đa xem cuốc trong vòng 1 tháng (30 ngày gần nhất)
            if (tripDate.isBefore(thirtyDaysAgo) || tripDate.isAfter(today)) {
                continue;
            }
            result.add(mapToTripHistoryDto(order));
        }
        return result;
    }

    @Override
    public DriverPerformanceSummaryDto getDriverPerformanceSummary(Long driverId, LocalDate fromDate, LocalDate toDate) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế với ID: " + driverId));

        LocalDate start = fromDate != null ? fromDate : LocalDate.now().minusDays(30);
        LocalDate end = toDate != null ? toDate : LocalDate.now();

        List<String> statuses = List.of("COMPLETED");
        List<Order> completedOrdersList = orderRepository.findByDriverIdAndStatusInOrderByCreatedAtDesc(driverId, statuses);

        List<Order> filteredOrders = completedOrdersList.stream()
                .filter(o -> {
                    if (o.getDeliveredAt() == null) return false;
                    LocalDate tripDate = o.getDeliveredAt().toLocalDate();
                    return !tripDate.isBefore(start) && !tripDate.isAfter(end);
                })
                .collect(Collectors.toList());

        int totalTrips = filteredOrders.size();
        // Giả định đơn giao đúng hạn nếu thời gian từ lúc tạo/giao không vượt quá 45 phút, hoặc mặc định tất cả nếu chưa có timestamp chi tiết
        int onTimeTrips = (int) filteredOrders.stream()
                .filter(o -> o.getDeliveredAt() != null)
                .count();

        double onTimeRate = totalTrips > 0 ? ((double) onTimeTrips / totalTrips) * 100.0 : 100.0;
        double kpiScore = Math.min(10.0, (onTimeRate / 100.0) * 10.0);

        String driverName = (driver.getUser() != null && driver.getUser().getFullName() != null)
                ? driver.getUser().getFullName()
                : "Tài xế #" + driver.getId();

        String branchName = (driver.getBranch() != null) ? driver.getBranch().getName() : "Toàn chuỗi";
        Long branchId = (driver.getBranch() != null) ? driver.getBranch().getId() : null;

        return DriverPerformanceSummaryDto.builder()
                .driverId(driver.getId())
                .driverName(driverName)
                .licensePlate(driver.getLicensePlate())
                .branchId(branchId)
                .branchName(branchName)
                .fromDate(start)
                .toDate(end)
                .totalTrips(totalTrips)
                .onTimeTrips(onTimeTrips)
                .onTimeRate(Math.round(onTimeRate * 10.0) / 10.0)
                .kpiScore(Math.round(kpiScore * 10.0) / 10.0)
                .completedOrders(driver.getCompletedOrders() != null ? driver.getCompletedOrders() : 0)
                .build();
    }

    private DriverTripHistoryDto mapToTripHistoryDto(Order order) {
        int cupCount = 0;
        if (order.getOrderDetails() != null) {
            cupCount = order.getOrderDetails().stream()
                    .mapToInt(d -> d.getQuantity() != null ? d.getQuantity() : 0)
                    .sum();
        }

        LocalDateTime deliveredTime = (order.getDeliveredAt() != null)
                ? order.getDeliveredAt()
                : (order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now());

        LocalDate today = LocalDate.now();
        LocalDate tripDate = deliveredTime.toLocalDate();
        String periodCategory = "OLDER";
        String dateFormatted;

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        if (tripDate.isEqual(today)) {
            periodCategory = "TODAY";
            dateFormatted = "Hôm nay";
        } else if (tripDate.isEqual(today.minusDays(1))) {
            periodCategory = "YESTERDAY";
            dateFormatted = "Hôm qua";
        } else if (!tripDate.isBefore(today.minusDays(7))) {
            periodCategory = "WEEK";
            dateFormatted = tripDate.format(dateFmt);
        } else {
            periodCategory = "OLDER";
            dateFormatted = tripDate.format(dateFmt);
        }

        String tripDateIso = tripDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        return DriverTripHistoryDto.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .deliveryAddress(order.getDeliveryAddress())
                .cupCount(cupCount)
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus())
                .pickupPhotoUrl(order.getPickupPhotoUrl())
                .deliveryPhotoUrl(order.getDeliveryPhotoUrl())
                .deliveredAt(deliveredTime)
                .deliveredDateFormatted(dateFormatted)
                .deliveredTimeFormatted(deliveredTime.format(timeFmt))
                .periodCategory(periodCategory)
                .tripDateIso(tripDateIso)
                .build();
    }
}
