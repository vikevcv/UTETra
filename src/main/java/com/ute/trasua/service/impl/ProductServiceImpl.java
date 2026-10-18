package com.ute.trasua.service.impl;

import com.ute.trasua.dto.ProductRequest;
import com.ute.trasua.dto.ProductResponse;
import com.ute.trasua.entity.Branch;
import com.ute.trasua.entity.BranchMenu;
import com.ute.trasua.entity.Category;
import com.ute.trasua.entity.Product;
import com.ute.trasua.repository.BranchMenuRepository;
import com.ute.trasua.repository.BranchRepository;
import com.ute.trasua.repository.CategoryRepository;
import com.ute.trasua.repository.ProductRepository;
import com.ute.trasua.service.CloudinaryService;
import com.ute.trasua.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Product/Menu item management (UC22).
 */
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BranchRepository branchRepository;
    private final BranchMenuRepository branchMenuRepository;
    private final com.ute.trasua.repository.OrderDetailRepository orderDetailRepository;
    private final CloudinaryService cloudinaryService;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BranchRepository branchRepository,
            BranchMenuRepository branchMenuRepository,
            com.ute.trasua.repository.OrderDetailRepository orderDetailRepository,
            CloudinaryService cloudinaryService
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.branchRepository = branchRepository;
        this.branchMenuRepository = branchMenuRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByStatusOrderByNameAsc("ACTIVE").stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> filterProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, String sortBy) {
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        List<Product> products = productRepository.filterActiveProducts(kw, categoryId, minPrice, maxPrice);

        List<ProductResponse> responses = products.stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            switch (sortBy.toLowerCase().trim()) {
                case "price_asc":
                    responses.sort(Comparator.comparing(ProductResponse::getBasePrice, Comparator.nullsLast(BigDecimal::compareTo)));
                    break;
                case "price_desc":
                    responses.sort(Comparator.comparing(ProductResponse::getBasePrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
                    break;
                case "rating":
                    responses.sort((p1, p2) -> {
                        int c1 = p1.getReviewCount() != null ? p1.getReviewCount() : 0;
                        int c2 = p2.getReviewCount() != null ? p2.getReviewCount() : 0;
                        boolean hasReviews1 = c1 > 0;
                        boolean hasReviews2 = c2 > 0;

                        // Sản phẩm có đánh giá luôn xếp trước sản phẩm không có đánh giá
                        if (hasReviews1 && !hasReviews2) return -1;
                        if (!hasReviews1 && hasReviews2) return 1;
                        if (!hasReviews1 && !hasReviews2) return 0;

                        // Cả hai đều có đánh giá: so sánh số sao trung bình giảm dần
                        double r1 = p1.getAverageRating() != null ? p1.getAverageRating() : 0.0;
                        double r2 = p2.getAverageRating() != null ? p2.getAverageRating() : 0.0;
                        int comp = Double.compare(r2, r1);
                        if (comp != 0) return comp;

                        // Nếu bằng sao: món có nhiều lượt đánh giá hơn xếp trước
                        return Integer.compare(c2, c1);
                    });
                    break;
                default:
                    // Giữ thứ tự ưu tiên mặc định từ query
                    break;
            }
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Long categoryId, String status, Pageable pageable) {
        Page<Product> page = productRepository.searchProducts(keyword, categoryId, status, pageable);
        return page.map(ProductResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = getProductEntityById(id);
        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + id));
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name must not be empty!");
        }

        String name = request.getName().trim();
        if (productRepository.existsByName(name)) {
            throw new IllegalArgumentException("Product with name [" + name + "] already exists!");
        }

        String productCode = request.getProductCode();
        if (productCode == null || productCode.trim().isEmpty()) {
            productCode = "SP-" + String.format("%04d", productRepository.count() + 1);
        } else {
            productCode = productCode.trim().toUpperCase();
            if (productRepository.existsByProductCode(productCode)) {
                throw new IllegalArgumentException("Product code [" + productCode + "] already exists!");
            }
        }

        if (request.getBasePrice() == null || request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price must be greater than or equal to 0!");
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category does not exist with ID: " + request.getCategoryId()));
        }

        String imageUrl = request.getCurrentImageUrl();
        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            log.info(">>> Uploading new product image to Cloudinary...");
            imageUrl = cloudinaryService.uploadImage(request.getImageFile(), "products");
        }

        Product product = Product.builder()
                .productCode(productCode)
                .name(name)
                .category(category)
                .basePrice(request.getBasePrice())
                .imageUrl(imageUrl)
                .description(request.getDescription() != null ? request.getDescription().trim() : null)
                .status(request.getStatus() != null ? request.getStatus().trim() : "ACTIVE")
                .build();

        Product saved = productRepository.save(product);
        log.info(">>> Created new Product: [{}] {}", saved.getProductCode(), saved.getName());

        // Tự động đồng bộ sản phẩm vào thực đơn của tất cả các chi nhánh đang hoạt động
        syncProductToAllBranches(saved);

        return ProductResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getProductEntityById(id);

        if (request.getProductCode() != null && !request.getProductCode().trim().isEmpty()) {
            String code = request.getProductCode().trim().toUpperCase();
            if (productRepository.existsByProductCodeAndIdNot(code, id)) {
                throw new IllegalArgumentException("Product code [" + code + "] is already used by another item!");
            }
            product.setProductCode(code);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String name = request.getName().trim();
            if (productRepository.existsByNameAndIdNot(name, id)) {
                throw new IllegalArgumentException("Product name [" + name + "] is already used by another item!");
            }
            product.setName(name);
        }

        if (request.getBasePrice() != null) {
            if (request.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Product price must be greater than or equal to 0!");
            }
            product.setBasePrice(request.getBasePrice());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
            String oldImageUrl = product.getImageUrl();
            log.info(">>> Uploading replacement image to Cloudinary for product ID: {}", id);
            String newImageUrl = cloudinaryService.uploadImage(request.getImageFile(), "products");
            product.setImageUrl(newImageUrl);

            // Xóa ảnh cũ trên Cloudinary nếu có để dọn dẹp dung lượng
            if (oldImageUrl != null && oldImageUrl.contains("cloudinary.com")) {
                String oldPublicId = cloudinaryService.extractPublicId(oldImageUrl);
                if (!oldPublicId.isBlank()) {
                    cloudinaryService.deleteImage(oldPublicId);
                }
            }
        } else if (request.getCurrentImageUrl() != null) {
            product.setImageUrl(request.getCurrentImageUrl().trim());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus().trim());
        }

        Product updated = productRepository.save(product);
        log.info(">>> Updated Product: [{}] {}", updated.getProductCode(), updated.getName());
        return ProductResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public boolean toggleProductStatus(Long id) {
        Product product = getProductEntityById(id);
        boolean isActive = "ACTIVE".equalsIgnoreCase(product.getStatus());
        String newStatus = isActive ? "INACTIVE" : "ACTIVE";
        product.setStatus(newStatus);
        productRepository.save(product);
        log.info(">>> Toggled Product ID {} status to: {}", id, newStatus);
        return "ACTIVE".equalsIgnoreCase(newStatus);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductEntityById(id);

        if (orderDetailRepository.existsByProductId(id)) {
            throw new IllegalStateException("Không thể xóa sản phẩm [" + product.getName() +
                    "] vì đã có đơn hàng trong lịch sử! Vui lòng chuyển trạng thái sang TẠM NGƯNG KINH DOANH (INACTIVE) để bảo toàn dữ liệu.");
        }

        // Xóa các bản ghi liên kết thực đơn chi nhánh trước để tránh lỗi Foreign Key
        branchMenuRepository.deleteByProductId(id);

        if (product.getImageUrl() != null && product.getImageUrl().contains("cloudinary.com")) {
            String publicId = cloudinaryService.extractPublicId(product.getImageUrl());
            if (!publicId.isBlank()) {
                cloudinaryService.deleteImage(publicId);
            }
        }

        productRepository.delete(product);
        log.info(">>> Deleted Product ID: [{}] {}", id, product.getName());
    }

    @Override
    @Transactional
    public void syncProductToAllBranches(Product product) {
        List<Branch> branches = branchRepository.findAll();
        for (Branch b : branches) {
            if (!branchMenuRepository.existsByBranchIdAndProductId(b.getId(), product.getId())) {
                BranchMenu bm = BranchMenu.builder()
                        .branch(b)
                        .product(product)
                        .available(true)
                        .build();
                branchMenuRepository.save(bm);
            }
        }
        log.info(">>> Synced Product [{}] to all {} branches in the chain.", product.getProductCode(), branches.size());
    }
}
