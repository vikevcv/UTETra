package com.ute.trasua.service.impl;

import com.ute.trasua.dto.kitchen.CupStickerDto;
import com.ute.trasua.entity.Order;
import com.ute.trasua.entity.OrderDetail;
import com.ute.trasua.repository.OrderRepository;
import com.ute.trasua.service.StickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Triển khai dịch vụ in tem dán ly theo biểu mẫu QLPC_BM1 (UC09).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StickerServiceImpl implements StickerService {

    private final OrderRepository orderRepository;

    private static final DateTimeFormatter STICKER_TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @Override
    @Transactional(readOnly = true)
    public List<CupStickerDto> printCupStickers(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng ID: " + orderId));

        String branchName = (order.getBranch() != null) ? order.getBranch().getName() : "UTETra Store";
        String printedAt = LocalDateTime.now().format(STICKER_TIME_FMT);

        // Tính tổng số lượng ly thực tế
        int totalCups = 0;
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                totalCups += (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;
            }
        }
        if (totalCups == 0) totalCups = 1;

        List<CupStickerDto> stickers = new ArrayList<>();
        int currentCupIndex = 1;

        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                int qty = (d.getQuantity() != null && d.getQuantity() > 0) ? d.getQuantity() : 1;

                List<String> toppings = (d.getToppings() != null)
                        ? d.getToppings().stream().map(t -> t.getToppingName()).collect(Collectors.toList())
                        : new ArrayList<>();
                String toppingsSummary = toppings.isEmpty() ? "Không topping" : String.join(", ", toppings);

                for (int q = 0; q < qty; q++) {
                    CupStickerDto sticker = CupStickerDto.builder()
                            .orderCode(order.getOrderCode())
                            .cupIndex(currentCupIndex++)
                            .totalCups(totalCups)
                            .branchName(branchName)
                            .productName(d.getProductName())
                            .sizeName(d.getSizeName() != null ? d.getSizeName() : "M")
                            .sugarLevel(d.getSugarLevel() != null ? d.getSugarLevel() : "100%")
                            .iceLevel(d.getIceLevel() != null ? d.getIceLevel() : "100%")
                            .toppings(toppings)
                            .toppingsSummary(toppingsSummary)
                            .customerNote(d.getNote() != null ? d.getNote() : (order.getNote() != null ? order.getNote() : ""))
                            .dailyQueueNumber(order.getDailyQueueNumber() != null ? order.getDailyQueueNumber() : 1)
                            .printedAt(printedAt)
                            .isReprint(false)
                            .build();
                    stickers.add(sticker);
                }
            }
        }

        log.info(">>> Đã xuất biểu mẫu QLPC_BM1 in {} tem ly cho đơn {}", stickers.size(), order.getOrderCode());
        return stickers;
    }

    @Override
    @Transactional(readOnly = true)
    public CupStickerDto printSingleCupSticker(Long orderId, Integer cupIndex) {
        List<CupStickerDto> allStickers = printCupStickers(orderId);
        int targetIdx = (cupIndex != null && cupIndex > 0) ? cupIndex : 1;

        for (CupStickerDto s : allStickers) {
            if (s.getCupIndex().equals(targetIdx)) {
                s.setIsReprint(true);
                log.info(">>> Đã in lại tem ly số {}/{} cho đơn {}", targetIdx, s.getTotalCups(), s.getOrderCode());
                return s;
            }
        }

        if (!allStickers.isEmpty()) {
            CupStickerDto first = allStickers.get(0);
            first.setIsReprint(true);
            return first;
        }

        throw new IllegalArgumentException("Không tìm thấy tem ly thứ " + cupIndex + " của đơn hàng ID: " + orderId);
    }
}
