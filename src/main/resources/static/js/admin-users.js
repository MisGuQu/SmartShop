// Admin Users Management
let users = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadUsers();
    
    // Filter listeners
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const roleFilter = document.getElementById('roleFilter');
    
    if (searchInput) searchInput.addEventListener('input', filterUsers);
    if (statusFilter) statusFilter.addEventListener('change', filterUsers);
    if (roleFilter) roleFilter.addEventListener('change', filterUsers);
});

// Load users
async function loadUsers() {
    try {
        const response = await api.getAllUsers();
        // Handle ApiResponse wrapper - response.data contains the list
        users = response.data || response || [];
        
        // Debug: Log to check data structure
        console.log('Loaded users:', users);
        if (users.length > 0) {
            console.log('First user data:', users[0]);
            console.log('First user isActive:', users[0].isActive, typeof users[0].isActive);
        }
        
        renderUsersTable(users);
    } catch (error) {
        console.error('Error loading users:', error);
        showAlert('Lỗi khi tải danh sách người dùng: ' + (error.message || 'Unknown error'), 'error');
        const tbody = document.getElementById('usersTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-danger">Lỗi khi tải dữ liệu</td></tr>';
        }
    }
}

// Filter users
function filterUsers() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;
    const roleFilter = document.getElementById('roleFilter').value;

    let filtered = users.filter(user => {
        const matchSearch = !searchTerm || 
            (user.username && user.username.toLowerCase().includes(searchTerm)) ||
            (user.email && user.email.toLowerCase().includes(searchTerm)) ||
            (user.phone && user.phone.toLowerCase().includes(searchTerm)) ||
            (user.fullName && user.fullName.toLowerCase().includes(searchTerm));

        // Fix status filter - handle boolean comparison correctly
        // Check both isActive and active fields (Jackson may serialize differently)
        let isActiveValue = user.isActive !== undefined ? user.isActive : (user.active !== undefined ? user.active : true);
        
        // Convert to boolean properly
        let userActive = false;
        if (typeof isActiveValue === 'boolean') {
            userActive = isActiveValue;
        } else if (typeof isActiveValue === 'string') {
            userActive = isActiveValue === 'true' || isActiveValue === '1';
        } else if (typeof isActiveValue === 'number') {
            userActive = isActiveValue === 1;
        } else {
            userActive = isActiveValue !== false && isActiveValue !== 'false' && isActiveValue !== 0 && isActiveValue !== '0';
        }
        
        const matchStatus = !statusFilter || 
            (statusFilter === 'true' && userActive) ||
            (statusFilter === 'false' && !userActive);

        // Fix role filter - match exact role name (ROLE_CUSTOMER or ROLE_ADMIN)
        const matchRole = !roleFilter || 
            (user.roles && Array.isArray(user.roles) && user.roles.some(role => {
                // Handle both string roles and role objects
                const roleName = typeof role === 'string' ? role : (role.name || role);
                // Exact match with the filter value
                return roleName === roleFilter;
            }));

        return matchSearch && matchStatus && matchRole;
    });

    renderUsersTable(filtered);
}

// Render users table
function renderUsersTable(usersToRender) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;

    if (!usersToRender || usersToRender.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center">Không có người dùng nào</td></tr>';
        return;
    }

    tbody.innerHTML = usersToRender.map(user => {
        // Handle roles - it's a List<String> from UserResponse
        let rolesDisplay = 'Không có vai trò';
        let roleBadges = '';
        if (user.roles && user.roles.length > 0) {
            const roleNames = user.roles.map(r => {
                // Handle both string and object formats
                const roleName = typeof r === 'string' ? r : r.name;
                // Remove ROLE_ prefix for display
                return roleName.replace('ROLE_', '');
            });
            rolesDisplay = roleNames.join(', ');
            roleBadges = user.roles.map(r => {
                const roleName = typeof r === 'string' ? r : r.name;
                const displayName = roleName.replace('ROLE_', '');
                const badgeClass = roleName.includes('ADMIN') ? 'bg-danger' : 'bg-primary';
                return `<span class="badge ${badgeClass} me-1">${displayName}</span>`;
            }).join('');
        }
        
        // Handle isActive field - check both isActive and active (Jackson may serialize differently)
        // Also handle boolean, string, and number types
        let isActiveValue = user.isActive !== undefined ? user.isActive : (user.active !== undefined ? user.active : true);
        
        // Convert to boolean properly
        let isUserActive = false;
        if (typeof isActiveValue === 'boolean') {
            isUserActive = isActiveValue;
        } else if (typeof isActiveValue === 'string') {
            isUserActive = isActiveValue === 'true' || isActiveValue === '1';
        } else if (typeof isActiveValue === 'number') {
            isUserActive = isActiveValue === 1;
        } else {
            // Default to true if undefined/null
            isUserActive = isActiveValue !== false && isActiveValue !== 'false' && isActiveValue !== 0 && isActiveValue !== '0';
        }
        
        const statusBadge = isUserActive ? 
            '<span class="badge bg-success">Hoạt động</span>' : 
            '<span class="badge bg-danger">Đã khóa</span>';
        
        const createdAt = user.createdAt ? 
            new Date(user.createdAt).toLocaleDateString('vi-VN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';
        
        return `
        <tr>
            <td>${user.id}</td>
            <td>${user.username || '-'}</td>
            <td>${user.email || '-'}</td>
            <td>${user.phone || '-'}</td>
            <td>${roleBadges || rolesDisplay}</td>
            <td>${statusBadge}</td>
            <td>${createdAt}</td>
            <td>
                <div class="btn-group" role="group">
                    <button class="btn btn-sm ${isUserActive ? 'btn-warning' : 'btn-success'}" 
                            onclick="toggleUserStatus(${user.id}, ${!isUserActive})"
                            title="${isUserActive ? 'Khóa' : 'Mở khóa'} người dùng">
                        <i class="bi bi-${isUserActive ? 'lock' : 'unlock'}"></i> 
                        ${isUserActive ? 'Khóa' : 'Mở khóa'}
                    </button>
                    <button class="btn btn-sm btn-info" 
                            onclick="editUserRoles(${user.id})"
                            title="Chỉnh sửa vai trò">
                        <i class="bi bi-person-gear"></i> Vai trò
                    </button>
                </div>
            </td>
        </tr>
    `;
    }).join('');
}

// Toggle user status
async function toggleUserStatus(userId, isActive) {
    if (!confirm(`Bạn có chắc chắn muốn ${isActive ? 'mở khóa' : 'khóa'} người dùng này?`)) {
        return;
    }

    try {
        // Ensure isActive is a boolean
        const statusValue = isActive === true || isActive === 'true';
        const response = await api.updateUserStatus(userId, statusValue);
        
        // Check if response indicates success
        if (response) {
            showAlert(`Đã ${statusValue ? 'mở khóa' : 'khóa'} người dùng thành công!`, 'success');
            // Reload users to get updated data
            await loadUsers();
        } else {
            throw new Error('Không nhận được phản hồi từ server');
        }
    } catch (error) {
        console.error('Error updating user status:', error);
        showAlert('Lỗi khi cập nhật trạng thái người dùng: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Current user being edited
let currentEditingUserId = null;

// Edit user roles
async function editUserRoles(userId) {
    const user = users.find(u => u.id === userId);
    if (!user) {
        showAlert('Không tìm thấy người dùng', 'error');
        return;
    }

    currentEditingUserId = userId;

    // Get current roles
    const currentRoles = user.roles || [];
    const currentRoleNames = currentRoles.map(r => typeof r === 'string' ? r : r.name);
    
    // Update modal info
    document.getElementById('roleModalUserInfo').innerHTML = 
        `<strong>Người dùng:</strong> ${user.username || user.email}<br>` +
        `<strong>Vai trò hiện tại:</strong> ${currentRoleNames.length > 0 ? currentRoleNames.join(', ') : 'Không có'}`;
    
    // Set checkboxes based on current roles
    document.getElementById('roleCustomer').checked = currentRoleNames.includes('ROLE_CUSTOMER');
    document.getElementById('roleAdmin').checked = currentRoleNames.includes('ROLE_ADMIN');
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('roleModal'));
    modal.show();
}

// Save user roles
async function saveUserRoles() {
    if (!currentEditingUserId) {
        showAlert('Lỗi: Không tìm thấy người dùng', 'error');
        return;
    }

    // Get selected roles
    const selectedRoles = [];
    if (document.getElementById('roleCustomer').checked) {
        selectedRoles.push('ROLE_CUSTOMER');
    }
    if (document.getElementById('roleAdmin').checked) {
        selectedRoles.push('ROLE_ADMIN');
    }

    if (selectedRoles.length === 0) {
        showAlert('Vui lòng chọn ít nhất một vai trò', 'error');
        return;
    }

    try {
        await api.updateUserRoles(currentEditingUserId, selectedRoles);
        showAlert('Cập nhật vai trò thành công!', 'success');
        
        // Hide modal
        const modal = bootstrap.Modal.getInstance(document.getElementById('roleModal'));
        modal.hide();
        
        // Reload users
        loadUsers();
        currentEditingUserId = null;
    } catch (error) {
        console.error('Error updating user roles:', error);
        showAlert('Lỗi khi cập nhật vai trò: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Export to Excel
function exportToExcel() {
    if (!users || users.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    try {
        const data = users.map(user => {
            let isActiveValue = user.isActive !== undefined ? user.isActive : (user.active !== undefined ? user.active : true);
            let isUserActive = false;
            if (typeof isActiveValue === 'boolean') {
                isUserActive = isActiveValue;
            } else if (typeof isActiveValue === 'string') {
                isUserActive = isActiveValue === 'true' || isActiveValue === '1';
            } else {
                isUserActive = isActiveValue !== false && isActiveValue !== 'false' && isActiveValue !== 0 && isActiveValue !== '0';
            }

            const roles = user.roles && user.roles.length > 0 ? 
                user.roles.map(r => typeof r === 'string' ? r.replace('ROLE_', '') : r.name.replace('ROLE_', '')).join(', ') : 
                'Không có';

            return {
                'ID': user.id || '',
                'Tên người dùng': user.username || '',
                'Email': user.email || '',
                'Họ tên': user.fullName || '',
                'Số điện thoại': user.phone || '',
                'Vai trò': roles,
                'Trạng thái': isUserActive ? 'Hoạt động' : 'Đã khóa',
                'Ngày đăng ký': user.createdAt ? new Date(user.createdAt).toLocaleString('vi-VN') : ''
            };
        });

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(data);
        
        const colWidths = [
            { wch: 10 }, // ID
            { wch: 20 }, // Tên người dùng
            { wch: 30 }, // Email
            { wch: 25 }, // Họ tên
            { wch: 15 }, // Số điện thoại
            { wch: 20 }, // Vai trò
            { wch: 15 }, // Trạng thái
            { wch: 20 }  // Ngày đăng ký
        ];
        ws['!cols'] = colWidths;

        XLSX.utils.book_append_sheet(wb, ws, 'Người dùng');
        const filename = `BaoCaoNguoiDung_${new Date().toISOString().split('T')[0]}.xlsx`;
        XLSX.writeFile(wb, filename);
        showAlert('Xuất Excel thành công!', 'success');
    } catch (error) {
        console.error('Error exporting to Excel:', error);
        showAlert('Lỗi khi xuất Excel: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Export to PDF
function exportToPDF() {
    if (!users || users.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    try {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('l', 'mm', 'a4');
        
        doc.setFontSize(16);
        doc.text('BÁO CÁO NGƯỜI DÙNG', 14, 15);
        doc.setFontSize(10);
        doc.text(`Ngày xuất: ${new Date().toLocaleDateString('vi-VN')}`, 14, 22);
        
        const tableData = users.map(user => {
            let isActiveValue = user.isActive !== undefined ? user.isActive : (user.active !== undefined ? user.active : true);
            let isUserActive = false;
            if (typeof isActiveValue === 'boolean') {
                isUserActive = isActiveValue;
            } else if (typeof isActiveValue === 'string') {
                isUserActive = isActiveValue === 'true' || isActiveValue === '1';
            } else {
                isUserActive = isActiveValue !== false && isActiveValue !== 'false' && isActiveValue !== 0 && isActiveValue !== '0';
            }

            const roles = user.roles && user.roles.length > 0 ? 
                user.roles.map(r => typeof r === 'string' ? r.replace('ROLE_', '') : r.name.replace('ROLE_', '')).join(', ') : 
                '-';

            return [
                user.id || '',
                (user.username || '').substring(0, 15),
                (user.email || '').substring(0, 25),
                (user.fullName || '-').substring(0, 20),
                roles.substring(0, 15),
                isUserActive ? 'Hoạt động' : 'Đã khóa',
                user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : ''
            ];
        });

        doc.autoTable({
            startY: 28,
            head: [['ID', 'Tên đăng nhập', 'Email', 'Họ tên', 'Vai trò', 'Trạng thái', 'Ngày ĐK']],
            body: tableData,
            styles: { fontSize: 8 },
            headStyles: { fillColor: [66, 139, 202], textColor: 255 },
            alternateRowStyles: { fillColor: [245, 245, 245] },
            margin: { top: 28, left: 14, right: 14 }
        });
        
        const filename = `BaoCaoNguoiDung_${new Date().toISOString().split('T')[0]}.pdf`;
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

