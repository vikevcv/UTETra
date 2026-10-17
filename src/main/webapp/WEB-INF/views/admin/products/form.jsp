<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<head>
  <title>${pageTitle}</title>
  <style>
    .zen-form-card {
      background: #ffffff;
      border: 1px solid var(--zen-border, #e2e8f0);
      border-radius: var(--zen-radius-lg, 12px);
      box-shadow: var(--zen-shadow-sm, 0 1px 3px rgba(0,0,0,0.05));
      padding: 24px;
    }

    /* Drag & Drop Zone */
    .dropzone-container {
      border: 2px dashed #cbd5e1;
      border-radius: 12px;
      padding: 28px 20px;
      text-align: center;
      background: #f8fafc;
      transition: all 0.2s ease;
      cursor: pointer;
      position: relative;
    }
    .dropzone-container:hover, .dropzone-container.dragover {
      border-color: #3b82f6;
      background: #eff6ff;
    }
    .dropzone-icon {
      width: 56px;
      height: 56px;
      background: #ffffff;
      border-radius: 50%;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
      color: #3b82f6;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
      margin-bottom: 12px;
    }

    /* Image Preview Box */
    .preview-box {
      border-radius: 12px;
      overflow: hidden;
      border: 1px solid #e2e8f0;
      background: #f8fafc;
      position: relative;
      height: 240px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .preview-img {
      max-width: 100%;
      max-height: 100%;
      object-fit: contain;
    }

    .badge-cloudinary {
      background: rgba(14, 165, 233, 0.1);
      color: #0284c7;
      font-weight: 600;
      border: 1px solid rgba(14, 165, 233, 0.2);
    }
  </style>
</head>

<div class="container-fluid px-3 px-md-4 py-3">

  <!-- Breadcrumb & Header -->
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb mb-1 fs-sm">
          <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/dashboard" class="text-decoration-none">Admin</a></li>
          <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/products" class="text-decoration-none">Thực Đơn</a></li>
          <li class="breadcrumb-item active" aria-current="page">${isEdit ? 'Chỉnh Sửa Món' : 'Thêm Món Mới'}</li>
        </ol>
      </nav>
      <h3 class="fw-bold text-dark mb-0">${isEdit ? 'Chỉnh Sửa Món Nước' : 'Thêm Món Nước Mới Vào Thực Đơn'}</h3>
      <p class="text-muted small mb-0">Cập nhật công thức giá, thông tin hiển thị và tải ảnh mây lên Cloudinary</p>
    </div>
    <div>
      <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary">
        <i class="fa-solid fa-arrow-left me-1"></i> Quay lại danh sách
      </a>
    </div>
  </div>

  <!-- Product Form -->
  <form id="productForm" 
        action="${isEdit ? pageContext.request.contextPath.concat('/admin/products/').concat(product.id).concat('/update') : pageContext.request.contextPath.concat('/admin/products/create')}" 
        method="post" 
        enctype="multipart/form-data">

    <input type="hidden" name="id" value="${product.id}">
    <input type="hidden" id="currentImageUrl" name="currentImageUrl" value="${product.currentImageUrl}">

    <div class="row g-4">
      <!-- Cột Trái: Thông Tin Chi Tiết Món -->
      <div class="col-12 col-lg-7">
        <div class="zen-form-card">
          <h5 class="fw-bold text-dark mb-4 pb-2 border-bottom d-flex align-items-center">
            <i class="fa-solid fa-circle-info text-primary me-2"></i>Thông Tin Cơ Bản
          </h5>

          <!-- Tên món nước -->
          <div class="mb-3">
            <label for="name" class="form-label fw-semibold">Tên Món Nước <span class="text-danger">*</span></label>
            <input type="text" class="form-control" id="name" name="name" 
                   value="${product.name}" placeholder="VD: Trà Sữa Oolong Nướng, Trà Đào Cam Sả..." required>
          </div>

          <div class="row g-3 mb-3">
            <!-- Mã sản phẩm -->
            <div class="col-12 col-sm-6">
              <label for="productCode" class="form-label fw-semibold">Mã Món Nước</label>
              <input type="text" class="form-control text-uppercase" id="productCode" name="productCode" 
                     value="${product.productCode}" placeholder="VD: SP-001 (Tự động nếu trống)">
              <div class="form-text small">Để trống để hệ thống tự phát sinh mã.</div>
            </div>

            <!-- Danh mục -->
            <div class="col-12 col-sm-6">
              <label for="categoryId" class="form-label fw-semibold">Danh Mục <span class="text-danger">*</span></label>
              <select class="form-select" id="categoryId" name="categoryId" required>
                <option value="">-- Chọn danh mục --</option>
                <c:forEach var="c" items="${categories}">
                  <option value="${c.id}" ${product.categoryId == c.id ? 'selected' : ''}>${c.name}</option>
                </c:forEach>
              </select>
            </div>
          </div>

          <div class="row g-3 mb-3">
            <!-- Giá niêm yết -->
            <div class="col-12 col-sm-6">
              <label for="basePrice" class="form-label fw-semibold">Giá Niêm Yết (VNĐ) <span class="text-danger">*</span></label>
              <div class="input-group">
                <input type="number" class="form-control" id="basePrice" name="basePrice" 
                       value="${product.basePrice != null ? product.basePrice : '30000'}" min="0" step="1000" required>
                <span class="input-group-text bg-light text-muted">đ</span>
              </div>
              <div class="form-text small" id="pricePreviewText">30,000 VNĐ</div>
            </div>

            <!-- Trạng thái kinh doanh -->
            <div class="col-12 col-sm-6">
              <label for="status" class="form-label fw-semibold">Trạng Thái Kinh Doanh</label>
              <select class="form-select" id="status" name="status">
                <option value="ACTIVE" ${product.status == 'ACTIVE' || empty product.status ? 'selected' : ''}>Đang bán</option>
                <option value="INACTIVE" ${product.status == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng</option>
              </select>
            </div>
          </div>

          <!-- Mô tả món -->
          <div class="mb-4">
            <label for="description" class="form-label fw-semibold">Mô Tả Hương Vị & Thành Phần</label>
            <textarea class="form-control" id="description" name="description" rows="4" 
                      placeholder="Mô tả hương vị trà, độ ngọt, lớp kem cheese béo ngậy...">${product.description}</textarea>
          </div>

          <!-- Nút bấm submit -->
          <div class="d-flex align-items-center gap-2 pt-3 border-top">
            <button type="submit" class="btn btn-primary px-4 shadow-sm" id="submitBtn">
              <i class="fa-solid fa-floppy-disk me-1"></i> ${isEdit ? 'Cập Nhật Món Nước' : 'Lưu & Đồng Bộ Chi Nhánh'}
            </button>
            <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary">
              Hủy bỏ
            </a>
          </div>
        </div>
      </div>

      <!-- Cột Phải: Tải Ảnh Cloudinary Có Drag & Drop và Live Preview -->
      <div class="col-12 col-lg-5">
        <div class="zen-form-card">
          <div class="d-flex align-items-center justify-content-between mb-3 pb-2 border-bottom">
            <h5 class="fw-bold text-dark mb-0 d-flex align-items-center">
              <i class="fa-solid fa-cloud-arrow-up text-primary me-2"></i>Hình Ảnh Món Nước
            </h5>
            <span class="badge badge-cloudinary px-2 py-1">
              <i class="fa-solid fa-cloud me-1"></i>Cloudinary SDK
            </span>
          </div>

          <!-- Live Preview Box -->
          <div class="mb-3">
            <div class="preview-box" id="previewContainer">
              <c:choose>
                <c:when test="${not empty product.currentImageUrl}">
                  <img src="${product.currentImageUrl}" alt="Preview" class="preview-img" id="imagePreview">
                </c:when>
                <c:otherwise>
                  <img src="https://placehold.co/400x300?text=Chưa+Có+Ảnh" alt="Placeholder" class="preview-img" id="imagePreview">
                </c:otherwise>
              </c:choose>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-2">
              <span class="small text-muted" id="fileInfoText">
                <c:choose>
                  <c:when test="${not empty product.currentImageUrl}">
                    <i class="fa-solid fa-check text-success me-1"></i>Đang dùng ảnh hiện tại
                  </c:when>
                  <c:otherwise>Chưa chọn ảnh mới</c:otherwise>
                </c:choose>
              </span>
              <button type="button" class="btn btn-sm btn-link text-danger text-decoration-none p-0 d-none" 
                      id="resetImageBtn" onclick="resetSelectedImage()">
                <i class="fa-solid fa-rotate-left me-1"></i>Hoàn tác ảnh
              </button>
            </div>
          </div>

          <!-- Drag & Drop Zone -->
          <div class="dropzone-container mb-3" id="dropzone" onclick="document.getElementById('imageFileInput').click()">
            <input type="file" id="imageFileInput" name="imageFile" accept="image/png, image/jpeg, image/webp" 
                   class="d-none" onchange="handleFileSelect(this.files)">
            <div class="dropzone-icon">
              <i class="fa-solid fa-image"></i>
            </div>
            <h6 class="fw-semibold text-dark mb-1">Kéo thả ảnh vào đây hoặc bấm để chọn</h6>
            <p class="text-muted small mb-0">Hỗ trợ định dạng PNG, JPG, WEBP (Tối đa 5MB)</p>
          </div>

          <!-- Hướng dẫn & Tiêu chuẩn ảnh -->
          <div class="p-3 bg-light rounded-3">
            <h6 class="fw-semibold text-dark small mb-2 d-flex align-items-center">
              <i class="fa-solid fa-lightbulb text-warning me-1"></i>Gợi ý hình ảnh thực đơn chuẩn:
            </h6>
            <ul class="text-muted small mb-0 ps-3">
              <li>Tỉ lệ ảnh chuẩn <strong>1:1</strong> (vuông) hoặc <strong>4:3</strong>.</li>
              <li>Chụp sản phẩm trên nền sáng hoặc tách nền trong suốt PNG.</li>
              <li>Ảnh sẽ tự động nén tối ưu và phân phối qua Cloudinary CDN tốc độ cao.</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </form>
</div>

<script>
  // Lưu ảnh gốc ban đầu
  const initialImageUrl = '${product.currentImageUrl}';
  const placeholderUrl = 'https://placehold.co/400x300?text=Chưa+Có+Ảnh';

  // Định dạng hiển thị giá realtime
  const priceInput = document.getElementById('basePrice');
  const pricePreview = document.getElementById('pricePreviewText');
  if (priceInput && pricePreview) {
    priceInput.addEventListener('input', function() {
      const val = parseInt(this.value, 10);
      if (!isNaN(val)) {
        pricePreview.innerText = val.toLocaleString('vi-VN') + ' VNĐ';
      } else {
        pricePreview.innerText = '0 VNĐ';
      }
    });
  }

  // Drag & drop logic
  const dropzone = document.getElementById('dropzone');
  const fileInput = document.getElementById('imageFileInput');
  const imagePreview = document.getElementById('imagePreview');
  const fileInfoText = document.getElementById('fileInfoText');
  const resetBtn = document.getElementById('resetImageBtn');

  ['dragenter', 'dragover'].forEach(eventName => {
    dropzone.addEventListener(eventName, (e) => {
      e.preventDefault();
      e.stopPropagation();
      dropzone.classList.add('dragover');
    }, false);
  });

  ['dragleave', 'drop'].forEach(eventName => {
    dropzone.addEventListener(eventName, (e) => {
      e.preventDefault();
      e.stopPropagation();
      dropzone.classList.remove('dragover');
    }, false);
  });

  dropzone.addEventListener('drop', (e) => {
    const dt = e.dataTransfer;
    const files = dt.files;
    if (files && files.length > 0) {
      fileInput.files = files;
      handleFileSelect(files);
    }
  });

  // Xử lý khi người dùng chọn file
  function handleFileSelect(files) {
    if (!files || files.length === 0) return;
    const file = files[0];

    // Kiểm tra định dạng ảnh
    if (!file.type.startsWith('image/')) {
      alert('Vui lòng chỉ chọn tệp hình ảnh (PNG, JPG, WEBP)!');
      return;
    }

    // Kiểm tra dung lượng (5MB)
    if (file.size > 5 * 1024 * 1024) {
      alert('Dung lượng tệp vượt quá 5MB. Vui lòng chọn ảnh nhỏ hơn!');
      return;
    }

    // Đọc và preview tức thì
    const reader = new FileReader();
    reader.onload = function(e) {
      imagePreview.src = e.target.result;
      const sizeKB = (file.size / 1024).toFixed(1);
      fileInfoText.innerHTML = '<span class="text-primary fw-medium"><i class="fa-solid fa-upload me-1"></i>' + 
                               file.name + ' (' + sizeKB + ' KB)</span>';
      resetBtn.classList.remove('d-none');
    };
    reader.readAsDataURL(file);
  }

  // Hoàn tác về ảnh ban đầu
  function resetSelectedImage() {
    fileInput.value = '';
    if (initialImageUrl && initialImageUrl.trim() !== '') {
      imagePreview.src = initialImageUrl;
      fileInfoText.innerHTML = '<i class="fa-solid fa-check text-success me-1"></i>Đang dùng ảnh hiện tại';
    } else {
      imagePreview.src = placeholderUrl;
      fileInfoText.innerText = 'Chưa chọn ảnh mới';
    }
    resetBtn.classList.add('d-none');
  }
</script>
