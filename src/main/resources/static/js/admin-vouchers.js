// Admin Vouchers Management
let vouchers = [];
let categories = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
    loadVouchers();
    setupExportButtons();
});

// Setup export button event listeners
function setupExportButtons() {
    console.log('setupExportButtons called');
    
    // Use event delegation on the document to catch clicks
    document.addEventListener('click', function(e) {
        // Check if clicked element is export Excel button or inside it
        if (e.target.closest('.export-excel-btn')) {
            e.preventDefault();
            e.stopPropagation();
            console.log('Excel export button clicked via delegation');
            try {
                exportToExcel();
            } catch (error) {
                console.error('Error in exportToExcel:', error);
                alert('Lỗi khi xuất Excel: ' + error.message);
            }
            return false;
        }
        
        // Check if clicked element is export PDF button or inside it
        if (e.target.closest('.export-pdf-btn')) {
            e.preventDefault();
            e.stopPropagation();
            console.log('PDF export button clicked via delegation');
            try {
                exportToPDF();
            } catch (error) {
                console.error('Error in exportToPDF:', error);
                alert('Lỗi khi xuất PDF: ' + error.message);
            }
            return false;
        }
    });
    
    // Also try direct attachment as backup
    setTimeout(function() {
        const excelBtn = document.querySelector('.export-excel-btn');
        const pdfBtn = document.querySelector('.export-pdf-btn');
        
        if (excelBtn) {
            console.log('Excel button found, attaching direct listener');
            excelBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                console.log('Excel export button clicked (direct)');
                try {
                    exportToExcel();
                } catch (error) {
                    console.error('Error in exportToExcel:', error);
                    alert('Lỗi khi xuất Excel: ' + error.message);
                }
                return false;
            }, true); // Use capture phase
        } else {
            console.warn('Excel export button not found');
        }
        
        if (pdfBtn) {
            console.log('PDF button found, attaching direct listener');
            pdfBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                console.log('PDF export button clicked (direct)');
                try {
                    exportToPDF();
                } catch (error) {
                    console.error('Error in exportToPDF:', error);
                    alert('Lỗi khi xuất PDF: ' + error.message);
                }
                return false;
            }, true); // Use capture phase
        } else {
            console.warn('PDF export button not found');
        }
    }, 500);
}

// Load categories
async function loadCategories() {
    try {
        categories = await api.getCategories();
        renderCategoryOptions();
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

// Render category options
function renderCategoryOptions() {
    const categorySelect = document.getElementById('voucherCategory');
    if (!categorySelect) return;
    
    categorySelect.innerHTML = '<option value="">Tất cả danh mục</option>';
    if (categories && categories.length > 0) {
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            categorySelect.appendChild(option);
        });
    }
}

// Load vouchers
async function loadVouchers() {
    try {
        vouchers = await api.getAllVouchers();
        renderVouchersTable();
    } catch (error) {
        console.error('Error loading vouchers:', error);
        showAlert('Lỗi khi tải danh sách voucher', 'error');
    }
}

// Render vouchers table
function renderVouchersTable() {
    const tbody = document.getElementById('vouchersTableBody');
    if (!tbody) return;

    if (!vouchers || vouchers.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="text-center">Không có voucher nào</td></tr>';
        return;
    }

    tbody.innerHTML = vouchers.map(voucher => {
        const typeText = voucher.type === 'PERCENTAGE' ? 'Phần trăm' : 'Số tiền';
        const valueText = voucher.type === 'PERCENTAGE' ? 
            `${voucher.value}%` : 
            formatPrice(voucher.value || 0);
        const statusBadge = voucher.isActive ? 
            '<span class="badge bg-success">Hoạt động</span>' : 
            '<span class="badge bg-secondary">Đã vô hiệu</span>';
        
        const startDate = voucher.startDate ? new Date(voucher.startDate).toLocaleDateString('vi-VN') : '-';
        const endDate = voucher.endDate ? new Date(voucher.endDate).toLocaleDateString('vi-VN') : '-';
        const minOrderText = voucher.minOrder ? formatPrice(voucher.minOrder) : '-';
        const categoryText = voucher.categoryName || 'Tất cả';
        
        return `
        <tr>
            <td>${voucher.id}</td>
            <td><strong>${voucher.code || '-'}</strong></td>
            <td>${typeText}</td>
            <td>${valueText}</td>
            <td>${minOrderText}</td>
            <td>${categoryText}</td>
            <td>${startDate}</td>
            <td>${endDate}</td>
            <td>${statusBadge}</td>
            <td>
                <button class="btn btn-sm btn-primary me-1" onclick="editVoucher(${voucher.id})">
                    <i class="bi bi-pencil"></i> Sửa
                </button>
                ${voucher.isActive ? `
                    <button class="btn btn-sm btn-warning me-1" onclick="disableVoucher(${voucher.id})">
                        <i class="bi bi-x-circle"></i> Vô hiệu
                    </button>
                ` : ''}
                <button class="btn btn-sm btn-danger" onclick="confirmDeleteVoucher(${voucher.id})">
                    <i class="bi bi-trash"></i> Xóa
                </button>
            </td>
        </tr>
    `;
    }).join('');
}

// Open voucher modal for adding
function openVoucherModal() {
    document.getElementById('voucherForm').reset();
    document.getElementById('voucherId').value = '';
    document.getElementById('voucherModalTitle').textContent = 'Thêm voucher';
    document.getElementById('voucherActive').checked = true;
    document.getElementById('voucherCategory').value = '';
}

// Edit voucher
async function editVoucher(voucherId) {
    try {
        const voucher = await api.getVoucher(voucherId);
        document.getElementById('voucherId').value = voucher.id;
        document.getElementById('voucherCode').value = voucher.code || '';
        document.getElementById('voucherType').value = voucher.type || '';
        document.getElementById('voucherValue').value = voucher.value || '';
        document.getElementById('minOrderValue').value = voucher.minOrder || '';
        document.getElementById('voucherCategory').value = voucher.categoryId || '';
        
        if (voucher.startDate) {
            // Convert LocalDateTime to date input format (YYYY-MM-DD)
            const startDate = new Date(voucher.startDate);
            document.getElementById('startDate').value = startDate.toISOString().split('T')[0];
        }
        if (voucher.endDate) {
            const endDate = new Date(voucher.endDate);
            document.getElementById('endDate').value = endDate.toISOString().split('T')[0];
        }
        
        document.getElementById('voucherActive').checked = voucher.isActive !== false;
        
        document.getElementById('voucherModalTitle').textContent = 'Sửa voucher';
        new bootstrap.Modal(document.getElementById('voucherModal')).show();
    } catch (error) {
        console.error('Error loading voucher:', error);
        showAlert('Lỗi khi tải thông tin voucher', 'error');
    }
}

// Save voucher (create or update)
async function saveVoucher() {
    try {
        const voucherId = document.getElementById('voucherId').value;
        const code = document.getElementById('voucherCode').value.trim();
        const type = document.getElementById('voucherType').value;
        const value = parseFloat(document.getElementById('voucherValue').value);
        const minOrderValue = document.getElementById('minOrderValue').value;
        const categoryId = document.getElementById('voucherCategory').value;
        const startDateStr = document.getElementById('startDate').value;
        const endDateStr = document.getElementById('endDate').value;
        const isActive = document.getElementById('voucherActive').checked;

        // Validate
        if (!code) {
            showAlert('Vui lòng nhập mã voucher', 'error');
            return;
        }
        if (!type) {
            showAlert('Vui lòng chọn loại giảm giá', 'error');
            return;
        }
        if (isNaN(value) || value <= 0) {
            showAlert('Vui lòng nhập giá trị hợp lệ', 'error');
            return;
        }
        if (!startDateStr || !endDateStr) {
            showAlert('Vui lòng chọn ngày bắt đầu và ngày kết thúc', 'error');
            return;
        }

        // Convert date strings to LocalDateTime format (YYYY-MM-DDTHH:mm:ss)
        const startDate = startDateStr ? `${startDateStr}T00:00:00` : null;
        const endDate = endDateStr ? `${endDateStr}T23:59:59` : null;

        const formData = {
            code: code,
            type: type,
            value: value,
            minOrder: minOrderValue ? parseFloat(minOrderValue) : null,
            categoryId: categoryId ? parseInt(categoryId) : null,
            startDate: startDate,
            endDate: endDate,
            isActive: isActive
        };

        if (voucherId) {
            await api.updateVoucher(voucherId, formData);
            showAlert('Cập nhật voucher thành công!', 'success');
        } else {
            await api.createVoucher(formData);
            showAlert('Tạo voucher thành công!', 'success');
        }

        bootstrap.Modal.getInstance(document.getElementById('voucherModal')).hide();
        loadVouchers();
        document.getElementById('voucherForm').reset();
        document.getElementById('voucherId').value = '';
    } catch (error) {
        console.error('Error saving voucher:', error);
        const errorMessage = error.message || 'Có lỗi xảy ra';
        showAlert('Lỗi khi lưu voucher: ' + errorMessage, 'error');
    }
}

// Disable voucher
async function disableVoucher(voucherId) {
    if (!confirm('Bạn có chắc chắn muốn vô hiệu hóa voucher này?')) {
        return;
    }

    try {
        await api.disableVoucher(voucherId);
        showAlert('Vô hiệu hóa voucher thành công!', 'success');
        loadVouchers();
    } catch (error) {
        console.error('Error disabling voucher:', error);
        showAlert('Lỗi khi vô hiệu hóa voucher: ' + error.message, 'error');
    }
}

// Delete voucher
async function confirmDeleteVoucher(voucherId) {
    if (!confirm('Bạn có chắc chắn muốn xóa voucher này?')) {
        return;
    }

    try {
        await api.deleteVoucher(voucherId);
        showAlert('Xóa voucher thành công!', 'success');
        loadVouchers();
    } catch (error) {
        console.error('Error deleting voucher:', error);
        showAlert('Lỗi khi xóa voucher: ' + error.message, 'error');
    }
}

// Format price to VNĐ
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

// Export to Excel - Make globally available
window.exportToExcel = function() {
    if (!vouchers || vouchers.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    // Check if XLSX library is loaded
    if (typeof XLSX === 'undefined') {
        showAlert('Thư viện Excel chưa được tải. Vui lòng tải lại trang!', 'error');
        return;
    }

    try {
        const data = vouchers.map(voucher => {
            const typeText = voucher.type === 'PERCENTAGE' ? 'Phần trăm' : 'Số tiền';
            const valueText = voucher.type === 'PERCENTAGE' ? 
                `${voucher.value}%` : 
                formatPrice(voucher.value || 0);

            return {
                'ID': voucher.id || '',
                'Mã voucher': voucher.code || '',
                'Loại': typeText,
                'Giá trị': valueText,
                'Đơn tối thiểu (VNĐ)': voucher.minOrder || 0,
                'Danh mục': voucher.categoryName || 'Tất cả',
                'Ngày bắt đầu': voucher.startDate ? new Date(voucher.startDate).toLocaleDateString('vi-VN') : '',
                'Ngày kết thúc': voucher.endDate ? new Date(voucher.endDate).toLocaleDateString('vi-VN') : '',
                'Trạng thái': voucher.isActive ? 'Hoạt động' : 'Đã vô hiệu'
            };
        });

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(data);
        
        const colWidths = [
            { wch: 10 }, // ID
            { wch: 20 }, // Mã voucher
            { wch: 15 }, // Loại
            { wch: 15 }, // Giá trị
            { wch: 18 }, // Đơn tối thiểu
            { wch: 20 }, // Danh mục
            { wch: 15 }, // Ngày bắt đầu
            { wch: 15 }, // Ngày kết thúc
            { wch: 15 }  // Trạng thái
        ];
        ws['!cols'] = colWidths;

        XLSX.utils.book_append_sheet(wb, ws, 'Voucher');
        const filename = `BaoCaoVoucher_${new Date().toISOString().split('T')[0]}.xlsx`;
        XLSX.writeFile(wb, filename);
        showAlert('Xuất Excel thành công!', 'success');
    } catch (error) {
        console.error('Error exporting to Excel:', error);
        showAlert('Lỗi khi xuất Excel: ' + (error.message || 'Unknown error'), 'error');
    }
};

// Export to PDF - Make globally available
window.exportToPDF = function() {
    if (!vouchers || vouchers.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    // Check if jsPDF library is loaded
    if (typeof window.jspdf === 'undefined') {
        showAlert('Thư viện PDF chưa được tải. Vui lòng tải lại trang!', 'error');
        return;
    }

    try {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('l', 'mm', 'a4');
        
        doc.setFontSize(16);
        doc.text('BÁO CÁO VOUCHER', 14, 15);
        doc.setFontSize(10);
        doc.text(`Ngày xuất: ${new Date().toLocaleDateString('vi-VN')}`, 14, 22);
        
        const tableData = vouchers.map(voucher => {
            const typeText = voucher.type === 'PERCENTAGE' ? 'Phần trăm' : 'Số tiền';
            const valueText = voucher.type === 'PERCENTAGE' ? 
                `${voucher.value}%` : 
                formatPrice(voucher.value || 0);

            return [
                voucher.id || '',
                (voucher.code || '').substring(0, 15),
                typeText.substring(0, 10),
                valueText.substring(0, 15),
                voucher.minOrder ? formatPrice(voucher.minOrder).substring(0, 15) : '-',
                (voucher.categoryName || 'Tất cả').substring(0, 15),
                voucher.startDate ? new Date(voucher.startDate).toLocaleDateString('vi-VN') : '',
                voucher.endDate ? new Date(voucher.endDate).toLocaleDateString('vi-VN') : '',
                voucher.isActive ? 'Hoạt động' : 'Vô hiệu'
            ];
        });

        doc.autoTable({
            startY: 28,
            head: [['ID', 'Mã', 'Loại', 'Giá trị', 'Đơn tối thiểu', 'Danh mục', 'Bắt đầu', 'Kết thúc', 'Trạng thái']],
            body: tableData,
            styles: { fontSize: 7 },
            headStyles: { fillColor: [66, 139, 202], textColor: 255 },
            alternateRowStyles: { fillColor: [245, 245, 245] },
            margin: { top: 28, left: 14, right: 14 }
        });
        
        const filename = `BaoCaoVoucher_${new Date().toISOString().split('T')[0]}.pdf`;
        doc.save(filename);
        showAlert('Xuất PDF thành công!', 'success');
    } catch (error) {
        console.error('Error exporting to PDF:', error);
        showAlert('Lỗi khi xuất PDF: ' + (error.message || 'Unknown error'), 'error');
    }
};

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

