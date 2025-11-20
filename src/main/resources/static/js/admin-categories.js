// Admin Categories Management
let categories = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadCategories();
});

// Load categories
async function loadCategories() {
    try {
        categories = await api.getCategories();
        renderCategoriesTable();
        loadCategoriesForParent();
    } catch (error) {
        console.error('Error loading categories:', error);
        showAlert('Lỗi khi tải danh sách danh mục', 'error');
    }
}

// Load categories for parent select
function loadCategoriesForParent() {
    const select = document.getElementById('categoryParent');
    select.innerHTML = '<option value="">Không có (danh mục gốc)</option>';
    categories.forEach(cat => {
        const option = document.createElement('option');
        option.value = cat.id;
        option.textContent = cat.name;
        select.appendChild(option);
    });
}

// Render categories table
function renderCategoriesTable() {
    const tbody = document.getElementById('categoriesTableBody');
    if (!tbody) return;

    if (!categories || categories.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center">Không có danh mục nào</td></tr>';
        return;
    }

    tbody.innerHTML = categories.map(category => `
        <tr>
            <td>${category.id}</td>
            <td>${category.name || ''}</td>
            <td>${category.parentName || '-'}</td>
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
        document.getElementById('categoryParent').value = category.parentId || '';
        
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
            description: document.getElementById('categoryDescription').value,
            parentId: document.getElementById('categoryParent').value ? 
                parseInt(document.getElementById('categoryParent').value) : null
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

// Export to Excel
function exportToExcel() {
    if (!categories || categories.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    try {
        const data = categories.map(category => ({
            'ID': category.id || '',
            'Tên danh mục': category.name || '',
            'Danh mục cha': category.parentName || '-',
            'Số sản phẩm': category.productCount || 0,
            'Mô tả': category.description || '-'
        }));

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(data);
        
        const colWidths = [
            { wch: 10 }, // ID
            { wch: 30 }, // Tên
            { wch: 25 }, // Danh mục cha
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
}

// Export to PDF
function exportToPDF() {
    if (!categories || categories.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
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
            (category.parentName || '-').substring(0, 20),
            category.productCount || 0,
            (category.description || '-').substring(0, 40)
        ]);

        doc.autoTable({
            startY: 28,
            head: [['ID', 'Tên danh mục', 'Danh mục cha', 'Số SP', 'Mô tả']],
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

