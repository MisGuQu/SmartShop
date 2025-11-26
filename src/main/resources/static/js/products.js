// Products page functionality
let currentPage = 0;
const pageSize = 1000; // Load all products

document.addEventListener('DOMContentLoaded', async () => {
    await updateAuthUI();
    
    // Get search keyword from URL
    const urlParams = new URLSearchParams(window.location.search);
    const searchKeyword = urlParams.get('q') || '';
    
    // Set search input value if keyword exists
    const searchInput = document.getElementById('searchInput');
    if (searchInput && searchKeyword) {
        searchInput.value = searchKeyword;
    }
    
    // Load products
    await loadProducts();
});

async function loadProducts() {
    // Get search keyword from URL - support both 'q' and 'keyword'
    const urlParams = new URLSearchParams(window.location.search);
    const keyword = urlParams.get('q') || urlParams.get('keyword') || '';

    const params = {
        page: currentPage,
        size: pageSize,
        sort: 'createdAt',
        direction: 'desc'
    };

    // API supports both 'keyword' and 'q' parameter
    if (keyword) {
        params.keyword = keyword;
    }

    try {
        console.log('Loading products with params:', params);
        const response = await api.getProducts(params);
        console.log('Products API response:', response);
        
        // Handle paginated response - get all products
        let products = [];
        if (response && response.content && Array.isArray(response.content)) {
            // Spring Data Page format
            products = response.content;
            console.log('Using response.content, found', products.length, 'products');
        } else if (response && Array.isArray(response)) {
            // Direct array response
            products = response;
            console.log('Using direct array, found', products.length, 'products');
        } else if (response && response.data && Array.isArray(response.data)) {
            // Wrapped in data property
            products = response.data;
            console.log('Using response.data, found', products.length, 'products');
        } else {
            console.warn('Unexpected response format:', response);
        }
        
        console.log('Total products to display:', products.length);
        displayProducts(products);
        
        // Hide pagination since we're showing all products
        const paginationEl = document.getElementById('pagination');
        if (paginationEl) {
            paginationEl.innerHTML = '';
        }
    } catch (error) {
        console.error('Error loading products:', error);
        const container = document.getElementById('productsGrid');
        if (container) {
            container.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <p style="color: #dc2626;">Không thể tải sản phẩm. Vui lòng thử lại sau.</p>
                    <p style="color: #dc2626; font-size: 0.9rem; margin-top: 8px;">Lỗi: ${error.message || 'Unknown error'}</p>
                </div>
            `;
        }
    }
}


function displayProducts(products) {
    const container = document.getElementById('productsGrid');
    
    // Get search keyword from URL
    const urlParams = new URLSearchParams(window.location.search);
    const searchKeyword = urlParams.get('q') || '';
    
    if (products.length === 0) {
        if (searchKeyword) {
            container.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="11" cy="11" r="8"/>
                        <path d="m21 21-4.35-4.35"/>
                    </svg>
                    <p>Không tìm thấy sản phẩm nào với từ khóa "${escapeHtml(searchKeyword)}"</p>
                    <a href="/product.html" style="margin-top: 16px; color: var(--color-primary); text-decoration: underline;">Xem tất cả sản phẩm</a>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="empty-state" style="grid-column: 1 / -1;">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="11" cy="11" r="8"/>
                        <path d="m21 21-4.35-4.35"/>
                    </svg>
                    <p>Không có sản phẩm nào</p>
                </div>
            `;
        }
        return;
    }

    container.innerHTML = products.map(product => `
        <a href="/product-detail.html?id=${product.id}" class="product-card">
            <img src="${product.imageUrl || 'https://images.unsplash.com/photo-1512447608772-994891cd05d0?auto=format&fit=crop&w=640&q=80'}" 
                 class="product-card__image" 
                 alt="${escapeHtml(product.name)}"
                 onerror="this.src='https://via.placeholder.com/300x300?text=No+Image'">
            <div class="product-card__body">
                <h3 class="product-card__title">${escapeHtml(product.name)}</h3>
                <p class="product-card__description">${escapeHtml((product.description || '').substring(0, 100))}${product.description && product.description.length > 100 ? '...' : ''}</p>
                <p class="product-card__price">${formatPrice(product.price || 0)}</p>
            </div>
        </a>
    `).join('');
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


async function addToCart(productId, variantId, quantity) {
    const authenticated = await checkAuthStatus();
    if (!authenticated) {
        window.location.href = '/auth/login.html?redirect=' + encodeURIComponent(window.location.pathname);
        return;
    }

    try {
        await api.addToCart(productId, variantId, quantity);
        // Show success message
        const alert = document.createElement('div');
        alert.className = 'alert alert-success';
        alert.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 10000; padding: 12px 20px; border-radius: 8px; background: rgba(34, 197, 94, 0.1); color: #16a34a; border: 1px solid rgba(34, 197, 94, 0.2);';
        alert.textContent = 'Đã thêm vào giỏ hàng';
        document.body.appendChild(alert);
        setTimeout(() => alert.remove(), 3000);
    } catch (error) {
        console.error('Error adding to cart:', error);
        const alert = document.createElement('div');
        alert.className = 'alert alert-error';
        alert.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 10000; padding: 12px 20px; border-radius: 8px; background: rgba(239, 68, 68, 0.1); color: #dc2626; border: 1px solid rgba(239, 68, 68, 0.2);';
        alert.textContent = 'Không thể thêm vào giỏ hàng';
        document.body.appendChild(alert);
        setTimeout(() => alert.remove(), 3000);
    }
}

