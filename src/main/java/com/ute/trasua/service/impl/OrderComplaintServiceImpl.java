package com.ute.trasua.service.impl;

import com.ute.trasua.dto.complaint.ComplaintResolveRequest;
import com.ute.trasua.dto.complaint.ComplaintSubmitRequest;
import com.ute.trasua.dto.complaint.OrderComplaintDto;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderComplaint;
import com.ute.trasua.entity.Promotion;
import com.ute.trasua.entity.User;
import com.ute.trasua.repository.OrderComplaintRepository;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.OrderComplaintService;
import com.ute.trasua.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderComplaintServiceImpl implements OrderComplaintService {

    private final OrderComplaintRepository orderComplaintRepository;
    private final OrderRepository orderRepository;
    private final PromotionService promotionService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    @Transactional
    public OrderComplaintDto submitComplaint(String orderCode, ComplaintSubmitRequest request, User currentUser) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không hợp lệ.");
        }
        if (request == null || request.getComplaintType() == null || request.getComplaintType().isBlank()) {
            throw new IllegalArgumentException("Vui lòng chọn loại khiếu nại.");
        }
        if (request.getDescription() == null || request.getDescription().trim().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mô tả sự cố.");
        }

        Order order = orderRepository.findByOrderCode(orderCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng: " + orderCode));

        // 1. Kiểm tra trạng thái đơn: Bắt buộc phải là COMPLETED hoặc DELIVERED
        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if (!"COMPLETED".equals(status) && !"DELIVERED".equals(status)) {
            throw new IllegalStateException("Đơn hàng chưa hoàn tất, chưa thể gửi khiếu nại.");
        }

        // 2. Chặn spam: Mỗi đơn hàng chỉ được phép có 1 khiếu nại duy nhất
        if (orderComplaintRepository.existsByOrderId(order.getId())) {
            throw new IllegalStateException("Đơn hàng này đã được gửi khiếu nại trước đó. Vui lòng chờ phản hồi từ quản trị viên.");
        }

        // 3. Kiểm tra phân quyền nếu đơn gắn tài khoản
        if (currentUser != null && order.getUser() != null) {
            boolean isOwner = order.getUser().getId().equals(currentUser.getId());
            boolean isStaff = currentUser.getRoles() != null && currentUser.getRoles().stream()
                    .anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r) || "ROLE_MANAGER".equalsIgnoreCase(r));
            if (!isOwner && !isStaff) {
                throw new IllegalStateException("Bạn không có quyền khiếu nại đơn hàng này.");
            }
        }

        String customerName = "Khách hàng";
        if (currentUser != null && currentUser.getFullName() != null && !currentUser.getFullName().isBlank()) {
            customerName = currentUser.getFullName().trim();
        } else if (order.getCustomerName() != null && !order.getCustomerName().isBlank()) {
            customerName = order.getCustomerName().trim();
        }

        String phone = order.getCustomerPhone();

        OrderComplaint complaint = OrderComplaint.builder()
                .order(order)
                .user(currentUser != null ? currentUser : order.getUser())
                .customerName(customerName)
                .customerPhone(phone)
                .complaintType(request.getComplaintType().trim().toUpperCase())
                .description(request.getDescription().trim())
                .status("PENDING")
                .build();

        OrderComplaint saved = orderComplaintRepository.save(complaint);
        log.info(">>> Đã tiếp nhận khiếu nại #{} cho đơn hàng {}", saved.getId(), orderCode);

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderComplaintDto getComplaintByOrderCode(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return null;
        }
        return orderComplaintRepository.findByOrder_OrderCode(orderCode.trim())
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderComplaintDto> getAllComplaints(String status) {
        List<OrderComplaint> list;
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            list = orderComplaintRepository.findByStatusOrderByCreatedAtDesc(status.trim().toUpperCase());
        } else {
            list = orderComplaintRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderComplaintDto resolveComplaint(Long complaintId, ComplaintResolveRequest request, User adminUser) {
        if (complaintId == null) {
            throw new IllegalArgumentException("ID khiếu nại không hợp lệ.");
        }
        if (request == null || request.getAction() == null || request.getAction().isBlank()) {
            throw new IllegalArgumentException("Thiếu quyết định xử lý khiếu nại.");
        }

        OrderComplaint complaint = orderComplaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khiếu nại ID: " + complaintId));

        if (!"PENDING".equalsIgnoreCase(complaint.getStatus())) {
            throw new IllegalStateException("Khiếu nại này đã được xử lý trước đó (trạng thái hiện tại: " + complaint.getStatus() + ").");
        }

        String action = request.getAction().trim().toUpperCase();
        String responseNote = (request.getAdminResponse() != null) ? request.getAdminResponse().trim() : "";

        if ("APPROVE".equals(action) || "RESOLVED".equals(action)) {
            // Tự động sinh voucher bồi hoàn 100% (hoặc rate tùy chỉnh)
            BigDecimal rate = (request.getCompensationRate() != null && request.getCompensationRate().compareTo(BigDecimal.ZERO) > 0)
                    ? request.getCompensationRate()
                    : new BigDecimal("1.00");

            Promotion voucher = promotionService.createCompensationVoucher(complaint.getOrder(), rate, 30);
            complaint.setStatus("RESOLVED");
            complaint.setCompensationVoucherCode(voucher.getVoucherCode());
            complaint.setCompensationAmount(voucher.getDiscountValue());
            complaint.setAdminResponse(responseNote.isBlank() ? "Quán đã tiếp nhận và bồi hoàn voucher 100% cho quý khách." : responseNote);
            log.info(">>> Đã duyệt bồi hoàn voucher {} cho khiếu nại #{}", voucher.getVoucherCode(), complaintId);
        } else if ("REJECT".equals(action) || "REJECTED".equals(action)) {
            complaint.setStatus("REJECTED");
            complaint.setAdminResponse(responseNote.isBlank() ? "Khiếu nại không đủ điều kiện xử lý sau khi kiểm tra đối soát." : responseNote);
            log.info(">>> Đã từ chối khiếu nại #{}", complaintId);
        } else {
            throw new IllegalArgumentException("Hành động xử lý không hợp lệ: " + action);
        }

        complaint.setResolvedBy(adminUser);
        complaint.setResolvedAt(LocalDateTime.now());

        OrderComplaint saved = orderComplaintRepository.save(complaint);
        return mapToDto(saved);
    }

    private OrderComplaintDto mapToDto(OrderComplaint complaint) {
        if (complaint == null) return null;

        Order order = complaint.getOrder();
        String formattedCreated = complaint.getCreatedAt() != null ? complaint.getCreatedAt().format(DATE_TIME_FORMATTER) : "";
        String formattedResolved = complaint.getResolvedAt() != null ? complaint.getResolvedAt().format(DATE_TIME_FORMATTER) : "";

        String typeLabel = mapComplaintTypeLabel(complaint.getComplaintType());
        String statusLabel = mapStatusLabel(complaint.getStatus());

        return OrderComplaintDto.builder()
                .id(complaint.getId())
                .orderId(order != null ? order.getId() : null)
                .orderCode(order != null ? order.getOrderCode() : "")
                .customerName(complaint.getCustomerName())
                .customerPhone(complaint.getCustomerPhone())
                .branchName(order != null && order.getBranch() != null ? order.getBranch().getName() : "Chi nhánh UTETra")
                .complaintType(complaint.getComplaintType())
                .complaintTypeLabel(typeLabel)
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .statusLabel(statusLabel)
                .compensationVoucherCode(complaint.getCompensationVoucherCode())
                .compensationAmount(complaint.getCompensationAmount())
                .adminResponse(complaint.getAdminResponse())
                .resolvedByUsername(complaint.getResolvedBy() != null ? complaint.getResolvedBy().getUsername() : null)
                .createdAt(complaint.getCreatedAt())
                .formattedCreatedAt(formattedCreated)
                .resolvedAt(complaint.getResolvedAt())
                .formattedResolvedAt(formattedResolved)
                .build();
    }

    private String mapComplaintTypeLabel(String type) {
        if (type == null) return "Sự cố khác";
        switch (type.toUpperCase()) {
            case "SAI_MON": return "Giao sai món / Sai đường đá";
            case "THIEU_MON": return "Thiếu món / Thiếu topping";
            case "HU_HAI": return "Ly trà bị đổ vỡ / Hư hỏng";
            case "CHAT_LUONG": return "Chất lượng không đạt";
            case "THAI_DO": return "Thái độ phục vụ";
            default: return "Lý do khác";
        }
    }

    private String mapStatusLabel(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status.toUpperCase()) {
            case "PENDING": return "Chờ xử lý";
            case "RESOLVED": return "Đã bồi hoàn";
            case "REJECTED": return "Đã từ chối";
            default: return status;
        }
    }
}
