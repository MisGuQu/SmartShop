// Admin Categories Management
let categories = [];

// Initialize
document.addEventListener('DOMContentLoaded', async function() {
    // Load data first
    await loadCategories();
    
    // Setup export buttons - Attach ONLY ONCE
    setupExportButtons();
});

// Setup export button event listeners - Simple and reliable
function setupExportButtons() {
    console.log('=== setupExportButtons CALLED ===');
    
    const excelBtn = document.getElementById('exportExcelBtn');
    const pdfBtn = document.getElementById('exportPdfBtn');
    
    console.log('Excel button found:', !!excelBtn);
    console.log('PDF button found:', !!pdfBtn);
    
    if (!excelBtn || !pdfBtn) {
        console.warn('Export buttons not found');
        return;
    }
    
    // Attach onclick directly - simple and reliable
    excelBtn.onclick = function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        // Close dropdown manually
        const dropdownBtn = document.querySelector('[data-bs-toggle="dropdown"]');
        if (typeof bootstrap !== 'undefined' && bootstrap.Dropdown && dropdownBtn) {
            const dropdown = bootstrap.Dropdown.getInstance(dropdownBtn);
            if (dropdown) {
                dropdown.hide();
            }
        }
        
        // Call export function
        if (typeof window.exportToExcel === 'function') {
            try {
                window.exportToExcel();
            } catch (error) {
                console.error('Error calling exportToExcel:', error);
                showAlert('Lỗi khi xuất Excel: ' + error.message, 'error');
            }
        } else {
            showAlert('Hàm exportToExcel chưa được tải. Vui lòng tải lại trang!', 'error');
        }
        return false;
    };
    
    pdfBtn.onclick = function(e) {
        e.preventDefault();
        e.stopPropagation();
        
        // Close dropdown manually
        const dropdownBtn = document.querySelector('[data-bs-toggle="dropdown"]');
        if (typeof bootstrap !== 'undefined' && bootstrap.Dropdown && dropdownBtn) {
            const dropdown = bootstrap.Dropdown.getInstance(dropdownBtn);
            if (dropdown) {
                dropdown.hide();
            }
        }
        
        // Call export function
        if (typeof window.exportToPDF === 'function') {
            try {
                window.exportToPDF();
            } catch (error) {
                console.error('Error calling exportToPDF:', error);
                showAlert('Lỗi khi xuất PDF: ' + error.message, 'error');
            }
        } else {
            showAlert('Hàm exportToPDF chưa được tải. Vui lòng tải lại trang!', 'error');
        }
        return false;
    };
    
    console.log('Export buttons setup complete!');
}

// Load categories
async function loadCategories() {
    try {
        categories = await api.getCategories();
        renderCategoriesTable();
    } catch (error) {
        console.error('Error loading categories:', error);
        showAlert('Lỗi khi tải danh sách danh mục', 'error');
    }
}

// Render categories table
function renderCategoriesTable() {
    const tbody = document.getElementById('categoriesTableBody');
    if (!tbody) return;

    if (!categories || categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center">Không có danh mục nào</td></tr>';
        return;
    }

    tbody.innerHTML = categories.map(category => `
        <tr>
            <td>${category.id}</td>
            <td>${category.name || ''}</td>
            <td>${category.productCount || 0}</td>
            <td>${category.description || '-'}</td>
            <td>
                <button class="btn btn-sm btn-primary me-1" onclick="editCategory(${category.id})">
                    <i class="bi bi-pencil"></i> Sửa
                </button>
                <button class="btn btn-sm btn-danger" onclick="confirmDeleteCategory(${category.id})">
                    <i class="bi bi-trash"></i> Xóa
                </button>
            </td>
        </tr>
    `).join('');
}

// Open category modal for adding
function openCategoryModal() {
    document.getElementById('categoryForm').reset();
    document.getElementById('categoryId').value = '';
    document.getElementById('categoryModalTitle').textContent = 'Thêm danh mục';
}

// Edit category
async function editCategory(categoryId) {
    try {
        const category = await api.getCategory(categoryId);
        document.getElementById('categoryId').value = category.id;
        document.getElementById('categoryName').value = category.name || '';
        document.getElementById('categoryDescription').value = category.description || '';
        
        document.getElementById('categoryModalTitle').textContent = 'Sửa danh mục';
        new bootstrap.Modal(document.getElementById('categoryModal')).show();
    } catch (error) {
        console.error('Error loading category:', error);
        showAlert('Lỗi khi tải thông tin danh mục', 'error');
    }
}

// Save category (create or update)
async function saveCategory() {
    try {
        const categoryId = document.getElementById('categoryId').value;
        const formData = {
            name: document.getElementById('categoryName').value,
            description: document.getElementById('categoryDescription').value
        };

        if (categoryId) {
            await api.updateCategory(categoryId, formData);
            showAlert('Cập nhật danh mục thành công!', 'success');
        } else {
            await api.createCategory(formData);
            showAlert('Tạo danh mục thành công!', 'success');
        }

        bootstrap.Modal.getInstance(document.getElementById('categoryModal')).hide();
        loadCategories();
        document.getElementById('categoryForm').reset();
    } catch (error) {
        console.error('Error saving category:', error);
        showAlert('Lỗi khi lưu danh mục: ' + error.message, 'error');
    }
}

// Delete category
async function confirmDeleteCategory(categoryId) {
    if (!confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
        return;
    }

    try {
        await api.deleteCategory(categoryId);
        showAlert('Xóa danh mục thành công!', 'success');
        loadCategories();
    } catch (error) {
        console.error('Error deleting category:', error);
        showAlert('Lỗi khi xóa danh mục: ' + error.message, 'error');
    }
}

// Export to Excel - Make globally available
window.exportToExcel = function() {
    console.log('=== exportToExcel CALLED ===');
    
    // Check if data is loaded
    if (!categories || categories.length === 0) {
        showAlert('Không có dữ liệu để xuất! Vui lòng đợi dữ liệu được tải.', 'error');
        return;
    }

    // Check if XLSX library is loaded
    if (typeof XLSX === 'undefined') {
        showAlert('Thư viện Excel chưa được tải. Vui lòng tải lại trang!', 'error');
        return;
    }

    try {
        const data = categories.map(category => ({
            'ID': category.id || '',
            'Tên danh mục': category.name || '',
            'Số sản phẩm': category.productCount || 0,
            'Mô tả': category.description || '-'
        }));

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(data);
        
        const colWidths = [
            { wch: 10 }, // ID
            { wch: 30 }, // Tên
            { wch: 15 }, // Số sản phẩm
            { wch: 50 }  // Mô tả
        ];
        ws['!cols'] = colWidths;

        XLSX.utils.book_append_sheet(wb, ws, 'Danh mục');
        const filename = `BaoCaoDanhMuc_${new Date().toISOString().split('T')[0]}.xlsx`;
        XLSX.writeFile(wb, filename);
        showAlert('Xuất Excel thành công!', 'success');
    } catch (error) {
        console.error('Error exporting to Excel:', error);
        showAlert('Lỗi khi xuất Excel: ' + (error.message || 'Unknown error'), 'error');
    }
};

// Export to PDF - Make globally available
window.exportToPDF = function() {
    console.log('=== exportToPDF CALLED ===');
    
    // Check if data is loaded
    if (!categories || categories.length === 0) {
        showAlert('Không có dữ liệu để xuất! Vui lòng đợi dữ liệu được tải.', 'error');
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
        doc.text('BÁO CÁO DANH MỤC', 14, 15);
        doc.setFontSize(10);
        doc.text(`Ngày xuất: ${new Date().toLocaleDateString('vi-VN')}`, 14, 22);
        
        const tableData = categories.map(category => [
            category.id || '',
            (category.name || '').substring(0, 30),
            category.productCount || 0,
            (category.description || '-').substring(0, 40)
        ]);

        doc.autoTable({
            startY: 28,
            head: [['ID', 'Tên danh mục', 'Số SP', 'Mô tả']],
            body: tableData,
            styles: { fontSize: 8 },
            headStyles: { fillColor: [66, 139, 202], textColor: 255 },
            alternateRowStyles: { fillColor: [245, 245, 245] },
            margin: { top: 28, left: 14, right: 14 }
        });
        
        const filename = `BaoCaoDanhMuc_${new Date().toISOString().split('T')[0]}.pdf`;
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
    if (!alertContainer) {
        console.warn('alertContainer not found');
        return;
    }

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

