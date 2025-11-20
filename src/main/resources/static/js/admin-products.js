// Admin Products Management
let products = [];
let categories = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadProducts();
    loadCategories();
});

// Load products
async function loadProducts() {
    try {
        // For admin, we want all products including inactive ones
        // Pass a large size to get all products and includeInactive=true
        const response = await api.getProducts({ 
            page: 0, 
            size: 10000,
            includeInactive: true 
        });
        
        // Handle Page object response (from paginated API)
        // Page object has structure: { content: [...], totalElements: ..., totalPages: ..., ... }
        if (response && response.content && Array.isArray(response.content)) {
            products = response.content;
        } else if (Array.isArray(response)) {
            // Fallback: if response is already an array
            products = response;
        } else if (response && response.data && Array.isArray(response.data)) {
            // Handle ApiResponse wrapper if needed
            products = response.data;
        } else {
            products = [];
        }
        
        renderProductsTable();
    } catch (error) {
        console.error('Error loading products:', error);
        showAlert('Lỗi khi tải danh sách sản phẩm: ' + (error.message || 'Unknown error'), 'error');
        const tbody = document.getElementById('productsTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Lỗi khi tải dữ liệu</td></tr>';
        }
    }
}

// Load categories for select
async function loadCategories() {
    try {
        categories = await api.getCategories();
        const select = document.getElementById('productCategory');
        select.innerHTML = '<option value="">Chọn danh mục</option>';
        categories.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = cat.name;
            select.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Render products table
function renderProductsTable() {
    const tbody = document.getElementById('productsTableBody');
    if (!tbody) return;

    if (!products || products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">Không có sản phẩm nào</td></tr>';
        return;
    }

    tbody.innerHTML = products.map(product => {
        // Handle isActive field - check both isActive and active
        let isActiveValue = product.isActive !== undefined ? product.isActive : (product.active !== undefined ? product.active : true);
        let isProductActive = false;
        if (typeof isActiveValue === 'boolean') {
            isProductActive = isActiveValue;
        } else if (typeof isActiveValue === 'string') {
            isProductActive = isActiveValue === 'true' || isActiveValue === '1';
        } else if (typeof isActiveValue === 'number') {
            isProductActive = isActiveValue === 1;
        } else {
            isProductActive = isActiveValue !== false && isActiveValue !== 'false' && isActiveValue !== 0 && isActiveValue !== '0';
        }
        
        const statusBadge = isProductActive ? 
            '<span class="badge bg-success">Hoạt động</span>' : 
            '<span class="badge bg-secondary">Ngưng hoạt động</span>';
        
        return `
        <tr>
            <td>${product.id}</td>
            <td>
                ${product.imageUrl ? 
                    `<img src="${product.imageUrl}" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;">` : 
                    '<span class="text-muted">No image</span>'
                }
            </td>
            <td>${product.name || ''}</td>
            <td class="text-end">${formatPrice(product.price || 0)}</td>
            <td class="text-center">${product.stockQuantity || product.stock || 0}</td>
            <td>${product.categoryName || 'N/A'}</td>
            <td>${statusBadge}</td>
            <td>
                <div class="btn-group" role="group">
                    <button class="btn btn-sm btn-primary me-1" onclick="editProduct(${product.id})" title="Sửa sản phẩm">
                        <i class="bi bi-pencil"></i> Sửa
                    </button>
                    <button class="btn btn-sm ${isProductActive ? 'btn-warning' : 'btn-success'} me-1" 
                            onclick="toggleProductStatus(${product.id}, ${!isProductActive})"
                            title="${isProductActive ? 'Ngưng hoạt động' : 'Kích hoạt'}">
                        <i class="bi bi-${isProductActive ? 'pause-circle' : 'play-circle'}"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="confirmDeleteProduct(${product.id})" title="Xóa sản phẩm">
                        <i class="bi bi-trash"></i> Xóa
                    </button>
                </div>
            </td>
        </tr>
    `;
    }).join('');
}

// Open product modal for adding
function openProductModal() {
    document.getElementById('productForm').reset();
    document.getElementById('productId').value = '';
    document.getElementById('productModalTitle').textContent = 'Thêm sản phẩm';
    clearImagePreview();
    hideCurrentImage();
}

// Preview image function
function previewImage(input) {
    const previewContainer = document.getElementById('imagePreviewContainer');
    const preview = document.getElementById('imagePreview');
    const file = input.files[0];

    if (file) {
        const reader = new FileReader();
        reader.onload = function(e) {
            preview.src = e.target.result;
            previewContainer.style.display = 'block';
            hideCurrentImage(); // Hide current image when new image is selected
        };
        reader.readAsDataURL(file);
    } else {
        clearImagePreview();
    }
}

// Clear image preview
function clearImagePreview() {
    const previewContainer = document.getElementById('imagePreviewContainer');
    const preview = document.getElementById('imagePreview');
    const fileInput = document.getElementById('productImage');
    
    previewContainer.style.display = 'none';
    preview.src = '';
    if (fileInput) {
        fileInput.value = '';
    }
}

// Show current image (when editing)
function showCurrentImage(imageUrl) {
    const container = document.getElementById('currentImageContainer');
    const img = document.getElementById('currentImage');
    if (imageUrl) {
        img.src = imageUrl;
        container.style.display = 'block';
    } else {
        hideCurrentImage();
    }
}

// Hide current image
function hideCurrentImage() {
    const container = document.getElementById('currentImageContainer');
    container.style.display = 'none';
}

// Edit product
async function editProduct(productId) {
    try {
        const product = await api.getProduct(productId);
        document.getElementById('productId').value = product.id;
        document.getElementById('productName').value = product.name || '';
        document.getElementById('productDescription').value = product.description || '';
        document.getElementById('productPrice').value = product.price || '';
        document.getElementById('productStock').value = product.stockQuantity || product.stock || '';
        document.getElementById('productCategory').value = product.categoryId || '';
        
        // Set created_at if available
        if (product.createdAt) {
            const date = new Date(product.createdAt);
            const localDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
            document.getElementById('productCreatedAt').value = localDate.toISOString().slice(0, 16);
        } else {
            document.getElementById('productCreatedAt').value = '';
        }
        
        // Show current image if exists
        if (product.imageUrl) {
            showCurrentImage(product.imageUrl);
        } else {
            hideCurrentImage();
        }
        
        // Clear preview when editing
        clearImagePreview();
        
        document.getElementById('productModalTitle').textContent = 'Sửa sản phẩm';
        new bootstrap.Modal(document.getElementById('productModal')).show();
    } catch (error) {
        console.error('Error loading product:', error);
        showAlert('Lỗi khi tải thông tin sản phẩm', 'error');
    }
}

// Save product (create or update)
async function saveProduct() {
    try {
        const productId = document.getElementById('productId').value;
        const formData = {
            name: document.getElementById('productName').value,
            description: document.getElementById('productDescription').value,
            price: parseFloat(document.getElementById('productPrice').value),
            stockQuantity: parseInt(document.getElementById('productStock').value),
            categoryId: parseInt(document.getElementById('productCategory').value)
        };

        // Add created_at if provided (only for create, not update)
        if (!productId) {
            const createdAt = document.getElementById('productCreatedAt').value;
            if (createdAt) {
                // Convert datetime-local format to ISO string for backend
                formData.createdAt = new Date(createdAt).toISOString();
            }
        }

        let result;
        if (productId) {
            result = await api.updateProduct(productId, formData);
            showAlert('Cập nhật sản phẩm thành công!', 'success');
        } else {
            result = await api.createProduct(formData);
            showAlert('Tạo sản phẩm thành công!', 'success');
        }

        // Upload image if available
        const imageFile = document.getElementById('productImage').files[0];
        if (imageFile) {
            try {
                // Validate file type
                if (!imageFile.type.startsWith('image/')) {
                    showAlert('File phải là hình ảnh!', 'error');
                    return;
                }

                // Validate file size (10MB)
                if (imageFile.size > 10 * 1024 * 1024) {
                    showAlert('Kích thước file phải nhỏ hơn 10MB!', 'error');
                    return;
                }

                const formDataImg = new FormData();
                formDataImg.append('file', imageFile); // API expects 'file' parameter
                const productIdToUpload = productId || result.id;
                await api.uploadProductImage(productIdToUpload, formDataImg);
                showAlert('Upload ảnh sản phẩm thành công!', 'success');
            } catch (error) {
                console.error('Upload image error:', error);
                showAlert('Lỗi khi upload ảnh: ' + (error.message || 'Upload failed'), 'error');
                // Don't return here, continue to reload products
            }
        }

        bootstrap.Modal.getInstance(document.getElementById('productModal')).hide();
        loadProducts();
        document.getElementById('productForm').reset();
    } catch (error) {
        console.error('Error saving product:', error);
        showAlert('Lỗi khi lưu sản phẩm: ' + error.message, 'error');
    }
}

// Delete product
async function confirmDeleteProduct(productId) {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
        return;
    }

    try {
        await api.deleteProduct(productId);
        showAlert('Xóa sản phẩm thành công!', 'success');
        loadProducts();
    } catch (error) {
        console.error('Error deleting product:', error);
        showAlert('Lỗi khi xóa sản phẩm: ' + error.message, 'error');
    }
}

// Format price to VNĐ
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

// Toggle product status
async function toggleProductStatus(productId, isActive) {
    if (!confirm(`Bạn có chắc chắn muốn ${isActive ? 'kích hoạt' : 'ngưng hoạt động'} sản phẩm này?`)) {
        return;
    }

    try {
        await api.toggleProductStatus(productId);
        showAlert(`Đã ${isActive ? 'kích hoạt' : 'ngưng hoạt động'} sản phẩm thành công!`, 'success');
        await loadProducts();
    } catch (error) {
        console.error('Error toggling product status:', error);
        showAlert('Lỗi khi cập nhật trạng thái sản phẩm: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Export to Excel
function exportToExcel() {
    if (!products || products.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    try {
        // Prepare data
        const data = products.map(product => {
            let isActiveValue = product.isActive !== undefined ? product.isActive : (product.active !== undefined ? product.active : true);
            let isProductActive = false;
            if (typeof isActiveValue === 'boolean') {
                isProductActive = isActiveValue;
            } else if (typeof isActiveValue === 'string') {
                isProductActive = isActiveValue === 'true' || isActiveValue === '1';
            } else {
                isProductActive = isActiveValue !== false && isActiveValue !== 'false' && isActiveValue !== 0 && isActiveValue !== '0';
            }

            return {
                'ID': product.id || '',
                'Tên sản phẩm': product.name || '',
                'Mô tả': product.description || '',
                'Giá (VNĐ)': product.price || 0,
                'Tồn kho': product.stockQuantity || product.stock || 0,
                'Danh mục': product.categoryName || 'N/A',
                'Trạng thái': isProductActive ? 'Hoạt động' : 'Ngưng hoạt động',
                'Ngày tạo': product.createdAt ? new Date(product.createdAt).toLocaleString('vi-VN') : ''
            };
        });

        // Create workbook
        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(data);
        
        // Set column widths
        const colWidths = [
            { wch: 10 }, // ID
            { wch: 30 }, // Tên sản phẩm
            { wch: 50 }, // Mô tả
            { wch: 15 }, // Giá
            { wch: 10 }, // Tồn kho
            { wch: 20 }, // Danh mục
            { wch: 15 }, // Trạng thái
            { wch: 20 }  // Ngày tạo
        ];
        ws['!cols'] = colWidths;

        XLSX.utils.book_append_sheet(wb, ws, 'Sản phẩm');
        
        // Generate filename with current date
        const filename = `BaoCaoSanPham_${new Date().toISOString().split('T')[0]}.xlsx`;
        
        // Save file
        XLSX.writeFile(wb, filename);
        showAlert('Xuất Excel thành công!', 'success');
    } catch (error) {
        console.error('Error exporting to Excel:', error);
        showAlert('Lỗi khi xuất Excel: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Export to PDF
function exportToPDF() {
    if (!products || products.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    try {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('l', 'mm', 'a4'); // Landscape orientation
        
        // Title
        doc.setFontSize(16);
        doc.text('BÁO CÁO SẢN PHẨM', 14, 15);
        
        // Date
        doc.setFontSize(10);
        doc.text(`Ngày xuất: ${new Date().toLocaleDateString('vi-VN')}`, 14, 22);
        
        // Prepare table data
        const tableData = products.map(product => {
            let isActiveValue = product.isActive !== undefined ? product.isActive : (product.active !== undefined ? product.active : true);
            let isProductActive = false;
            if (typeof isActiveValue === 'boolean') {
                isProductActive = isActiveValue;
            } else if (typeof isActiveValue === 'string') {
                isProductActive = isActiveValue === 'true' || isActiveValue === '1';
            } else {
                isProductActive = isActiveValue !== false && isActiveValue !== 'false' && isActiveValue !== 0 && isActiveValue !== '0';
            }

            return [
                product.id || '',
                (product.name || '').substring(0, 30),
                formatPrice(product.price || 0),
                product.stockQuantity || product.stock || 0,
                (product.categoryName || 'N/A').substring(0, 15),
                isProductActive ? 'Hoạt động' : 'Ngưng'
            ];
        });

        // Add table
        doc.autoTable({
            startY: 28,
            head: [['ID', 'Tên sản phẩm', 'Giá', 'Tồn kho', 'Danh mục', 'Trạng thái']],
            body: tableData,
            styles: { fontSize: 8 },
            headStyles: { fillColor: [66, 139, 202], textColor: 255 },
            alternateRowStyles: { fillColor: [245, 245, 245] },
            margin: { top: 28, left: 14, right: 14 }
        });
        
        // Generate filename with current date
        const filename = `BaoCaoSanPham_${new Date().toISOString().split('T')[0]}.pdf`;
        
        // Save file
        doc.save(filename);
        showAlert('Xuất PDF thành công!', 'success');
    } catch (error) {
        console.error('Error exporting to PDF:', error);
        showAlert('Lỗi khi xuất PDF: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Show alert
function showAlert(message, type = 'success') {
    const alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) return;

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type === 'error' ? 'danger' : type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    alertContainer.innerHTML = '';
    alertContainer.appendChild(alertDiv);

    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

