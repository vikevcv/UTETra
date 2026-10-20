package com.ute.trasua.controller;

import com.ute.trasua.dto.ProductResponse;
import com.ute.trasua.service.ProductReviewService;
import com.ute.trasua.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
@Slf4j
public class ProductDetailController {

    private final ProductService productService;
    private final ProductReviewService productReviewService;

    @GetMapping("/{id}")
    public String viewProductDetail(@PathVariable("id") Long id, Model model) {
        ProductResponse product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/";
        }

        Map<String, Object> reviewSummary = productReviewService.getProductReviewSummary(id, null, 10, false);

        model.addAttribute("product", product);
        model.addAttribute("reviewSummary", reviewSummary);
        model.addAttribute("siteTitle", product.getName() + " — UTETra Zen Gallery");
        return "product/detail";
    }
}
