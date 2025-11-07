const wishlistToastEl = document.getElementById('wishlistToast');
const wishlistPageEl = document.querySelector('[data-wishlist-page]');

function showToast(message, tone = 'success') {
    if (!wishlistToastEl) return;
    wishlistToastEl.textContent = message;
    wishlistToastEl.style.background = tone === 'error' ? 'rgba(220,38,38,0.95)' : 'rgba(37,99,235,0.95)';
    wishlistToastEl.hidden = false;
    setTimeout(() => {
        wishlistToastEl.hidden = true;
    }, 2600);
}

async function fetchWishlist(url, options) {
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

async function handleAddToWishlist(button) {
    const productId = Number(button.dataset.productId);
    if (!productId) {
        showToast('Không tìm thấy sản phẩm', 'error');
        return;
    }

    const form = button.closest('form');
    const variantSelect = form ? form.querySelector('[name="variantId"]') : null;
    const variantId = variantSelect && variantSelect.value ? Number(variantSelect.value) : null;

    try {
        const params = new URLSearchParams({ productId: productId });
        if (variantId) {
            params.append('variantId', variantId);
        }

        await fetchWishlist(`/api/wishlist/items?${params}`, {
            method: 'POST',
        });

        button.classList.add('is-active');
        showToast('Đã thêm vào danh sách yêu thích');
    } catch (error) {
        showToast(error.message || 'Không thể thêm vào danh sách yêu thích', 'error');
    }
}

async function handleRemoveFromWishlist(button) {
    const itemId = Number(button.dataset.itemId);
    if (!itemId) {
        showToast('Không tìm thấy mục yêu thích', 'error');
        return;
    }

    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi danh sách yêu thích?')) {
        return;
    }

    try {
        await fetchWishlist(`/api/wishlist/items/${itemId}`, {
            method: 'DELETE',
        });

        const itemEl = button.closest('[data-item-id]');
        if (itemEl) {
            itemEl.remove();
        }

        if (wishlistPageEl) {
            const items = wishlistPageEl.querySelectorAll('[data-item-id]');
            if (items.length === 0) {
                window.location.reload();
            }
        }

        showToast('Đã xóa khỏi danh sách yêu thích');
    } catch (error) {
        showToast(error.message || 'Không thể xóa khỏi danh sách yêu thích', 'error');
    }
}

async function handleAddToCartFromWishlist(button) {
    const productId = Number(button.dataset.productId);
    const variantId = button.dataset.variantId ? Number(button.dataset.variantId) : null;

    if (!productId) {
        showToast('Không tìm thấy sản phẩm', 'error');
        return;
    }

    try {
        const response = await fetch('/api/cart/items', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ productId, variantId, quantity: 1 }),
        });

        if (response.status === 401) {
            window.location.href = '/auth/login?redirect=' + encodeURIComponent(window.location.pathname);
            return;
        }

        if (!response.ok) {
            throw new Error('Không thể thêm vào giỏ hàng');
        }

        showToast('Đã thêm vào giỏ hàng');
    } catch (error) {
        showToast(error.message || 'Không thể thêm vào giỏ hàng', 'error');
    }
}

document.addEventListener('click', (event) => {
    const addButton = event.target.closest('[data-add-to-wishlist]');
    if (addButton) {
        event.preventDefault();
        handleAddToWishlist(addButton);
        return;
    }

    const removeButton = event.target.closest('[data-remove-from-wishlist]');
    if (removeButton) {
        event.preventDefault();
        handleRemoveFromWishlist(removeButton);
        return;
    }

    const addToCartButton = event.target.closest('[data-add-to-cart-from-wishlist]');
    if (addToCartButton) {
        event.preventDefault();
        handleAddToCartFromWishlist(addToCartButton);
        return;
    }
});

