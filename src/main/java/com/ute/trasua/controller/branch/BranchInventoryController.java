package com.ute.trasua.controller.branch;

import com.ute.trasua.dto.BranchInventoryResponse;
import com.ute.trasua.dto.BranchResponse;
import com.ute.trasua.dto.IngredientResponse;
import com.ute.trasua.dto.ImportReceiptRequest;
import com.ute.trasua.dto.ImportReceiptResponse;
import com.ute.trasua.service.BranchService;
import com.ute.trasua.service.IngredientService;
import com.ute.trasua.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for branch stock tracking, shortage alerts, and BM2 import receipts (Task 4.4 / Module 14 / UC29).
 */
@Controller
@RequestMapping("/branch/inventory")
@RequiredArgsConstructor
@Slf4j
public class BranchInventoryController {

    private final InventoryService inventoryService;
    private final BranchService branchService;
    private final IngredientService ingredientService;

    @GetMapping
    public String index(@RequestParam(value = "branchId", required = false) Long branchId) {
        return "redirect:/branch/inventory/stock" + (branchId != null ? "?branchId=" + branchId : "");
    }

    /**
     * Display real-time branch inventory with low-stock warnings.
     */
    @GetMapping("/stock")
    public String showStockList(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "filter", defaultValue = "all") String filter,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long targetBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        List<BranchResponse> allBranches = branchService.getAllBranches();

        Boolean isLowStockFilter = null;
        if ("low_stock".equalsIgnoreCase(filter)) {
            isLowStockFilter = true;
        }

        Page<BranchInventoryResponse> stockPage = Page.empty();
        long totalItems = 0;
        long lowStockCount = 0;
        long outOfStockCount = 0;

        if (targetBranchId != null) {
            Pageable pageable = PageRequest.of(Math.max(0, page), size);
            stockPage = inventoryService.getStockByBranchPaged(targetBranchId, keyword, isLowStockFilter, pageable);
            lowStockCount = inventoryService.countLowStock(targetBranchId);
            outOfStockCount = inventoryService.countOutOfStock(targetBranchId);

            List<BranchInventoryResponse> allStock = inventoryService.getStockByBranch(targetBranchId, null, null);
            totalItems = allStock.size();
        }

        long safeStockCount = Math.max(0, totalItems - lowStockCount);

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("stockPage", stockPage);
        model.addAttribute("stockList", stockPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("filter", filter);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("safeStockCount", safeStockCount);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
        model.addAttribute("pageTitle", "Theo Dõi Tồn Kho Chi Nhánh (UC29)");

        return "branch/inventory/stock-list";
    }

    /**
     * Update minimum threshold for a branch ingredient.
     */
    @PostMapping("/stock/update-threshold")
    public String updateThreshold(
            @RequestParam("branchId") Long branchId,
            @RequestParam("ingredientId") Long ingredientId,
            @RequestParam("minThreshold") BigDecimal minThreshold,
            RedirectAttributes redirectAttributes
    ) {
        try {
            inventoryService.updateMinThreshold(branchId, ingredientId, minThreshold);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật ngưỡng cảnh báo tồn tối thiểu thành công!");
        } catch (Exception e) {
            log.error("Lỗi cập nhật ngưỡng tối thiểu: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/branch/inventory/stock?branchId=" + branchId;
    }

    /**
     * Show import receipt creation form following BM2 standard (UC29).
     */
    @GetMapping("/import")
    public String showImportForm(
            @RequestParam(value = "branchId", required = false) Long branchId,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        if (currentBranch == null) {
            return "redirect:/branch/inventory/stock";
        }

        List<BranchResponse> allBranches = branchService.getAllBranches();
        List<IngredientResponse> ingredients = ingredientService.getActiveIngredients();

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String defaultCode = String.format("PNK-%s-%s-XXX", 
                currentBranch.getBranchCode(), 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        String currentManager = (authentication != null) ? authentication.getName() : "Quản lý chi nhánh";

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("ingredients", ingredients);
        model.addAttribute("todayFormatted", today);
        model.addAttribute("defaultCode", defaultCode);
        model.addAttribute("currentManager", currentManager);
        model.addAttribute("pageTitle", "Lập Phiếu Nhập Nguyên Liệu Chi Nhánh (BM2)");

        return "branch/inventory/import-form";
    }

    /**
     * Handle submission of BM2 import receipt (UC29).
     */
    @PostMapping("/import")
    public String processImportReceipt(
            @RequestParam("branchId") Long branchId,
            @ModelAttribute ImportReceiptRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String username = (authentication != null) ? authentication.getName() : null;
        try {
            ImportReceiptResponse response = inventoryService.createImportReceipt(branchId, username, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Đã lập thành công phiếu nhập kho BM2: " + response.getReceiptCode() 
                    + " (Tổng giá trị: " + response.getFormattedTotalAmount() + ")!");
            return "redirect:/branch/inventory/stock?branchId=" + branchId;
        } catch (Exception e) {
            log.error("Lỗi khi lập phiếu nhập kho: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/branch/inventory/import?branchId=" + branchId;
        }
    }

    /**
     * Show import receipts history for branch with filtering and pagination (Task 4.5 / UC29).
     */
    @GetMapping("/history")
    public String showImportHistory(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Authentication authentication,
            Model model
    ) {
        BranchResponse currentBranch = resolveCurrentBranch(branchId, authentication);
        Long targetBranchId = (currentBranch != null) ? currentBranch.getId() : null;

        List<BranchResponse> allBranches = branchService.getAllBranches();

        LocalDateTime startDateTime = null;
        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                startDateTime = java.time.LocalDate.parse(startDateStr.trim()).atStartOfDay();
            } catch (Exception ignored) {}
        }

        LocalDateTime endDateTime = null;
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                endDateTime = java.time.LocalDate.parse(endDateStr.trim()).atTime(23, 59, 59);
            } catch (Exception ignored) {}
        }

        String filterStatus = ("ALL".equalsIgnoreCase(status) || (status != null && status.isBlank())) ? null : status;

        Page<ImportReceiptResponse> receiptsPage = Page.empty();
        long totalReceiptsCount = 0;
        BigDecimal totalImportAmount = BigDecimal.ZERO;
        long centralWarehouseCount = 0;
        long authorizedSupplierCount = 0;

        if (targetBranchId != null) {
            Pageable pageable = PageRequest.of(Math.max(0, page), size);
            receiptsPage = inventoryService.getReceiptsByBranchPaged(targetBranchId, keyword, filterStatus, startDateTime, endDateTime, pageable);

            List<ImportReceiptResponse> allFiltered = inventoryService.getReceiptsByBranch(targetBranchId, keyword, filterStatus, startDateTime, endDateTime);
            totalReceiptsCount = allFiltered.size();
            for (ImportReceiptResponse r : allFiltered) {
                if (r.getTotalAmount() != null) {
                    totalImportAmount = totalImportAmount.add(r.getTotalAmount());
                }
                if ("CENTRAL_WAREHOUSE".equalsIgnoreCase(r.getSupplierType()) || "KhoTong".equalsIgnoreCase(r.getSupplierType())) {
                    centralWarehouseCount++;
                } else {
                    authorizedSupplierCount++;
                }
            }
        }

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,### đ");
        String formattedTotalImportAmount = df.format(totalImportAmount);

        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("receiptsPage", receiptsPage);
        model.addAttribute("receipts", receiptsPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status != null ? status : "ALL");
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        model.addAttribute("totalReceiptsCount", totalReceiptsCount);
        model.addAttribute("totalImportAmount", totalImportAmount);
        model.addAttribute("formattedTotalImportAmount", formattedTotalImportAmount);
        model.addAttribute("centralWarehouseCount", centralWarehouseCount);
        model.addAttribute("authorizedSupplierCount", authorizedSupplierCount);
        model.addAttribute("pageTitle", "Lịch Sử Phiếu Nhập Kho (BM2)");

        return "branch/inventory/import-history";
    }

    /**
     * View BM2 import receipt details and print preview (Task 4.5 / UC29).
     */
    @GetMapping("/detail/{id}")
    public String showReceiptDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "print", defaultValue = "false") boolean print,
            Authentication authentication,
            Model model
    ) {
        ImportReceiptResponse receipt = inventoryService.getReceiptById(id);
        BranchResponse currentBranch = null;
        if (receipt.getBranchId() != null) {
            try {
                currentBranch = branchService.getBranchById(receipt.getBranchId());
            } catch (Exception ignored) {}
        }
        if (currentBranch == null) {
            currentBranch = resolveCurrentBranch(null, authentication);
        }

        List<BranchResponse> allBranches = branchService.getAllBranches();

        model.addAttribute("receipt", receipt);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("allBranches", allBranches);
        model.addAttribute("autoPrint", print);
        model.addAttribute("pageTitle", "Chi Tiết Phiếu Nhập Kho: " + receipt.getReceiptCode() + " (BM2)");

        return "branch/inventory/import-detail";
    }

    /**
     * Dedicated direct print endpoint for BM2 receipt.
     */
    @GetMapping("/print/{id}")
    public String printReceipt(@PathVariable("id") Long id) {
        return "redirect:/branch/inventory/detail/" + id + "?print=true";
    }

    private BranchResponse resolveCurrentBranch(Long requestedBranchId, Authentication authentication) {
        if (requestedBranchId != null) {
            try {
                return branchService.getBranchById(requestedBranchId);
            } catch (Exception ignored) {}
        }

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            var branchOpt = branchService.getBranchByManagerUsername(username);
            if (branchOpt.isPresent()) {
                return branchOpt.get();
            }
        }

        List<BranchResponse> all = branchService.getAllBranches();
        return all.isEmpty() ? null : all.get(0);
    }
}
