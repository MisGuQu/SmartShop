// Admin Categories Management
let categories = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
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
    console.log('categories:', categories);
    console.log('categories length:', categories ? categories.length : 0);
    console.log('XLSX available:', typeof XLSX !== 'undefined');
    
    alert('Đang xuất Excel...'); // Temporary alert to confirm function is called
    
    if (!categories || categories.length === 0) {
        console.warn('No categories to export');
        showAlert('Không có dữ liệu để xuất!', 'error');
        alert('Không có dữ liệu để xuất!');
        return;
    }

    // Check if XLSX library is loaded
    if (typeof XLSX === 'undefined') {
        console.error('XLSX library not loaded');
        showAlert('Thư viện Excel chưa được tải. Vui lòng tải lại trang!', 'error');
        alert('Thư viện Excel chưa được tải. Vui lòng tải lại trang!');
        return;
    }

    console.log('XLSX library loaded, starting export...');
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
        console.log('Writing file:', filename);
        XLSX.writeFile(wb, filename);
        console.log('Excel export successful');
        showAlert('Xuất Excel thành công!', 'success');
        alert('Xuất Excel thành công!');
    } catch (error) {
        console.error('Error exporting to Excel:', error);
        console.error('Error stack:', error.stack);
        const errorMsg = 'Lỗi khi xuất Excel: ' + (error.message || 'Unknown error');
        showAlert(errorMsg, 'error');
        alert(errorMsg);
    }
};

// Export to PDF - Make globally available
window.exportToPDF = function() {
    console.log('=== exportToPDF CALLED ===');
    console.log('categories:', categories);
    console.log('categories length:', categories ? categories.length : 0);
    console.log('jsPDF available:', typeof window.jspdf !== 'undefined');
    
    alert('Đang xuất PDF...'); // Temporary alert to confirm function is called
    
    if (!categories || categories.length === 0) {
        console.warn('No categories to export');
        showAlert('Không có dữ liệu để xuất!', 'error');
        alert('Không có dữ liệu để xuất!');
        return;
    }

    // Check if jsPDF library is loaded
    if (typeof window.jspdf === 'undefined') {
        console.error('jsPDF library not loaded');
        showAlert('Thư viện PDF chưa được tải. Vui lòng tải lại trang!', 'error');
        alert('Thư viện PDF chưa được tải. Vui lòng tải lại trang!');
        return;
    }

    console.log('jsPDF library loaded, starting export...');
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
        console.log('Saving PDF:', filename);
        doc.save(filename);
        console.log('PDF export successful');
        showAlert('Xuất PDF thành công!', 'success');
        alert('Xuất PDF thành công!');
    } catch (error) {
        console.error('Error exporting to PDF:', error);
        console.error('Error stack:', error.stack);
        const errorMsg = 'Lỗi khi xuất PDF: ' + (error.message || 'Unknown error');
        showAlert(errorMsg, 'error');
        alert(errorMsg);
    }
};

// Show alert
function showAlert(message, type = 'success') {
    console.log('showAlert called:', message, type);
    const alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) {
        console.warn('alertContainer not found, using alert() as fallback');
        alert(message);
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
    console.log('Alert displayed in container');

    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

