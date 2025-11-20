// Products page functionality
let currentPage = 0;
const pageSize = 12;

document.addEventListener('DOMContentLoaded', async () => {
    await updateAuthUI();
    
    // Load categories
    await loadCategories();
    
    // Load products
    await loadProducts();
    
    // Setup filter form
    document.getElementById('filterForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        currentPage = 0;
        await loadProducts();
    });
});

async function loadProducts() {
    const keyword = document.getElementById('keyword').value.trim();
    const category = document.getElementById('category').value;
    const minPrice = document.getElementById('minPrice').value;
    const maxPrice = document.getElementById('maxPrice').value;
    const sort = document.getElementById('sort').value;

    const params = {
        page: currentPage,
        size: pageSize,
        sort: sort,
        direction: 'desc'
    };

    if (keyword) params.keyword = keyword;
    if (category) params.category = category;
    if (minPrice) params.minPrice = parseFloat(minPrice);
    if (maxPrice) params.maxPrice = parseFloat(maxPrice);

    try {
        const response = await api.getProducts(params);
        // Handle paginated response
        const products = response.content || response || [];
        displayProducts(products);
        displayPagination(response);
    } catch (error) {
        console.error('Error loading products:', error);
        const container = document.getElementById('productsGrid');
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <p style="color: #dc2626;">Không thể tải sản phẩm. Vui lòng thử lại sau.</p>
            </div>
        `;
    }
}

async function loadCategories() {
    try {
        const categories = await api.getCategories();
        const categorySelect = document.getElementById('category');
        
        // Clear existing options except the first one
        categorySelect.innerHTML = '<option value="">Tất cả danh mục</option>';
        
        // Add categories
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            categorySelect.appendChild(option);
        });
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function displayProducts(products) {
    const container = document.getElementById('productsGrid');
    
    if (products.length === 0) {
        container.innerHTML = `
            <div class="empty-state" style="grid-column: 1 / -1;">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"/>
                    <path d="m21 21-4.35-4.35"/>
                </svg>
                <p>Không tìm thấy sản phẩm nào</p>
            </div>
        `;
        return;
    }

    container.innerHTML = products.map(product => `
        <a href="/product-detail.html?id=${product.id}" class="product-card">
            <img src="${product.imageUrl || 'https://images.unsplash.com/photo-1512447608772-994891cd05d0?auto=format&fit=crop&w=640&q=80'}" 
                 class="product-card__image" 
                 alt="${escapeHtml(product.name)}">
            <div class="product-card__body">
                <h3 class="product-card__title">${escapeHtml(product.name)}</h3>
                <p class="product-card__description">${escapeHtml((product.description || '').substring(0, 100))}${product.description && product.description.length > 100 ? '...' : ''}</p>
                <p class="product-card__price">${formatPrice(product.price || 0)}</p>
                <div class="product-card__actions">
                    <span class="btn-view">Xem chi tiết</span>
                    <button type="button" class="btn-cart" onclick="event.preventDefault(); addToCart(${product.id}, null, 1)">
                        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M3.5 5h2.2l1.4 9.2a1 1 0 0 0 .99.85h9.26a1 1 0 0 0 .98-.8l1.12-5.6H7.16"/>
                            <circle cx="10" cy="19" r="1.4"/>
                            <circle cx="16.5" cy="19" r="1.4"/>
                        </svg>
                        Thêm vào giỏ
                    </button>
                </div>
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

function displayPagination(pageData) {
    const pagination = document.getElementById('pagination');
    
    // Handle both paginated and non-paginated responses
    const totalPages = pageData.totalPages || (pageData.totalElements ? Math.ceil(pageData.totalElements / pageSize) : 0);
    const isFirst = pageData.first !== undefined ? pageData.first : (currentPage === 0);
    const isLast = pageData.last !== undefined ? pageData.last : (currentPage >= totalPages - 1);
    
    if (totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }

    let html = '';
    
    // Previous button
    html += `<li class="pagination__item">
        <a class="pagination__link ${isFirst ? 'disabled' : ''}" href="#" onclick="changePage(${currentPage - 1}); return false;">Trước</a>
    </li>`;
    
    // Page numbers - show max 10 pages
    const maxPagesToShow = 10;
    let startPage = Math.max(0, currentPage - Math.floor(maxPagesToShow / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxPagesToShow - 1);
    
    if (endPage - startPage < maxPagesToShow - 1) {
        startPage = Math.max(0, endPage - maxPagesToShow + 1);
    }
    
    for (let i = startPage; i <= endPage; i++) {
        html += `<li class="pagination__item">
            <a class="pagination__link ${i === currentPage ? 'active' : ''}" href="#" onclick="changePage(${i}); return false;">${i + 1}</a>
        </li>`;
    }
    
    // Next button
    html += `<li class="pagination__item">
        <a class="pagination__link ${isLast ? 'disabled' : ''}" href="#" onclick="changePage(${currentPage + 1}); return false;">Sau</a>
    </li>`;
    
    pagination.innerHTML = html;
}

function changePage(page) {
    currentPage = page;
    loadProducts();
    window.scrollTo({ top: 0, behavior: 'smooth' });
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

