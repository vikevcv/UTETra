package com.ute.trasua.service.impl;

import com.ute.trasua.dto.pos.ReceiptResponse;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.entity.Payment;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.repository.PaymentRepository;
import com.ute.trasua.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReceiptServiceImpl implements ReceiptService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @org.springframework.beans.factory.annotation.Value("${app.vietqr.bank-bin:970422}")
    private String bankBin;

    @org.springframework.beans.factory.annotation.Value("${app.vietqr.account-no:0396202885}")
    private String accountNo;

    @org.springframework.beans.factory.annotation.Value("${app.vietqr.account-name:UTETRA TEA GALLERY}")
    private String accountName;

    @Override
    @Transactional(readOnly = true)
    public ReceiptResponse printReceipt(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        Payment payment = paymentRepository.findByOrderId(orderId).orElse(null);

        String branchName = (order.getBranch() != null) ? order.getBranch().getName() : "UTETra Zen Gallery - Chi nhánh chính";
        String branchAddress = (order.getBranch() != null) ? order.getBranch().getAddress() : "01 Võ Văn Ngân, TP. Thủ Đức";
        String branchHotline = (order.getBranch() != null && order.getBranch().getHotline() != null) ? order.getBranch().getHotline() : "1900 6868";

        List<ReceiptResponse.ReceiptItemDto> items = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                List<String> toppingNames = (d.getToppings() != null) ?
                        d.getToppings().stream().map(t -> t.getToppingName()).collect(Collectors.toList()) : new ArrayList<>();

                items.add(ReceiptResponse.ReceiptItemDto.builder()
                        .productName(d.getProductName())
                        .sizeName(d.getSizeName())
                        .sugarLevel(d.getSugarLevel())
                        .iceLevel(d.getIceLevel())
                        .toppings(toppingNames)
                        .quantity(d.getQuantity())
                        .unitPrice(d.getUnitPrice())
                        .subtotal(d.getSubtotal())
                        .note(d.getNote())
                        .build());
            }
        }

        String vietQrQuickUrl = "";
        if (order.getFinalAmount() != null) {
            String encodedCode = URLEncoder.encode(order.getOrderCode(), StandardCharsets.UTF_8);
            String encodedName = URLEncoder.encode(accountName != null ? accountName : "UTETRA TEA GALLERY", StandardCharsets.UTF_8);
            vietQrQuickUrl = String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                    bankBin != null ? bankBin : "970422",
                    accountNo != null ? accountNo : "0396202885",
                    order.getFinalAmount().longValue(), encodedCode, encodedName);
        }

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedTime = (order.getCreatedAt() != null) ? order.getCreatedAt().format(dtf) : java.time.LocalDateTime.now().format(dtf);

        return ReceiptResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .dailyQueueNumber(order.getDailyQueueNumber())
                .branchName(branchName)
                .branchAddress(branchAddress)
                .branchHotline(branchHotline)
                .cashierName(order.getCashierUsername() != null ? order.getCashierUsername() : "Thu ngân quầy")
                .orderTime(order.getCreatedAt())
                .formattedOrderTime(formattedTime)
                .items(items)
                .totalAmount(order.getTotalAmount())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod() : "CASH")
                .cashTendered(payment != null ? payment.getCashTendered() : null)
                .changeAmount(payment != null ? payment.getChangeAmount() : null)
                .vietQrQuickUrl(vietQrQuickUrl)
                .build();
    }
}
