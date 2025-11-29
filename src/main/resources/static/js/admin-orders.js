// Admin Orders Management
let orders = [];

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    loadOrders();
    
    // Filter listeners
    const searchInput = document.getElementById('searchInput');
    const statusFilter = document.getElementById('statusFilter');
    const fromDate = document.getElementById('fromDate');
    const toDate = document.getElementById('toDate');
    
    if (searchInput) searchInput.addEventListener('input', filterOrders);
    if (statusFilter) statusFilter.addEventListener('change', filterOrders);
    if (fromDate) fromDate.addEventListener('change', filterOrders);
    if (toDate) toDate.addEventListener('change', filterOrders);
});

// Load orders
async function loadOrders() {
    try {
        const response = await api.getAllOrders();
        // Handle ApiResponse wrapper - response.data contains the list
        orders = response.data || response || [];
        console.log('Loaded orders:', orders);
        renderOrdersTable(orders);
    } catch (error) {
        console.error('Error loading orders:', error);
        showAlert('Lỗi khi tải danh sách đơn hàng: ' + (error.message || 'Unknown error'), 'error');
        const tbody = document.getElementById('ordersTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-danger">Lỗi khi tải dữ liệu</td></tr>';
        }
    }
}

// Filter orders
function filterOrders() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;
    const fromDate = document.getElementById('fromDate').value;
    const toDate = document.getElementById('toDate').value;

    let filtered = orders.filter(order => {
        const matchSearch = !searchTerm || 
            (order.orderNumber && order.orderNumber.toLowerCase().includes(searchTerm)) ||
            (order.customerName && order.customerName.toLowerCase().includes(searchTerm));

        const matchStatus = !statusFilter || order.status === statusFilter;

        let matchDate = true;
        if (fromDate || toDate) {
            const orderDate = new Date(order.createdAt);
            if (fromDate) {
                const from = new Date(fromDate);
                if (orderDate < from) matchDate = false;
            }
            if (toDate) {
                const to = new Date(toDate);
                to.setHours(23, 59, 59, 999);
                if (orderDate > to) matchDate = false;
            }
        }

        return matchSearch && matchStatus && matchDate;
    });

    renderOrdersTable(filtered);
}

// Render orders table
function renderOrdersTable(ordersToRender) {
    const tbody = document.getElementById('ordersTableBody');
    if (!tbody) return;

    if (!ordersToRender || ordersToRender.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center">Không có đơn hàng nào</td></tr>';
        return;
    }

    tbody.innerHTML = ordersToRender.map(order => {
        const statusBadge = getStatusBadge(order.status);
        const totalAmount = order.totalAmount || 0;
        const createdAt = order.createdAt ? 
            new Date(order.createdAt).toLocaleDateString('vi-VN', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            }) : '-';
        
        return `
        <tr>
            <td>${order.id}</td>
            <td><strong>${order.orderNumber || '-'}</strong></td>
            <td>${order.customerName || order.customerEmail || '-'}</td>
            <td class="text-end">${formatPrice(totalAmount)}</td>
            <td>${statusBadge}</td>
            <td>${createdAt}</td>
            <td>
                <div class="btn-group" role="group">
                    <button class="btn btn-sm btn-info me-1" onclick="viewOrderDetail(${order.id})" title="Xem chi tiết">
                        <i class="bi bi-eye"></i> Xem
                    </button>
                    ${order.status !== 'CANCELLED' && order.status !== 'COMPLETED' && order.status !== 'REFUNDED' ? `
                        <select class="form-select form-select-sm" style="width: auto;" 
                                onchange="updateOrderStatus(${order.id}, this.value)">
                            <option value="">Cập nhật</option>
                            ${getStatusOptions(order.status)}
                        </select>
                    ` : '<span class="badge bg-secondary">Hoàn tất</span>'}
                </div>
            </td>
        </tr>
    `;
    }).join('');
}

// Get status badge
function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge bg-warning">Chờ xác nhận</span>',
        'CONFIRMED': '<span class="badge bg-info">Đã xác nhận</span>',
        'PROCESSING': '<span class="badge bg-primary">Đang xử lý</span>',
        'SHIPPING': '<span class="badge bg-info">Đang giao hàng</span>',
        'SHIPPED': '<span class="badge bg-info">Đang giao hàng</span>',
        'DELIVERED': '<span class="badge bg-success">Đã giao hàng</span>',
        'COMPLETED': '<span class="badge bg-success">Hoàn thành</span>',
        'CANCELLED': '<span class="badge bg-danger">Đã hủy</span>',
        'REFUNDED': '<span class="badge bg-secondary">Đã hoàn tiền</span>'
    };
    return badges[status] || '<span class="badge bg-secondary">' + status + '</span>';
}

// Get status options for select
function getStatusOptions(currentStatus) {
    const statusMap = {
        'PENDING': '<option value="PROCESSING">Đang xử lý</option><option value="CANCELLED">Hủy</option>',
        'CONFIRMED': '<option value="PROCESSING">Đang xử lý</option><option value="CANCELLED">Hủy</option>',
        'PROCESSING': '<option value="SHIPPING">Đang giao hàng</option><option value="CANCELLED">Hủy</option>',
        'SHIPPING': '<option value="DELIVERED">Đã giao hàng</option>',
        'SHIPPED': '<option value="DELIVERED">Đã giao hàng</option>',
        'DELIVERED': '<option value="COMPLETED">Hoàn thành</option>'
    };
    return statusMap[currentStatus] || '';
}

// View order detail
async function viewOrderDetail(orderId) {
    try {
        const response = await api.getOrderDetail(orderId);
        // Handle ApiResponse wrapper if needed
        const order = response.data || response;
        
        if (!order) {
            throw new Error('Không tìm thấy đơn hàng');
        }
        
        const modalBody = document.getElementById('orderDetailBody');
        
        // Get customer info from order or items
        const customerName = order.customerName || 'N/A';
        const customerEmail = order.customerEmail || 'N/A';
        const customerPhone = order.customerPhone || 'N/A';
        
        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <h5>Đơn hàng: <strong>${order.orderNumber || '-'}</strong></h5>
                </div>
                <div class="col-md-6 text-end">
                    ${getStatusBadge(order.status || 'PENDING')}
                </div>
            </div>
            <hr>
            <div class="row mb-3">
                <div class="col-md-6">
                    <p><strong>Khách hàng:</strong> ${customerName}</p>
                    <p><strong>Email:</strong> ${customerEmail}</p>
                    <p><strong>Số điện thoại:</strong> ${customerPhone}</p>
                    <p><strong>Địa chỉ:</strong> ${order.shippingAddress || '-'}</p>
                </div>
                <div class="col-md-6">
                    <p><strong>Phương thức thanh toán:</strong> ${order.paymentMethod || '-'}</p>
                    <p><strong>Trạng thái thanh toán:</strong> ${order.paymentStatus || '-'}</p>
                    <p><strong>Ngày đặt:</strong> ${order.createdAt ? new Date(order.createdAt).toLocaleString('vi-VN') : '-'}</p>
                </div>
            </div>
            ${order.voucherCode ? `
                <div class="alert alert-info">
                    <strong>Voucher:</strong> ${order.voucherCode} 
                    ${order.voucherDiscount ? `- Giảm ${formatPrice(order.voucherDiscount)}` : ''}
                </div>
            ` : ''}
            <hr>
            <h6>Sản phẩm:</h6>
            <div class="table-responsive">
                <table class="table table-sm table-bordered">
                    <thead class="table-light">
                        <tr>
                            <th>Sản phẩm</th>
                            <th class="text-center">Số lượng</th>
                            <th class="text-end">Giá</th>
                            <th class="text-end">Thành tiền</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${order.items && order.items.length > 0 ? order.items.map(item => `
                            <tr>
                                <td>${item.productName || 'N/A'}</td>
                                <td class="text-center">${item.quantity || 0}</td>
                                <td class="text-end">${formatPrice(item.price || 0)}</td>
                                <td class="text-end">${formatPrice(item.lineTotal || (item.price || 0) * (item.quantity || 0))}</td>
                            </tr>
                        `).join('') : '<tr><td colspan="4" class="text-center">Không có sản phẩm</td></tr>'}
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" class="text-end"><strong>Tạm tính:</strong></td>
                            <td class="text-end">${formatPrice((order.items || []).reduce((sum, item) => sum + (item.lineTotal || 0), 0))}</td>
                        </tr>
                        ${order.voucherDiscount && order.voucherDiscount > 0 ? `
                            <tr>
                                <td colspan="3" class="text-end"><strong>Giảm giá:</strong></td>
                                <td class="text-end text-danger">-${formatPrice(order.voucherDiscount || 0)}</td>
                            </tr>
                        ` : ''}
                        <tr>
                            <td colspan="3" class="text-end"><strong>Phí vận chuyển:</strong></td>
                            <td class="text-end">${formatPrice(order.shippingFee || 0)}</td>
                        </tr>
                        <tr class="table-primary">
                            <td colspan="3" class="text-end"><strong>Tổng cộng:</strong></td>
                            <td class="text-end"><strong>${formatPrice(order.totalAmount || 0)}</strong></td>
                        </tr>
                    </tfoot>
                </table>
            </div>
            ${order.statusHistory && order.statusHistory.length > 0 ? `
                <hr>
                <h6>Lịch sử trạng thái:</h6>
                <div class="list-group">
                    ${order.statusHistory.map(history => `
                        <div class="list-group-item">
                            <div class="d-flex justify-content-between">
                                <span>${history.oldStatus || '-'} → ${history.newStatus || '-'}</span>
                                <small class="text-muted">${history.createdAt ? new Date(history.createdAt).toLocaleString('vi-VN') : '-'}</small>
                            </div>
                        </div>
                    `).join('')}
                </div>
            ` : ''}
        `;
        
        new bootstrap.Modal(document.getElementById('orderDetailModal')).show();
    } catch (error) {
        console.error('Error loading order detail:', error);
        showAlert('Lỗi khi tải chi tiết đơn hàng: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Update order status
async function updateOrderStatus(orderId, newStatus) {
    if (!newStatus) return;
    
    // Get status display name
    const statusNames = {
        'PENDING': 'Chờ xử lý',
        'PROCESSING': 'Đang xử lý',
        'SHIPPED': 'Đã giao hàng',
        'DELIVERED': 'Đã nhận hàng',
        'CANCELLED': 'Đã hủy'
    };
    const statusDisplay = statusNames[newStatus] || newStatus;
    
    if (!confirm(`Bạn có chắc chắn muốn cập nhật trạng thái đơn hàng sang "${statusDisplay}"?`)) {
        // Reset select to current value
        const select = event?.target;
        if (select) select.value = '';
        return;
    }

    try {
        await api.updateOrderStatus(orderId, newStatus);
        showAlert('Cập nhật trạng thái đơn hàng thành công!', 'success');
        await loadOrders();
    } catch (error) {
        console.error('Error updating order status:', error);
        showAlert('Lỗi khi cập nhật trạng thái đơn hàng: ' + (error.message || 'Unknown error'), 'error');
        // Reset select on error
        const select = event?.target;
        if (select) select.value = '';
    }
}

// Format price to VNĐ
function formatPrice(price) {
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(price);
}

// Export to Excel
function exportToExcel() {
    if (!orders || orders.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    try {
        const data = orders.map(order => ({
            'ID': order.id || '',
            'Mã đơn hàng': order.orderNumber || '',
            'Khách hàng': order.customerName || order.customerEmail || '',
            'Email': order.customerEmail || '',
            'Tổng tiền (VNĐ)': order.totalAmount || 0,
            'Trạng thái': order.status || '',
            'Trạng thái thanh toán': order.paymentStatus || '',
            'Ngày đặt': order.createdAt ? new Date(order.createdAt).toLocaleString('vi-VN') : ''
        }));

        const wb = XLSX.utils.book_new();
        const ws = XLSX.utils.json_to_sheet(data);
        
        const colWidths = [
            { wch: 10 }, // ID
            { wch: 20 }, // Mã đơn hàng
            { wch: 25 }, // Khách hàng
            { wch: 30 }, // Email
            { wch: 18 }, // Tổng tiền
            { wch: 15 }, // Trạng thái
            { wch: 20 }, // Trạng thái thanh toán
            { wch: 20 }  // Ngày đặt
        ];
        ws['!cols'] = colWidths;

        XLSX.utils.book_append_sheet(wb, ws, 'Đơn hàng');
        const filename = `BaoCaoDonHang_${new Date().toISOString().split('T')[0]}.xlsx`;
        XLSX.writeFile(wb, filename);
        showAlert('Xuất Excel thành công!', 'success');
    } catch (error) {
        console.error('Error exporting to Excel:', error);
        showAlert('Lỗi khi xuất Excel: ' + (error.message || 'Unknown error'), 'error');
    }
}

// Export to PDF
function exportToPDF() {
    if (!orders || orders.length === 0) {
        showAlert('Không có dữ liệu để xuất!', 'error');
        return;
    }

    try {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('l', 'mm', 'a4');
        
        doc.setFontSize(16);
        doc.text('BÁO CÁO ĐƠN HÀNG', 14, 15);
        doc.setFontSize(10);
        doc.text(`Ngày xuất: ${new Date().toLocaleDateString('vi-VN')}`, 14, 22);
        
        const tableData = orders.map(order => [
            order.id || '',
            (order.orderNumber || '').substring(0, 15),
            (order.customerName || order.customerEmail || '').substring(0, 20),
            formatPrice(order.totalAmount || 0),
            (order.status || '').substring(0, 15),
            order.createdAt ? new Date(order.createdAt).toLocaleDateString('vi-VN') : ''
        ]);

        doc.autoTable({
            startY: 28,
            head: [['ID', 'Mã đơn', 'Khách hàng', 'Tổng tiền', 'Trạng thái', 'Ngày đặt']],
            body: tableData,
            styles: { fontSize: 8 },
            headStyles: { fillColor: [66, 139, 202], textColor: 255 },
            alternateRowStyles: { fillColor: [245, 245, 245] },
            margin: { top: 28, left: 14, right: 14 }
        });
        
        const filename = `BaoCaoDonHang_${new Date().toISOString().split('T')[0]}.pdf`;
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

