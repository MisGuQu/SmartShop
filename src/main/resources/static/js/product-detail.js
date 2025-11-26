// Product detail page functionality
let productId = null;
let selectedVariantId = null;

document.addEventListener('DOMContentLoaded', async () => {
    await updateAuthUI();
    
    // Get product ID from URL
    const urlParams = new URLSearchParams(window.location.search);
    productId = urlParams.get('id');
    
    if (!productId) {
        document.getElementById('productDetail').innerHTML = 
            '<div class="alert alert--error" style="margin: 32px 0;">Không tìm thấy sản phẩm</div>';
        return;
    }
    
    await loadProductDetail();
    await loadReviews();
});

async function loadProductDetail() {
    try {
        const product = await api.getProduct(productId);
        displayProduct(product);
    } catch (error) {
        console.error('Error loading product:', error);
        document.getElementById('productDetail').innerHTML = 
            '<div class="alert alert--error" style="margin: 32px 0;">Không thể tải thông tin sản phẩm</div>';
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

function displayProduct(product) {
    const container = document.getElementById('productDetail');
    
    const mainImageUrl = product.imageUrl || product.primaryImageUrl || 'https://images.unsplash.com/photo-1512447608772-994891cd05d0?auto=format&fit=crop&w=800&q=80';
    const gallery = product.gallery || [];
    
    let variantsHtml = '';
    if (product.hasVariants && product.variants && product.variants.length > 0) {
        variantsHtml = `
            <div class="product-detail__form">
                <div class="form-field">
                    <label for="variantSelect">Chọn phiên bản:</label>
                    <select id="variantSelect" onchange="selectVariant(this.value)">
                        <option value="">-- Chọn phiên bản --</option>
                        ${product.variants.map(v => `
                            <option value="${v.id}" data-price="${v.price}">
                                ${escapeHtml(v.variantName || '')} - ${formatPrice(v.price)}
                            </option>
                        `).join('')}
                    </select>
                </div>
            </div>
        `;
    }
    
    const currentPrice = selectedVariantId 
        ? product.variants?.find(v => v.id == selectedVariantId)?.price || product.price
        : product.price;
    
    container.innerHTML = `
        <div class="product-detail">
            <div class="product-detail__gallery">
                <div class="product-detail__main" style="background-image: url('${mainImageUrl}');"></div>
                ${gallery.length > 0 ? `
                    <div class="product-detail__thumbs">
                        ${gallery.slice(0, 4).map(img => `
                            <button class="product-detail__thumb" 
                                    style="background-image: url('${img}');"
                                    onclick="changeMainImage('${img}')">
                            </button>
                        `).join('')}
                    </div>
                ` : ''}
            </div>
            
            <div class="product-detail__info">
                <h1 style="margin: 0 0 16px; font-size: clamp(1.8rem, 3vw, 2.4rem);">${escapeHtml(product.name || 'Sản phẩm')}</h1>
                <p class="product-detail__price">${formatPrice(currentPrice || 0)}</p>
                ${product.description ? `<p class="product-detail__description">${escapeHtml(product.description)}</p>` : ''}
                
                ${variantsHtml}
                
                <div class="product-detail__form">
                    <div class="form-field">
                        <label for="quantity">Số lượng:</label>
                        <input type="number" id="quantity" value="1" min="1" max="${product.stockQuantity || 999}" style="width: 120px;">
                    </div>
                </div>
                
                ${product.stockQuantity !== undefined ? `
                    <div class="product-detail__meta">
                        <ul>
                            <li>Số lượng còn lại: <strong>${product.stockQuantity}</strong></li>
                            ${product.categoryName ? `<li>Danh mục: <strong>${escapeHtml(product.categoryName)}</strong></li>` : ''}
                        </ul>
                    </div>
                ` : ''}
                
                <div class="product-detail__actions">
                    <button class="button button--primary" onclick="addToCart()">
                        <svg viewBox="0 0 24 24" style="width: 18px; height: 18px;" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 1 0 0 4 2 2 0 0 0 0-4zm-8 2a2 2 0 1 1-4 0 2 2 0 0 1 4 0z"/>
                        </svg>
                        Thêm vào giỏ hàng
                    </button>
                    <button class="button button--wishlist" id="wishlistBtn" onclick="addToWishlist()">
                        <svg viewBox="0 0 24 24" style="width: 18px; height: 18px;" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                        </svg>
                        Yêu thích
                    </button>
                </div>
            </div>
        </div>
    `;
}

function changeMainImage(imageUrl) {
    const mainImage = document.querySelector('.product-detail__main');
    if (mainImage) {
        mainImage.style.backgroundImage = `url('${imageUrl}')`;
    }
}

function selectVariant(variantId) {
    selectedVariantId = variantId;
    loadProductDetail(); // Reload to update price
}

async function addToCart() {
    const authenticated = await checkAuthStatus();
    if (!authenticated) {
        window.location.href = '/auth/login.html?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
        return;
    }
    
    const quantityInput = document.getElementById('quantity');
    const quantity = quantityInput ? parseInt(quantityInput.value) || 1 : 1;
    
    try {
        await api.addToCart(productId, selectedVariantId, quantity);
        showToast('Đã thêm vào giỏ hàng', 'success');
    } catch (error) {
        console.error('Add to cart error:', error);
        showToast('Không thể thêm vào giỏ hàng', 'error');
    }
}

async function addToWishlist() {
    const authenticated = await checkAuthStatus();
    if (!authenticated) {
        window.location.href = '/auth/login.html?redirect=' + encodeURIComponent(window.location.pathname + window.location.search);
        return;
    }
    
    try {
        await api.addToWishlist(productId, selectedVariantId);
        const btn = document.getElementById('wishlistBtn');
        if (btn) {
            btn.classList.add('is-active');
        }
        showToast('Đã thêm vào danh sách yêu thích', 'success');
    } catch (error) {
        console.error('Add to wishlist error:', error);
        showToast('Không thể thêm vào yêu thích', 'error');
    }
}

// Reviews are now handled by reviews.js
async function loadReviews() {
    // This function is kept for compatibility but reviews are loaded by reviews.js
}

async function updateAuthUI() {
    try {
        const authenticated = await checkAuthStatus();
        const anonymousDiv = document.getElementById('headerAccountAnonymous');
        const authDiv = document.getElementById('headerAccountAuth');
        const wishlistLink = document.getElementById('wishlistLink');
        const cartLink = document.getElementById('cartLink');
        
        if (authenticated) {
            anonymousDiv.classList.add('d-none');
            authDiv.classList.remove('d-none');
            wishlistLink.href = '/wishlist.html';
            cartLink.href = '/cart.html';
            
            try {
                const user = await api.getCurrentUser();
                if (user) {
                    const userNameEl = document.getElementById('userName');
                    const userProfileLink = document.getElementById('userProfileLink');
                    const adminChip = document.getElementById('adminChip');
                    
                    if (userNameEl && user.fullName) {
                        userNameEl.textContent = user.fullName;
                    } else if (userNameEl && user.email) {
                        userNameEl.textContent = user.email;
                    }
                    
                    if (user.roles && user.roles.includes('ADMIN')) {
                        adminChip.classList.remove('d-none');
                        if (userProfileLink) {
                            userProfileLink.href = '/admin/products.html';
                        }
                    } else {
                        if (userProfileLink) {
                            userProfileLink.href = '/cart.html';
                        }
                    }
                }
            } catch (error) {
                console.error('Error loading user info:', error);
            }
            
            const logoutButton = document.getElementById('logoutButton');
            if (logoutButton) {
                logoutButton.addEventListener('click', handleLogout);
            }
        } else {
            anonymousDiv.classList.remove('d-none');
            authDiv.classList.add('d-none');
            wishlistLink.href = '/auth/login.html?redirect=/wishlist.html';
            cartLink.href = '/auth/login.html?redirect=/cart.html';
        }
    } catch (error) {
        console.error('Error updating auth UI:', error);
    }
}

async function handleLogout() {
    try {
        await api.logout();
        window.location.href = '/';
    } catch (error) {
        console.error('Logout error:', error);
        window.location.href = '/';
    }
}

function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `cart-toast ${type === 'success' ? 'cart-toast--success' : 'cart-toast--error'}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transition = 'opacity 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}