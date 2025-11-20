// Home page functionality
document.addEventListener('DOMContentLoaded', async () => {
    // Initialize carousel
    initCarousel();
    
    // Check authentication status - with retry for fresh login
    await updateAuthUI();
    
    // If redirected from login, check again after a short delay
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('login') || document.cookie.includes('SMARTSHOP_TOKEN')) {
        setTimeout(async () => {
            resetAuthStatus();
            await updateAuthUI();
        }, 500);
    }

    // Load featured products
    await loadFeaturedProducts();

    // Setup newsletter form
    setupNewsletterForm();

    // Setup search form
    setupSearchForm();

    // Setup footer newsletter form
    setupFooterNewsletterForm();
});

// Carousel functionality
function initCarousel() {
    const viewport = document.querySelector('[data-carousel]');
    if (!viewport) return;
    
    const slides = viewport.querySelectorAll('.carousel__slide');
    if (slides.length === 0) return;
    
    const prevBtn = document.querySelector('[data-carousel-prev]');
    const nextBtn = document.querySelector('[data-carousel-next]');
    const dotsContainer = document.querySelector('[data-carousel-dots]');
    
    let currentSlide = 0;
    let autoPlayInterval;
    
    // Create dots if not already created
    if (dotsContainer && dotsContainer.children.length === 0) {
        slides.forEach((_, index) => {
            const dot = document.createElement('button');
            dot.className = 'carousel__dot' + (index === 0 ? ' is-active' : '');
            dot.setAttribute('aria-label', `Chuyển đến slide ${index + 1}`);
            dotsContainer.appendChild(dot);
        });
    }
    
    const dots = dotsContainer ? dotsContainer.querySelectorAll('.carousel__dot') : [];
    
    function showSlide(index) {
        slides.forEach((slide, i) => {
            slide.classList.toggle('is-active', i === index);
        });
        dots.forEach((dot, i) => {
            dot.classList.toggle('is-active', i === index);
        });
        currentSlide = index;
    }
    
    function nextSlide() {
        const next = (currentSlide + 1) % slides.length;
        showSlide(next);
    }
    
    function prevSlide() {
        const prev = (currentSlide - 1 + slides.length) % slides.length;
        showSlide(prev);
    }
    
    function goToSlide(index) {
        if (index >= 0 && index < slides.length) {
            showSlide(index);
        }
    }
    
    // Setup dot click handlers
    dots.forEach((dot, index) => {
        dot.addEventListener('click', () => goToSlide(index));
    });
    
    if (prevBtn) {
        prevBtn.addEventListener('click', prevSlide);
    }
    
    if (nextBtn) {
        nextBtn.addEventListener('click', nextSlide);
    }
    
    // Auto-play carousel
    function startAutoPlay() {
        autoPlayInterval = setInterval(nextSlide, 5000);
    }
    
    function stopAutoPlay() {
        if (autoPlayInterval) {
            clearInterval(autoPlayInterval);
            autoPlayInterval = null;
        }
    }
    
    startAutoPlay();
    
    // Pause on hover
    if (viewport) {
        viewport.addEventListener('mouseenter', stopAutoPlay);
        viewport.addEventListener('mouseleave', startAutoPlay);
    }
}

async function updateAuthUI() {
    try {
        console.log('Updating auth UI...');
        const authenticated = await checkAuthStatus();
        console.log('Auth status:', authenticated);
        
        const anonymousDiv = document.getElementById('headerAccountAnonymous');
        const authDiv = document.getElementById('headerAccountAuth');
        const wishlistLink = document.getElementById('wishlistLink');
        const cartLink = document.getElementById('cartLink');
        
        if (!anonymousDiv || !authDiv) {
            console.error('Header elements not found');
            return;
        }
        
        if (authenticated) {
            console.log('User is authenticated, showing auth UI');
            // Show authenticated UI
            anonymousDiv.classList.add('d-none');
            authDiv.classList.remove('d-none');
            
            // Update links
            if (wishlistLink) wishlistLink.href = '/wishlist.html';
            if (cartLink) cartLink.href = '/cart.html';
            
            // Load user info
            try {
                const user = await api.getCurrentUser();
                console.log('User info:', user);
                
                if (user) {
                    const userProfileLink = document.getElementById('userProfileLink');
                    const userName = document.getElementById('userName');
                    const adminChip = document.getElementById('adminChip');
                    const logoutForm = document.getElementById('logoutForm');
                    
                    // Set user name
                    if (userName) {
                        const displayName = user.fullName || user.email || user.username || 'Người dùng';
                        userName.textContent = displayName;
                        console.log('Set user name to:', displayName);
                        
                        // Also set name in dropdown menu
                        const userMenuName = document.getElementById('userMenuName');
                        if (userMenuName) {
                            userMenuName.textContent = displayName;
                        }
                    }
                    
                    // Check if user is admin
                    const isAdmin = user.roles && (user.roles.includes('ADMIN') || user.roles.includes('ROLE_ADMIN') || user.roles.some(r => r.includes('ADMIN')));
                    console.log('Is admin:', isAdmin);
                    
                    // Get admin menu link
                    const adminMenuLink = document.getElementById('adminMenuLink');
                    
                    if (isAdmin) {
                        // Show admin chip
                        if (adminChip) {
                            adminChip.classList.remove('d-none');
                        }
                        
                        // Show admin chip in dropdown menu
                        const userMenuChip = document.getElementById('userMenuChip');
                        if (userMenuChip) {
                            userMenuChip.classList.remove('d-none');
                        }
                        
                        // Show admin menu link
                        if (adminMenuLink) {
                            adminMenuLink.classList.remove('d-none');
                        }
                    } else {
                        // Hide admin chip
                        if (adminChip) {
                            adminChip.classList.add('d-none');
                        }
                        
                        // Hide admin chip in dropdown menu
                        const userMenuChip = document.getElementById('userMenuChip');
                        if (userMenuChip) {
                            userMenuChip.classList.add('d-none');
                        }
                        
                        // Hide admin menu link
                        if (adminMenuLink) {
                            adminMenuLink.classList.add('d-none');
                        }
                    }
                    
                    // Setup dropdown menu toggle (only once)
                    if (userProfileLink && !userProfileLink.dataset.menuListenerAdded) {
                        userProfileLink.href = '#';
                        userProfileLink.addEventListener('click', (e) => {
                            e.preventDefault();
                            toggleUserMenu();
                        });
                        userProfileLink.dataset.menuListenerAdded = 'true';
                    }
                    
                    // Setup dropdown menu (only once)
                    if (!document.getElementById('userMenu')?.dataset.setup) {
                        setupUserMenu();
                        document.getElementById('userMenu').dataset.setup = 'true';
                    }
                    
                    // Setup logout button in dropdown menu
                    const logoutButton = document.getElementById('logoutButton');
                    if (logoutButton && !logoutButton.dataset.listenerAdded) {
                        logoutButton.addEventListener('click', async (e) => {
                            e.preventDefault();
                            await handleLogout();
                        });
                        logoutButton.dataset.listenerAdded = 'true';
                    }
                }
            } catch (error) {
                console.error('Error loading user info:', error);
                // Still show auth UI even if user info fails to load
            }
        } else {
            console.log('User is not authenticated, showing anonymous UI');
            // Show anonymous UI
            anonymousDiv.classList.remove('d-none');
            authDiv.classList.add('d-none');
            
            // Update links to redirect to login
            if (wishlistLink) wishlistLink.href = '/auth/login.html?redirect=/wishlist.html';
            if (cartLink) cartLink.href = '/auth/login.html?redirect=/cart.html';
        }
    } catch (error) {
        console.error('Error updating auth UI:', error);
    }
}

async function loadFeaturedProducts() {
    try {
        const container = document.getElementById('featuredProductsGrid');
        if (!container) {
            console.error('Featured products container not found');
            return;
        }

        // API trả về List<ProductResponse> trực tiếp, không có pagination
        console.log('Fetching products from API...');
        const response = await api.getProducts({});
        console.log('API Response:', response);
        
        // Kiểm tra xem response là array hay object có content property
        let products = [];
        if (Array.isArray(response)) {
            products = response;
            console.log(`Loaded ${products.length} products from API`);
        } else if (response && response.content && Array.isArray(response.content)) {
            products = response.content;
            console.log(`Loaded ${products.length} products from response.content`);
        } else if (response && Array.isArray(response.data)) {
            products = response.data;
            console.log(`Loaded ${products.length} products from response.data`);
        } else {
            console.error('Unexpected response format:', response);
            container.innerHTML = '<div class="text-center" style="grid-column: 1 / -1;"><p class="text-danger">Định dạng dữ liệu không đúng</p></div>';
            return;
        }

        // Giới hạn số lượng sản phẩm hiển thị (4 sản phẩm)
        const featuredProducts = products.slice(0, 4);

        if (featuredProducts.length === 0) {
            container.innerHTML = '<div class="text-center" style="grid-column: 1 / -1;"><p>Chưa có sản phẩm nào</p></div>';
            return;
        }

        container.innerHTML = featuredProducts.map(product => {
            const imageUrl = product.imageUrl || 'https://images.unsplash.com/photo-1512447608772-994891cd05d0?auto=format&fit=crop&w=640&q=80';
            const description = product.description ? 
                (product.description.length > 80 ? product.description.substring(0, 80) + '...' : product.description) : 
                'Sản phẩm chất lượng cao từ SmartShop';
            
            return `
                <a class="product-card" href="/product-detail.html?id=${product.id}">
                    <div class="product-card__image" style="background-image: url('${imageUrl}');"></div>
                    <div class="product-card__body">
                        <h3 class="product-card__title">${escapeHtml(product.name || 'Sản phẩm')}</h3>
                        <p class="product-card__price">${formatPrice(product.price || 0)}</p>
                        <p class="product-card__description">${escapeHtml(description)}</p>
                    </div>
                </a>
            `;
        }).join('');
    } catch (error) {
        console.error('Error loading products:', error);
        const container = document.getElementById('featuredProductsGrid');
        if (container) {
            container.innerHTML = '<div class="text-center" style="grid-column: 1 / -1;"><p class="text-danger">Không thể tải sản phẩm. Vui lòng thử lại sau.</p></div>';
        }
    }
}

// Utility function để escape HTML
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

function setupNewsletterForm() {
    const form = document.getElementById('newsletterForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const emailInput = form.querySelector('input[type="email"]');
            const email = emailInput.value;
            
            try {
                // You can implement newsletter subscription API here
                // For now, just show an alert
                alert('Cảm ơn bạn đã đăng ký nhận bản tin! Chúng tôi sẽ gửi email về ' + email);
                emailInput.value = '';
            } catch (error) {
                console.error('Newsletter subscription error:', error);
                alert('Có lỗi xảy ra. Vui lòng thử lại sau.');
            }
        });
    }
}

async function handleLogout() {
    try {
        await api.logout();
        window.location.href = '/';
    } catch (error) {
        console.error('Logout error:', error);
        // Still redirect even if logout API fails
        window.location.href = '/';
    }
}

function setupSearchForm() {
    const searchForm = document.getElementById('searchForm');
    const searchInput = document.getElementById('searchInput');
    
    if (searchForm && searchInput) {
        // Handle form submission
        searchForm.addEventListener('submit', handleSearch);
        
        // Optional: Handle Enter key
        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleSearch(e);
            }
        });
    }
}

function handleSearch(event) {
    event.preventDefault();
    const searchInput = document.getElementById('searchInput');
    const keyword = searchInput ? searchInput.value.trim() : '';
    
    if (keyword) {
        // Redirect to products page with search query
        window.location.href = `/product.html?q=${encodeURIComponent(keyword)}`;
    } else {
        // If empty, just go to products page
        window.location.href = '/product.html';
    }
    
    return false;
}

function setupFooterNewsletterForm() {
    const form = document.getElementById('footerNewsletterForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const emailInput = form.querySelector('input[type="email"]');
            const email = emailInput.value.trim();
            
            if (email) {
                // You can implement newsletter subscription API here
                alert('Cảm ơn bạn đã đăng ký nhận bản tin! Chúng tôi sẽ gửi email về ' + email);
                emailInput.value = '';
            }
        });
    }
}

// User menu dropdown functionality
function setupUserMenu() {
    const userProfileLink = document.getElementById('userProfileLink');
    const userMenu = document.getElementById('userMenu');
    
    if (!userProfileLink || !userMenu) return;
    
    // Close dropdown when clicking outside
    document.addEventListener('click', (e) => {
        const isClickInside = userProfileLink.contains(e.target) || userMenu.contains(e.target);
        if (!isClickInside && !userMenu.classList.contains('d-none')) {
            closeUserMenu();
        }
    });
    
    // Close dropdown when clicking on menu links (except logout button which is handled separately)
    const menuLinks = userMenu.querySelectorAll('.user-menu__link:not(.user-menu__link--logout)');
    menuLinks.forEach(link => {
        link.addEventListener('click', () => {
            closeUserMenu();
        });
    });
}

function toggleUserMenu() {
    const userProfileLink = document.getElementById('userProfileLink');
    const userMenu = document.getElementById('userMenu');
    
    if (!userProfileLink || !userMenu) return;
    
    const isOpen = !userMenu.classList.contains('d-none');
    
    if (isOpen) {
        closeUserMenu();
    } else {
        openUserMenu();
    }
}

function openUserMenu() {
    const userProfileLink = document.getElementById('userProfileLink');
    const userMenu = document.getElementById('userMenu');
    
    if (!userProfileLink || !userMenu) return;
    
    userMenu.classList.remove('d-none');
    userProfileLink.setAttribute('aria-expanded', 'true');
}

function closeUserMenu() {
    const userProfileLink = document.getElementById('userProfileLink');
    const userMenu = document.getElementById('userMenu');
    
    if (!userProfileLink || !userMenu) return;
    
    userMenu.classList.add('d-none');
    userProfileLink.setAttribute('aria-expanded', 'false');
}
