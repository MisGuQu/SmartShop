// Wishlist page functionality
let allWishlistItems = [];

document.addEventListener('DOMContentLoaded', async () => {
    const authenticated = await checkAuthStatus();
    if (!authenticated) {
        window.location.href = '/auth/login.html?redirect=/wishlist.html';
        return;
    }
    
    await updateAuthUI();
    await loadWishlist();
    
    // Setup search form
    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', (e) => {
            e.preventDefault();
            filterWishlist();
        });
        
        const searchInput = document.getElementById('searchKeyword');
        if (searchInput) {
            searchInput.addEventListener('input', filterWishlist);
        }
    }
});

async function loadWishlist() {
    try {
        allWishlistItems = await api.getWishlist();
        displayWishlist(allWishlistItems);
    } catch (error) {
        console.error('Error loading wishlist:', error);
        document.getElementById('wishlistContent').innerHTML = 
            '<div class="alert alert-danger">Không thể tải danh sách yêu thích</div>';
    }
}

function filterWishlist() {
    const searchKeyword = document.getElementById('searchKeyword')?.value?.toLowerCase().trim() || '';
    
    if (!searchKeyword) {
        displayWishlist(allWishlistItems);
        return;
    }
    
    const filteredItems = allWishlistItems.filter(item => {
        const productName = (item.productName || '').toLowerCase();
        return productName.includes(searchKeyword);
    });
    
    displayWishlist(filteredItems, searchKeyword);
}

function displayWishlist(items, searchKeyword = '') {
    const container = document.getElementById('wishlistContent');
    
    if (!items || items.length === 0) {
        if (searchKeyword) {
            container.innerHTML = `
                <div class="text-center py-5">
                    <i class="bi bi-search fs-1 text-muted"></i>
                    <p class="mt-3">Không tìm thấy sản phẩm nào với từ khóa "${searchKeyword}"</p>
                    <button class="btn btn-outline-secondary" onclick="clearSearch()">Xóa bộ lọc</button>
                </div>
            `;
        } else {
            container.innerHTML = `
                <div class="text-center py-5">
                    <i class="bi bi-heart fs-1 text-muted"></i>
                    <p class="mt-3">Danh sách yêu thích của bạn đang trống</p>
                    <a href="/product.html" class="btn btn-primary">Tiếp tục mua sắm</a>
                </div>
            `;
        }
        return;
    }
    
    container.innerHTML = `
        ${searchKeyword ? `<div class="alert alert-info mb-3">Tìm thấy ${items.length} sản phẩm với từ khóa "${searchKeyword}"</div>` : ''}
        <div class="row g-4">
            ${items.map(item => {
                const price = item.price || 0;
                return `
                <div class="col-md-3 col-sm-6">
                    <div class="card h-100">
                        <img src="${item.imageUrl || '/images/placeholder.jpg'}" 
                             class="card-img-top" 
                             alt="${item.productName || 'Sản phẩm'}"
                             style="height: 200px; object-fit: cover;">
                        <div class="card-body d-flex flex-column">
                            <h5 class="card-title">${item.productName || 'Sản phẩm'}</h5>
                            <p class="card-text fw-bold text-primary">${formatPrice(price)}</p>
                            <div class="mt-auto">
                                <a href="/product-detail.html?id=${item.productId}" class="btn btn-primary btn-sm w-100 mb-2">Xem chi tiết</a>
                                <button class="btn btn-outline-primary btn-sm w-100 mb-2" onclick="addToCartFromWishlist(${item.productId})">
                                    <i class="bi bi-cart-plus"></i> Thêm vào giỏ
                                </button>
                                <button class="btn btn-outline-danger btn-sm w-100" onclick="removeFromWishlist(${item.productId})">
                                    <i class="bi bi-trash"></i> Xóa
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            }).join('')}
        </div>
    `;
}

function clearSearch() {
    const searchInput = document.getElementById('searchKeyword');
    if (searchInput) {
        searchInput.value = '';
    }
    displayWishlist(allWishlistItems);
}

async function addToCartFromWishlist(productId) {
    try {
        await api.addToCart(productId, null, 1);
        showAlert('Đã thêm vào giỏ hàng', 'success');
    } catch (error) {
        console.error('Error adding to cart:', error);
        showAlert('Không thể thêm vào giỏ hàng', 'danger');
    }
}

async function removeFromWishlist(productId) {
    if (!confirm('Bạn có chắc muốn xóa sản phẩm này khỏi danh sách yêu thích?')) {
        return;
    }
    
    try {
        await api.removeFromWishlist(productId);
        await loadWishlist();
        showAlert('Đã xóa khỏi danh sách yêu thích', 'success');
    } catch (error) {
        console.error('Error removing from wishlist:', error);
        showAlert('Không thể xóa', 'danger');
    }
}

async function updateAuthUI() {
    const authenticated = await checkAuthStatus();
    if (authenticated) {
        document.getElementById('loginLink').classList.add('d-none');
        document.getElementById('logoutLink').classList.remove('d-none');
    } else {
        document.getElementById('loginLink').classList.remove('d-none');
        document.getElementById('logoutLink').classList.add('d-none');
    }
}

async function handleLogout() {
    try {
        await api.logout();
        window.location.href = '/';
    } catch (error) {
        console.error('Logout error:', error);
    }
}

