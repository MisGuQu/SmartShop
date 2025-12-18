// Cart page functionality
document.addEventListener('DOMContentLoaded', async () => {
    const authenticated = await checkAuthStatus();
    if (!authenticated) {
        window.location.href = '/auth/login.html?redirect=/cart.html';
        return;
    }
    
    await updateAuthUI();
    await loadCart();
});

async function loadCart() {
    try {
        const cart = await api.getCart();
        displayCart(cart);
    } catch (error) {
        console.error('Error loading cart:', error);
        document.getElementById('cartContent').innerHTML = 
            '<div style="padding: 40px; text-align: center; color: #dc2626;">Không thể tải giỏ hàng. Vui lòng thử lại sau.</div>';
    }
}

function displayCart(cart) {
    const container = document.getElementById('cartContent');
    
    if (!cart || !cart.items || cart.items.length === 0) {
        if (window.setCartBadgeCount) {
            window.setCartBadgeCount(0);
        }
        container.innerHTML = `
            <div class="empty-cart">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3.5 5h2.2l1.4 9.2a1 1 0 0 0 .99.85h9.26a1 1 0 0 0 .98-.8l1.12-5.6H7.16"/>
                    <circle cx="10" cy="19" r="1.4"/>
                    <circle cx="16.5" cy="19" r="1.4"/>
                    <line x1="3" y1="5" x2="21" y2="5" stroke-width="2"/>
                </svg>
                <h3>Giỏ hàng của bạn đang trống</h3>
                <p>Hãy thêm sản phẩm vào giỏ hàng để tiếp tục mua sắm</p>
                <a href="/product.html" class="button">Tiếp tục mua sắm</a>
            </div>
        `;
        return;
    }
    
    const totalAmount = cart.totalAmount || 0;
    const subtotal = cart.subtotal || totalAmount;
    const shipping = cart.shippingFee || 0;
    const discount = cart.discountAmount || 0;
    
    let itemsHtml = '';
    cart.items.forEach(item => {
        const price = item.price || 0;
        const quantity = item.quantity || 1;
        const lineTotal = item.lineTotal || (price * quantity);
        const productName = escapeHtml(item.productName || 'Sản phẩm');
        
        itemsHtml += `
            <div class="cart-item">
                <img src="${item.imageUrl || 'https://images.unsplash.com/photo-1512447608772-994891cd05d0?auto=format&fit=crop&w=640&q=80'}" 
                     class="cart-item-image" 
                     alt="${productName}">
                <div class="cart-item-info">
                    <div class="cart-item-name">
                        <a href="/product-detail.html?id=${item.productId}">${productName}</a>
                    </div>
                    <div class="cart-item-price">${formatPrice(price)}</div>
                </div>
                <div class="cart-item-quantity">
                    <input type="number" 
                           class="quantity-input" 
                           value="${quantity}" 
                           min="1"
                           onchange="updateQuantity(${item.productId}, this.value)">
                </div>
                <div class="cart-item-total">${formatPrice(lineTotal)}</div>
                <div class="cart-item-actions">
                    <button class="btn-remove" onclick="removeItem(${item.productId})" title="Xóa sản phẩm">
                        <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="3 6 5 6 21 6"/>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                        </svg>
                    </button>
                </div>
            </div>
        `;
    });
    
    container.innerHTML = `
        <div style="display: grid; grid-template-columns: 1fr 400px; gap: 32px;">
            <div>
                ${itemsHtml}
            </div>
            <div class="cart-summary">
                <h3 style="margin: 0 0 24px; font-size: 1.3rem;">Tóm tắt đơn hàng</h3>
                <div class="summary-row">
                    <span class="summary-label">Tạm tính</span>
                    <span class="summary-value">${formatPrice(subtotal)}</span>
                </div>
                ${shipping > 0 ? `
                <div class="summary-row">
                    <span class="summary-label">Phí vận chuyển</span>
                    <span class="summary-value">${formatPrice(shipping)}</span>
                </div>
                ` : ''}
                ${discount > 0 ? `
                <div class="summary-row">
                    <span class="summary-label">Giảm giá</span>
                    <span class="summary-value" style="color: #16a34a;">-${formatPrice(discount)}</span>
                </div>
                ` : ''}
                <div class="summary-row">
                    <span class="summary-label">Tổng cộng</span>
                    <span class="summary-value summary-total">${formatPrice(totalAmount)}</span>
                </div>
                <div style="margin-top: 24px; display: flex; flex-direction: column; gap: 12px;">
                    <a href="/checkout.html" class="button" style="text-align: center; width: 100%;">Thanh toán</a>
                    <a href="/product.html" class="button button--ghost" style="text-align: center; width: 100%;">Tiếp tục mua sắm</a>
                </div>
            </div>
        </div>
    `;
    const quantity = cart.totalQuantity ?? cart.items.reduce((sum, item) => sum + (item.quantity || 0), 0);
    if (window.setCartBadgeCount) {
        window.setCartBadgeCount(quantity);
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

async function updateQuantity(productId, quantity) {
    try {
        await api.updateCartItem(productId, parseInt(quantity));
        await loadCart();
        // Thông báo đã tắt theo yêu cầu
    } catch (error) {
        console.error('Error updating quantity:', error);
        await loadCart(); // Reload to reset
    }
}

async function removeItem(productId) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
        return;
    }
    
    try {
        await api.removeCartItem(productId);
        await loadCart();
        // Thông báo đã tắt theo yêu cầu
    } catch (error) {
        console.error('Error removing item:', error);
    }
}


