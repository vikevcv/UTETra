package com.ute.trasua.controller.branch;

import com.ute.trasua.entity.Branch;
import com.ute.trasua.repository.BranchRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controller quản lý tình trạng thực đơn còn/hết hàng tại chi nhánh (UC11).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class BranchMenuController {

    private final BranchRepository branchRepository;

    @GetMapping({"/branch/menu-status", "/pos/menu-status"})
    public String branchMenuStatusView(@RequestParam(value = "branchId", required = false) Long branchId,
                                       HttpSession session,
                                       Model model) {
        List<Branch> branches = branchRepository.findAll();
        Branch currentBranch = null;

        if (branchId != null) {
            currentBranch = branchRepository.findById(branchId).orElse(null);
        } else {
            Long sessionBranchId = (Long) session.getAttribute("POS_BRANCH_ID");
            if (sessionBranchId != null) {
                currentBranch = branchRepository.findById(sessionBranchId).orElse(null);
            }
        }

        if (currentBranch == null && !branches.isEmpty()) {
            currentBranch = branches.get(0);
        }

        Long activeBranchId = (currentBranch != null) ? currentBranch.getId() : 1L;
        session.setAttribute("POS_BRANCH_ID", activeBranchId);

        String activeCashiers = (String) session.getAttribute("POS_ACTIVE_CASHIERS_NAME");
        if (activeCashiers == null || activeCashiers.isBlank()) {
            activeCashiers = "Quản lý / Barista";
        }

        model.addAttribute("branchId", activeBranchId);
        model.addAttribute("currentBranch", currentBranch);
        model.addAttribute("branchName", currentBranch != null ? currentBranch.getName() : "UTETra Flagship Thủ Đức");
        model.addAttribute("branches", branches);
        model.addAttribute("cashierUsername", activeCashiers);

        return "pos/menu_status";
    }
}
