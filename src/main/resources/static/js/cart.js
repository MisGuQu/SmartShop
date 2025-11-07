const cartToastEl = document.getElementById('cartToast');
const cartPageEl = document.querySelector('[data-cart-page]');

function showToast(message, tone = 'success') {
    if (!cartToastEl) return;
    cartToastEl.textContent = message;
    cartToastEl.style.background = tone === 'error' ? 'rgba(220,38,38,0.95)' : 'rgba(37,99,235,0.95)';
    cartToastEl.hidden = false;
    setTimeout(() => {
        cartToastEl.hidden = true;
    }, 2600);
}

function formatCurrency(value) {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value || 0);
}

async function fetchCart(url, options) {
    const response = await fetch(url, {
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        credentials: 'include',
        ...options,
    });

    if (response.status === 401) {
        window.location.href = '/auth/login?redirect=' + encodeURIComponent(window.location.pathname);
        return null;
    }

    if (!response.ok) {
        let message = 'Đã xảy ra lỗi';
        const text = await response.text();
        if (text) {
            try {
                const errorBody = JSON.parse(text);
                if (errorBody && errorBody.message) {
                    message = errorBody.message;
                }
            } catch (e) {
                message = text;
            }
        }
        throw new Error(message);
    }

    return response.json();
}

function collectVariantId(form) {
    if (!form) return null;
    const select = form.querySelector('[name="variantId"]');
    if (!select) return null;
    if (!select.value) {
        return null;
    }
    return Number(select.value);
}

async function handleAddToCart(button) {
    const form = button.closest('form');
    const productIdInput = form ? form.querySelector('[name="productId"]') : null;
    const quantityInput = form ? form.querySelector('[name="quantity"]') : null;

    const productId = productIdInput ? Number(productIdInput.value) : Number(button.dataset.productId);
    const quantity = quantityInput ? Number(quantityInput.value || 1) : 1;
    const variantId = collectVariantId(form);

    if (form && form.querySelector('[name="variantId"]') && !variantId) {
        showToast('Vui lòng chọn phiên bản sản phẩm.', 'error');
        return;
    }

    try {
        const summary = await fetchCart('/api/cart/items', {
            method: 'POST',
            body: JSON.stringify({ productId, variantId, quantity }),
        });
        if (!summary) return;
        updateCartDom(summary);
        showToast('Đã thêm vào giỏ hàng');
    } catch (error) {
        showToast(error.message || 'Không thể thêm vào giỏ hàng', 'error');
    }
}

async function handleQuantityChange(input) {
    const itemId = Number(input.dataset.cartQuantity);
    const quantity = Number(input.value);
    if (!itemId || quantity <= 0) {
        showToast('Số lượng không hợp lệ', 'error');
        return;
    }
    try {
        const summary = await fetchCart(`/api/cart/items/${itemId}`, {
            method: 'PATCH',
            body: JSON.stringify({ quantity }),
        });
        if (!summary) return;
        updateCartDom(summary);
        showToast('Đã cập nhật giỏ hàng');
    } catch (error) {
        showToast(error.message || 'Không thể cập nhật giỏ hàng', 'error');
    }
}

async function handleRemoveItem(button) {
    const itemId = Number(button.dataset.itemId);
    if (!itemId) return;
    try {
        const summary = await fetchCart(`/api/cart/items/${itemId}`, { method: 'DELETE' });
        if (!summary) return;
        updateCartDom(summary);
        showToast('Đã xóa sản phẩm khỏi giỏ');
    } catch (error) {
        showToast(error.message || 'Không thể xóa sản phẩm', 'error');
    }
}

function updateCartDom(summary) {
    if (!summary) return;

    const subtotalEl = document.querySelector('[data-cart-subtotal]');
    if (subtotalEl) subtotalEl.textContent = formatCurrency(summary.subtotal);

    const countEl = document.querySelector('[data-cart-count]');
    if (countEl) countEl.textContent = summary.totalQuantity;

    const totalEl = document.querySelector('[data-cart-total]');
    if (totalEl) totalEl.textContent = formatCurrency(summary.subtotal);

    if (summary.items && Array.isArray(summary.items)) {
        summary.items.forEach((item) => {
            const qtyInput = document.querySelector(`[data-cart-quantity="${item.id}"]`);
            if (qtyInput) qtyInput.value = item.quantity;

            const subtotalItemEl = document.querySelector(`[data-item-subtotal][data-item-id="${item.id}"]`);
            if (subtotalItemEl) subtotalItemEl.textContent = formatCurrency(item.subtotal);
        });
    }

    if (cartPageEl && summary.items && summary.items.length === 0) {
        window.location.reload();
    }
}

const quantityTimers = new Map();

document.addEventListener('click', (event) => {
    const addButton = event.target.closest('[data-add-to-cart]');
    if (addButton) {
        event.preventDefault();
        handleAddToCart(addButton);
        return;
    }

    const removeButton = event.target.closest('[data-remove-item]');
    if (removeButton) {
        event.preventDefault();
        handleRemoveItem(removeButton);
    }
});

document.addEventListener('input', (event) => {
    const quantityInput = event.target.closest('[data-cart-quantity]');
    if (!quantityInput) return;
    const itemId = quantityInput.dataset.cartQuantity;
    if (!itemId) return;

    clearTimeout(quantityTimers.get(itemId));
    const timer = setTimeout(() => handleQuantityChange(quantityInput), 450);
    quantityTimers.set(itemId, timer);
});

document.addEventListener('change', (event) => {
    const quantityInput = event.target.closest('[data-cart-quantity]');
    if (!quantityInput) return;
    handleQuantityChange(quantityInput);
});

